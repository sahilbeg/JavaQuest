package jq.m12.incident;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/** The finance team's month-end report: revenue per station for one month. */
public class MonthEndReport {

    /**
     * Every station, with its rides and fares in one month. Filled in by bind(...): ?1 is the first moment of the
     * month and ?2 the first moment of the next month, both Riverton local time.
     */
    static final String REVENUE_SQL = """
            SELECT s.id AS station_id, s.name AS station_name,
                   COUNT(r.id) AS rides,
                   COALESCE(SUM(r.fare_cents), 0) AS revenue_cents
            FROM station s
            LEFT JOIN ride r
                   ON r.station_id = s.id
                  AND r.started_at >= ?
                  AND r.started_at < ?
            GROUP BY s.id, s.name
            ORDER BY revenue_cents DESC, s.id
            """;

    private final DataSource dataSource;

    public MonthEndReport(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** The report's own migration: the indexes its queries need. Safe to run more than once. */
    public static void migrate(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE INDEX IF NOT EXISTS ride_station_started_idx ON ride(station_id, started_at)");
        } catch (SQLException e) {
            throw new ReportException("Could not create the report's indexes", e);
        }
    }

    /** Revenue per station for one month, biggest first, ties by station id. Every station is listed. */
    public List<StationRevenue> revenueByStation(YearMonth month) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(REVENUE_SQL)) {
            bind(statement, month);
            List<StationRevenue> report = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    report.add(toRevenue(rows));
                }
            }
            return report;
        } catch (SQLException e) {
            throw new ReportException("Could not build the revenue report for " + month, e);
        }
    }

    /** Ready: the plan H2 picks for REVENUE_SQL, with the same parameters. Do not change this method. */
    public String explainRevenue(YearMonth month) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("EXPLAIN " + REVENUE_SQL)) {
            bind(statement, month);
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getString(1);
            }
        } catch (SQLException e) {
            throw new ReportException("Could not explain the revenue report for " + month, e);
        }
    }

    /** Fills in REVENUE_SQL's parameters for one month. */
    private static void bind(PreparedStatement statement, YearMonth month) throws SQLException {
        statement.setObject(1, month.atDay(1).atStartOfDay());
        statement.setObject(2, month.plusMonths(1).atDay(1).atStartOfDay());
    }

    /** Ready: one result row as a StationRevenue. A NULL revenue stays null. */
    private static StationRevenue toRevenue(ResultSet rows) throws SQLException {
        return new StationRevenue(rows.getString("station_id"), rows.getString("station_name"),
                rows.getLong("rides"), rows.getObject("revenue_cents", Long.class));
    }
}
