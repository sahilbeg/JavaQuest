package jq.m12.incident;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/** The month's rides for the city council, one page at a time, in (started_at, id) order. */
public class CouncilExport {

    private final DataSource dataSource;

    public CouncilExport(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * One page of the month's rides. cursor is null for the first page, or a page's nextCursor to go on.
     * nextCursor is null when the page has fewer than pageSize rows.
     */
    public ExportPage page(YearMonth month, String cursor, int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be at least 1, but was " + pageSize);
        }
        LocalDateTime afterStartedAt = LocalDateTime.MIN;
        long afterId = Long.MIN_VALUE;
        boolean first = cursor == null;
        if (!first) {
            String[] parts = cursor.split("\\|");
            afterStartedAt = LocalDateTime.parse(parts[0]);
            afterId = Long.parseLong(parts[1]);
        }
        String sql = """
                SELECT id, rider_id, station_id, started_at, fare_cents
                FROM ride
                WHERE started_at >= ? AND started_at < ?
                """ + (first ? "" : "  AND (started_at > ? OR (started_at = ? AND id > ?))\n") + """
                ORDER BY started_at, id
                LIMIT ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = 1;
            statement.setObject(i++, month.atDay(1).atStartOfDay());
            statement.setObject(i++, month.plusMonths(1).atDay(1).atStartOfDay());
            if (!first) {
                statement.setObject(i++, afterStartedAt);
                statement.setObject(i++, afterStartedAt);
                statement.setLong(i++, afterId);
            }
            statement.setInt(i, pageSize);
            List<ExportRow> rows = new ArrayList<>();
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(toRow(result));
                }
            }
            String next = null;
            if (rows.size() == pageSize) {
                ExportRow last = rows.get(rows.size() - 1);
                next = last.startedAt() + "|" + last.id();
            }
            return new ExportPage(List.copyOf(rows), next);
        } catch (SQLException e) {
            throw new ReportException("Could not read a page of the export for " + month, e);
        }
    }

    /** Ready: one result row as an ExportRow. */
    private static ExportRow toRow(ResultSet result) throws SQLException {
        return new ExportRow(result.getLong("id"), result.getString("rider_id"), result.getString("station_id"),
                result.getObject("started_at", LocalDateTime.class), result.getLong("fare_cents"));
    }
}
