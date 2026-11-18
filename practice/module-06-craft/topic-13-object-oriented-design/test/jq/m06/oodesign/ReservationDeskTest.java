package jq.m06.oodesign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationDeskTest {

    private static final Instant NINE = Instant.parse("2026-11-18T09:00:00Z");

    /** A clock the test can move forward by hand, so expiry needs no real waiting. */
    static final class TestClock extends Clock {
        private volatile Instant now;

        TestClock(Instant start) {
            this.now = start;
        }

        void advance(Duration step) {
            now = now.plus(step);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    private final TestClock clock = new TestClock(NINE);

    /** Docks 1 to 4: CLASSIC B-2001, EBIKE B-1042, CLASSIC B-2002, and an empty dock 4. */
    private ReservationDesk riverside() {
        ReservationDesk desk = new ReservationDesk(4, clock, new StandardPricing());
        desk.dock(1, new Bike("B-2001", BikeType.CLASSIC));
        desk.dock(2, new Bike("B-1042", BikeType.EBIKE));
        desk.dock(3, new Bike("B-2002", BikeType.CLASSIC));
        return desk;
    }

    @Test
    @DisplayName("StandardPricing charges 100 cents to unlock plus the bike type's rate per minute")
    void standardPricing() {
        PricingRule standard = new StandardPricing();
        assertEquals(100, standard.fareCents(new Bike("B-2001", BikeType.CLASSIC), 0),
                "A 0 minute ride is just the 100 cent unlock fee");
        assertEquals(250, standard.fareCents(new Bike("B-2001", BikeType.CLASSIC), 10),
                "10 minutes on a CLASSIC should be 100 + 10 x 15 = 250 cents");
        assertEquals(400, standard.fareCents(new Bike("B-1042", BikeType.EBIKE), 10),
                "10 minutes on an EBIKE should be 100 + 10 x 30 = 400 cents");
        assertEquals(350, standard.fareCents(new Bike("B-3001", BikeType.CARGO), 10),
                "10 minutes on a CARGO bike should be 100 + 10 x 25 = 350 cents");
    }

    @Test
    @DisplayName("A reservation is active until its expiry moment, and expired from that moment on")
    void reservationKnowsWhenItIsActive() {
        Reservation reservation = new Reservation("rider-7", new Bike("B-1042", BikeType.EBIKE), 2,
                NINE.plus(Duration.ofMinutes(10)));
        assertTrue(reservation.isActiveAt(NINE),
                "isActiveAt(09:00) should be true for a reservation that expires at 09:10");
        assertTrue(reservation.isActiveAt(Instant.parse("2026-11-18T09:09:59Z")),
                "isActiveAt(09:09:59) should still be true: one second is left");
        assertFalse(reservation.isActiveAt(Instant.parse("2026-11-18T09:10:00Z")),
                "isActiveAt(09:10:00) should be false: at the expiry moment exactly, it has expired");
        assertFalse(reservation.isActiveAt(Instant.parse("2026-11-18T09:30:00Z")),
                "isActiveAt(09:30) should be false: long after expiry");
    }

    @Test
    @DisplayName("Docks are numbered from 1, hold one bike each, and refuse bad ids")
    void docksHoldOneBikeEach() {
        ReservationDesk desk = new ReservationDesk(3, clock, new StandardPricing());
        assertEquals(Optional.empty(), desk.bikeAt(1), "A new station's dock 1 should be empty");
        Bike bike = new Bike("B-1042", BikeType.EBIKE);
        desk.dock(1, bike);
        assertEquals(Optional.of(bike), desk.bikeAt(1), "After dock(1, B-1042), bikeAt(1) should give B-1042");
        assertThrows(IllegalStateException.class, () -> desk.dock(1, new Bike("B-2001", BikeType.CLASSIC)),
                "Docking into a dock that already holds a bike should throw IllegalStateException");
        assertThrows(IllegalArgumentException.class, () -> desk.dock(4, new Bike("B-2001", BikeType.CLASSIC)),
                "A station with 3 docks has no dock 4, so dock(4, ...) should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> desk.dock(0, new Bike("B-2001", BikeType.CLASSIC)),
                "Dock ids start at 1, so dock(0, ...) should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new ReservationDesk(0, clock, new StandardPricing()),
                "A station with 0 docks makes no sense: the constructor should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("reserve holds the free bike of that type in the lowest dock, for 10 minutes")
    void reserveTakesLowestDockOfThatType() {
        ReservationDesk desk = riverside();
        Optional<Reservation> held = desk.reserve("rider-7", BikeType.CLASSIC);
        assertTrue(held.isPresent(), "Riverside has two CLASSIC bikes, so reserve should succeed");
        assertEquals(new Reservation("rider-7", new Bike("B-2001", BikeType.CLASSIC), 1,
                        Instant.parse("2026-11-18T09:10:00Z")), held.get(),
                "rider-7 should get B-2001 in dock 1 (the lowest dock with a CLASSIC), expiring at 09:10");
        assertEquals(held, desk.reservationOf("rider-7"),
                "reservationOf(rider-7) should give back the reservation just made");
        assertEquals(Optional.empty(), desk.reservationOf("rider-8"),
                "reservationOf a rider with no reservation should be Optional.empty()");
    }

    @Test
    @DisplayName("A reserved bike is never handed to a second rider, and no free bike gives an empty result")
    void reservedBikeIsNotGivenTwice() {
        ReservationDesk desk = riverside();
        Optional<Reservation> first = desk.reserve("rider-1", BikeType.CLASSIC);
        Optional<Reservation> second = desk.reserve("rider-2", BikeType.CLASSIC);
        assertTrue(first.isPresent() && second.isPresent(), "Two CLASSIC bikes means two riders can reserve one each");
        assertEquals(1, first.get().dockId(), "rider-1 should get dock 1");
        assertEquals(3, second.get().dockId(), "rider-2 should skip held dock 1 and get dock 3, the next CLASSIC");
        assertEquals(Optional.empty(), desk.reserve("rider-3", BikeType.CLASSIC),
                "Both CLASSIC bikes are held, so a third CLASSIC reservation should be Optional.empty()");
        assertEquals(Optional.empty(), desk.reserve("rider-4", BikeType.CARGO),
                "Riverside has no CARGO bike, so reserve should be Optional.empty(), not an exception");
        Optional<Reservation> ebike = desk.reserve("rider-3", BikeType.EBIKE);
        assertTrue(ebike.isPresent(), "rider-3 found no CLASSIC, but can still reserve the free EBIKE");
        assertEquals(2, ebike.get().dockId(), "The EBIKE B-1042 stands in dock 2");
    }

    @Test
    @DisplayName("A rider can hold only one reservation at a time")
    void oneReservationPerRider() {
        ReservationDesk desk = riverside();
        Optional<Reservation> first = desk.reserve("rider-7", BikeType.CLASSIC);
        assertTrue(first.isPresent(), "rider-7's first reservation should succeed");
        assertThrows(IllegalStateException.class, () -> desk.reserve("rider-7", BikeType.EBIKE),
                "rider-7 already holds a bike, so a second reserve should throw IllegalStateException");
        assertEquals(first, desk.reservationOf("rider-7"),
                "The refused attempt must leave rider-7's first reservation unchanged");
        assertTrue(desk.reserve("rider-8", BikeType.EBIKE).isPresent(),
                "The refused attempt must not hold the EBIKE, so rider-8 can still reserve it");
    }

    @Test
    @DisplayName("Reservations expire after 10 minutes on the injected clock, freeing the bike and the rider")
    void reservationsExpire() {
        ReservationDesk desk = riverside();
        assertTrue(desk.reserve("rider-1", BikeType.EBIKE).isPresent(), "rider-1 should reserve the only EBIKE");

        clock.advance(Duration.ofMinutes(9).plusSeconds(59));
        assertTrue(desk.reservationOf("rider-1").isPresent(), "At 09:09:59 rider-1's hold is still active");
        assertEquals(Optional.empty(), desk.reserve("rider-2", BikeType.EBIKE),
                "At 09:09:59 the EBIKE is still held for rider-1, so rider-2 gets Optional.empty()");

        clock.advance(Duration.ofSeconds(1));
        assertEquals(Optional.empty(), desk.reservationOf("rider-1"),
                "At 09:10:00 exactly rider-1's hold has expired, so reservationOf should be empty."
                        + " Read the time from the injected clock, never from Instant.now()");
        Optional<Reservation> taken = desk.reserve("rider-2", BikeType.EBIKE);
        assertTrue(taken.isPresent(), "Once rider-1's hold expires, the EBIKE is free for rider-2");
        assertEquals(Instant.parse("2026-11-18T09:20:00Z"), taken.get().expiresAt(),
                "rider-2 reserved at 09:10, so the hold should end at 09:20");
        assertTrue(desk.reserve("rider-1", BikeType.CLASSIC).isPresent(),
                "An expired hold no longer counts, so rider-1 may reserve again without an exception");
    }

    @Test
    @DisplayName("pickUp hands over the bike, empties the dock and tells every listener in order")
    void pickUpFreesTheDockAndNotifies() {
        ReservationDesk desk = riverside();
        List<String> heard = new ArrayList<>();
        desk.onDockFreed(dockId -> heard.add("map " + dockId));
        desk.onDockFreed(dockId -> heard.add("truck " + dockId));
        assertTrue(desk.reserve("rider-7", BikeType.EBIKE).isPresent(), "rider-7 should reserve the EBIKE");

        Bike bike = desk.pickUp("rider-7");
        assertEquals(new Bike("B-1042", BikeType.EBIKE), bike, "pickUp should return the reserved bike, B-1042");
        assertEquals(Optional.empty(), desk.bikeAt(2), "After pickUp, dock 2 should be empty");
        assertEquals(List.of("map 2", "truck 2"), heard,
                "Every listener should hear dockFreed(2) once, in the order they subscribed");
        assertEquals(Optional.empty(), desk.reservationOf("rider-7"),
                "After pickUp the reservation is used up, so reservationOf should be empty");
        desk.dock(2, new Bike("B-1043", BikeType.EBIKE));
        assertEquals("B-1043", desk.bikeAt(2).map(Bike::id).orElse("nothing"),
                "The freed dock 2 should accept a new bike");
    }

    @Test
    @DisplayName("pickUp without an active reservation is refused and tells no listener")
    void pickUpNeedsAnActiveReservation() {
        ReservationDesk desk = riverside();
        List<Integer> heard = new ArrayList<>();
        desk.onDockFreed(heard::add);
        assertThrows(IllegalStateException.class, () -> desk.pickUp("rider-9"),
                "rider-9 never reserved, so pickUp should throw IllegalStateException");

        assertTrue(desk.reserve("rider-1", BikeType.EBIKE).isPresent(), "rider-1 should reserve the EBIKE");
        clock.advance(Duration.ofMinutes(10));
        assertThrows(IllegalStateException.class, () -> desk.pickUp("rider-1"),
                "rider-1's hold expired at 09:10, so pickUp at 09:10 should throw IllegalStateException");
        assertEquals(List.of(), heard, "A refused pickUp frees no dock, so no listener should hear anything");
        assertTrue(desk.bikeAt(2).isPresent(), "A refused pickUp must leave the EBIKE in dock 2");
    }

    @Test
    @DisplayName("quoteCents prices the rider's reserved bike with the pricing rule")
    void quotePricesTheReservedBike() {
        ReservationDesk desk = riverside();
        assertTrue(desk.reserve("rider-1", BikeType.EBIKE).isPresent(), "rider-1 should reserve the EBIKE");
        assertTrue(desk.reserve("rider-2", BikeType.CLASSIC).isPresent(), "rider-2 should reserve a CLASSIC");
        assertEquals(400, desk.quoteCents("rider-1", 10),
                "rider-1 holds an EBIKE: 10 minutes should cost 100 + 10 x 30 = 400 cents");
        assertEquals(400, desk.quoteCents("rider-2", 20),
                "rider-2 holds a CLASSIC: 20 minutes should cost 100 + 20 x 15 = 400 cents");
        assertThrows(IllegalArgumentException.class, () -> desk.quoteCents("rider-1", -5),
                "A ride cannot last -5 minutes, so quoteCents should throw IllegalArgumentException");
        assertThrows(IllegalStateException.class, () -> desk.quoteCents("rider-3", 10),
                "rider-3 holds no bike, so there is nothing to quote: expect IllegalStateException");
    }

    /** A rule written long after ReservationDesk: a flat 300 cents, and cargo bikes ride free. */
    static final class CommunityDayPricing implements PricingRule {
        @Override
        public long fareCents(Bike bike, int minutes) {
            return bike.type() == BikeType.CARGO ? 0 : 300;
        }
    }

    @Test
    @DisplayName("A new pricing rule plugs into the desk without changing the desk")
    void newPricingRulePlugsIn() {
        ReservationDesk desk = new ReservationDesk(2, clock, new CommunityDayPricing());
        desk.dock(1, new Bike("B-3001", BikeType.CARGO));
        desk.dock(2, new Bike("B-1042", BikeType.EBIKE));
        assertTrue(desk.reserve("rider-1", BikeType.CARGO).isPresent(), "rider-1 should reserve the CARGO bike");
        assertTrue(desk.reserve("rider-2", BikeType.EBIKE).isPresent(), "rider-2 should reserve the EBIKE");
        assertEquals(0, desk.quoteCents("rider-1", 45),
                "The desk must ask the PricingRule it was given: community day makes CARGO rides free");
        assertEquals(300, desk.quoteCents("rider-2", 45),
                "The desk must ask the PricingRule it was given: community day charges a flat 300 cents");

        ReservationDesk lambdaDesk = new ReservationDesk(1, clock, (bike, minutes) -> 7L * minutes);
        lambdaDesk.dock(1, new Bike("B-2001", BikeType.CLASSIC));
        assertTrue(lambdaDesk.reserve("rider-3", BikeType.CLASSIC).isPresent(), "rider-3 should reserve the CLASSIC");
        assertEquals(70, lambdaDesk.quoteCents("rider-3", 10),
                "A lambda is a PricingRule too: 7 cents a minute for 10 minutes is 70");
    }

    @Test
    @DisplayName("Four riders racing for the last EBIKE: exactly one wins, every time")
    void raceForTheLastBike() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            for (int round = 1; round <= 100; round++) {
                ReservationDesk desk = new ReservationDesk(1, clock, new StandardPricing());
                desk.dock(1, new Bike("B-1042", BikeType.EBIKE));
                CountDownLatch start = new CountDownLatch(1);
                AtomicInteger winners = new AtomicInteger();
                ConcurrentLinkedQueue<Throwable> crashes = new ConcurrentLinkedQueue<>();
                List<Thread> riders = new ArrayList<>();
                for (int r = 1; r <= 4; r++) {
                    String riderId = "rider-" + r;
                    riders.add(new Thread(() -> {
                        try {
                            start.await();
                            if (desk.reserve(riderId, BikeType.EBIKE).isPresent()) {
                                winners.incrementAndGet();
                            }
                        } catch (Throwable t) {
                            crashes.add(t);
                        }
                    }));
                }
                riders.forEach(Thread::start);
                start.countDown();
                for (Thread rider : riders) {
                    rider.join();
                }
                assertEquals(List.of(), List.copyOf(crashes),
                        "Round " + round + ": no rider's reserve call should throw");
                assertTrue(winners.get() >= 1,
                        "Round " + round + ": four riders tried for one free EBIKE and nobody won."
                                + " Does reserve work yet?");
                assertEquals(1, winners.get(),
                        "Round " + round + ": four riders tried for one EBIKE, so exactly one should win."
                                + " Is every public method synchronized?");
            }
        }, "The race test took too long. Does a thread wait forever?");
    }

    @Test
    @DisplayName("One rider tapping reserve twice at once still gets only one reservation")
    void doubleTapGivesOneReservation() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            for (int round = 1; round <= 100; round++) {
                ReservationDesk desk = riverside();
                CountDownLatch start = new CountDownLatch(1);
                AtomicInteger held = new AtomicInteger();
                AtomicInteger refused = new AtomicInteger();
                List<Thread> taps = new ArrayList<>();
                for (int t = 0; t < 2; t++) {
                    taps.add(new Thread(() -> {
                        try {
                            start.await();
                            if (desk.reserve("rider-7", BikeType.CLASSIC).isPresent()) {
                                held.incrementAndGet();
                            }
                        } catch (IllegalStateException e) {
                            refused.incrementAndGet();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }));
                }
                taps.forEach(Thread::start);
                start.countDown();
                for (Thread tap : taps) {
                    tap.join();
                }
                assertEquals(1, held.get(),
                        "Round " + round + ": rider-7 tapped twice at once, so exactly one tap should get a bike");
                assertEquals(1, refused.get(),
                        "Round " + round + ": the other tap should be refused with IllegalStateException");
            }
        }, "The double-tap test took too long. Does a thread wait forever?");
    }
}
