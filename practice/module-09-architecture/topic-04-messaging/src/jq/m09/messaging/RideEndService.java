package jq.m09.messaging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Ends rides in the Rentals database. The rental row and the outbox row are written in ONE transaction,
 * so either both are saved or neither is. That is the transactional outbox.
 */
public final class RideEndService {

    /** The topic every ride event goes to. */
    public static final String TOPIC = "ride-events";

    private final Supplier<String> eventIds;

    /** @param eventIds hands out a new event id, such as "evt-1001", each time it is called */
    public RideEndService(Supplier<String> eventIds) {
        this.eventIds = eventIds;
    }

    /**
     * Ends an ACTIVE rental after the given number of minutes and records a RideEnded event in the outbox.
     * Returns the event, or Optional.empty() when there is no ACTIVE rental with this id (nothing is written).
     * Throws the SQLException, with nothing saved, when any write fails.
     */
    public Optional<RideEnded> endRide(Connection db, String rentalId, int minutes) throws SQLException {
        if (minutes < 0) {
            throw new IllegalArgumentException("minutes cannot be negative: " + minutes);
        }
        // TODO 1: Start a transaction. Remember db.getAutoCommit(), then call db.setAutoCommit(false).
        //         Wrap the rest in try / catch / finally: on an SQLException or RuntimeException, db.rollback() and
        //         throw it again; in finally, put auto-commit back to what it was.
        // TODO 2: SELECT rider_id, bike_id, bike_type FROM rental WHERE id = ? AND status = 'ACTIVE'.
        //         No row: roll back and return Optional.empty(). Otherwise build the event:
        //         new RideEnded(eventIds.get(), rentalId, riderId, bikeId, type, minutes, type.fareCents(minutes)),
        //         where type = BikeType.valueOf(the bike_type text).
        // TODO 3: UPDATE rental SET status = 'ENDED', minutes = ?, fare_cents = ? WHERE id = ? AND status = 'ACTIVE'.
        //         Then INSERT INTO outbox (event_id, topic, message_key, payload) VALUES (?, ?, ?, ?)
        //         with the event id, TOPIC, the rider id as the key, and event.toJson().
        //         Then db.commit() and return Optional.of(event).
        return Optional.empty();
    }
}
