package jq.m12.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;
import jq.m12.feature.MartDatabase.Totals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Stage1MonthAndTotalsTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);
    private static final YearMonth NOVEMBER = YearMonth.of(2026, 11);

    private final MartDatabase db = new MartDatabase();
    private final StatementJob job = new StatementJob(db.dataSource);

    @AfterEach
    void dropDatabase() {
        db.shutdown();
    }

    @Test
    @DisplayName("Stage 1: October runs from midnight on 1 October to midnight on 1 November, Riverton time")
    void octoberInRivertonTime() {
        MonthRange october = MonthRange.of(OCTOBER);
        assertEquals(OffsetDateTime.parse("2026-10-01T04:00Z").toInstant(), october.start().toInstant(),
                "midnight on 1 October in Riverton (America/New_York, summer time, UTC-4) is 04:00 UTC, not 00:00 UTC");
        assertEquals(OffsetDateTime.parse("2026-11-01T04:00Z").toInstant(), october.end().toInstant(),
                "October ends at midnight on 1 November in Riverton, still summer time: 04:00 UTC");
        assertEquals(OCTOBER, october.month(), "the range should remember its month");
    }

    @Test
    @DisplayName("Stage 1: November is one hour longer, because Riverton's clocks go back on 1 November 2026")
    void novemberCrossesTheClockChange() {
        MonthRange november = MonthRange.of(NOVEMBER);
        assertEquals(OffsetDateTime.parse("2026-11-01T04:00Z").toInstant(), november.start().toInstant(),
                "November starts where October ends: 04:00 UTC");
        assertEquals(OffsetDateTime.parse("2026-12-01T05:00Z").toInstant(), november.end().toInstant(),
                "midnight on 1 December in Riverton is winter time (UTC-5): 05:00 UTC. Let ZoneId work it out with "
                        + "atStartOfDay(MonthRange.RIVERTON); never add a fixed offset");
    }

    @Test
    @DisplayName("Stage 1: one row per rider per city, with ride count, total cents and longest ride")
    void oneRowPerRiderPerCity() {
        db.ride("rider-7", "riverton", "EBIKE", "2026-10-02T12:10Z", 12);    // 460
        db.ride("rider-7", "riverton", "CLASSIC", "2026-10-09T21:45Z", 10);  // 250
        db.ride("rider-7", "riverton", "CLASSIC", "2026-10-20T11:05Z", 20);  // 400
        db.ride("rider-8", "riverton", "CARGO", "2026-10-03T16:00Z", 8);     // 300
        db.ride("rider-7", "bramford", "CLASSIC", "2026-10-15T14:00Z", 10);  // 270
        db.ride("rider-9", "bramford", "EBIKE", "2026-10-16T09:30Z", 10);    // 470
        db.ride("rider-9", "bramford", "CLASSIC", "2026-10-17T09:30Z", 5);   // 195

        job.runMonth(OCTOBER);

        assertEquals(List.of(
                        new Totals("2026-10", "bramford", "rider-7", 1, 270, 10),
                        new Totals("2026-10", "bramford", "rider-9", 2, 665, 10),
                        new Totals("2026-10", "riverton", "rider-7", 3, 1110, 20),
                        new Totals("2026-10", "riverton", "rider-8", 1, 300, 8)),
                db.totals(OCTOBER),
                "runMonth(2026-10) should write one row per (city, rider): ride_count, total_cents = the sum of "
                        + "fare_cents, longest_minutes = the longest ride. rider-7 rode in both cities, so has two rows");
    }

    @Test
    @DisplayName("Stage 1: a ride at 23:30 on 31 October in Riverton belongs to October, although it is 1 November in UTC")
    void lateNightRideBelongsToRivertonsDay() {
        db.ride("rider-7", "riverton", "CLASSIC", "2026-10-01T03:59Z", 10);  // 23:59 on 30 September in Riverton
        db.ride("rider-7", "riverton", "CLASSIC", "2026-10-01T04:00Z", 20);  // 00:00 on 1 October in Riverton
        db.ride("rider-7", "riverton", "CLASSIC", "2026-11-01T03:30Z", 6);   // 23:30 on 31 October in Riverton

        job.runMonth(OCTOBER);

        assertEquals(List.of(new Totals("2026-10", "riverton", "rider-7", 2, 590, 20)), db.totals(OCTOBER),
                "October in Riverton holds the ride at 04:00 UTC on 1 October (400) and the one at 03:30 UTC on "
                        + "1 November (190), but not the one at 03:59 UTC on 1 October (250), which was still September "
                        + "there");
    }

    @Test
    @DisplayName("Stage 1: a ride at 23:30 on 30 November in Riverton (04:30 UTC on 1 December) belongs to November")
    void winterTimeEndOfMonth() {
        db.ride("rider-8", "riverton", "EBIKE", "2026-11-15T13:00Z", 10);    // 400
        db.ride("rider-8", "riverton", "EBIKE", "2026-12-01T04:30Z", 5);     // 250, 23:30 on 30 November in Riverton
        db.ride("rider-8", "riverton", "EBIKE", "2026-12-01T05:00Z", 30);    // 1000, 00:00 on 1 December in Riverton

        job.runMonth(NOVEMBER);

        assertEquals(List.of(new Totals("2026-11", "riverton", "rider-8", 2, 650, 10)), db.totals(NOVEMBER),
                "after the clocks go back, midnight in Riverton is 05:00 UTC. So 04:30 UTC on 1 December is still "
                        + "November, and 05:00 UTC is December");
    }
}
