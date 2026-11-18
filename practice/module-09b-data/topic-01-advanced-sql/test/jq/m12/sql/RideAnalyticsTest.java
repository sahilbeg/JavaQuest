package jq.m12.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;
import jq.m12.sql.RideAnalytics.DailyRevenue;
import jq.m12.sql.RideAnalytics.Reach;
import jq.m12.sql.RideAnalytics.RideGap;
import jq.m12.sql.RideAnalytics.Rider;
import jq.m12.sql.RideAnalytics.RiderTier;
import jq.m12.sql.RideAnalytics.TopRider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RideAnalyticsTest {

    private Connection connection;
    private RideAnalytics analytics;

    @BeforeEach
    void freshDatabase() throws SQLException {
        connection = SpokeData.fresh();
        analytics = new RideAnalytics(connection);
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        connection.close();
    }

    /** Runs one report and turns a query that cannot run into a readable failure. */
    private static <T> T report(Supplier<T> call) {
        try {
            return call.get();
        } catch (AnalyticsException e) {
            return fail(e.getMessage());
        }
    }

    private static List<RideGap> gapsOf(List<RideGap> gaps, String riderId) {
        return gaps.stream().filter(gap -> riderId.equals(gap.riderId())).toList();
    }

    // ---------- TOP_RIDERS_PER_STATION_MONTH ----------

    @Test
    @DisplayName("Top 3 riders per station and month, with tied riders sharing a place")
    void topThreeRiders() {
        List<TopRider> top = report(() -> analytics.topRidersPerStationMonth(3));

        assertEquals(List.of(
                new TopRider("harbor", "2026-10", "rider-1", 4, 1),
                new TopRider("harbor", "2026-10", "rider-2", 4, 1),
                new TopRider("harbor", "2026-10", "rider-3", 3, 3),
                new TopRider("harbor", "2026-11", "rider-3", 2, 1),
                new TopRider("harbor", "2026-11", "rider-1", 1, 2),
                new TopRider("harbor", "2026-11", "rider-6", 1, 2),
                new TopRider("riverside", "2026-10", "rider-1", 2, 1),
                new TopRider("riverside", "2026-10", "rider-6", 1, 2)), top,
                "With n = 3: rider-1 and rider-2 tie on 4 rides at Harbor in October, so both get place 1 and rider-3 gets "
                        + "place 3 (RANK skips a place after a tie). Rows are sorted by station_id, month, place, rider_id.");
    }

    @Test
    @DisplayName("A tie at the cut-off keeps every tied rider, and the place after a tie is skipped")
    void tieAtTheCutOff() {
        List<TopRider> octoberAtHarbor = report(() -> analytics.topRidersPerStationMonth(4)).stream()
                .filter(row -> row.stationId().equals("harbor") && row.month().equals("2026-10"))
                .toList();

        assertEquals(List.of(
                new TopRider("harbor", "2026-10", "rider-1", 4, 1),
                new TopRider("harbor", "2026-10", "rider-2", 4, 1),
                new TopRider("harbor", "2026-10", "rider-3", 3, 3),
                new TopRider("harbor", "2026-10", "rider-4", 2, 4),
                new TopRider("harbor", "2026-10", "rider-5", 2, 4)), octoberAtHarbor,
                "With n = 4, rider-4 and rider-5 tie for place 4, so both are kept: 5 rows. ROW_NUMBER would drop one of them "
                        + "and number the places 1, 2, 3, 4. DENSE_RANK would give places 1, 1, 2, 3, 3. Use RANK.");
    }

    @Test
    @DisplayName("With n = 1, each station and month lists only its first-place riders")
    void onlyTheWinners() {
        List<TopRider> top = report(() -> analytics.topRidersPerStationMonth(1));

        assertEquals(List.of(
                new TopRider("harbor", "2026-10", "rider-1", 4, 1),
                new TopRider("harbor", "2026-10", "rider-2", 4, 1),
                new TopRider("harbor", "2026-11", "rider-3", 2, 1),
                new TopRider("riverside", "2026-10", "rider-1", 2, 1)), top,
                "With n = 1, keep the rows whose place is 1. The ranking restarts for every station and month, "
                        + "so PARTITION BY needs both the station and the month.");
    }

    @Test
    @DisplayName("Guest rides, which have no rider, never appear in the top riders")
    void guestsAreLeftOut() {
        List<TopRider> top = report(() -> analytics.topRidersPerStationMonth(10));

        assertTrue(top.stream().noneMatch(row -> row.riderId() == null),
                "Rides with rider_id NULL are guest rides. Leave them out with WHERE rider_id IS NOT NULL, "
                        + "or the 3 guest rides at Harbor in October form a 'rider' of their own. Got: " + top);
        assertEquals(10, top.size(),
                "With n = 10 every real rider at every station and month is kept: 5 + 3 + 2 = 10 rows. Got: " + top);
    }

    // ---------- DAYS_BETWEEN_RIDES ----------

    @Test
    @DisplayName("Days between rides: rider-1's seven rides, in time order")
    void gapsForAda() {
        List<RideGap> ada = gapsOf(report(() -> analytics.daysBetweenRides()), "rider-1");

        assertEquals(List.of(
                new RideGap("rider-1", 1, null),
                new RideGap("rider-1", 4, 1),
                new RideGap("rider-1", 8, 1),
                new RideGap("rider-1", 13, 2),
                new RideGap("rider-1", 16, 0),
                new RideGap("rider-1", 18, 4),
                new RideGap("rider-1", 25, 25)), ada,
                "Each ride's gap is DATEDIFF('DAY', the rider's previous started_at, this started_at), using LAG over "
                        + "PARTITION BY rider_id ORDER BY started_at. The first ride has no previous ride, so its gap is NULL. "
                        + "Rides 13 and 16 are on the same day, so ride 16's gap is 0.");
    }

    @Test
    @DisplayName("Days between rides restart for every rider and cross month ends")
    void gapsRestartPerRider() {
        List<RideGap> gaps = report(() -> analytics.daysBetweenRides());

        assertEquals(List.of(
                new RideGap("rider-3", 7, null),
                new RideGap("rider-3", 12, 1),
                new RideGap("rider-3", 15, 2),
                new RideGap("rider-3", 23, 28),
                new RideGap("rider-3", 24, 1)), gapsOf(gaps, "rider-3"),
                "rider-3's first ride must have a NULL gap, even though rider-2 rode just before. That is what PARTITION BY rider_id does. "
                        + "From 5 October to 2 November is 28 days.");
        assertEquals(List.of(new RideGap("rider-6", 17, null), new RideGap("rider-6", 22, 24)), gapsOf(gaps, "rider-6"),
                "rider-6 rode on 9 October and 2 November: a gap of 24 days");
    }

    @Test
    @DisplayName("Days between rides has one row per rider ride, sorted by rider, then time, and no guest rides")
    void gapsShape() {
        List<RideGap> gaps = report(() -> analytics.daysBetweenRides());

        assertEquals(22, gaps.size(), "There are 25 rides and 3 are guest rides, so expect 22 rows. Got: " + gaps);
        assertTrue(gaps.stream().noneMatch(gap -> gap.riderId() == null), "Guest rides (rider_id NULL) must be left out");
        assertEquals(List.of("rider-1", "rider-2", "rider-3", "rider-4", "rider-5", "rider-6"),
                gaps.stream().filter(gap -> gap.gapDays() == null).map(RideGap::riderId).toList(),
                "Exactly one row per rider has a NULL gap, their first ride, and rows are sorted by rider_id first");
    }

    // ---------- DAILY_REVENUE ----------

    @Test
    @DisplayName("Daily revenue with a running total and a three-ride-day window, guests included")
    void dailyRevenue() {
        List<DailyRevenue> days = report(() -> analytics.dailyRevenue());

        assertEquals(List.of(
                new DailyRevenue(LocalDate.of(2026, 10, 1), 1410, 1410, 1410),
                new DailyRevenue(LocalDate.of(2026, 10, 2), 1615, 3025, 3025),
                new DailyRevenue(LocalDate.of(2026, 10, 3), 2075, 5100, 5100),
                new DailyRevenue(LocalDate.of(2026, 10, 5), 1465, 6565, 5155),
                new DailyRevenue(LocalDate.of(2026, 10, 9), 2120, 8685, 5660),
                new DailyRevenue(LocalDate.of(2026, 11, 2), 770, 9455, 4355),
                new DailyRevenue(LocalDate.of(2026, 11, 3), 590, 10045, 3480)), days,
                "One row per day that had rides, oldest first. day_cents adds every fare that day, guests included. "
                        + "running_cents adds every day so far (ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW). "
                        + "last_three_cents adds this day and the two ride-days before it (ROWS BETWEEN 2 PRECEDING AND CURRENT ROW).");
    }

    @Test
    @DisplayName("A ride added later shows up in its day and in every running total after it")
    void dailyRevenueReadsTheTable() throws SQLException {
        SpokeData.run(connection, "INSERT INTO ride VALUES (26, 'rider-7', 'B-1088', 'uptown', TIMESTAMP '2026-10-05 20:00:00', 10, 250)");

        List<DailyRevenue> days = report(() -> analytics.dailyRevenue());

        assertEquals(new DailyRevenue(LocalDate.of(2026, 10, 5), 1715, 6815, 5405), days.get(3),
                "After a 250-cent ride on 5 October, that day earns 1465 + 250 = 1715, and the running total grows by 250");
        assertEquals(10295, days.getLast().runningCents(),
                "The last running total must equal every fare added together: 10045 + 250 = 10295");
    }

    // ---------- RIDERS_WITHOUT_EBIKE ----------

    @Test
    @DisplayName("Riders who never rode an e-bike, including riders with no rides, even though a guest rode one")
    void ridersWithoutEbike() {
        List<Rider> riders = report(() -> analytics.ridersWithoutEbike());

        assertEquals(List.of(new Rider("rider-2", "Ben"), new Rider("rider-4", "Dee"), new Rider("rider-6", "Fay"),
                        new Rider("rider-7", "Gus"), new Rider("rider-8", "Hana")), riders,
                "rider-1, rider-3 and rider-5 rode an EBIKE, so they are out. rider-7 and rider-8 never rode at all, so they are in. "
                        + "If you got no rows, you used NOT IN: guest ride 3 rode an EBIKE with rider_id NULL, and x NOT IN (..., NULL) "
                        + "is never true. Use NOT EXISTS.");
    }

    @Test
    @DisplayName("A rider drops off the no-e-bike list once they ride an e-bike")
    void firstEbikeRide() throws SQLException {
        SpokeData.run(connection, "INSERT INTO ride VALUES (26, 'rider-7', 'B-1023', 'uptown', TIMESTAMP '2026-11-04 08:00:00', 10, 400)");

        List<Rider> riders = report(() -> analytics.ridersWithoutEbike());

        assertFalse(riders.contains(new Rider("rider-7", "Gus")),
                "rider-7 just rode B-1023, an EBIKE, so they must leave the list. Got: " + riders);
        assertEquals(4, riders.size(), "Four riders are left: rider-2, rider-4, rider-6 and rider-8. Got: " + riders);
    }

    // ---------- RIDER_TIERS ----------

    @Test
    @DisplayName("Rider tiers: every rider with their ride count and NEW, REGULAR or COMMUTER")
    void riderTiers() {
        List<RiderTier> tiers = report(() -> analytics.riderTiers());

        assertEquals(List.of(
                new RiderTier("rider-1", 7, "COMMUTER"),
                new RiderTier("rider-2", 4, "REGULAR"),
                new RiderTier("rider-3", 5, "COMMUTER"),
                new RiderTier("rider-4", 2, "REGULAR"),
                new RiderTier("rider-5", 2, "REGULAR"),
                new RiderTier("rider-6", 2, "REGULAR"),
                new RiderTier("rider-7", 0, "NEW"),
                new RiderTier("rider-8", 0, "NEW")), tiers,
                "Every rider appears, sorted by id. 0 rides is NEW, 1 to 4 is REGULAR, 5 or more is COMMUTER. "
                        + "rider-2 (4 rides) and rider-3 (5 rides) sit on either side of the line.");
    }

    @Test
    @DisplayName("A rider with no rides counts 0 rides, not 1")
    void noRidesCountsZero() {
        RiderTier gus = report(() -> analytics.riderTiers()).stream()
                .filter(tier -> "rider-7".equals(tier.riderId()))
                .findFirst()
                .orElse(null);

        assertEquals(new RiderTier("rider-7", 0, "NEW"), gus,
                "rider-7 has no rides. After a LEFT JOIN, the row for rider-7 has NULL in every ride column. COUNT(*) counts "
                        + "that row and gives 1. COUNT(r.id) skips the NULL and gives 0. A missing rider-7 means you used an inner join.");
    }

    // ---------- REACHABLE_STATIONS ----------

    @Test
    @DisplayName("Stations reachable from Harbor within 2 and within 4 hops, fewest hops first")
    void reachableFromHarbor() {
        assertEquals(List.of(new Reach("uptown", 1), new Reach("depot", 2)),
                report(() -> analytics.reachableStations("harbor", 2)),
                "From harbor, the lanes go harbor -> uptown (1 hop) -> depot (2 hops). Stop growing the path when hops reaches the limit.");
        assertEquals(List.of(new Reach("uptown", 1), new Reach("depot", 2), new Reach("riverside", 3), new Reach("old-mill", 4)),
                report(() -> analytics.reachableStations("harbor", 4)),
                "Within 4 hops: uptown 1, depot 2, riverside 3, old-mill 4. Sort by hops, then station_id.");
    }

    @Test
    @DisplayName("Each station is listed once with its fewest hops, and the start station is never listed")
    void fewestHopsAndNoStart() {
        List<Reach> reach = report(() -> analytics.reachableStations("old-mill", 4));

        assertEquals(List.of(new Reach("harbor", 1), new Reach("riverside", 1), new Reach("uptown", 2), new Reach("depot", 3)), reach,
                "Lanes form loops: old-mill -> riverside -> old-mill, and harbor -> uptown -> harbor. So the same station is "
                        + "reached many ways. GROUP BY station_id with MIN(hops) keeps the shortest, and WHERE station_id <> the start "
                        + "drops old-mill itself. Ties on hops are sorted by station_id.");
    }

    @Test
    @DisplayName("A station with no lanes reaches nothing")
    void unknownStation() {
        assertEquals(List.of(), report(() -> analytics.reachableStations("atlantis", 3)),
                "No lane starts at 'atlantis', so the starting part of the recursive CTE finds no rows, and nothing is reachable");
    }
}
