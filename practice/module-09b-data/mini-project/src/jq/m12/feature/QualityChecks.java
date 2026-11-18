package jq.m12.feature;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** The checks a month's statements must pass before they are committed. */
public final class QualityChecks {

    public static final String NO_NEGATIVE_FARES = "no-negative-fares";
    public static final String TOTALS_MATCH = "totals-match";
    public static final String EVERY_RIDER_HAS_A_ROW = "every-rider-has-a-row";

    private QualityChecks() {}

    /** Runs the three checks in order, on this connection, and throws DataQualityException at the first failure. */
    public static void verify(Connection connection, MonthRange range) throws SQLException {
        // TODO 5 (stage 4): run the three checks, in this order, with SQL on this connection.
        //   1. no-negative-fares: no ride in the range has fare_cents < 0.
        //   2. totals-match: SUM(total_cents) of the month's statement rows equals SUM(fare_cents) of the range's rides
        //      (COALESCE both to 0).
        //   3. every-rider-has-a-row: every (city, rider_id) with a ride in the range has a statement row for the month.
        //   At the first failure: throw new DataQualityException(<the check's name constant>, <what was wrong>).
        //   bindRange and single below are ready to use.
    }

    /** Ready: sets range.start() and range.end() as parameters first and first + 1. */
    private static void bindRange(PreparedStatement statement, MonthRange range, int first) throws SQLException {
        statement.setObject(first, range.start());
        statement.setObject(first + 1, range.end());
    }

    /** Ready: runs a query that returns one number, and returns it. */
    private static long single(PreparedStatement statement) throws SQLException {
        try (ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getLong(1);
        }
    }
}
