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

    // TODO 3 (stage 2): fill in city_rank and change_cents instead of 0 and NULL.
    //   city_rank: RANK() OVER (PARTITION BY city ORDER BY total_cents DESC), over this month's rows only.
    //   change_cents: this month's total minus the same rider's total in the same city in the CALENDAR month before,
    //   read from the raw rides; NULL when that rider had no rides there that month. One way: read both months,
    //   tag each ride with CASE WHEN started_at >= <this month's start> THEN 1 ELSE 0 END, group, use LAG, and keep
    //   the rows tagged 1. Change the parameters you bind in runMonth to match.
    /** One row per city and rider for the month. ?1 = the month as text, ?2 = range start, ?3 = range end. */
    static final String BUILD_SQL = """
            INSERT INTO rider_monthly_statement
                (statement_month, city, rider_id, ride_count, total_cents, longest_minutes, city_rank, change_cents)
            SELECT ?, city, rider_id, COUNT(*), SUM(fare_cents), MAX(minutes), 0, NULL
            FROM ride
            WHERE started_at >= ? AND started_at < ?
            GROUP BY city, rider_id
            """;

    private final DataSource dataSource;

    public StatementJob(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Rebuilds one month's statements in one transaction: delete, insert, check, commit. A rerun gives the same rows. */
    public void runMonth(YearMonth month) {
        MonthRange range = MonthRange.of(month);
        // TODO 2 (stage 1): get a Connection from dataSource, prepare BUILD_SQL, bind month.toString() ("2026-10"),
        //   then range.start() and range.end() with setObject, and executeUpdate(). Close both with try-with-resources.
        // TODO 4 (stage 3): make it one transaction, so a rerun is safe:
        //   connection.setAutoCommit(false); DELETE the month's rows; run BUILD_SQL; QualityChecks.verify(connection, range);
        //   connection.commit(). On any SQLException or RuntimeException: connection.rollback(), then rethrow a
        //   StatementJobException as it is, or wrap anything else in a new StatementJobException.
    }

    /** Runs every month from 'from' to 'to', both included, oldest first. Returns the months in the order run. */
    public List<YearMonth> backfill(YearMonth from, YearMonth to) {
        // TODO 6 (stage 5): throw IllegalArgumentException when from is after to. Then runMonth each month from 'from'
        //   to 'to' (month.plusMonths(1)), add it to a list once it has run, and return the list.
        //   A failing month throws, which stops the backfill there.
        return List.of();
    }
}
