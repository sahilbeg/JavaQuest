package jq.m09.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BillingConsumerTest {

    private Databases billing;
    private final BillingConsumer consumer = new BillingConsumer();

    @BeforeEach
    void open() throws SQLException {
        billing = Databases.billing();
    }

    @AfterEach
    void close() throws SQLException {
        billing.close();
    }

    private static Message ride(String eventId, String rentalId, String riderId, BikeType type, int minutes) {
        RideEnded event = new RideEnded(eventId, rentalId, riderId, "B-1042", type, minutes, type.fareCents(minutes));
        return new Message(eventId, "ride-events", riderId, event.toJson());
    }

    @Test
    @DisplayName("The first delivery of a ride event adds its fare to the rider's bill and remembers the event id")
    void firstDelivery() throws SQLException {
        assertTrue(consumer.handle(billing.app, ride("evt-1001", "rental-17", "rider-7", BikeType.EBIKE, 20)),
                "evt-1001 is new, so handle should bill it and return true");

        assertEquals("rider-7|700|1", billing.rows("SELECT rider_id, total_cents, rides FROM rider_bill"),
                "rider-7 has no bill yet, so handle should insert one: 700 cents, 1 ride. Did you commit()?");
        assertEquals("evt-1001", billing.text("SELECT event_id FROM processed_event"),
                "handle should remember evt-1001 in processed_event, so a repeat can be spotted");
        assertTrue(billing.app.getAutoCommit(), "handle should put auto-commit back the way it was (on) when it is done");
    }

    @Test
    @DisplayName("Two different rides for one rider add up, and each rider gets their own bill")
    void ridesAddUp() throws SQLException {
        consumer.handle(billing.app, ride("evt-1001", "rental-17", "rider-7", BikeType.EBIKE, 20));
        consumer.handle(billing.app, ride("evt-1002", "rental-18", "rider-9", BikeType.CLASSIC, 10));
        assertTrue(consumer.handle(billing.app, ride("evt-1003", "rental-21", "rider-7", BikeType.CARGO, 4)),
                "evt-1003 is a different event, so it must be billed too, even though rider-7 has a bill already");

        assertEquals("rider-7|900|2, rider-9|250|1",
                billing.rows("SELECT rider_id, total_cents, rides FROM rider_bill ORDER BY rider_id"),
                "rider-7 rode twice (700 + 200 = 900 cents, 2 rides): add to the existing row. rider-9 rode once (250 cents)");
    }

    @Test
    @DisplayName("A redelivered event changes nothing and returns false")
    void redeliveryChangesNothing() throws SQLException {
        Message message = ride("evt-1001", "rental-17", "rider-7", BikeType.EBIKE, 20);
        assertTrue(consumer.handle(billing.app, message), "evt-1001 is new, so the first handle should bill it and return true");

        boolean again = consumer.handle(billing.app, message);

        assertFalse(again, "evt-1001 was handled before, so handle should return false. "
                + "Catch the duplicate key error (SQLState 23505) on processed_event instead of throwing");
        assertEquals("rider-7|700|1", billing.rows("SELECT rider_id, total_cents, rides FROM rider_bill"),
                "a repeat of evt-1001 must not bill rider-7 a second time");
        assertEquals("1", billing.text("SELECT COUNT(*) FROM processed_event"), "evt-1001 should be remembered once");
        assertTrue(billing.app.getAutoCommit(), "handle should put auto-commit back on after a repeat too");
    }

    @Test
    @DisplayName("If the bill update fails, the event id is not remembered either, so a retry bills it exactly once")
    void failedEffectIsNotRemembered() throws SQLException {
        Message message = ride("evt-1001", "rental-17", "rider-7", BikeType.EBIKE, 20);
        Databases.run(billing.observer, "ALTER TABLE rider_bill RENAME TO rider_bill_offline");

        assertThrows(SQLException.class, () -> consumer.handle(billing.app, message),
                "the rider_bill table is missing, so the bill update fails and handle should throw that SQLException");
        assertEquals("0", billing.text("SELECT COUNT(*) FROM processed_event"),
                "the bill was not changed, so evt-1001 must not be remembered as processed. Otherwise the retry is skipped "
                        + "and the ride is never billed. Write processed_event and the bill in ONE transaction");

        Databases.run(billing.observer, "ALTER TABLE rider_bill_offline RENAME TO rider_bill");
        assertTrue(consumer.handle(billing.app, message), "after the fix, the retried evt-1001 should be billed: true");
        assertEquals("rider-7|700|1", billing.rows("SELECT rider_id, total_cents, rides FROM rider_bill"),
                "the retry should bill rider-7 exactly once");
        assertTrue(billing.app.getAutoCommit(), "handle should put auto-commit back on, after failures too");
    }
}
