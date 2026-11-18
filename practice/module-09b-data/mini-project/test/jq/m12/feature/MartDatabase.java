package jq.m12.feature;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.h2.jdbcx.JdbcDataSource;

/** A fresh in-memory H2 database with the raw ride table and the empty rider_monthly_statement table. */
final class MartDatabase {

    /** One row of rider_monthly_statement. changeCents is null when the column is NULL. */
    record Row(String month, String city, String riderId, int rideCount, long totalCents, int longestMinutes,
               int cityRank, Long changeCents) {

        /** The stage 1 columns only, without rank and change. */
        Totals totals() {
            return new Totals(month, city, riderId, rideCount, totalCents, longestMinutes);
        }
    }

    /** The columns stage 1 builds: who, how many rides, how much, and the longest ride. */
    record Totals(String month, String city, String riderId, int rideCount, long totalCents, int longestMinutes) {}

    private static final AtomicInteger NEXT_NAME = new AtomicInteger();

    final JdbcDataSource dataSource = new JdbcDataSource();
    private long nextId = 1;

    MartDatabase() {
        dataSource.setURL("jdbc:h2:mem:feat12-" + NEXT_NAME.incrementAndGet() + ";DB_CLOSE_DELAY=-1");
        execute("""
                CREATE TABLE ride (
                  id         BIGINT PRIMARY KEY,
                  rider_id   VARCHAR(20) NOT NULL,
                  city       VARCHAR(20) NOT NULL,
                  bike_type  VARCHAR(10) NOT NULL,
                  started_at TIMESTAMP WITH TIME ZONE NOT NULL,
                  minutes    INT NOT NULL,
                  fare_cents BIGINT NOT NULL)""");
        execute("""
                CREATE TABLE rider_monthly_statement (
                  statement_month VARCHAR(7)  NOT NULL,
                  city            VARCHAR(20) NOT NULL,
                  rider_id        VARCHAR(20) NOT NULL,
                  ride_count      INT    NOT NULL,
                  total_cents     BIGINT NOT NULL,
                  longest_minutes INT    NOT NULL,
                  city_rank       INT    NOT NULL,
                  change_cents    BIGINT,
                  PRIMARY KEY (statement_month, city, rider_id))""");
    }

    /** A ride with the right fare: Riverton unlocks for 100, Bramford for 120, plus the bike type's price a minute. */
    long ride(String riderId, String city, String bikeType, String startedAtUtc, int minutes) {
        return ride(riderId, city, bikeType, startedAtUtc, minutes, fare(city, bikeType, minutes));
    }

    long ride(String riderId, String city, String bikeType, String startedAtUtc, int minutes, long fareCents) {
        long id = nextId++;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement insert = connection.prepareStatement("INSERT INTO ride VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            insert.setLong(1, id);
            insert.setString(2, riderId);
            insert.setString(3, city);
            insert.setString(4, bikeType);
            insert.setObject(5, OffsetDateTime.parse(startedAtUtc));
            insert.setInt(6, minutes);
            insert.setLong(7, fareCents);
            insert.executeUpdate();
            return id;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    static long fare(String city, String bikeType, int minutes) {
        boolean bramford = city.equals("bramford");
        long unlock = bramford ? 120 : 100;
        long perMinute = switch (bikeType) {
            case "CLASSIC" -> 15;
            case "EBIKE" -> bramford ? 35 : 30;
            case "CARGO" -> 25;
            default -> throw new IllegalArgumentException(bikeType);
        };
        return unlock + perMinute * minutes;
    }

    /** The statement rows of one month, ordered by city, then rider id. */
    List<Row> rows(YearMonth month) {
        return query("SELECT * FROM rider_monthly_statement WHERE statement_month = '" + month
                + "' ORDER BY city, rider_id");
    }

    /** The month's rows with only the stage 1 columns. */
    List<Totals> totals(YearMonth month) {
        return rows(month).stream().map(Row::totals).toList();
    }

    /** Every statement row, ordered by month, city, then rider id. */
    List<Row> allRows() {
        return query("SELECT * FROM rider_monthly_statement ORDER BY statement_month, city, rider_id");
    }

    private List<Row> query(String sql) {
        List<Row> rows = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                rows.add(new Row(result.getString("statement_month"), result.getString("city"),
                        result.getString("rider_id"), result.getInt("ride_count"), result.getLong("total_cents"),
                        result.getInt("longest_minutes"), result.getInt("city_rank"),
                        result.getObject("change_cents", Long.class)));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return rows;
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
