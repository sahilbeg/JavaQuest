package jq.m09.messaging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Billing's consumer of ride events. The broker may deliver an event more than once, so this consumer is
 * idempotent: it remembers each event id it handled, in the same transaction as the bill change.
 */
public final class BillingConsumer {

    /**
     * Adds the ride's fare to the rider's bill, once per event id.
     * Returns true when the bill changed, and false when this event id was handled before (nothing changes).
     * Throws the SQLException, with nothing saved, when the bill update fails, so the event can be retried.
     */
    public boolean handle(Connection billingDb, Message message) throws SQLException {
        RideEnded ride = RideEnded.fromJson(message.payload());
        boolean oldAutoCommit = billingDb.getAutoCommit();
        billingDb.setAutoCommit(false);
        try {
            try (PreparedStatement remember = billingDb.prepareStatement(
                    "INSERT INTO processed_event (event_id) VALUES (?)")) {
                remember.setString(1, message.eventId());
                remember.executeUpdate();
            } catch (SQLException e) {
                if ("23505".equals(e.getSQLState())) {
                    billingDb.rollback();
                    return false;
                }
                throw e;
            }

            int changed;
            try (PreparedStatement add = billingDb.prepareStatement(
                    "UPDATE rider_bill SET total_cents = total_cents + ?, rides = rides + 1 WHERE rider_id = ?")) {
                add.setLong(1, ride.fareCents());
                add.setString(2, ride.riderId());
                changed = add.executeUpdate();
            }
            if (changed == 0) {
                try (PreparedStatement first = billingDb.prepareStatement(
                        "INSERT INTO rider_bill (rider_id, total_cents, rides) VALUES (?, ?, 1)")) {
                    first.setString(1, ride.riderId());
                    first.setLong(2, ride.fareCents());
                    first.executeUpdate();
                }
            }

            billingDb.commit();
            return true;
        } catch (SQLException | RuntimeException e) {
            billingDb.rollback();
            throw e;
        } finally {
            billingDb.setAutoCommit(oldAutoCommit);
        }
    }
}
