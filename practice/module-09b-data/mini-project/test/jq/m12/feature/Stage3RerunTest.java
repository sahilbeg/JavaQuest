package jq.m12.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.YearMonth;
import java.util.List;
import jq.m12.feature.MartDatabase.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Stage3RerunTest {

    private static final YearMonth SEPTEMBER = YearMonth.of(2026, 9);
    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    private final MartDatabase db = new MartDatabase();
    private final StatementJob job = new StatementJob(db.dataSource);
    private long rider10Ride;

    @BeforeEach
    void seed() {
        db.ride("rider-7", "riverton", "CLASSIC", "2026-09-10T12:00Z", 10);  // September 250
        db.ride("rider-8", "riverton", "EBIKE", "2026-09-11T12:00Z", 10);    // September 400
        db.ride("rider-7", "riverton", "CLASSIC", "2026-10-10T12:00Z", 20);  // October 400
        db.ride("rider-7", "riverton", "EBIKE", "2026-10-12T12:00Z", 10);    // October 400
        db.ride("rider-8", "riverton", "CARGO", "2026-10-12T15:00Z", 8);     // October 300
        rider10Ride = db.ride("rider-10", "riverton", "CLASSIC", "2026-10-14T15:00Z", 6);  // October 190
    }

    @AfterEach
    void dropDatabase() {
        db.shutdown();
    }

    @Test
    @DisplayName("Stage 3: running October twice gives exactly the same rows, with no duplicates")
    void rerunIsIdentical() {
        job.runMonth(OCTOBER);
        List<Row> first = db.rows(OCTOBER);
        job.runMonth(OCTOBER);
        assertEquals(3, first.size(), "October has three riders: rider-7, rider-8 and rider-10");
        assertEquals(first, db.rows(OCTOBER),
                "a rerun must give identical rows. Delete the month's rows first, then insert, in one transaction");
    }

    @Test
    @DisplayName("Stage 3: a rerun picks up changed raw rides, and drops a rider whose only ride was removed")
    void rerunReflectsChanges() {
        job.runMonth(OCTOBER);
        db.ride("rider-8", "riverton", "CLASSIC", "2026-10-20T15:00Z", 10);   // a late ride arrives: +250
        db.execute("DELETE FROM ride WHERE id = " + rider10Ride);                 // a test ride is removed

        job.runMonth(OCTOBER);

        assertEquals(List.of(
                        new Row("2026-10", "riverton", "rider-7", 2, 800, 20, 1, 550L),
                        new Row("2026-10", "riverton", "rider-8", 2, 550, 10, 2, 150L)),
                db.rows(OCTOBER),
                "after the rerun, rider-8 has 2 rides (550) and rider-10 is gone. An upsert (MERGE) alone would leave "
                        + "rider-10's old row behind: delete the whole month, then insert it again");
    }

    @Test
    @DisplayName("Stage 3: rebuilding October leaves September's rows alone")
    void rerunTouchesOnlyItsMonth() {
        job.runMonth(SEPTEMBER);
        List<Row> september = db.rows(SEPTEMBER);
        job.runMonth(OCTOBER);
        job.runMonth(OCTOBER);
        assertEquals(2, september.size(), "September has two riders: rider-7 and rider-8");
        assertEquals(september, db.rows(SEPTEMBER),
                "runMonth(2026-10) may delete only statement_month = '2026-10'. September's rows must stay as they were");
    }

    @Test
    @DisplayName("Stage 3: when the insert fails halfway, the month is rolled back and the old rows are still there")
    void failedRunRollsBack() {
        job.runMonth(OCTOBER);
        List<Row> before = db.rows(OCTOBER);
        db.execute("ALTER TABLE rider_monthly_statement ADD CONSTRAINT at_most_two_rides CHECK (ride_count <= 2)");
        db.ride("rider-7", "riverton", "CLASSIC", "2026-10-25T12:00Z", 10);   // rider-7 now has 3 rides

        StatementJobException failure = assertThrows(StatementJobException.class, () -> job.runMonth(OCTOBER),
                "the insert breaks the CHECK constraint, so runMonth should throw StatementJobException");

        assertFalse(before.isEmpty(), "the first run should have built October");
        assertEquals(before, db.rows(OCTOBER),
                "the failed run deleted October's rows before its insert failed. In one transaction that delete is "
                        + "rolled back too, so riders still see last night's statements. Turn off auto-commit, and roll back "
                        + "on any failure. The job said: " + failure.getMessage());
    }
}
