package jq.m12.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MonthEndBronzeTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

    private final ReportDatabase db = new ReportDatabase();
    private final MonthEndReport report = new MonthEndReport(db.dataSource);

    @AfterEach
    void dropDatabase() {
        db.shutdown();
    }

    /** October: Riverside 3 rides, Harbor and Old Mill 2 rides with equal totals, Uptown only in September, Depot none. */
    private void seedOctober() {
        db.ride(1, "riverside", "2026-10-02T08:10", 10)      // 250
          .ride(2, "riverside", "2026-10-09T17:45", 20)      // 400
          .ride(3, "riverside", "2026-10-30T07:05", 6)       // 190
          .ride(4, "harbor", "2026-10-03T12:00", 12)         // 280
          .ride(5, "harbor", "2026-10-21T18:30", 8)          // 220
          .ride(6, "old-mill", "2026-10-05T09:00", 10)       // 250
          .ride(7, "old-mill", "2026-10-25T16:20", 10)       // 250
          .ride(8, "uptown", "2026-09-28T08:00", 30)         // September, not October
          .ride(9, "riverside", "2026-11-01T09:00", 40);     // November, not October
    }

    @Test
    @DisplayName("Bronze: the October report lists all five stations, even those with no October rides")
    void everyStationIsListed() {
        seedOctober();
        List<String> ids = report.revenueByStation(OCTOBER).stream().map(StationRevenue::stationId).toList();
        assertEquals(5, ids.size(),
                "finance needs one line per station, 5 in all, but got " + ids + ". A WHERE filter on the ride table "
                        + "throws away the rows a LEFT JOIN kept for stations without rides: move the month filter into ON");
    }

    @Test
    @DisplayName("Bronze: Depot, with no rides at all, shows 0 rides and 0 cents")
    void stationWithoutRidesShowsZero() {
        seedOctober();
        StationRevenue depot = find(report.revenueByStation(OCTOBER), "depot");
        assertEquals(0L, depot.rides(),
                "Depot had no rides. COUNT(*) counts the NULL row a LEFT JOIN makes, so it says 1: count r.id instead");
        assertEquals(Long.valueOf(0), depot.revenueCents(),
                "Depot's revenue should be 0. SUM over no rides is NULL, not 0: wrap it in COALESCE(SUM(...), 0)");
    }

    @Test
    @DisplayName("Bronze: Uptown, which only had a ride in September, shows 0 for October")
    void stationWithRidesInOtherMonthsShowsZero() {
        seedOctober();
        StationRevenue uptown = find(report.revenueByStation(OCTOBER), "uptown");
        assertEquals(new StationRevenue("uptown", "Uptown", 0, 0L), uptown,
                "Uptown's only ride was on 28 September, so October shows 0 rides and 0 cents, not a missing line");
    }

    @Test
    @DisplayName("Bronze: busy stations count only their October rides and add up their fares")
    void busyStationsAddUp() {
        seedOctober();
        List<StationRevenue> lines = report.revenueByStation(OCTOBER);
        assertEquals(new StationRevenue("riverside", "Riverside", 3, 840L), find(lines, "riverside"),
                "Riverside had 3 October rides: 250 + 400 + 190 = 840 cents. Its 1 November ride must not count");
        assertEquals(new StationRevenue("harbor", "Harbor", 2, 500L), find(lines, "harbor"),
                "Harbor had 2 October rides: 280 + 220 = 500 cents");
        assertEquals(new StationRevenue("old-mill", "Old Mill", 2, 500L), find(lines, "old-mill"),
                "Old Mill had 2 October rides: 250 + 250 = 500 cents");
    }

    @Test
    @DisplayName("Bronze: lines come biggest revenue first, and ties are broken by station id")
    void orderIsStable() {
        seedOctober();
        List<String> ids = report.revenueByStation(OCTOBER).stream().map(StationRevenue::stationId).toList();
        assertEquals(List.of("riverside", "harbor", "old-mill", "depot", "uptown"), ids,
                "order by revenue_cents descending, then by station id: Harbor and Old Mill tie on 500, "
                        + "Depot and Uptown tie on 0. Without the second key, a database may return ties in any order");
    }

    @Test
    @DisplayName("Bronze: a month with no rides at all still lists every station at 0")
    void emptyMonth() {
        seedOctober();
        List<StationRevenue> march = report.revenueByStation(YearMonth.of(2026, 3));
        assertEquals(List.of(
                        new StationRevenue("depot", "Depot", 0, 0L),
                        new StationRevenue("harbor", "Harbor", 0, 0L),
                        new StationRevenue("old-mill", "Old Mill", 0, 0L),
                        new StationRevenue("riverside", "Riverside", 0, 0L),
                        new StationRevenue("uptown", "Uptown", 0, 0L)),
                march,
                "March 2026 had no rides: all five stations should show 0 rides and 0 cents, in station id order");
    }

    private static StationRevenue find(List<StationRevenue> lines, String stationId) {
        return lines.stream().filter(line -> line.stationId().equals(stationId)).findFirst()
                .orElseThrow(() -> new AssertionError("the report has no line for " + stationId + ", only " + lines
                        + ". Every station needs a line: put the month filter in the LEFT JOIN's ON, not in WHERE"));
    }
}
