package jq.m08.sql;

import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A fresh in-memory H2 database with Spoke's three tables and sample data. Each test gets its own,
 * so a change made by one test never leaks into another.
 */
final class SpokeDatabase implements AutoCloseable {

    static final String SCHEMA_AND_DATA = """
            CREATE TABLE station (
                id    VARCHAR(20) PRIMARY KEY,
                name  VARCHAR(40) NOT NULL,
                docks INT NOT NULL
            );
            CREATE TABLE bike (
                id         VARCHAR(10) PRIMARY KEY,
                type       VARCHAR(10) NOT NULL,
                status     VARCHAR(12) NOT NULL,
                station_id VARCHAR(20) REFERENCES station(id)
            );
            CREATE TABLE ride (
                id               INT PRIMARY KEY,
                bike_id          VARCHAR(10) NOT NULL REFERENCES bike(id),
                rider_id         VARCHAR(10) NOT NULL,
                start_station_id VARCHAR(20) NOT NULL REFERENCES station(id),
                end_station_id   VARCHAR(20) REFERENCES station(id),
                started_at       TIMESTAMP NOT NULL,
                minutes          INT NOT NULL,
                fare_cents       BIGINT NOT NULL
            );
            INSERT INTO station VALUES
                ('riverside', 'Riverside', 12), ('old-mill', 'Old Mill', 12), ('harbor', 'Harbor', 12),
                ('uptown', 'Uptown', 12), ('depot', 'Depot', 12);
            INSERT INTO bike VALUES
                ('B-1088', 'CLASSIC', 'AVAILABLE', 'harbor'),
                ('B-1023', 'EBIKE', 'AVAILABLE', 'riverside'),
                ('B-1042', 'EBIKE', 'AVAILABLE', 'harbor'),
                ('B-1063', 'EBIKE', 'MAINTENANCE', 'harbor'),
                ('B-1031', 'CLASSIC', 'AVAILABLE', 'riverside'),
                ('B-1017', 'CARGO', 'AVAILABLE', 'harbor'),
                ('B-1076', 'EBIKE', 'AVAILABLE', 'old-mill'),
                ('B-1090', 'CLASSIC', 'MAINTENANCE', 'uptown'),
                ('B-1012', 'CARGO', 'AVAILABLE', 'depot'),
                ('B-1099', 'EBIKE', 'RENTED', NULL),
                ('B-1050', 'CLASSIC', 'RENTED', NULL);
            INSERT INTO ride VALUES
                (1, 'B-1042', 'r-17', 'harbor', 'riverside', TIMESTAMP '2026-11-09 08:05:00', 12, 460),
                (2, 'B-1088', 'r-23', 'harbor', 'old-mill', TIMESTAMP '2026-11-09 08:40:00', 20, 400),
                (3, 'B-1023', 'r-17', 'riverside', 'harbor', TIMESTAMP '2026-11-09 17:30:00', 25, 850),
                (4, 'B-1031', 'r-31', 'riverside', 'uptown', TIMESTAMP '2026-11-10 07:55:00', 9, 235),
                (5, 'B-1017', 'r-42', 'harbor', 'depot', TIMESTAMP '2026-11-10 12:10:00', 41, 1125),
                (6, 'B-1076', 'r-23', 'old-mill', 'harbor', TIMESTAMP '2026-11-11 09:00:00', 33, 1090),
                (7, 'B-1042', 'r-17', 'harbor', 'old-mill', TIMESTAMP '2026-11-11 18:20:00', 6, 280),
                (8, 'B-1050', 'r-31', 'old-mill', 'riverside', TIMESTAMP '2026-11-12 08:15:00', 14, 310),
                (9, 'B-1099', 'r-42', 'riverside', 'harbor', TIMESTAMP '2026-11-12 16:45:00', 18, 640),
                (10, 'B-1088', 'r-23', 'harbor', 'riverside', TIMESTAMP '2026-11-13 10:30:00', 27, 505),
                (11, 'B-1076', 'r-58', 'old-mill', 'uptown', TIMESTAMP '2026-11-13 17:05:00', 22, 760);
            """;

    private static final AtomicInteger NEXT_NAME = new AtomicInteger();

    private final Connection connection;

    private SpokeDatabase(Connection connection) {
        this.connection = connection;
    }

    /** A brand-new database with a unique name, filled with the sample data above. */
    static SpokeDatabase fresh() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:spoke-sql-" + NEXT_NAME.incrementAndGet());
        try (Statement statement = connection.createStatement()) {
            statement.execute(SCHEMA_AND_DATA);
        }
        return new SpokeDatabase(connection);
    }

    /** Runs a query and returns each row as its columns joined by ", ", for example "Harbor, 5". */
    List<String> rows(String constantName, String sql) {
        requireSql(constantName, sql);
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            int columns = result.getMetaData().getColumnCount();
            List<String> rows = new ArrayList<>();
            while (result.next()) {
                List<String> values = new ArrayList<>();
                for (int column = 1; column <= columns; column++) {
                    values.add(result.getString(column));
                }
                rows.add(String.join(", ", values));
            }
            return rows;
        } catch (SQLException e) {
            return fail(constantName + " did not run. The database said: " + e.getMessage());
        }
    }

    /** Runs a statement that has exactly one ? parameter, filled with the given value. Returns the rows changed. */
    int update(String constantName, String sql, String value) {
        requireSql(constantName, sql);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int parameters = statement.getParameterMetaData().getParameterCount();
            if (parameters != 1) {
                fail(constantName + " should have exactly one ? where the bike id goes, but it has " + parameters);
            }
            statement.setString(1, value);
            return statement.executeUpdate();
        } catch (SQLException e) {
            return fail(constantName + " did not run. The database said: " + e.getMessage());
        }
    }

    /** One value from a query the test itself writes, for checking what an update changed. */
    String single(String sql) {
        List<String> rows = rows("The test's own query", sql);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private static void requireSql(String constantName, String sql) {
        if (sql == null || sql.isBlank()) {
            fail(constantName + " is empty. Write one SQL statement in it.");
        }
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
