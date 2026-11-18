package jq.m08.concurrentwrites;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FareEditorTest {

    private TwoServers servers;

    @BeforeEach
    void fares() throws SQLException {
        servers = new TwoServers(
                "CREATE TABLE fare (bike_type VARCHAR(10) PRIMARY KEY, cents_per_minute BIGINT NOT NULL, version BIGINT NOT NULL)",
                "INSERT INTO fare VALUES ('CLASSIC', 15, 1), ('EBIKE', 30, 1), ('CARGO', 25, 1)");
    }

    @AfterEach
    void close() throws SQLException {
        servers.close();
    }

    @Test
    @DisplayName("read returns the fare with its version, and empty for an unknown bike type")
    void readsAFare() throws SQLException {
        assertEquals(Optional.of(new Fare("EBIKE", 30, 1)), FareEditor.read(servers.serverA, "EBIKE"),
                "EBIKE costs 30 cents a minute and its row is at version 1");
        assertEquals(Optional.empty(), FareEditor.read(servers.serverA, "SCOOTER"), "there is no SCOOTER fare, so read gives Optional.empty()");
    }

    @Test
    @DisplayName("An update with the current version changes the price and bumps the version")
    void updateWithCurrentVersion() throws SQLException {
        assertTrue(FareEditor.updateWithVersion(servers.serverA, "EBIKE", 35, 1),
                "the EBIKE row is at version 1, so an update that expects version 1 should work and return true");
        assertEquals("35 2", TwoServers.text(servers.serverA, "SELECT cents_per_minute || ' ' || version FROM fare WHERE bike_type = 'EBIKE'"),
                "EBIKE should now cost 35 and be at version 2 (version = version + 1)");
        assertEquals("15 1", TwoServers.text(servers.serverA, "SELECT cents_per_minute || ' ' || version FROM fare WHERE bike_type = 'CLASSIC'"),
                "other fares must not change");
    }

    @Test
    @DisplayName("Two editors read version 1: the second save is refused instead of silently undoing the first")
    void staleVersionIsRefused() throws SQLException {
        Optional<Fare> readA = FareEditor.read(servers.serverA, "EBIKE");
        Optional<Fare> readB = FareEditor.read(servers.serverB, "EBIKE");
        assertTrue(readA.isPresent() && readB.isPresent(), "both editors should be able to read the EBIKE fare. Finish TODO 2 first");
        long versionA = readA.get().version();
        long versionB = readB.get().version();

        boolean savedA = FareEditor.updateWithVersion(servers.serverA, "EBIKE", 35, versionA);
        boolean savedB = FareEditor.updateWithVersion(servers.serverB, "EBIKE", 32, versionB);

        assertTrue(savedA, "editor A saved first, while the row was still at version 1, so A should get true");
        assertFalse(savedB, "editor B read version 1, but A already moved the row to version 2. B's save must return false");
        assertEquals("35 2", TwoServers.text(servers.serverA, "SELECT cents_per_minute || ' ' || version FROM fare WHERE bike_type = 'EBIKE'"),
                "A's price of 35 must survive. If it says 32, B's stale save overwrote it: that is a lost update");

        long freshVersion = FareEditor.read(servers.serverB, "EBIKE").map(Fare::version).orElse(-1L);
        assertTrue(FareEditor.updateWithVersion(servers.serverB, "EBIKE", 32, freshVersion),
                "after B reads again and sees version 2, its retry should work");
        assertEquals("32 3", TwoServers.text(servers.serverA, "SELECT cents_per_minute || ' ' || version FROM fare WHERE bike_type = 'EBIKE'"),
                "after B's retry, EBIKE should cost 32 at version 3");
    }
}
