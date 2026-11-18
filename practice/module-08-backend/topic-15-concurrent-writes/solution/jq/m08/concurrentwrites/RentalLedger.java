package jq.m08.concurrentwrites;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Records rentals. A rider may have only one active rental, and the database itself enforces it. */
public final class RentalLedger {

    private RentalLedger() {}

    /** active_rider holds the rider id while the ride is on, and NULL once it ends. */
    public static final String SCHEMA = """
            CREATE TABLE rental (
                id           VARCHAR(20) PRIMARY KEY,
                rider_id     VARCHAR(20) NOT NULL,
                bike_id      VARCHAR(10) NOT NULL,
                active_rider VARCHAR(20),
                CONSTRAINT one_active_rental_per_rider UNIQUE (active_rider)
            )""";

    /** Starts a rental. Returns false, instead of throwing, when the row breaks a UNIQUE constraint (SQLState 23505). */
    public static boolean startRental(Connection connection, String rentalId, String riderId, String bikeId)
            throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO rental (id, rider_id, bike_id, active_rider) VALUES (?, ?, ?, ?)")) {
            insert.setString(1, rentalId);
            insert.setString(2, riderId);
            insert.setString(3, bikeId);
            insert.setString(4, riderId);
            insert.executeUpdate();
            return true;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                return false;
            }
            throw e;
        }
    }

    /** Ends an active rental by clearing active_rider. Returns false when there was no active rental with this id. */
    public static boolean endRental(Connection connection, String rentalId) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE rental SET active_rider = NULL WHERE id = ? AND active_rider IS NOT NULL")) {
            update.setString(1, rentalId);
            return update.executeUpdate() == 1;
        }
    }
}
