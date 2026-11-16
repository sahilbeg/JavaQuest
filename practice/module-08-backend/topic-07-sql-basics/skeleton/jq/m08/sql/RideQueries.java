package jq.m08.sql;

/**
 * Theo's saved queries. Each constant is one SQL statement for the station, bike and ride tables.
 * The tests build a fresh database with those tables and sample data, run your SQL, and check the rows.
 * Every constant starts as "SELECT 1 WHERE FALSE": valid SQL that returns no rows. Replace each one.
 */
public final class RideQueries {

    private RideQueries() {}

    // TODO 1: the id of every AVAILABLE bike whose station_id is 'harbor', sorted by id
    public static final String FREE_BIKES_AT_HARBOR = "SELECT 1 WHERE FALSE";

    // TODO 2: one number, how many bikes have type 'EBIKE' (any status)
    public static final String EBIKES_COUNT = "SELECT 1 WHERE FALSE";

    // TODO 3: station name and ride count, only stations where a ride started (inner join), most rides first, ties by name
    public static final String RIDES_PER_START_STATION = "SELECT 1 WHERE FALSE";

    // TODO 4: ride id and minutes of the three longest rides, longest first
    public static final String LONGEST_THREE_RIDES = "SELECT 1 WHERE FALSE";

    // TODO 5: the name of every station where no ride started, sorted by name (use a LEFT JOIN)
    public static final String STATIONS_WITH_NO_RIDES = "SELECT 1 WHERE FALSE";

    // TODO 6: rider id and total fare_cents for riders whose total is over 1000, biggest total first
    public static final String RIDERS_OVER_1000_CENTS = "SELECT 1 WHERE FALSE";

    // TODO 7: set status to 'MAINTENANCE' for the one bike whose id is the ? parameter
    public static final String MARK_BIKE_MAINTENANCE = "SELECT 1 WHERE FALSE";
}
