package jq.m08.concurrentwrites;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Rents bikes safely, even when two servers try to rent the same bike at the same moment. */
public final class BikeDesk {

    private BikeDesk() {}

    /**
     * Rents the bike to the rider only if it is AVAILABLE right now. Returns true when this call rented it.
     * The caller owns the transaction: do not commit, roll back or change auto-commit here.
     */
    public static boolean rentIfAvailable(Connection connection, String bikeId, String riderId) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE bike SET status = 'RENTED', rider_id = ? WHERE id = ? AND status = 'AVAILABLE'")) {
            update.setString(1, riderId);
            update.setString(2, bikeId);
            return update.executeUpdate() == 1;
        }
    }
}
