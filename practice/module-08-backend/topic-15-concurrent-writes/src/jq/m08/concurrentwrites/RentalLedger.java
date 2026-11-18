package jq.m08.concurrentwrites;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/** Records rentals. A rider may have only one active rental, and the database itself enforces it. */
public final class RentalLedger {

    private RentalLedger() {}

    /** active_rider holds the rider id while the ride is on, and NULL once it ends. */
    // TODO 4: add a constraint named one_active_rental_per_rider that makes active_rider UNIQUE.
    //         Many rows may hold NULL (ended rides), but only one row may hold each rider id.
    public static final String SCHEMA = """
            CREATE TABLE rental (
                id           VARCHAR(20) PRIMARY KEY,
                rider_id     VARCHAR(20) NOT NULL,
                bike_id      VARCHAR(10) NOT NULL,
                active_rider VARCHAR(20)
            )""";

    /** Starts a rental. Returns false, instead of throwing, when the row breaks a UNIQUE constraint (SQLState 23505). */
    public static boolean startRental(Connection connection, String rentalId, String riderId, String bikeId)
            throws SQLException {
        // TODO 5: INSERT INTO rental (id, rider_id, bike_id, active_rider) VALUES (?, ?, ?, ?), with active_rider = riderId.
        //         Return true when it worked. Catch SQLException: return false when e.getSQLState() is "23505"
        //         (a UNIQUE violation), and throw e again for anything else.
        //         Do not SELECT first to check: let the UNIQUE constraint decide.
        return false;
    }

    /** Ends an active rental by clearing active_rider. Returns false when there was no active rental with this id. */
    public static boolean endRental(Connection connection, String rentalId) throws SQLException {
        // TODO 6: UPDATE rental SET active_rider = NULL WHERE id = ? AND active_rider IS NOT NULL; true when 1 row changed
        return false;
    }
}
