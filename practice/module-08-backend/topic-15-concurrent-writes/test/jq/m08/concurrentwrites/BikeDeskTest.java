package jq.m08.concurrentwrites;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BikeDeskTest {

    private TwoServers servers;

    @BeforeEach
    void fleet() throws SQLException {
        servers = new TwoServers(
                "CREATE TABLE bike (id VARCHAR(10) PRIMARY KEY, status VARCHAR(12) NOT NULL, rider_id VARCHAR(20))",
                "INSERT INTO bike VALUES ('B-1042', 'AVAILABLE', NULL), ('B-1088', 'MAINTENANCE', NULL)");
    }

    @AfterEach
    void close() throws SQLException {
        servers.close();
    }

    private String bike(String id) throws SQLException {
        return TwoServers.text(servers.serverA, "SELECT status || ' ' || COALESCE(rider_id, '-') FROM bike WHERE id = '" + id + "'");
    }

    @Test
    @DisplayName("An available bike is rented to the rider")
    void rentsAnAvailableBike() throws SQLException {
        assertTrue(BikeDesk.rentIfAvailable(servers.serverA, "B-1042", "r-17"),
                "B-1042 is AVAILABLE, so rentIfAvailable should rent it and return true");
        assertEquals("RENTED r-17", bike("B-1042"), "B-1042 should now be RENTED to r-17");
    }

    @Test
    @DisplayName("When two servers rent the same bike one after the other, only the first one gets it")
    void secondServerLoses() throws SQLException {
        boolean first = BikeDesk.rentIfAvailable(servers.serverA, "B-1042", "r-17");
        boolean second = BikeDesk.rentIfAvailable(servers.serverB, "B-1042", "r-23");

        assertTrue(first, "server A asked first, while B-1042 was AVAILABLE, so it should get true");
        assertFalse(second, "server B asked after B-1042 was RENTED, so it should get false");
        assertEquals("RENTED r-17", bike("B-1042"), "B-1042 must stay with r-17. Server B must not overwrite the rider");
    }

    @Test
    @DisplayName("When the other server rents the bike between your read and your write, only one rider gets it")
    void otherServerSneaksIn() throws SQLException {
        boolean[] otherGotIt = {false};
        TwoServers.SneakBeforeFirstWrite b = new TwoServers.SneakBeforeFirstWrite(servers.serverB, () -> {
            try {
                otherGotIt[0] = BikeDesk.rentIfAvailable(servers.serverA, "B-1042", "r-23");
            } catch (SQLException lockedOut) {
                otherGotIt[0] = false;   // it waited for your lock and gave up: also fine
            }
        });
        servers.serverB.setAutoCommit(false);   // the caller owns the transaction

        boolean youGotIt = BikeDesk.rentIfAvailable(b.connection, "B-1042", "r-17");
        servers.serverB.commit();

        assertTrue(b.sneaked, "rentIfAvailable should write to the bike table with an UPDATE, but it sent no write at all");
        assertTrue(youGotIt != otherGotIt[0], "exactly one server may get B-1042, but your call returned " + youGotIt
                + " and the other server's returned " + otherGotIt[0]
                + ". A SELECT followed by a plain UPDATE lets both win: put the status check inside the UPDATE's WHERE.");
        assertEquals("RENTED " + (youGotIt ? "r-17" : "r-23"), bike("B-1042"),
                "the bike must belong to whichever server got true");
    }

    @Test
    @DisplayName("A bike in maintenance, or one that does not exist, is not rented")
    void notAvailable() throws SQLException {
        BikeDesk.rentIfAvailable(servers.serverA, "B-1042", "r-17");

        assertFalse(BikeDesk.rentIfAvailable(servers.serverA, "B-1088", "r-17"), "B-1088 is in MAINTENANCE, so the answer is false");
        assertFalse(BikeDesk.rentIfAvailable(servers.serverA, "B-9999", "r-17"), "there is no bike B-9999, so the answer is false");
        assertEquals("MAINTENANCE -", bike("B-1088"), "B-1088 must stay in MAINTENANCE with no rider");
        assertEquals("RENTED r-17", bike("B-1042"), "B-1042 must not change");
    }
}
