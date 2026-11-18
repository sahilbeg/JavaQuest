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
        // TODO 7: Start a transaction, as in RideEndService (remember auto-commit, setAutoCommit(false),
        //         rollback and rethrow on an error, put auto-commit back in finally).
        //         INSERT INTO processed_event (event_id) VALUES (?) with message.eventId().
        //         If that throws an SQLException whose getSQLState() is "23505" (a duplicate key), this event was
        //         handled before: roll back and return false. Throw any other SQLException on.
        // TODO 8: Add the fare to the bill: UPDATE rider_bill SET total_cents = total_cents + ?, rides = rides + 1
        //         WHERE rider_id = ?. If that changed 0 rows, INSERT INTO rider_bill (rider_id, total_cents, rides)
        //         VALUES (?, ?, 1). Then commit() and return true.
        return false;
    }
}
