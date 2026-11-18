package jq.m08.databasedesign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchemaMigratorTest {

    private static final Migration V1 = new Migration(1, "stations",
            "CREATE TABLE station (id VARCHAR(20) PRIMARY KEY, name VARCHAR(40) NOT NULL)");
    private static final Migration V2 = new Migration(2, "each station's bikes",
            "CREATE TABLE bike (id VARCHAR(10) PRIMARY KEY, station_id VARCHAR(20) REFERENCES station(id))");
    private static final Migration V3 = new Migration(3, "first stations",
            "INSERT INTO station VALUES ('harbor', 'Harbor'), ('uptown', 'Uptown')");

    private Connection db;

    @BeforeEach
    void open() throws SQLException {
        db = TestDatabases.fresh();
    }

    @AfterEach
    void close() throws SQLException {
        db.close();
    }

    private int migrate(List<Migration> migrations) {
        return assertTimeoutPreemptively(Duration.ofSeconds(5), () -> SchemaMigrator.migrate(db, migrations),
                "migrate should finish quickly. Is a loop never ending?");
    }

    @Test
    @DisplayName("On an empty database every migration runs, and schema_history records each version with its checksum")
    void appliesEverythingOnce() throws SQLException {
        int applied = migrate(List.of(V1, V2, V3));

        assertEquals(3, applied, "migrate should return 3, the number of migrations it applied");
        assertTrue(TestDatabases.tableExists(db, "bike"), "after migrating, the bike table from V2 should exist");
        assertEquals("2\n", TestDatabases.text(db, "SELECT COUNT(*) FROM station"), "V3 should have inserted 2 stations");
        assertEquals(List.of(1, 2, 3), TestDatabases.historyVersions(db), "schema_history should hold versions 1, 2 and 3");
        assertEquals(SchemaMigrator.checksum(V2.sql()) + "\n",
                TestDatabases.text(db, "SELECT checksum FROM schema_history WHERE version = 2"),
                "schema_history should store checksum(sql) for each version, so later edits can be spotted");
        assertEquals("each station's bikes\n", TestDatabases.text(db, "SELECT description FROM schema_history WHERE version = 2"),
                "schema_history should store each migration's description exactly, apostrophe included. "
                        + "Fill it with a ? parameter, never by gluing it into the SQL with +");
    }

    @Test
    @DisplayName("Running migrate again applies nothing, because every version is already recorded")
    void secondRunAppliesNothing() throws SQLException {
        migrate(List.of(V1, V2, V3));

        int applied = migrate(List.of(V1, V2, V3));

        assertEquals(0, applied, "the second run should apply 0 migrations. Read schema_history and skip versions already in it");
        assertEquals("2\n", TestDatabases.text(db, "SELECT COUNT(*) FROM station"),
                "V3 must not run twice, so there should still be exactly 2 stations");
    }

    @Test
    @DisplayName("Migrations given out of order still run lowest version first")
    void sortsByVersion() throws SQLException {
        List<Migration> shuffled = List.of(V3, V1, V2);

        assertEquals(3, migrate(shuffled), "all 3 migrations should be applied");
        assertEquals(List.of(1, 2, 3), TestDatabases.historyVersions(db), "schema_history should hold versions 1, 2 and 3");
        assertEquals(List.of(V3, V1, V2), shuffled, "migrate must not change the list it was given");
    }

    @Test
    @DisplayName("A new migration added later is the only one that runs")
    void onlyNewMigrationRuns() throws SQLException {
        migrate(List.of(V1, V2));

        int applied = migrate(List.of(V1, V2, V3));

        assertEquals(1, applied, "only V3 is new, so migrate should apply exactly 1 migration");
        assertEquals(List.of(1, 2, 3), TestDatabases.historyVersions(db), "schema_history should now hold versions 1, 2 and 3");
    }

    @Test
    @DisplayName("Editing a migration that was already applied is refused, and nothing new runs")
    void editedMigrationIsRefused() throws SQLException {
        migrate(List.of(V1, V2));
        Migration editedV1 = new Migration(1, "stations",
                "CREATE TABLE station (id VARCHAR(20) PRIMARY KEY, name VARCHAR(80) NOT NULL)");

        MigrationException error = assertThrows(MigrationException.class, () -> migrate(List.of(editedV1, V2, V3)),
                "V1's SQL changed after it was applied, so migrate should throw a MigrationException");
        assertTrue(error.getMessage() != null && error.getMessage().startsWith("V1"),
                "the message should start with V1, so the team knows which file was edited. It was: " + error.getMessage());
        assertEquals(List.of(1, 2), TestDatabases.historyVersions(db),
                "check every checksum BEFORE applying anything: V3 must not run when V1 was edited");
    }

    @Test
    @DisplayName("A failing migration is not recorded, and the fixed version runs next time")
    void failedMigrationIsNotRecorded() throws SQLException {
        Migration brokenV2 = new Migration(2, "bikes",
                "CREATE TABLE bike (id VARCHAR(10) PRIMARY KEY, station_id VARCHAR(20) REFERENCES stations(id))");

        MigrationException error = assertThrows(MigrationException.class, () -> migrate(List.of(V1, brokenV2, V3)),
                "V2 refers to a table that does not exist, so migrate should throw a MigrationException");
        assertTrue(error.getMessage() != null && error.getMessage().startsWith("V2"),
                "the message should start with V2, the migration that failed. It was: " + error.getMessage());
        assertInstanceOf(SQLException.class, error.getCause(), "the MigrationException should keep the SQLException as its cause");
        assertEquals(List.of(1), TestDatabases.historyVersions(db),
                "only V1 worked: V2 failed and V3 must not run after a failure");
        assertTrue(db.getAutoCommit(), "apply should put auto-commit back the way it was, even after a failure");

        assertEquals(2, migrate(List.of(V1, V2, V3)), "with V2 fixed, the next run should apply V2 and V3");
        assertEquals(List.of(1, 2, 3), TestDatabases.historyVersions(db), "schema_history should now hold versions 1, 2 and 3");
    }

    @Test
    @DisplayName("Two migrations with the same version are rejected before anything runs")
    void duplicateVersionsAreRejected() throws SQLException {
        Migration alsoV2 = new Migration(2, "docks", "CREATE TABLE dock (id INT PRIMARY KEY)");

        assertThrows(IllegalArgumentException.class, () -> migrate(List.of(V1, V2, alsoV2)),
                "two migrations share version 2, so migrate should throw IllegalArgumentException");
        assertFalse(TestDatabases.tableExists(db, "station"), "nothing should run when the list itself is wrong");
    }
}
