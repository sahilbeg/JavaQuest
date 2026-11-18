package jq.m12.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MonthEndSilverTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);
    private static final String[] STATIONS = {"riverside", "old-mill", "harbor", "uptown", "depot"};

    private final ReportDatabase db = new ReportDatabase();
    private final MonthEndReport report = new MonthEndReport(db.dataSource);

    /** A year of rides: 2,400 in 2026, about 200 a month, at every station. */
    @BeforeEach
    void seedAYear() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 6, 0);
        for (int i = 1; i <= 2_400; i++) {
            db.ride(i, "rider-" + (i % 40), STATIONS[i % 5], start.plusMinutes(i * 219L), 10, 250);
        }
    }

    @AfterEach
    void dropDatabase() {
        db.shutdown();
    }

    @Test
    @DisplayName("Silver: migrate creates ride_station_started_idx on (station_id, started_at), and can run twice")
    void migrateCreatesTheCompositeIndex() throws SQLException {
        MonthEndReport.migrate(db.dataSource);
        MonthEndReport.migrate(db.dataSource);
        assertEquals(List.of("STATION_ID", "STARTED_AT"), indexColumns("RIDE_STATION_STARTED_IDX"),
                "migrate should create the index ride_station_started_idx on ride(station_id, started_at), in that order: "
                        + "the column you match with = comes first, the range column second. "
                        + "Running migrate a second time must not fail: use CREATE INDEX IF NOT EXISTS");
    }

    @Test
    @DisplayName("Silver: the October report reads ride through the index, for both the station and the month range")
    void reportUsesTheIndexForBothColumns() {
        MonthEndReport.migrate(db.dataSource);
        String access = rideAccess(report.explainRevenue(OCTOBER));
        assertTrue(usesTheIndexForBoth(access), indexAdvice(access));
    }

    @Test
    @DisplayName("Silver: the range is half-open: midnight on 1 October counts, midnight on 1 November does not")
    void monthBoundariesAreHalfOpen() {
        MonthEndReport.migrate(db.dataSource);
        db.execute("DELETE FROM ride WHERE started_at >= TIMESTAMP '2026-09-30 00:00:00' "
                + "AND started_at < TIMESTAMP '2026-11-02 00:00:00'");
        db.ride(9001, "rider-7", "harbor", LocalDateTime.parse("2026-09-30T23:59:59.999999"), 10, 250);
        db.ride(9002, "rider-7", "harbor", LocalDateTime.parse("2026-10-01T00:00:00"), 10, 250);
        db.ride(9003, "rider-7", "harbor", LocalDateTime.parse("2026-10-31T23:59:59.999999"), 20, 400);
        db.ride(9004, "rider-7", "harbor", LocalDateTime.parse("2026-11-01T00:00:00"), 10, 250);
        for (int i = 0; i < STATIONS.length; i++) {
            if (!STATIONS[i].equals("harbor")) {
                db.ride(9100 + i, "rider-8", STATIONS[i], LocalDateTime.parse("2026-10-15T12:00"), 10, 250 + i);
            }
        }
        StationRevenue harbor = report.revenueByStation(OCTOBER).stream()
                .filter(line -> line.stationId().equals("harbor")).findFirst().orElseThrow();
        assertEquals(new StationRevenue("harbor", "Harbor", 2, 650L), harbor,
                "October at Harbor is the ride at exactly 00:00 on 1 October (250) and the one a moment before "
                        + "midnight on 31 October (400). Use >= for the start and < for the first moment of November: "
                        + "BETWEEN would count the ride at 00:00 on 1 November too");
        String access = rideAccess(report.explainRevenue(OCTOBER));
        assertTrue(usesTheIndexForBoth(access), "the answer is right, but " + indexAdvice(access));
    }

    private static boolean usesTheIndexForBoth(String access) {
        return access.startsWith("PUBLIC.RIDE_STATION_STARTED_IDX:") && access.contains("STATION_ID =")
                && access.contains("STARTED_AT >=") && access.contains("STARTED_AT <");
    }

    private static String indexAdvice(String access) {
        return "H2 reads the ride table with: " + access + ". It should say PUBLIC.RIDE_STATION_STARTED_IDX: with "
                + "STARTED_AT >= ?1 AND STARTED_AT < ?2 AND STATION_ID = S.ID. Three things must all be true: migrate "
                + "creates the index; the month is a range on the bare column (started_at >= ? AND started_at < ?), "
                + "because EXTRACT(... FROM started_at) hides the column inside a function; and the range sits in the "
                + "LEFT JOIN's ON, because H2 only uses ON conditions to look rows up in the joined table";
    }

    /** The text of H2's comment about how it reads the ride table, on one line. */
    private static String rideAccess(String plan) {
        int table = plan.indexOf("\"PUBLIC\".\"RIDE\"");
        assertTrue(table >= 0, "the report's SQL should read the ride table, but the plan was:\n" + plan);
        int open = plan.indexOf("/*", table);
        int close = plan.indexOf("*/", open);
        return plan.substring(open + 2, close).replaceAll("\\s+", " ").strip();
    }

    private List<String> indexColumns(String indexName) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (Connection connection = db.dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.INDEX_COLUMNS "
                     + "WHERE TABLE_NAME = 'RIDE' AND INDEX_NAME = '" + indexName + "' ORDER BY ORDINAL_POSITION")) {
            while (rows.next()) {
                columns.add(rows.getString(1));
            }
        }
        return columns;
    }
}
