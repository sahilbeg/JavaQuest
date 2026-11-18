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
        boolean oldAutoCommit = db.getAutoCommit();
        db.setAutoCommit(false);
        try {
            RideEnded event = null;
            try (PreparedStatement select = db.prepareStatement(
                    "SELECT rider_id, bike_id, bike_type FROM rental WHERE id = ? AND status = 'ACTIVE'")) {
                select.setString(1, rentalId);
                try (ResultSet row = select.executeQuery()) {
                    if (row.next()) {
                        BikeType type = BikeType.valueOf(row.getString("bike_type"));
                        event = new RideEnded(eventIds.get(), rentalId, row.getString("rider_id"),
                                row.getString("bike_id"), type, minutes, type.fareCents(minutes));
                    }
                }
            }
            if (event == null) {
                db.rollback();
                return Optional.empty();
            }

            try (PreparedStatement update = db.prepareStatement(
                    "UPDATE rental SET status = 'ENDED', minutes = ?, fare_cents = ? WHERE id = ? AND status = 'ACTIVE'")) {
                update.setInt(1, minutes);
                update.setLong(2, event.fareCents());
                update.setString(3, rentalId);
                if (update.executeUpdate() != 1) {
                    db.rollback();
                    return Optional.empty();
                }
            }

            try (PreparedStatement insert = db.prepareStatement(
                    "INSERT INTO outbox (event_id, topic, message_key, payload) VALUES (?, ?, ?, ?)")) {
                insert.setString(1, event.eventId());
                insert.setString(2, TOPIC);
                insert.setString(3, event.riderId());
                insert.setString(4, event.toJson());
                insert.executeUpdate();
            }

            db.commit();
            return Optional.of(event);
        } catch (SQLException | RuntimeException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(oldAutoCommit);
        }
    }
}
