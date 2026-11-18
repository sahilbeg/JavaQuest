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
        // TODO 4: SELECT id, event_id, topic, message_key, payload FROM outbox WHERE sent = FALSE ORDER BY id.
        //         Keep each row's id, and a new Message(event_id, topic, message_key, payload), in two lists.
        // TODO 5: For each row, in order: publisher.publish(message). If it throws a RuntimeException, stop at once
        //         (break). Only after publish returns: UPDATE outbox SET sent = TRUE WHERE id = ?, and count it.
        //         Return the count.
        return 0;
    }

    /** Deletes the rows already marked sent, so the outbox stays small. Returns how many rows it deleted. */
    public int deleteSent(Connection db) throws SQLException {
        // TODO 6: DELETE FROM outbox WHERE sent = TRUE, and return how many rows it deleted.
        return 0;
    }
}
