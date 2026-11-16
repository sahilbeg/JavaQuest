package jq.m08.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RideQueriesTest {

    private SpokeDatabase db;

    @BeforeEach
    void freshDatabase() throws SQLException {
        db = SpokeDatabase.fresh();
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        db.close();
    }

    @Test
    @DisplayName("FREE_BIKES_AT_HARBOR lists the available bikes docked at Harbor, sorted by id")
    void freeBikesAtHarbor() {
        List<String> rows = db.rows("FREE_BIKES_AT_HARBOR", RideQueries.FREE_BIKES_AT_HARBOR);

        assertEquals(List.of("B-1017", "B-1042", "B-1088"), rows,
                "FREE_BIKES_AT_HARBOR should return only the id column, for bikes with status 'AVAILABLE' and station_id 'harbor', "
                        + "smallest id first. B-1063 is at Harbor but in MAINTENANCE, so it must not appear.");
    }

    @Test
    @DisplayName("EBIKES_COUNT counts every e-bike in the fleet, whatever its status")
    void ebikesCount() {
        List<String> rows = db.rows("EBIKES_COUNT", RideQueries.EBIKES_COUNT);

        assertEquals(List.of("5"), rows,
                "EBIKES_COUNT should return one row with one number: the 5 bikes whose type is 'EBIKE', "
                        + "including the one in MAINTENANCE and the one out on a ride");
    }

    @Test
    @DisplayName("RIDES_PER_START_STATION counts rides per start station, busiest first, ties by name")
    void ridesPerStartStation() {
        List<String> rows = db.rows("RIDES_PER_START_STATION", RideQueries.RIDES_PER_START_STATION);

        assertEquals(List.of("Harbor, 5", "Old Mill, 3", "Riverside, 3"), rows,
                "RIDES_PER_START_STATION should return two columns, the station name and its ride count. "
                        + "Join ride to station to get the name, GROUP BY it, then ORDER BY the count DESC and the name. "
                        + "Old Mill and Riverside tie on 3, so Old Mill comes first.");
    }

    @Test
    @DisplayName("LONGEST_THREE_RIDES returns the three longest rides, longest first")
    void longestThreeRides() {
        List<String> rows = db.rows("LONGEST_THREE_RIDES", RideQueries.LONGEST_THREE_RIDES);

        assertEquals(List.of("5, 41", "6, 33", "10, 27"), rows,
                "LONGEST_THREE_RIDES should return two columns, the ride id and its minutes, "
                        + "sorted by minutes DESC and cut to 3 rows with LIMIT 3");
    }

    @Test
    @DisplayName("STATIONS_WITH_NO_RIDES finds the stations where no ride started, sorted by name")
    void stationsWithNoRides() {
        List<String> rows = db.rows("STATIONS_WITH_NO_RIDES", RideQueries.STATIONS_WITH_NO_RIDES);

        assertEquals(List.of("Depot", "Uptown"), rows,
                "STATIONS_WITH_NO_RIDES should return one column, the station name, for stations that no ride started from. "
                        + "LEFT JOIN ride on start_station_id, keep the rows where the ride id IS NULL, and sort by name.");
    }

    @Test
    @DisplayName("RIDERS_OVER_1000_CENTS lists riders whose fares add up to more than 1000 cents, biggest total first")
    void ridersOver1000Cents() {
        List<String> rows = db.rows("RIDERS_OVER_1000_CENTS", RideQueries.RIDERS_OVER_1000_CENTS);

        assertEquals(List.of("r-23, 1995", "r-42, 1765", "r-17, 1590"), rows,
                "RIDERS_OVER_1000_CENTS should return two columns, rider_id and SUM(fare_cents). "
                        + "GROUP BY rider_id, keep the groups with HAVING SUM(fare_cents) > 1000, and sort by the total DESC. "
                        + "r-31 (545) and r-58 (760) are under 1000, so they must not appear.");
    }

    @Test
    @DisplayName("MARK_BIKE_MAINTENANCE moves exactly one bike, the one in the ?, to MAINTENANCE")
    void markBikeMaintenanceChangesOneBike() {
        int changed = db.update("MARK_BIKE_MAINTENANCE", RideQueries.MARK_BIKE_MAINTENANCE, "B-1042");

        assertEquals(1, changed, "MARK_BIKE_MAINTENANCE with B-1042 should change exactly 1 row. Did you forget WHERE id = ?");
        assertEquals("MAINTENANCE", db.single("SELECT status FROM bike WHERE id = 'B-1042'"),
                "after MARK_BIKE_MAINTENANCE with B-1042, that bike's status should be 'MAINTENANCE'");
        assertEquals("3", db.single("SELECT COUNT(*) FROM bike WHERE status = 'MAINTENANCE'"),
                "only B-1042 should change, so 3 bikes should be in MAINTENANCE: B-1063, B-1090 and B-1042");
    }

    @Test
    @DisplayName("MARK_BIKE_MAINTENANCE with an unknown bike id changes nothing")
    void markBikeMaintenanceUnknownBike() {
        int changed = db.update("MARK_BIKE_MAINTENANCE", RideQueries.MARK_BIKE_MAINTENANCE, "B-9999");

        assertEquals(0, changed, "MARK_BIKE_MAINTENANCE with B-9999, a bike that does not exist, should change 0 rows");
        assertEquals("2", db.single("SELECT COUNT(*) FROM bike WHERE status = 'MAINTENANCE'"),
                "no bike should change, so only B-1063 and B-1090 should still be in MAINTENANCE");
    }

    @Test
    @DisplayName("After a bike goes to maintenance, FREE_BIKES_AT_HARBOR no longer lists it")
    void maintenanceRemovesBikeFromFreeList() {
        db.update("MARK_BIKE_MAINTENANCE", RideQueries.MARK_BIKE_MAINTENANCE, "B-1042");

        List<String> rows = db.rows("FREE_BIKES_AT_HARBOR", RideQueries.FREE_BIKES_AT_HARBOR);

        assertEquals(List.of("B-1017", "B-1088"), rows,
                "after B-1042 goes to MAINTENANCE, FREE_BIKES_AT_HARBOR should list only B-1017 and B-1088. "
                        + "Your query must read the status from the table, not a fixed list.");
    }
}
