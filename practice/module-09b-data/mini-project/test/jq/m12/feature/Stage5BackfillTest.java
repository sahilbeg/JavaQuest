package jq.m12.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.YearMonth;
import java.util.List;
import jq.m12.feature.MartDatabase.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Stage5BackfillTest {

    private static final YearMonth AUGUST = YearMonth.of(2026, 8);
    private static final YearMonth SEPTEMBER = YearMonth.of(2026, 9);
    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    private final MartDatabase db = new MartDatabase();
    private final StatementJob job = new StatementJob(db.dataSource);

    @BeforeEach
    void seed() {
        db.ride("rider-7", "riverton", "CLASSIC", "2026-08-10T12:00Z", 10);  // August 250
        db.ride("rider-7", "riverton", "CLASSIC", "2026-09-10T12:00Z", 20);  // September 400
        db.ride("rider-8", "bramford", "EBIKE", "2026-09-15T12:00Z", 10);    // September 470
        db.ride("rider-7", "riverton", "EBIKE", "2026-10-10T12:00Z", 10);    // October 400
        db.ride("rider-8", "bramford", "CLASSIC", "2026-10-15T12:00Z", 10);  // October 270
    }

    @AfterEach
    void dropDatabase() {
        db.shutdown();
    }

    @Test
    @DisplayName("Stage 5: backfill from August to October runs the three months, oldest first")
    void backfillRunsEveryMonthInOrder() {
        List<YearMonth> done = job.backfill(AUGUST, OCTOBER);

        assertEquals(List.of(AUGUST, SEPTEMBER, OCTOBER), done,
                "backfill returns the months it ran, in the order it ran them: both ends included, oldest first");
        assertEquals(List.of(
                        new Row("2026-08", "riverton", "rider-7", 1, 250, 10, 1, null),
                        new Row("2026-09", "bramford", "rider-8", 1, 470, 10, 1, null),
                        new Row("2026-09", "riverton", "rider-7", 1, 400, 20, 1, 150L),
                        new Row("2026-10", "bramford", "rider-8", 1, 270, 10, 1, -200L),
                        new Row("2026-10", "riverton", "rider-7", 1, 400, 10, 1, 0L)),
                db.allRows(), "each month of the backfill should be built exactly as runMonth builds it");
    }

    @Test
    @DisplayName("Stage 5: running the same backfill twice leaves the same table")
    void backfillIsSafeToRepeat() {
        job.backfill(AUGUST, OCTOBER);
        List<Row> first = db.allRows();
        job.backfill(AUGUST, OCTOBER);
        assertEquals(5, first.size(), "August to October holds 5 statement rows");
        assertEquals(first, db.allRows(), "a repeated backfill must not add or change a single row");
    }

    @Test
    @DisplayName("Stage 5: a backfill whose start is after its end is refused")
    void backwardsBackfillIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> job.backfill(OCTOBER, AUGUST),
                "backfill(2026-10, 2026-08) has its months the wrong way round: throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Stage 5: a month that fails stops the backfill; the months before it stay built")
    void failingMonthStopsTheBackfill() {
        db.ride("rider-9", "riverton", "CLASSIC", "2026-09-20T12:00Z", 10, -100);   // bad data in September

        DataQualityException failure = assertThrows(DataQualityException.class, () -> job.backfill(AUGUST, OCTOBER),
                "September has a negative fare, so the backfill should stop with its DataQualityException");

        assertEquals("no-negative-fares", failure.check(), "September fails the no-negative-fares check");
        assertEquals(1, db.rows(AUGUST).size(), "August ran before September, in its own transaction, so it stays built");
        assertTrue(db.rows(SEPTEMBER).isEmpty(), "September failed, so it has no rows");
        assertTrue(db.rows(OCTOBER).isEmpty(), "October comes after the failure, so the backfill never ran it");
    }
}
