package jq.m09.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BillingConsumerTest {

    private final EventLog log = new EventLog(4);
    private final OffsetStore offsets = new OffsetStore();
    private final BillingStore store = new BillingStore();
    private final FakeGateway gateway = new FakeGateway();
    private final BillingConsumer consumer = new BillingConsumer(log, offsets, store, gateway);

    @Test
    @DisplayName("Each ride is charged its fare, with the event id as the idempotency key")
    void chargesEachRide() {
        Rides.publish(log, Rides.EVT_1001, Rides.EVT_1002);
        assertEquals(2, consumer.poll(10), "poll should handle both events");
        assertEquals(2, gateway.successes().size(), "both rides should be charged");
        assertTrue(gateway.successes().contains(new FakeGateway.Attempt("rider-7", 460, "evt-1001", true)),
                "rider-7 should be charged 460 cents with idempotency key evt-1001, but the charges were "
                        + gateway.successes());
        assertTrue(gateway.successes().contains(new FakeGateway.Attempt("rider-8", 400, "evt-1002", true)),
                "rider-8 should be charged 400 cents with idempotency key evt-1002, but the charges were "
                        + gateway.successes());
        assertEquals(2, store.charges().size(), "both charges should be recorded in the billing store");
        assertTrue(store.deadLetters().isEmpty(), "nothing went wrong, so the dead-letter queue stays empty");
    }

    @Test
    @DisplayName("The same event appended twice is charged only once")
    void duplicateEvent() {
        Rides.publish(log, Rides.EVT_1001, Rides.EVT_1001);
        assertEquals(2, consumer.poll(10), "both copies are read and handled");
        assertEquals(1, gateway.attempts().size(), "the second copy of evt-1001 must not charge rider-7 again");
        assertEquals(new Statement("rider-7", 460, 1), store.statement("rider-7"), "rider-7 paid for one ride");
    }

    @Test
    @DisplayName("A fare that breaks the fare rule goes to the dead-letter queue, and the next ride is still charged")
    void fareMismatch() {
        Rides.publish(log, Rides.EVT_1004_WRONG_FARE, Rides.EVT_1005);
        assertEquals(2, consumer.poll(10), "both events are handled, even though the first one is wrong");
        assertEquals(1, store.deadLetters().size(), "the event with the wrong fare goes to the dead-letter queue");
        DeadLetter letter = store.deadLetters().get(0);
        assertEquals("evt-1004", letter.event().eventId(), "evt-1004 is the one with the wrong fare");
        assertTrue(letter.reason().startsWith("fare mismatch"),
                "the reason should start with \"fare mismatch\", but was: " + letter.reason());
        assertEquals(0, gateway.attemptsFor("evt-1004"), "a ride with the wrong fare must not be charged at all");
        assertEquals(1, gateway.attemptsFor("evt-1005"), "the next ride, evt-1005, is still charged");
        assertEquals(new Statement("rider-7", 160, 1), store.statement("rider-7"),
                "only evt-1005 counts on rider-7's statement");
    }

    @Test
    @DisplayName("A charge that fails twice is retried, and succeeds on the third attempt")
    void retryThenSuccess() {
        gateway.fail("evt-1001", 2);
        Rides.publish(log, Rides.EVT_1001);
        assertEquals(1, consumer.poll(10), "poll should handle evt-1001");
        assertEquals(3, gateway.attemptsFor("evt-1001"), "two failed attempts, then a third that works");
        assertEquals(1, gateway.successes().size(), "the third attempt charged the rider");
        assertEquals(1, store.charges().size(), "the charge is recorded once");
        assertTrue(store.deadLetters().isEmpty(), "the charge worked in the end, so nothing is dead-lettered");
    }

    @Test
    @DisplayName("After 3 failed attempts the ride goes to the dead-letter queue, and the next ride is still charged")
    void retriesExhausted() {
        gateway.fail("evt-1001", 10);
        Rides.publish(log, Rides.EVT_1001, Rides.EVT_1003);
        assertEquals(2, consumer.poll(10), "both of rider-7's events are handled; one bad charge must not block the partition");
        assertEquals(3, gateway.attemptsFor("evt-1001"), "evt-1001 should be tried exactly 3 times, then given up");
        assertEquals(1, store.deadLetters().size(), "evt-1001 goes to the dead-letter queue");
        DeadLetter letter = store.deadLetters().get(0);
        assertEquals("evt-1001", letter.event().eventId(), "the dead letter holds evt-1001");
        assertTrue(letter.reason().startsWith("payment failed"),
                "the reason should start with \"payment failed\", but was: " + letter.reason());
        assertEquals(1, gateway.attemptsFor("evt-1003"), "the next ride, evt-1003, is still charged");
        assertEquals(0, consumer.lag(), "both offsets are committed, so the partition moves on");
        assertEquals(new Statement("rider-7", 350, 1), store.statement("rider-7"),
                "only evt-1003 was paid, so rider-7's statement shows 350 cents for 1 ride");
    }

    @Test
    @DisplayName("Each rider's rides are charged in the order they happened")
    void orderPerRider() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            for (int i = 1; i <= 3; i++) {
                for (String rider : List.of("rider-7", "rider-8", "rider-9", "rider-31")) {
                    Rides.publish(log, Rides.ride("evt-" + rider.substring(6) + "0" + i, rider, i));
                }
            }
            int rounds = 0;
            while (consumer.poll(1) > 0 && rounds < 20) {
                rounds++;
            }
            for (String rider : List.of("rider-7", "rider-8", "rider-9", "rider-31")) {
                String id = rider.substring(6);
                List<String> keys = gateway.successes().stream()
                        .filter(a -> a.riderId().equals(rider))
                        .map(FakeGateway.Attempt::idempotencyKey)
                        .toList();
                assertEquals(List.of("evt-" + id + "01", "evt-" + id + "02", "evt-" + id + "03"), keys,
                        rider + "'s three rides should be charged in the order they were appended");
            }
        }, "billing should finish 12 events quickly; is poll looping forever?");
    }

    @Test
    @DisplayName("An empty log bills nothing, and an event appended later is picked up")
    void emptyLog() {
        assertEquals(0, consumer.poll(10), "an empty log has nothing to handle");
        assertTrue(gateway.attempts().isEmpty(), "nobody is charged");
        Rides.publish(log, Rides.EVT_1002);
        assertEquals(1, consumer.poll(10), "the new event is read on the next poll");
        assertEquals(1, gateway.successes().size(), "rider-8 is charged for it");
    }

    @Test
    @DisplayName("A rider's statement adds up their charged rides; dead letters and other riders do not count")
    void statement() {
        Rides.publish(log, Rides.EVT_1001, Rides.EVT_1002, Rides.EVT_1003, Rides.EVT_1004_WRONG_FARE);
        consumer.poll(10);
        assertEquals(new Statement("rider-7", 810, 2), store.statement("rider-7"),
                "rider-7 paid 460 + 350 = 810 cents for 2 rides; the wrong-fare ride was not charged");
        assertEquals(new Statement("rider-8", 400, 1), store.statement("rider-8"), "rider-8 paid 400 cents for 1 ride");
        assertEquals(new Statement("rider-99", 0, 0), store.statement("rider-99"),
                "a rider with no rides gets a statement of 0 cents and 0 rides");
    }
}
