package jq.m12.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A fresh in-memory H2 database with Spoke's rider, bike, ride and station_link tables and the sample data below.
 * Every test gets its own database, so nothing one test does leaks into another.
 */
final class SpokeData {

    static final String SCHEMA = """
            CREATE TABLE rider (
                id   VARCHAR(10) PRIMARY KEY,
                name VARCHAR(20) NOT NULL
            );
            CREATE TABLE bike (
                id   VARCHAR(10) PRIMARY KEY,
                type VARCHAR(10) NOT NULL
            );
            CREATE TABLE ride (
                id               INT PRIMARY KEY,
                rider_id         VARCHAR(10) REFERENCES rider(id),
                bike_id          VARCHAR(10) NOT NULL REFERENCES bike(id),
                start_station_id VARCHAR(20) NOT NULL,
                started_at       TIMESTAMP NOT NULL,
                minutes          INT NOT NULL,
                fare_cents       BIGINT NOT NULL
            );
            CREATE TABLE station_link (
                from_id VARCHAR(20) NOT NULL,
                to_id   VARCHAR(20) NOT NULL
            );
            """;

    // rider_id NULL means a guest ride, paid at the kiosk without an account.
    // fare_cents = 100 to unlock + minutes x 15 (CLASSIC), 30 (EBIKE) or 25 (CARGO).
    static final String DATA = """
            INSERT INTO rider VALUES
                ('rider-1', 'Ada'), ('rider-2', 'Ben'), ('rider-3', 'Cy'), ('rider-4', 'Dee'),
                ('rider-5', 'Eli'), ('rider-6', 'Fay'), ('rider-7', 'Gus'), ('rider-8', 'Hana');
            INSERT INTO bike VALUES
                ('B-1042', 'EBIKE'), ('B-1023', 'EBIKE'), ('B-1088', 'CLASSIC'), ('B-1031', 'CLASSIC'), ('B-1017', 'CARGO');
            INSERT INTO ride VALUES
                ( 1, 'rider-1', 'B-1042', 'harbor',    TIMESTAMP '2026-10-01 08:00:00', 12,  460),
                ( 2, 'rider-2', 'B-1088', 'harbor',    TIMESTAMP '2026-10-01 09:00:00', 20,  400),
                ( 3, NULL,      'B-1023', 'harbor',    TIMESTAMP '2026-10-01 12:00:00', 15,  550),
                ( 4, 'rider-1', 'B-1088', 'harbor',    TIMESTAMP '2026-10-02 08:00:00', 10,  250),
                ( 5, 'rider-2', 'B-1031', 'harbor',    TIMESTAMP '2026-10-02 09:00:00', 18,  370),
                ( 6, NULL,      'B-1088', 'harbor',    TIMESTAMP '2026-10-02 12:00:00', 25,  475),
                ( 7, 'rider-3', 'B-1023', 'harbor',    TIMESTAMP '2026-10-02 17:00:00', 14,  520),
                ( 8, 'rider-1', 'B-1088', 'riverside', TIMESTAMP '2026-10-03 08:00:00',  9,  235),
                ( 9, 'rider-2', 'B-1088', 'harbor',    TIMESTAMP '2026-10-03 09:00:00', 22,  430),
                (10, 'rider-4', 'B-1017', 'harbor',    TIMESTAMP '2026-10-03 12:00:00', 30,  850),
                (11, NULL,      'B-1088', 'harbor',    TIMESTAMP '2026-10-03 13:00:00',  8,  220),
                (12, 'rider-3', 'B-1088', 'harbor',    TIMESTAMP '2026-10-03 17:00:00', 16,  340),
                (13, 'rider-1', 'B-1088', 'harbor',    TIMESTAMP '2026-10-05 08:00:00', 11,  265),
                (14, 'rider-5', 'B-1042', 'harbor',    TIMESTAMP '2026-10-05 12:00:00', 20,  700),
                (15, 'rider-3', 'B-1031', 'harbor',    TIMESTAMP '2026-10-05 17:00:00', 13,  295),
                (16, 'rider-1', 'B-1031', 'riverside', TIMESTAMP '2026-10-05 18:00:00',  7,  205),
                (17, 'rider-6', 'B-1088', 'riverside', TIMESTAMP '2026-10-09 07:30:00', 19,  385),
                (18, 'rider-1', 'B-1042', 'harbor',    TIMESTAMP '2026-10-09 08:00:00', 12,  460),
                (19, 'rider-2', 'B-1017', 'harbor',    TIMESTAMP '2026-10-09 09:00:00', 24,  700),
                (20, 'rider-4', 'B-1088', 'harbor',    TIMESTAMP '2026-10-09 12:00:00', 10,  250),
                (21, 'rider-5', 'B-1031', 'harbor',    TIMESTAMP '2026-10-09 12:30:00', 15,  325),
                (22, 'rider-6', 'B-1031', 'harbor',    TIMESTAMP '2026-11-02 08:00:00', 21,  415),
                (23, 'rider-3', 'B-1088', 'harbor',    TIMESTAMP '2026-11-02 17:00:00', 17,  355),
                (24, 'rider-3', 'B-1017', 'harbor',    TIMESTAMP '2026-11-03 17:00:00', 12,  400),
                (25, 'rider-1', 'B-1088', 'harbor',    TIMESTAMP '2026-11-03 23:50:00',  6,  190);
            INSERT INTO station_link VALUES
                ('riverside', 'old-mill'), ('old-mill', 'riverside'),
                ('old-mill', 'harbor'),
                ('harbor', 'uptown'), ('uptown', 'harbor'),
                ('uptown', 'depot'),
                ('depot', 'riverside');
            """;

    private static final AtomicInteger NEXT_NAME = new AtomicInteger();

    private SpokeData() {}

    /** A brand-new database with a unique name, holding the schema and the sample data. */
    static Connection fresh() throws SQLException {
        Connection connection = empty();
        run(connection, DATA);
        return connection;
    }

    /** A brand-new database with the schema and no rows at all. */
    static Connection empty() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:spoke-analytics-" + NEXT_NAME.incrementAndGet());
        run(connection, SCHEMA);
        return connection;
    }

    /** Runs extra SQL written by a test, for example one more ride. */
    static void run(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
