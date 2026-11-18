package jq.m12.pipelines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MiniSchedulerTest {

    private static final LocalDate NOV_17 = LocalDate.of(2026, 11, 17);
    private static final LocalDate NOV_18 = LocalDate.of(2026, 11, 18);
    private static final Duration FIVE_MINUTES = Duration.ofMinutes(5);

    private final Recorder rec = new Recorder();

    /** extract_rides -> clean_rides -> load_report, plus send_digest after load_report, plus archive_logs on its own. */
    private Dag nightlyReport() {
        return new Dag("daily_ride_report", List.of(
                TaskSpec.of("extract_rides", rec.task("extract_rides")),
                TaskSpec.of("clean_rides", rec.task("clean_rides"), "extract_rides"),
                TaskSpec.of("load_report", rec.task("load_report"), "clean_rides"),
                TaskSpec.of("send_digest", rec.task("send_digest"), "load_report"),
                TaskSpec.of("archive_logs", rec.task("archive_logs"))));
    }

    private static boolean runSafely(MiniScheduler scheduler, LocalDate date) {
        return assertTimeoutPreemptively(Duration.ofSeconds(2), () -> scheduler.run(date),
                "run took over 2 seconds: does your retry loop ever stop?");
    }

    @Test
    @DisplayName("A good night: every task runs once, in order, with the logical date, and ends SUCCESS")
    void everyTaskRunsInOrder() {
        MiniScheduler scheduler = new MiniScheduler(nightlyReport());

        assertTrue(runSafely(scheduler, NOV_17), "run should return true when every task succeeded");
        assertEquals(List.of("archive_logs 2026-11-17", "extract_rides 2026-11-17", "clean_rides 2026-11-17",
                        "load_report 2026-11-17", "send_digest 2026-11-17"), rec.calls,
                "run should call each task once, in dag.order(), passing the logical date 2026-11-17 to every action");
        for (String task : List.of("extract_rides", "clean_rides", "load_report", "send_digest", "archive_logs")) {
            assertEquals(TaskState.SUCCESS, scheduler.state(task, NOV_17), task + " should be SUCCESS for 2026-11-17");
            assertEquals(1, scheduler.attempts(task, NOV_17), task + " ran once, so attempts should be 1");
        }
        assertTrue(scheduler.succeeded(NOV_17), "succeeded(2026-11-17) should be true when every task is SUCCESS");
        assertTrue(scheduler.waits().isEmpty(), "nothing failed, so there should be no retry waits");
    }

    @Test
    @DisplayName("A task that fails twice and then works, with 2 retries allowed, ends SUCCESS after 3 attempts and 2 recorded waits")
    void flakyTaskSucceedsOnRetry() {
        Dag dag = new Dag("daily_ride_report", List.of(
                TaskSpec.of("extract_rides", rec.flaky("extract_rides", 2)).withRetries(2, FIVE_MINUTES),
                TaskSpec.of("load_report", rec.task("load_report"), "extract_rides")));
        MiniScheduler scheduler = new MiniScheduler(dag);

        assertTrue(runSafely(scheduler, NOV_17), "the third attempt worked, so the run should succeed");
        assertEquals(3, scheduler.attempts("extract_rides", NOV_17), "1 first try + 2 retries = 3 attempts");
        assertEquals(TaskState.SUCCESS, scheduler.state("extract_rides", NOV_17), "the last attempt worked, so extract_rides is SUCCESS");
        assertEquals(List.of(new RetryWait("extract_rides", NOV_17, FIVE_MINUTES), new RetryWait("extract_rides", NOV_17, FIVE_MINUTES)),
                scheduler.waits(), "record one RetryWait with the task's retryDelay before each retry, instead of sleeping");
        assertEquals(TaskState.SUCCESS, scheduler.state("load_report", NOV_17), "load_report runs after its upstream finally succeeded");
    }

    @Test
    @DisplayName("A task that keeps failing ends FAILED after 1 + maxRetries attempts, with one wait per retry")
    void retriesAreCapped() {
        Dag dag = new Dag("daily_ride_report", List.of(
                TaskSpec.of("extract_rides", rec.flaky("extract_rides", 99)).withRetries(3, FIVE_MINUTES)));
        MiniScheduler scheduler = new MiniScheduler(dag);

        assertFalse(runSafely(scheduler, NOV_17), "a task failed, so run should return false");
        assertEquals(TaskState.FAILED, scheduler.state("extract_rides", NOV_17), "every attempt threw, so extract_rides is FAILED");
        assertEquals(4, scheduler.attempts("extract_rides", NOV_17), "maxRetries 3 means 1 first try + 3 retries = 4 attempts, no more");
        assertEquals(3, scheduler.waits().size(), "3 retries means 3 waits. Never wait after the last attempt");
    }

    @Test
    @DisplayName("A task with no retries is tried once, and a checked exception counts as a failure")
    void noRetriesMeansOneAttempt() {
        rec.breakTask("extract_rides");   // it throws java.io.IOException, a checked exception
        MiniScheduler scheduler = new MiniScheduler(nightlyReport());

        assertFalse(runSafely(scheduler, NOV_17), "extract_rides failed, so run should return false");
        assertEquals(TaskState.FAILED, scheduler.state("extract_rides", NOV_17),
                "an IOException from the action is a failure too: catch Exception, not just RuntimeException");
        assertEquals(1, scheduler.attempts("extract_rides", NOV_17), "maxRetries 0 means exactly one attempt");
        assertTrue(scheduler.waits().isEmpty(), "no retries, so no waits");
    }

    @Test
    @DisplayName("Everything downstream of a failed task is UPSTREAM_FAILED and never called, but other branches still run")
    void downstreamOfAFailureIsSkipped() {
        rec.breakTask("clean_rides");
        MiniScheduler scheduler = new MiniScheduler(nightlyReport());

        assertFalse(runSafely(scheduler, NOV_17), "clean_rides failed, so run should return false");
        assertEquals(TaskState.FAILED, scheduler.state("clean_rides", NOV_17), "clean_rides threw, so it is FAILED");
        assertEquals(TaskState.UPSTREAM_FAILED, scheduler.state("load_report", NOV_17),
                "load_report needs clean_rides, which failed, so load_report is UPSTREAM_FAILED");
        assertEquals(TaskState.UPSTREAM_FAILED, scheduler.state("send_digest", NOV_17),
                "send_digest needs load_report, which never ran: UPSTREAM_FAILED passes all the way down the chain");
        assertFalse(rec.calls.contains("load_report 2026-11-17") || rec.calls.contains("send_digest 2026-11-17"),
                "a task whose upstream did not succeed must never be called, or it builds a report from bad data. Calls were: " + rec.calls);
        assertEquals(0, scheduler.attempts("load_report", NOV_17), "load_report was skipped, so it has 0 attempts");
        assertEquals(TaskState.SUCCESS, scheduler.state("archive_logs", NOV_17),
                "archive_logs does not depend on clean_rides, so it should still run and succeed");
        assertFalse(scheduler.succeeded(NOV_17), "succeeded should be false: not every task is SUCCESS");
    }

    @Test
    @DisplayName("A task with two upstreams is skipped when only one of them failed")
    void oneFailedUpstreamIsEnough() {
        rec.breakTask("sum_fares");
        Dag dag = new Dag("daily_ride_report", List.of(
                TaskSpec.of("count_rides", rec.task("count_rides")),
                TaskSpec.of("sum_fares", rec.task("sum_fares")),
                TaskSpec.of("publish_report", rec.task("publish_report"), "count_rides", "sum_fares")));
        MiniScheduler scheduler = new MiniScheduler(dag);

        runSafely(scheduler, NOV_17);

        assertEquals(TaskState.SUCCESS, scheduler.state("count_rides", NOV_17), "count_rides works and should be SUCCESS");
        assertEquals(TaskState.UPSTREAM_FAILED, scheduler.state("publish_report", NOV_17),
                "publish_report needs BOTH upstreams to succeed. sum_fares failed, so it is UPSTREAM_FAILED");
    }

    @Test
    @DisplayName("Each logical date keeps its own states, and a date that never ran is NONE")
    void statesArePerDate() {
        MiniScheduler scheduler = new MiniScheduler(nightlyReport());
        assertEquals(TaskState.NONE, scheduler.state("extract_rides", NOV_17), "before any run, every task is NONE");
        assertFalse(scheduler.succeeded(NOV_17), "a date that never ran has not succeeded");

        rec.breakTask("extract_rides");
        runSafely(scheduler, NOV_17);
        rec.fix("extract_rides");
        runSafely(scheduler, NOV_18);

        assertEquals(TaskState.FAILED, scheduler.state("extract_rides", NOV_17),
                "the run for 2026-11-18 must not change the state recorded for 2026-11-17");
        assertEquals(TaskState.SUCCESS, scheduler.state("extract_rides", NOV_18), "extract_rides worked for 2026-11-18");
        assertTrue(scheduler.succeeded(NOV_18), "every task succeeded for 2026-11-18");
        assertFalse(scheduler.succeeded(NOV_17), "2026-11-17 still has a failed task");
        assertEquals(TaskState.NONE, scheduler.state("extract_rides", NOV_18.plusDays(1)), "2026-11-19 never ran, so it is NONE");
    }
}
