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
 */
public final class RideAnalytics {

    public static final String TOP_RIDERS_PER_STATION_MONTH = """
            WITH counts AS (
                SELECT start_station_id AS station_id,
                       FORMATDATETIME(started_at, 'yyyy-MM') AS ride_month,
                       rider_id,
                       COUNT(*) AS rides
                FROM ride
                WHERE rider_id IS NOT NULL
                GROUP BY start_station_id, FORMATDATETIME(started_at, 'yyyy-MM'), rider_id
            ), ranked AS (
                SELECT station_id, ride_month, rider_id, rides,
                       RANK() OVER (PARTITION BY station_id, ride_month ORDER BY rides DESC) AS place
                FROM counts
            )
            SELECT station_id, ride_month, rider_id, rides, place
            FROM ranked
            WHERE place <= ?
            ORDER BY station_id, ride_month, place, rider_id""";

    public static final String DAYS_BETWEEN_RIDES = """
            SELECT rider_id,
                   id,
                   DATEDIFF('DAY', LAG(started_at) OVER (PARTITION BY rider_id ORDER BY started_at, id), started_at) AS gap_days
            FROM ride
            WHERE rider_id IS NOT NULL
            ORDER BY rider_id, started_at, id""";

    public static final String DAILY_REVENUE = """
            WITH daily AS (
                SELECT CAST(started_at AS DATE) AS ride_day, fare_cents
                FROM ride
            )
            SELECT ride_day,
                   SUM(fare_cents) AS day_cents,
                   SUM(SUM(fare_cents)) OVER (ORDER BY ride_day ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS running_cents,
                   SUM(SUM(fare_cents)) OVER (ORDER BY ride_day ROWS BETWEEN 2 PRECEDING AND CURRENT ROW) AS last_three_cents
            FROM daily
            GROUP BY ride_day
            ORDER BY ride_day""";

    public static final String RIDERS_WITHOUT_EBIKE = """
            SELECT p.id, p.name
            FROM rider p
            WHERE NOT EXISTS (
                SELECT 1
                FROM ride r JOIN bike b ON b.id = r.bike_id
                WHERE r.rider_id = p.id AND b.type = 'EBIKE'
            )
            ORDER BY p.id""";

    public static final String RIDER_TIERS = """
            SELECT p.id,
                   COUNT(r.id) AS rides,
                   CASE WHEN COUNT(r.id) = 0 THEN 'NEW'
                        WHEN COUNT(r.id) < 5 THEN 'REGULAR'
                        ELSE 'COMMUTER'
                   END AS tier
            FROM rider p LEFT JOIN ride r ON r.rider_id = p.id
            GROUP BY p.id
            ORDER BY p.id""";

    public static final String REACHABLE_STATIONS = """
            WITH RECURSIVE reach(station_id, hops) AS (
                SELECT to_id, 1
                FROM station_link
                WHERE from_id = ?
                UNION ALL
                SELECT l.to_id, r.hops + 1
                FROM reach r JOIN station_link l ON l.from_id = r.station_id
                WHERE r.hops < ?
            )
            SELECT station_id, MIN(hops) AS hops
            FROM reach
            WHERE station_id <> ?
            GROUP BY station_id
            ORDER BY hops, station_id""";

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
