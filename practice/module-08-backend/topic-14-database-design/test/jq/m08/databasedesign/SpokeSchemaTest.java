package jq.m08.databasedesign;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SpokeSchemaTest {

    private Connection db;

    @BeforeEach
    void createSchema() throws SQLException {
        db = TestDatabases.fresh();
        for (Migration migration : SpokeSchema.migrations()) {
            TestDatabases.run(db, migration.sql());
        }
        TestDatabases.run(db, """
                INSERT INTO station VALUES ('harbor', 'Harbor', 12), ('uptown', 'Uptown', 12);
                INSERT INTO bike VALUES ('B-1042', 'EBIKE', 'AVAILABLE', 'harbor');
                """);
    }

    @AfterEach
    void close() throws SQLException {
        db.close();
    }

    private void refused(String sql, String why) {
        assertThrows(SQLException.class, () -> TestDatabases.run(db, sql),
                "The database should refuse this row, because " + why + ", but it was saved:\n" + sql);
    }

    @Test
    @DisplayName("Good Spoke rows are accepted, including a rented bike with no station and a ride still going")
    void goodRowsAreAccepted() {
        assertDoesNotThrow(() -> TestDatabases.run(db, """
                INSERT INTO station VALUES ('depot', 'Depot', 20);
                INSERT INTO bike VALUES ('B-1088', 'CLASSIC', 'AVAILABLE', 'depot'), ('B-1017', 'CARGO', 'RENTED', NULL);
                INSERT INTO rental VALUES ('rental-17', 'r-17', 'B-1042', TIMESTAMP '2026-11-09 08:05:00', 12, 460),
                                          ('rental-18', 'r-23', 'B-1017', TIMESTAMP '2026-11-09 09:10:00', NULL, NULL);
                """), "Valid stations, bikes and rentals must still be saved. Did a constraint come out too strict?");
    }

    @Test
    @DisplayName("A station needs a unique name and at least one dock")
    void stationRules() {
        refused("INSERT INTO station VALUES ('depot', NULL, 20)", "a station must have a name (NOT NULL)");
        refused("INSERT INTO station VALUES ('harbor-2', 'Harbor', 12)", "two stations cannot share the name Harbor (UNIQUE)");
        refused("INSERT INTO station VALUES ('depot', 'Depot', 0)", "a station needs more than 0 docks (CHECK)");
        refused("INSERT INTO station VALUES ('depot', 'Depot', NULL)", "docks must be present (NOT NULL)");
    }

    @Test
    @DisplayName("A bike's type and status must be one of the allowed words")
    void bikeTypeAndStatus() {
        refused("INSERT INTO bike VALUES ('B-1099', 'SCOOTER', 'AVAILABLE', 'harbor')",
                "type must be CLASSIC, EBIKE or CARGO (CHECK ... IN)");
        refused("INSERT INTO bike VALUES ('B-1099', 'EBIKE', 'LOST', 'harbor')",
                "status must be AVAILABLE, RENTED or MAINTENANCE (CHECK ... IN)");
        refused("INSERT INTO bike VALUES ('B-1099', NULL, 'AVAILABLE', 'harbor')", "type must be present (NOT NULL)");
        refused("INSERT INTO bike VALUES ('B-1099', 'EBIKE', NULL, 'harbor')", "status must be present (NOT NULL)");
    }

    @Test
    @DisplayName("A bike cannot be docked at a station that does not exist")
    void bikeStationForeignKey() {
        refused("INSERT INTO bike VALUES ('B-1099', 'EBIKE', 'AVAILABLE', 'atlantis')",
                "there is no station 'atlantis' (station_id REFERENCES station(id))");
    }

    @Test
    @DisplayName("A rental needs a rider, a real bike and a start time, and can never have negative minutes or fare")
    void rentalRules() {
        refused("INSERT INTO rental VALUES ('rental-20', 'r-17', 'B-9999', TIMESTAMP '2026-11-09 08:05:00', 12, 460)",
                "there is no bike B-9999 (bike_id REFERENCES bike(id))");
        refused("INSERT INTO rental VALUES ('rental-20', NULL, 'B-1042', TIMESTAMP '2026-11-09 08:05:00', 12, 460)",
                "a rental must have a rider (NOT NULL)");
        refused("INSERT INTO rental VALUES ('rental-20', 'r-17', 'B-1042', NULL, 12, 460)",
                "a rental must have a start time (NOT NULL)");
        refused("INSERT INTO rental VALUES ('rental-20', 'r-17', 'B-1042', TIMESTAMP '2026-11-09 08:05:00', -5, 460)",
                "minutes can never be negative (CHECK (minutes >= 0))");
        refused("INSERT INTO rental VALUES ('rental-20', 'r-17', 'B-1042', TIMESTAMP '2026-11-09 08:05:00', 12, -100)",
                "a fare can never be negative (CHECK (fare_cents >= 0))");
    }

    @Test
    @DisplayName("A station that still docks bikes cannot be deleted")
    void cannotDeleteStationWithBikes() {
        refused("DELETE FROM station WHERE id = 'harbor'",
                "bike B-1042 still points at Harbor, so deleting it would leave a bike at a station that no longer exists");
    }

    @Test
    @DisplayName("A rider's history query uses idx_rental_rider_started to jump to that rider, not a full table scan")
    void historyQueryUsesTheIndex() throws SQLException {
        String plan = TestDatabases.text(db,
                "EXPLAIN SELECT id, started_at FROM rental WHERE rider_id = 'r-17' ORDER BY started_at").toUpperCase();

        assertTrue(plan.contains("IDX_RENTAL_RIDER_STARTED"),
                "EXPLAIN should name the index IDX_RENTAL_RIDER_STARTED. Add Migration 3 that creates it. The plan was:\n" + plan);
        assertTrue(plan.contains("IDX_RENTAL_RIDER_STARTED: RIDER_ID ="),
                "The index should be used to look up rider_id directly, so rider_id must be its FIRST column: "
                        + "ON rental(rider_id, started_at). The plan was:\n" + plan);
    }
}
