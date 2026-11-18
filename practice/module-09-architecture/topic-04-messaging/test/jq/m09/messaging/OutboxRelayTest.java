package jq.m09.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OutboxRelayTest {

    private Databases rentals;
    private final FakeBroker broker = new FakeBroker();

    @BeforeEach
    void open() throws SQLException {
        rentals = Databases.rentals();
    }

    @AfterEach
    void close() throws SQLException {
        rentals.close();
    }

    private void addFive() throws SQLException {
        for (int i = 1; i <= 5; i++) {
            Databases.addOutboxRow(rentals.observer, "evt-100" + i, "rider-" + i, false);
        }
    }

    @Test
    @DisplayName("The relay publishes every unsent row in id order, marks each one sent, and skips rows already sent")
    void publishesInIdOrder() throws SQLException {
        Databases.addOutboxRow(rentals.observer, "evt-0999", "rider-4", true);   // sent in an earlier run
        Databases.addOutboxRow(rentals.observer, "evt-1003", "rider-7", false);  // ids are not in event-id order
        Databases.addOutboxRow(rentals.observer, "evt-1001", "rider-9", false);
        Databases.addOutboxRow(rentals.observer, "evt-1002", "rider-7", false);
        OutboxRelay relay = new OutboxRelay(broker);

        assertEquals(3, relay.relayPending(rentals.app), "three rows were unsent, so relayPending should return 3");
        assertEquals("evt-1003, evt-1001, evt-1002", broker.eventIds(),
                "unsent rows must be published in outbox id order (the order they were written), not by event id, "
                        + "and evt-0999 was already sent");
        Message first = broker.received.get(0);
        assertEquals("ride-events|rider-7|" + new RideEnded("evt-1003", "rental-17", "rider-7", "B-1042", BikeType.EBIKE,
                        20, 700).toJson(), first.topic() + "|" + first.key() + "|" + first.payload(),
                "each Message should carry the row's topic, message_key as its key, and payload");
        assertEquals("TRUE, TRUE, TRUE, TRUE", rentals.text("SELECT sent FROM outbox ORDER BY id"),
                "every published row should now be marked sent = TRUE");

        assertEquals(0, relay.relayPending(rentals.app), "nothing is left to send, so a second run returns 0");
        assertEquals(3, broker.received.size(), "a second run must not publish anything again");
    }

    @Test
    @DisplayName("An empty outbox publishes nothing and returns 0")
    void emptyOutbox() throws SQLException {
        assertEquals(0, new OutboxRelay(broker).relayPending(rentals.app), "the outbox is empty, so relayPending returns 0");
        assertEquals(0, broker.calls, "with nothing in the outbox, the broker should never be called");
    }

    @Test
    @DisplayName("When the broker fails, the relay stops there, leaves the rest unsent, and the next run carries on in order")
    void stopsAtFirstFailureThenResumes() throws SQLException {
        addFive();
        broker.failOnCall = 3;
        OutboxRelay relay = new OutboxRelay(broker);

        assertEquals(2, relay.relayPending(rentals.app),
                "the broker failed on the 3rd message, so the relay should stop there and return 2");
        assertEquals(3, broker.calls,
                "after the failed 3rd publish the relay must stop: publishing evt-1004 now would put it ahead of evt-1003");
        assertEquals("evt-1001, evt-1002", broker.eventIds(), "only evt-1001 and evt-1002 reached the broker");
        assertEquals("TRUE, TRUE, FALSE, FALSE, FALSE", rentals.text("SELECT sent FROM outbox ORDER BY id"),
                "mark a row sent only AFTER publish succeeds: evt-1003 failed, so it and the rows after it stay unsent");

        assertEquals(3, relay.relayPending(rentals.app), "the broker is back, so the next run sends the other 3");
        assertEquals("evt-1001, evt-1002, evt-1003, evt-1004, evt-1005", broker.eventIds(),
                "after resuming, every event reached the broker exactly once, in order");
        assertEquals("0", rentals.text("SELECT COUNT(*) FROM outbox WHERE sent = FALSE"), "every row should now be sent");
    }

    @Test
    @DisplayName("If the broker got a message but its answer was lost, the row stays unsent and is published again")
    void lostAcknowledgementMeansPublishAgain() throws SQLException {
        Databases.addOutboxRow(rentals.observer, "evt-1001", "rider-7", false);
        broker.ackLostOnCall = 1;
        OutboxRelay relay = new OutboxRelay(broker);

        assertEquals(0, relay.relayPending(rentals.app),
                "publish threw, so the relay cannot know the message arrived: it must not count it as sent");
        assertEquals("FALSE", rentals.text("SELECT sent FROM outbox"),
                "publish threw, so the row must stay sent = FALSE");
        assertEquals(1, relay.relayPending(rentals.app), "the next run should publish evt-1001 again and return 1");
        assertEquals("evt-1001, evt-1001", broker.eventIds(),
                "the broker now has evt-1001 twice. That is at-least-once delivery: never lost, sometimes repeated");
    }

    @Test
    @DisplayName("deleteSent removes only the rows already sent")
    void deleteSentKeepsUnsentRows() throws SQLException {
        addFive();
        broker.failOnCall = 4;
        OutboxRelay relay = new OutboxRelay(broker);
        relay.relayPending(rentals.app);

        assertEquals(3, relay.deleteSent(rentals.app), "three rows were sent, so deleteSent should delete 3 and return 3");
        assertEquals("evt-1004, evt-1005", rentals.text("SELECT event_id FROM outbox ORDER BY id"),
                "the unsent rows evt-1004 and evt-1005 must stay, or those events are lost");
        assertEquals(0, relay.deleteSent(rentals.app), "nothing sent is left, so a second deleteSent returns 0");
    }
}
