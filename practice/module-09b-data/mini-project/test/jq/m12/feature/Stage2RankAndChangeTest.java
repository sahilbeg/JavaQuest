package jq.m12.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jq.m12.feature.MartDatabase.Row;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Stage2RankAndChangeTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    private final MartDatabase db = new MartDatabase();
    private final StatementJob job = new StatementJob(db.dataSource);

    @AfterEach
    void dropDatabase() {
        db.shutdown();
    }

    @Test
    @DisplayName("Stage 2: riders are ranked by total within their city, and ties share a rank with a gap after (RANK)")
    void rankWithinCity() {
        db.ride("rider-1", "riverton", "EBIKE", "2026-10-05T12:00Z", 30);    // 1000
        db.ride("rider-2", "riverton", "CLASSIC", "2026-10-05T13:00Z", 40);  // 700
        db.ride("rider-3", "riverton", "CLASSIC", "2026-10-06T13:00Z", 40);  // 700
        db.ride("rider-4", "riverton", "CARGO", "2026-10-07T13:00Z", 8);     // 300
        db.ride("rider-5", "bramford", "CLASSIC", "2026-10-07T15:00Z", 10);  // 270
        db.ride("rider-6", "bramford", "EBIKE", "2026-10-08T15:00Z", 10);    // 470

        job.runMonth(OCTOBER);

        Map<String, Integer> ranks = db.rows(OCTOBER).stream()
                .collect(Collectors.toMap(row -> row.city() + "/" + row.riderId(), Row::cityRank));
        assertEquals(Map.of("riverton/rider-1", 1, "riverton/rider-2", 2, "riverton/rider-3", 2, "riverton/rider-4", 4,
                        "bramford/rider-6", 1, "bramford/rider-5", 2),
                ranks,
                "city_rank is RANK() OVER (PARTITION BY city ORDER BY total_cents DESC). rider-2 and rider-3 tie on 700, "
                        + "so both are 2 and rider-4 is 4, not 3 (that would be DENSE_RANK). Bramford ranks on its own");
    }

    @Test
    @DisplayName("Stage 2: change_cents is this month's total minus last month's, and NULL for a rider new this month")
    void changeAgainstLastMonth() {
        db.ride("rider-7", "riverton", "CLASSIC", "2026-09-10T12:00Z", 10);  // September 250
        db.ride("rider-7", "riverton", "CLASSIC", "2026-09-20T12:00Z", 10);  // September 250
        db.ride("rider-7", "riverton", "EBIKE", "2026-10-10T12:00Z", 12);    // October 460
        db.ride("rider-7", "riverton", "CLASSIC", "2026-10-11T12:00Z", 30);  // October 550
        db.ride("rider-8", "riverton", "EBIKE", "2026-09-12T12:00Z", 30);    // September 1000
        db.ride("rider-8", "riverton", "CARGO", "2026-10-12T12:00Z", 8);     // October 300
        db.ride("rider-9", "riverton", "CLASSIC", "2026-10-13T12:00Z", 10);  // October 250, first month

        job.runMonth(OCTOBER);

        Map<String, Long> changes = new java.util.HashMap<>();
        db.rows(OCTOBER).forEach(row -> changes.put(row.riderId(), row.changeCents()));
        assertEquals(510L, changes.get("rider-7"), "rider-7 paid 500 in September and 1010 in October: change +510");
        assertEquals(-700L, changes.get("rider-8"), "rider-8 paid 1000 in September and 300 in October: change -700");
        assertEquals(3, changes.size(), "October has three riders: rider-7, rider-8 and rider-9");
        assertEquals(null, changes.get("rider-9"),
                "rider-9 had no rides in September, so change_cents is NULL (no month to compare with), not 0 and not 250");
    }

    @Test
    @DisplayName("Stage 2: change compares with the calendar month before, read from the raw rides, even if it was never built")
    void changeSkipsNothing() {
        db.ride("rider-7", "riverton", "CLASSIC", "2026-08-10T12:00Z", 10);  // August 250, no September ride
        db.ride("rider-7", "riverton", "CLASSIC", "2026-10-10T12:00Z", 20);  // October 400
        db.ride("rider-8", "riverton", "CLASSIC", "2026-09-30T12:00Z", 10);  // September 250
        db.ride("rider-8", "riverton", "CLASSIC", "2026-10-30T12:00Z", 10);  // October 250

        job.runMonth(OCTOBER);   // September's statements were never built

        assertEquals(List.of(
                        new Row("2026-10", "riverton", "rider-7", 1, 400, 20, 1, null),
                        new Row("2026-10", "riverton", "rider-8", 1, 250, 10, 2, 0L)),
                db.rows(OCTOBER),
                "rider-7 skipped September, so October's change is NULL: LAG over all of rider-7's months would wrongly "
                        + "compare with August. rider-8 paid 250 both months: change 0. Read September from the raw rides");
    }
}
