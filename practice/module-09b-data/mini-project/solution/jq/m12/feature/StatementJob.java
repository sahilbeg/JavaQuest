package jq.m12.feature;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/** The nightly job that builds rider_monthly_statement from the raw ride table. */
public class StatementJob {

    /** One row per city and rider for the month, with rank and change. ?1 = month start, ?2 = previous month start,
     *  ?3 = month end, ?4 = the month as text. */
    static final String BUILD_SQL = """
            INSERT INTO rider_monthly_statement
                (statement_month, city, rider_id, ride_count, total_cents, longest_minutes, city_rank, change_cents)
            WITH tagged AS (
                SELECT city, rider_id, minutes, fare_cents,
                       CASE WHEN started_at >= ? THEN 1 ELSE 0 END AS is_current
                FROM ride
                WHERE started_at >= ? AND started_at < ?),
            monthly AS (
                SELECT city, rider_id, is_current,
                       COUNT(*) AS ride_count, SUM(fare_cents) AS total_cents, MAX(minutes) AS longest_minutes
                FROM tagged
                GROUP BY city, rider_id, is_current),
            with_change AS (
                SELECT city, rider_id, is_current, ride_count, total_cents, longest_minutes,
                       total_cents - LAG(total_cents) OVER (PARTITION BY city, rider_id ORDER BY is_current) AS change_cents
                FROM monthly)
            SELECT ?, city, rider_id, ride_count, total_cents, longest_minutes,
                   RANK() OVER (PARTITION BY city ORDER BY total_cents DESC),
                   change_cents
            FROM with_change
            WHERE is_current = 1
            """;

    private final DataSource dataSource;

    public StatementJob(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Rebuilds one month's statements in one transaction: delete, insert, check, commit. A rerun gives the same rows. */
    public void runMonth(YearMonth month) {
        MonthRange range = MonthRange.of(month);
        MonthRange previous = MonthRange.of(month.minusMonths(1));
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM rider_monthly_statement WHERE statement_month = ?")) {
                    delete.setString(1, month.toString());
                    delete.executeUpdate();
                }
                try (PreparedStatement build = connection.prepareStatement(BUILD_SQL)) {
                    build.setObject(1, range.start());
                    build.setObject(2, previous.start());
                    build.setObject(3, range.end());
                    build.setString(4, month.toString());
                    build.executeUpdate();
                }
                QualityChecks.verify(connection, range);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                if (e instanceof StatementJobException jobFailure) {
                    throw jobFailure;
                }
                throw new StatementJobException("Could not build the statements for " + month + ": " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new StatementJobException("Could not reach the database for " + month + ": " + e.getMessage(), e);
        }
    }

    /** Runs every month from 'from' to 'to', both included, oldest first. Returns the months in the order run. */
    public List<YearMonth> backfill(YearMonth from, YearMonth to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("backfill needs from <= to, but got " + from + " to " + to);
        }
        List<YearMonth> done = new ArrayList<>();
        for (YearMonth month = from; !month.isAfter(to); month = month.plusMonths(1)) {
            runMonth(month);
            done.add(month);
        }
        return done;
    }
}
