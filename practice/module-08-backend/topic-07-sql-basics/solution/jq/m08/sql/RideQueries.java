package jq.m08.sql;

/**
 * Theo's saved queries. Each constant is one SQL statement for the station, bike and ride tables.
 * The tests build a fresh database with those tables and sample data, run your SQL, and check the rows.
 * Every constant starts as "SELECT 1 WHERE FALSE": valid SQL that returns no rows. Replace each one.
 */
public final class RideQueries {

    private RideQueries() {}

    public static final String FREE_BIKES_AT_HARBOR = """
            SELECT id FROM bike
            WHERE station_id = 'harbor' AND status = 'AVAILABLE'
            ORDER BY id""";

    public static final String EBIKES_COUNT = "SELECT COUNT(*) FROM bike WHERE type = 'EBIKE'";

    public static final String RIDES_PER_START_STATION = """
            SELECT s.name, COUNT(*) AS rides
            FROM ride r JOIN station s ON s.id = r.start_station_id
            GROUP BY s.name
            ORDER BY rides DESC, s.name""";

    public static final String LONGEST_THREE_RIDES = "SELECT id, minutes FROM ride ORDER BY minutes DESC LIMIT 3";

    public static final String STATIONS_WITH_NO_RIDES = """
            SELECT s.name
            FROM station s LEFT JOIN ride r ON r.start_station_id = s.id
            WHERE r.id IS NULL
            ORDER BY s.name""";

    public static final String RIDERS_OVER_1000_CENTS = """
            SELECT rider_id, SUM(fare_cents) AS total_cents
            FROM ride
            GROUP BY rider_id
            HAVING SUM(fare_cents) > 1000
            ORDER BY total_cents DESC""";

    public static final String MARK_BIKE_MAINTENANCE = "UPDATE bike SET status = 'MAINTENANCE' WHERE id = ?";
}
