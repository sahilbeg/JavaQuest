package jq.m09.messaging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Moves events from the outbox table to the broker. Runs again and again, for example every second.
 * Delivery is at-least-once: a message can be published twice, but never lost.
 */
public final class OutboxRelay {

    private final MessagePublisher publisher;

    public OutboxRelay(MessagePublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Publishes every unsent outbox row, lowest id first, and marks each row sent right after the broker
     * confirms it. Stops at the first failed publish and leaves that row and the rest for the next run.
     * Returns how many rows it published and marked sent in this run.
     */
    public int relayPending(Connection db) throws SQLException {
        List<Long> ids = new ArrayList<>();
        List<Message> messages = new ArrayList<>();
        try (PreparedStatement select = db.prepareStatement(
                "SELECT id, event_id, topic, message_key, payload FROM outbox WHERE sent = FALSE ORDER BY id");
             ResultSet rows = select.executeQuery()) {
            while (rows.next()) {
                ids.add(rows.getLong("id"));
                messages.add(new Message(rows.getString("event_id"), rows.getString("topic"),
                        rows.getString("message_key"), rows.getString("payload")));
            }
        }

        int sent = 0;
        for (int i = 0; i < messages.size(); i++) {
            try {
                publisher.publish(messages.get(i));
            } catch (RuntimeException brokerFailed) {
                break;
            }
            try (PreparedStatement mark = db.prepareStatement("UPDATE outbox SET sent = TRUE WHERE id = ?")) {
                mark.setLong(1, ids.get(i));
                mark.executeUpdate();
            }
            sent++;
        }
        return sent;
    }

    /** Deletes the rows already marked sent, so the outbox stays small. Returns how many rows it deleted. */
    public int deleteSent(Connection db) throws SQLException {
        try (PreparedStatement delete = db.prepareStatement("DELETE FROM outbox WHERE sent = TRUE")) {
            return delete.executeUpdate();
        }
    }
}
