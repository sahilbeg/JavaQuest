package jq.m08.concurrentwrites;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RentalLedgerTest {

    private TwoServers servers;

    @BeforeEach
    void ledger() throws SQLException {
        servers = new TwoServers(RentalLedger.SCHEMA);
    }

    @AfterEach
    void close() throws SQLException {
        servers.close();
    }

    private String active(String riderId) throws SQLException {
        return TwoServers.text(servers.serverA,
                "SELECT id FROM rental WHERE active_rider = '" + riderId + "' ORDER BY id");
    }

    @Test
    @DisplayName("Starting a rental saves it as active for that rider")
    void startsARental() throws SQLException {
        assertTrue(RentalLedger.startRental(servers.serverA, "rental-17", "r-17", "B-1042"),
                "r-17 has no active rental, so startRental should save rental-17 and return true");
        assertEquals("rental-17", active("r-17"), "rental-17 should be saved with active_rider = 'r-17'");
    }

    @Test
    @DisplayName("A rider cannot have two active rentals, even when the other server inserts first")
    void oneActiveRentalPerRider() throws SQLException {
        RentalLedger.startRental(servers.serverA, "rental-17", "r-17", "B-1042");
        boolean second = assertDoesNotThrow(() -> RentalLedger.startRental(servers.serverB, "rental-18", "r-17", "B-1076"),
                "startRental should catch the constraint violation and return false, not throw");
        assertFalse(second, "r-17 already rides rental-17, so a second active rental must be refused with false");

        TwoServers.SneakBeforeFirstWrite b = new TwoServers.SneakBeforeFirstWrite(servers.serverB,
                () -> RentalLedger.startRental(servers.serverA, "rental-20", "r-23", "B-1088"));
        boolean yours = assertDoesNotThrow(() -> RentalLedger.startRental(b.connection, "rental-21", "r-23", "B-1031"),
                "startRental should catch the constraint violation and return false, not throw");

        assertTrue(b.sneaked, "startRental should INSERT the rental, but it sent no write at all");
        assertFalse(yours, "the other server started rental-20 for r-23 just before your INSERT, so yours must return false. "
                + "Checking with a SELECT first cannot see it coming: let a UNIQUE constraint on active_rider decide");
        assertEquals("rental-17", active("r-17"), "r-17 must have exactly one active rental");
        assertEquals("rental-20", active("r-23"), "r-23 must have exactly one active rental");
    }

    @Test
    @DisplayName("After a rental ends, the rider can start a new one, and ended rides never clash")
    void endThenRentAgain() throws SQLException {
        RentalLedger.startRental(servers.serverA, "rental-17", "r-17", "B-1042");

        assertTrue(RentalLedger.endRental(servers.serverA, "rental-17"), "rental-17 was active, so endRental should return true");
        assertFalse(RentalLedger.endRental(servers.serverA, "rental-17"), "rental-17 already ended, so ending it again returns false");
        assertFalse(RentalLedger.endRental(servers.serverA, "rental-99"), "there is no rental-99, so endRental returns false");
        assertTrue(RentalLedger.startRental(servers.serverB, "rental-18", "r-17", "B-1076"),
                "r-17's old ride ended, so a new rental should work");
        assertTrue(RentalLedger.endRental(servers.serverB, "rental-18"), "rental-18 was active, so endRental should return true");
        assertTrue(RentalLedger.startRental(servers.serverA, "rental-19", "r-17", "B-1042"),
                "two ended rides both hold NULL in active_rider, and NULLs never clash in a UNIQUE column");
        assertEquals("rental-19", active("r-17"), "only rental-19 should be active for r-17");
        assertEquals("3", TwoServers.text(servers.serverA, "SELECT COUNT(*) FROM rental WHERE rider_id = 'r-17'"),
                "all three of r-17's rentals should still be in the table");
    }
}
