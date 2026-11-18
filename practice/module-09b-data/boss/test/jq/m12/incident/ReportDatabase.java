package jq.m12.incident;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import org.h2.jdbcx.JdbcDataSource;

/** A fresh in-memory H2 database with Spoke's station and ride tables, as production has them before the fix. */
final class ReportDatabase {

    private static final AtomicInteger NEXT_NAME = new AtomicInteger();

    final JdbcDataSource dataSource = new JdbcDataSource();

    ReportDatabase() {
        dataSource.setURL("jdbc:h2:mem:inc12-" + NEXT_NAME.incrementAndGet() + ";DB_CLOSE_DELAY=-1");
        execute("CREATE TABLE station (id VARCHAR(20) PRIMARY KEY, name VARCHAR(40) NOT NULL)");
        execute("""
                CREATE TABLE ride (
                  id         BIGINT PRIMARY KEY,
                  rider_id   VARCHAR(20) NOT NULL,
                  station_id VARCHAR(20) NOT NULL REFERENCES station(id),
                  started_at TIMESTAMP   NOT NULL,
                  minutes    INT         NOT NULL,
                  fare_cents BIGINT      NOT NULL)""");
        execute("CREATE INDEX ride_rider_idx ON ride(rider_id)");
        station("riverside", "Riverside");
        station("old-mill", "Old Mill");
        station("harbor", "Harbor");
        station("uptown", "Uptown");
        station("depot", "Depot");
    }

    void station(String id, String name) {
        execute("INSERT INTO station VALUES ('" + id + "', '" + name + "')");
    }

    /** Inserts one ride. Fares follow Riverton's CLASSIC price: 100 cents to unlock plus 15 a minute. */
    ReportDatabase ride(long id, String stationId, String startedAt, int minutes) {
        return ride(id, "rider-" + (id % 7), stationId, LocalDateTime.parse(startedAt), minutes, 100 + 15L * minutes);
    }

    ReportDatabase ride(long id, String riderId, String stationId, LocalDateTime startedAt, int minutes, long fareCents) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement insert = connection.prepareStatement("INSERT INTO ride VALUES (?, ?, ?, ?, ?, ?)")) {
            insert.setLong(1, id);
            insert.setString(2, riderId);
            insert.setString(3, stationId);
            insert.setObject(4, startedAt);
            insert.setInt(5, minutes);
            insert.setLong(6, fareCents);
            insert.executeUpdate();
            return this;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    void execute(String sql) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    void shutdown() {
        execute("SHUTDOWN");
    }
}
