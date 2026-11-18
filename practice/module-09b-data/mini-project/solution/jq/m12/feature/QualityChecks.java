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
        try (PreparedStatement negative = connection.prepareStatement("""
                SELECT id, fare_cents FROM ride
                WHERE started_at >= ? AND started_at < ? AND fare_cents < 0
                ORDER BY id LIMIT 1""")) {
            bindRange(negative, range, 1);
            try (ResultSet rows = negative.executeQuery()) {
                if (rows.next()) {
                    throw new DataQualityException(NO_NEGATIVE_FARES,
                            "ride " + rows.getLong("id") + " has fare_cents " + rows.getLong("fare_cents"));
                }
            }
        }

        long statementTotal;
        try (PreparedStatement total = connection.prepareStatement(
                "SELECT COALESCE(SUM(total_cents), 0) FROM rider_monthly_statement WHERE statement_month = ?")) {
            total.setString(1, range.month().toString());
            statementTotal = single(total);
        }
        long rawTotal;
        try (PreparedStatement total = connection.prepareStatement(
                "SELECT COALESCE(SUM(fare_cents), 0) FROM ride WHERE started_at >= ? AND started_at < ?")) {
            bindRange(total, range, 1);
            rawTotal = single(total);
        }
        if (statementTotal != rawTotal) {
            throw new DataQualityException(TOTALS_MATCH,
                    "statements add up to " + statementTotal + " cents, the raw rides to " + rawTotal + " cents");
        }

        try (PreparedStatement missing = connection.prepareStatement("""
                SELECT r.city, r.rider_id FROM ride r
                WHERE r.started_at >= ? AND r.started_at < ?
                  AND NOT EXISTS (SELECT 1 FROM rider_monthly_statement m
                                  WHERE m.statement_month = ? AND m.city = r.city AND m.rider_id = r.rider_id)
                ORDER BY r.city, r.rider_id LIMIT 1""")) {
            bindRange(missing, range, 1);
            missing.setString(3, range.month().toString());
            try (ResultSet rows = missing.executeQuery()) {
                if (rows.next()) {
                    throw new DataQualityException(EVERY_RIDER_HAS_A_ROW,
                            rows.getString("rider_id") + " rode in " + rows.getString("city") + " but has no statement row");
                }
            }
        }
    }

    private static void bindRange(PreparedStatement statement, MonthRange range, int first) throws SQLException {
        statement.setObject(first, range.start());
        statement.setObject(first + 1, range.end());
    }

    private static long single(PreparedStatement statement) throws SQLException {
        try (ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getLong(1);
        }
    }
}
