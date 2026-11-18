package jq.m12.pipelines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BackfillTest {

    private static final LocalDate NOV_14 = LocalDate.of(2026, 11, 14);
    private static final LocalDate NOV_15 = LocalDate.of(2026, 11, 15);
    private static final LocalDate NOV_16 = LocalDate.of(2026, 11, 16);
    private static final LocalDate NOV_17 = LocalDate.of(2026, 11, 17);

    private final Recorder rec = new Recorder();

    /** extract_rides -> load_report, plus archive_logs on its own. */
    private MiniScheduler scheduler() {
        return new MiniScheduler(new Dag("daily_ride_report", List.of(
                TaskSpec.of("extract_rides", rec.task("extract_rides")),
                TaskSpec.of("load_report", rec.task("load_report"), "extract_rides"),
                TaskSpec.of("archive_logs", rec.task("archive_logs")))));
    }

    private static List<LocalDate> backfill(MiniScheduler scheduler, LocalDate from, LocalDate to) {
        return assertTimeoutPreemptively(Duration.ofSeconds(2), () -> scheduler.backfill(from, to),
                "backfill took over 2 seconds: does your date loop ever stop?");
    }

    @Test
    @DisplayName("A backfill runs every date from 'from' to 'to', both included, oldest first")
    void backfillRunsEachDateInOrder() {
        MiniScheduler scheduler = scheduler();

        List<LocalDate> ran = backfill(scheduler, NOV_14, NOV_16);

        assertEquals(List.of(NOV_14, NOV_15, NOV_16), ran, "backfill should return the dates it ran, oldest first, including 'to'");
        assertEquals(List.of("archive_logs 2026-11-14", "extract_rides 2026-11-14", "load_report 2026-11-14",
                        "archive_logs 2026-11-15", "extract_rides 2026-11-15", "load_report 2026-11-15",
                        "archive_logs 2026-11-16", "extract_rides 2026-11-16", "load_report 2026-11-16"), rec.calls,
                "each date gets a whole DAG run, in dag.order(), before the next date starts");
        assertTrue(scheduler.succeeded(NOV_15), "every task succeeded for 2026-11-15");
    }

    @Test
    @DisplayName("Running the same backfill twice does nothing the second time")
    void backfillIsIdempotent() {
        MiniScheduler scheduler = scheduler();
        assertEquals(3, backfill(scheduler, NOV_14, NOV_16).size(), "the first backfill should run all 3 dates");
        rec.calls.clear();

        List<LocalDate> ran = backfill(scheduler, NOV_14, NOV_16);

        assertEquals(List.of(), ran, "every date already succeeded, so the second backfill should run no dates");
        assertEquals(List.of(), rec.calls, "no task should be called for a date that already fully succeeded");
    }

    @Test
    @DisplayName("A backfill re-runs only the dates that did not fully succeed, and one bad date does not stop the rest")
    void backfillFillsTheGaps() {
        MiniScheduler scheduler = scheduler();
        scheduler.run(NOV_14);               // a good night
        rec.breakTask("load_report");
        scheduler.run(NOV_15);               // load_report failed that night
        rec.fix("load_report");
        rec.calls.clear();

        List<LocalDate> ran = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            List<LocalDate> dates = new ArrayList<>(scheduler.backfill(NOV_14, NOV_15));
            rec.breakTask("extract_rides");  // the rides database is down while the 16th runs
            dates.addAll(scheduler.backfill(NOV_16, NOV_16));
            rec.fix("extract_rides");
            dates.addAll(scheduler.backfill(NOV_16, NOV_17));
            return dates;
        });

        assertEquals(List.of(NOV_15, NOV_16, NOV_16, NOV_17), ran,
                "skip 2026-11-14 (it succeeded), re-run 2026-11-15 (it failed), run 2026-11-16 (it fails),"
                        + " then run 2026-11-16 again and 2026-11-17");
        assertTrue(scheduler.succeeded(NOV_15), "after the backfill, 2026-11-15 should be fully SUCCESS");
        assertTrue(scheduler.succeeded(NOV_17), "a date after a failed date should still run and succeed");
        assertFalse(rec.calls.contains("extract_rides 2026-11-14"), "2026-11-14 already succeeded: none of its tasks should run again");
    }

    @Test
    @DisplayName("A backfill of one day runs that day, and 'from' after 'to' is refused")
    void backfillEdges() {
        MiniScheduler scheduler = scheduler();

        assertEquals(List.of(NOV_16), backfill(scheduler, NOV_16, NOV_16), "from == to is a backfill of exactly one date");
        assertThrows(IllegalArgumentException.class, () -> scheduler.backfill(NOV_17, NOV_14),
                "from 2026-11-17 to 2026-11-14 goes backwards: throw IllegalArgumentException");
    }

    @Test
    @DisplayName("rerunFailed runs only the FAILED and UPSTREAM_FAILED tasks of that date, in order")
    void rerunFailedRepairsOneDate() {
        MiniScheduler scheduler = scheduler();
        rec.breakTask("extract_rides");
        scheduler.run(NOV_16);
        assertEquals(TaskState.UPSTREAM_FAILED, scheduler.state("load_report", NOV_16), "setup: load_report should be UPSTREAM_FAILED");
        rec.fix("extract_rides");
        rec.calls.clear();

        boolean ok = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> scheduler.rerunFailed(NOV_16));

        assertTrue(ok, "after the fix, every task of 2026-11-16 succeeds, so rerunFailed should return true");
        assertEquals(List.of("extract_rides 2026-11-16", "load_report 2026-11-16"), rec.calls,
                "re-run extract_rides (FAILED) and then load_report (UPSTREAM_FAILED). archive_logs already succeeded: leave it alone");
        assertEquals(TaskState.SUCCESS, scheduler.state("load_report", NOV_16), "load_report should be SUCCESS now");
        assertEquals(2, scheduler.attempts("extract_rides", NOV_16), "attempts add up over runs: 1 last night + 1 now");
    }

    @Test
    @DisplayName("rerunFailed on a date that never ran is refused")
    void rerunFailedNeedsARun() {
        MiniScheduler scheduler = scheduler();

        assertThrows(IllegalStateException.class, () -> scheduler.rerunFailed(NOV_16),
                "the DAG never ran for 2026-11-16, so there is nothing to repair: throw IllegalStateException");
        assertEquals(List.of(), rec.calls, "no task should run");
    }
}
