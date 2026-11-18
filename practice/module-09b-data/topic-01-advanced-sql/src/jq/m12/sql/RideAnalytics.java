package jq.m12.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Jiraiya's ride reports. Each constant is one SQL query for the rider, bike, ride and station_link tables.
 * The methods below are given: they run your SQL with JDBC, fill in the ? marks, and turn each row into a record.
 * README.md lists the exact columns, order and tie rules for every query.
 * Every constant starts as "SELECT 1 WHERE FALSE": valid SQL that returns no rows. Replace each one.
 */
public final class RideAnalytics {

    // TODO 1: top n riders per station and month. Columns: station_id, month ('yyyy-MM'), rider_id, rides, place.
    // Leave out guest rides. place = RANK() by rides, most first, within each station and month. One ? for n: keep place <= ?.
    // Sort by station_id, month, place, rider_id. Hint: count per group in a CTE, rank in a second CTE, filter last.
    public static final String TOP_RIDERS_PER_STATION_MONTH = "SELECT 1 WHERE FALSE";

    // TODO 2: one row per ride with a rider. Columns: rider_id, ride id, gap_days.
    // gap_days = DATEDIFF('DAY', the rider's previous started_at, this started_at): LAG over PARTITION BY rider_id.
    // NULL for each rider's first ride. Sort by rider_id, started_at, id. No ? marks.
    public static final String DAYS_BETWEEN_RIDES = "SELECT 1 WHERE FALSE";

    // TODO 3: one row per day that had rides, guests included. Columns: ride_day (a DATE), day_cents, running_cents, last_three_cents.
    // running_cents: every day so far. last_three_cents: this day and the two ride-days before it. Sort by ride_day. No ? marks.
    // Name the column ride_day, not day: DAY is a reserved word in H2.
    public static final String DAILY_REVENUE = "SELECT 1 WHERE FALSE";

    // TODO 4: every rider who never rode an EBIKE, riders with no rides included. Columns: id, name. Sort by id.
    // Guest rides have rider_id NULL. Make it NULL-safe: NOT IN breaks here. No ? marks.
    public static final String RIDERS_WITHOUT_EBIKE = "SELECT 1 WHERE FALSE";

    // TODO 5: every rider with their number of rides and a tier. Columns: rider id, rides, tier. Sort by rider id.
    // tier: 'NEW' for 0 rides, 'REGULAR' for 1 to 4, 'COMMUTER' for 5 or more. A rider with no rides counts 0. No ? marks.
    public static final String RIDER_TIERS = "SELECT 1 WHERE FALSE";

    // TODO 6: every station reachable from a start station in 1 to maxHops hops along station_link (from_id -> to_id).
    // Columns: station_id, hops (the fewest). Never list the start station. Sort by hops, then station_id.
    // Three ? marks, in this order: 1 = the start station, 2 = maxHops, 3 = the start station again. Use WITH RECURSIVE.
    public static final String REACHABLE_STATIONS = "SELECT 1 WHERE FALSE";

    /** One row of TOP_RIDERS_PER_STATION_MONTH. */
    public record TopRider(String stationId, String month, String riderId, int rides, int place) {}

    /** One row of DAYS_BETWEEN_RIDES. gapDays is null for a rider's first ride. */
    public record RideGap(String riderId, int rideId, Integer gapDays) {}

    /** One row of DAILY_REVENUE. */
    public record DailyRevenue(LocalDate day, long dayCents, long runningCents, long lastThreeDaysCents) {}

    /** One row of RIDERS_WITHOUT_EBIKE. */
    public record Rider(String id, String name) {}

    /** One row of RIDER_TIERS. */
    public record RiderTier(String riderId, int rides, String tier) {}

    /** One row of REACHABLE_STATIONS. */
    public record Reach(String stationId, int hops) {}

    /** How long one query may run before the database stops it. */
    static final int QUERY_TIMEOUT_SECONDS = 3;

    private final Connection connection;

    public RideAnalytics(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    public List<TopRider> topRidersPerStationMonth(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("n must be at least 1, but was " + n);
        }
        return query("TOP_RIDERS_PER_STATION_MONTH", TOP_RIDERS_PER_STATION_MONTH,
                "station_id, month, rider_id, rides, place", List.of(n),
                row -> new TopRider(row.getString(1), row.getString(2), row.getString(3), row.getInt(4), row.getInt(5)));
    }

    public List<RideGap> daysBetweenRides() {
        return query("DAYS_BETWEEN_RIDES", DAYS_BETWEEN_RIDES, "rider_id, ride id, gap_days", List.of(),
                row -> new RideGap(row.getString(1), row.getInt(2), row.getObject(3) == null ? null : row.getInt(3)));
    }

    public List<DailyRevenue> dailyRevenue() {
        return query("DAILY_REVENUE", DAILY_REVENUE, "ride_day, day_cents, running_cents, last_three_cents", List.of(),
                row -> new DailyRevenue(row.getObject(1, LocalDate.class), row.getLong(2), row.getLong(3), row.getLong(4)));
    }

    public List<Rider> ridersWithoutEbike() {
        return query("RIDERS_WITHOUT_EBIKE", RIDERS_WITHOUT_EBIKE, "id, name", List.of(),
                row -> new Rider(row.getString(1), row.getString(2)));
    }

    public List<RiderTier> riderTiers() {
        return query("RIDER_TIERS", RIDER_TIERS, "rider id, rides, tier", List.of(),
                row -> new RiderTier(row.getString(1), row.getInt(2), row.getString(3)));
    }

    public List<Reach> reachableStations(String startStationId, int maxHops) {
        Objects.requireNonNull(startStationId, "startStationId");
        if (maxHops < 1) {
            throw new IllegalArgumentException("maxHops must be at least 1, but was " + maxHops);
        }
        return query("REACHABLE_STATIONS", REACHABLE_STATIONS, "station_id, hops",
                List.of(startStationId, maxHops, startStationId),
                row -> new Reach(row.getString(1), row.getInt(2)));
    }

    @FunctionalInterface
    private interface RowMapper<T> {
        T map(ResultSet row) throws SQLException;
    }

    /** Runs one query: checks the ? count, fills the ? marks in order, checks the column count, maps each row. */
    private <T> List<T> query(String name, String sql, String columns, List<Object> parameters, RowMapper<T> mapper) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int marks = statement.getParameterMetaData().getParameterCount();
            if (marks != parameters.size()) {
                throw new AnalyticsException(name + " should have exactly " + parameters.size() + " ? mark(s), but it has "
                        + marks + ". README.md says what each ? stands for.");
            }
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            try (ResultSet rows = statement.executeQuery()) {
                int expected = columns.split(",").length;
                int actual = rows.getMetaData().getColumnCount();
                if (actual != expected) {
                    throw new AnalyticsException(name + " should return " + expected + " columns (" + columns
                            + "), but it returns " + actual + ".");
                }
                List<T> result = new ArrayList<>();
                while (rows.next()) {
                    result.add(mapper.map(rows));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new AnalyticsException(name + " did not run. The database said: " + e.getMessage(), e);
        }
    }
}
