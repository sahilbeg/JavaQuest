package jq.m09.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EndToEndTest {

    @Test
    @DisplayName("End to end: a ride event published twice after a lost answer is billed once (effectively once)")
    void effectivelyOnce() throws SQLException {
        try (Databases rentals = Databases.rentals(); Databases billing = Databases.billing()) {
            AtomicInteger next = new AtomicInteger(1001);
            RideEndService rides = new RideEndService(() -> "evt-" + next.getAndIncrement());
            FakeBroker broker = new FakeBroker();
            broker.ackLostOnCall = 2;
            OutboxRelay relay = new OutboxRelay(broker);
            BillingConsumer consumer = new BillingConsumer();

            rides.endRide(rentals.app, "rental-17", 20);
            rides.endRide(rentals.app, "rental-18", 10);
            relay.relayPending(rentals.app);
            relay.relayPending(rentals.app);
            assertEquals("evt-1001, evt-1002, evt-1002", broker.eventIds(),
                    "the broker's answer for evt-1002 was lost, so the relay should publish evt-1002 again on its next run");

            int billed = 0;
            for (Message message : broker.received) {
                if (consumer.handle(billing.app, message)) {
                    billed++;
                }
            }
            assertEquals(2, billed, "three deliveries but two different events: handle should return true only twice");
            assertEquals("rider-7|700|1, rider-9|250|1",
                    billing.rows("SELECT rider_id, total_cents, rides FROM rider_bill ORDER BY rider_id"),
                    "each ride must be billed exactly once: at-least-once delivery plus an idempotent consumer");
        }
    }
}
