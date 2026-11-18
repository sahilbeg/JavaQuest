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
        // TODO 4 (gold): the cursor is a row count, so rows that arrive or leave during the export shift every page.
        //   Make the cursor "<startedAt>|<id>" of the page's last row (LocalDateTime.toString(), then '|', then id).
        //   For a non-null cursor, keep only rows after it: started_at > t OR (started_at = t AND id > id).
        //   Order by started_at, then id, and drop OFFSET.
        int offset = cursor == null ? 0 : Integer.parseInt(cursor);
        String sql = """
                SELECT id, rider_id, station_id, started_at, fare_cents
                FROM ride
                WHERE started_at >= ? AND started_at < ?
                ORDER BY started_at
                LIMIT ? OFFSET ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, month.atDay(1).atStartOfDay());
            statement.setObject(2, month.plusMonths(1).atDay(1).atStartOfDay());
            statement.setInt(3, pageSize);
            statement.setInt(4, offset);
            List<ExportRow> rows = new ArrayList<>();
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(toRow(result));
                }
            }
            String next = rows.size() < pageSize ? null : String.valueOf(offset + rows.size());
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
