package jq.m10.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MockLoopGoldTest {

    private static final Duration LIMIT = Duration.ofSeconds(4);

    private static ReservationRequest request(String riderId, String dockId, BikeType type) {
        return new ReservationRequest(riderId, dockId, type);
    }

    /** Runs each task on its own thread, all released at the same moment, and returns their results. */
    private static List<ReservationResult> race(DockReservations desk, List<ReservationRequest> requests)
            throws InterruptedException {
        CountDownLatch go = new CountDownLatch(1);
        ReservationResult[] results = new ReservationResult[requests.size()];
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            int index = i;
            Thread thread = new Thread(() -> {
                try {
                    go.await();
                    results[index] = desk.reserve(requests.get(index));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            thread.setDaemon(true);
            threads.add(thread);
            thread.start();
        }
        go.countDown();
        for (Thread thread : threads) {
            thread.join();
        }
        return List.of(results);
    }

    @Test
    @DisplayName("Gold: a new rule plugs in from outside, without editing DockReservations")
    void newRulePlugsIn() {
        ReservationRule suspended = r -> r.riderId().equals("rider-13")
                ? Optional.of("rider-suspended") : Optional.empty();
        DockReservations desk = new DockReservations(List.of(suspended));

        ReservationResult refused = desk.reserve(request("rider-13", "harbor-3", BikeType.EBIKE));
        assertFalse(refused.reserved(), "The rule handed to the constructor refuses rider-13, so reserve must refuse");
        assertEquals("rider-suspended", refused.reason(), "The refusal's reason should be the rule's own reason");
        assertEquals(Optional.empty(), desk.holder("harbor-3"), "A refused request must not hold the dock");

        ReservationResult ok = desk.reserve(request("rider-7", "harbor-3", BikeType.EBIKE));
        assertTrue(ok.reserved(), "The rule allows rider-7, so rider-7 gets harbor-3");
        assertEquals(Optional.of("rider-7"), desk.holder("harbor-3"), "harbor-3 should now be held by rider-7");
    }

    @Test
    @DisplayName("Gold: rules run in the order given, and the first refusal wins")
    void rulesRunInOrder() {
        List<String> ran = new ArrayList<>();
        ReservationRule allowAll = r -> {
            ran.add("allow-all");
            return Optional.empty();
        };
        ReservationRule quietHours = r -> {
            ran.add("quiet-hours");
            return Optional.of("quiet-hours");
        };
        ReservationRule uptownClosed = r -> {
            ran.add("uptown-closed");
            return Optional.of("uptown-closed");
        };
        DockReservations desk = new DockReservations(List.of(allowAll, quietHours, uptownClosed));

        ReservationResult result = desk.reserve(request("rider-7", "uptown-1", BikeType.CLASSIC));
        assertEquals("quiet-hours", result.reason(),
                "quiet-hours is the first rule that refuses, so its reason wins over uptown-closed");
        assertEquals(List.of("allow-all", "quiet-hours"), ran,
                "Rules run in the order given and stop at the first refusal; uptown-closed never runs");
    }

    @Test
    @DisplayName("Gold: the cargo rule is a plug-in rule, CargoOnlyAtDepot, not welded into reserve")
    void cargoRuleIsAPlugIn() {
        DockReservations noRules = new DockReservations(List.of());
        assertTrue(noRules.reserve(request("rider-7", "harbor-2", BikeType.CARGO)).reserved(),
                "With no rules, a cargo bike may hold harbor-2. The cargo check must live in a rule, not in reserve");

        DockReservations desk = new DockReservations(List.of(new CargoOnlyAtDepot()));
        ReservationResult refused = desk.reserve(request("rider-7", "harbor-2", BikeType.CARGO));
        assertFalse(refused.reserved(), "CargoOnlyAtDepot refuses a cargo bike at harbor");
        assertEquals("cargo-needs-depot", refused.reason(), "The reason should be cargo-needs-depot");
        assertTrue(desk.reserve(request("rider-8", "depot-1", BikeType.CARGO)).reserved(),
                "A cargo bike may hold a dock at depot");
        assertTrue(desk.reserve(request("rider-9", "old-mill-4", BikeType.EBIKE)).reserved(),
                "CargoOnlyAtDepot only cares about CARGO bikes: an EBIKE may hold old-mill-4");
    }

    @Test
    @DisplayName("Gold: a held dock is refused with dock-taken before any rule runs")
    void takenBeforeRules() {
        AtomicInteger calls = new AtomicInteger();
        ReservationRule counting = r -> {
            calls.incrementAndGet();
            return Optional.empty();
        };
        DockReservations desk = new DockReservations(List.of(counting));

        assertTrue(desk.reserve(request("rider-7", "riverside-5", BikeType.CLASSIC)).reserved(),
                "riverside-5 is free, so rider-7 gets it");
        assertEquals(1, calls.get(), "The rules run once for rider-7's request");

        ReservationResult second = desk.reserve(request("rider-8", "riverside-5", BikeType.CLASSIC));
        assertFalse(second.reserved(), "riverside-5 is held by rider-7, so rider-8 is refused");
        assertEquals("dock-taken", second.reason(), "The reason for a held dock is dock-taken");
        assertEquals(1, calls.get(), "A held dock is refused straight away; the rules should not run for it");
        assertEquals(Optional.of("rider-7"), desk.holder("riverside-5"), "rider-7 still holds riverside-5");
    }

    @Test
    @DisplayName("Gold: two riders tap for the same dock at the same moment, and exactly one gets it")
    void twoRidersOneDock() {
        assertTimeoutPreemptively(LIMIT, () -> {
            // This rule waits until both riders are inside reserve, so both have passed the "is it free?" check.
            CountDownLatch bothInside = new CountDownLatch(2);
            ReservationRule waitForRival = r -> {
                bothInside.countDown();
                try {
                    bothInside.await(500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return Optional.empty();
            };
            DockReservations desk = new DockReservations(List.of(waitForRival));

            List<ReservationResult> results = race(desk, List.of(
                    request("rider-7", "harbor-3", BikeType.EBIKE),
                    request("rider-8", "harbor-3", BikeType.EBIKE)));

            long winners = results.stream().filter(ReservationResult::reserved).count();
            assertEquals(1, winners, "Exactly one of rider-7 and rider-8 may hold harbor-3, but "
                    + winners + " did. Checking first and putting later lets both through: claim it in one atomic step");
            ReservationResult loser = results.get(0).reserved() ? results.get(1) : results.get(0);
            assertEquals("dock-taken", loser.reason(), "The rider who lost the race is refused with dock-taken");
            String winner = results.get(0).reserved() ? "rider-7" : "rider-8";
            assertEquals(Optional.of(winner), desk.holder("harbor-3"), "harbor-3 should be held by the winner");
        }, "The race should finish quickly; did a rider never get an answer?");
    }

    @Test
    @DisplayName("Gold: 200 rounds of four riders racing for one dock always give it to exactly one")
    void manyRaces() {
        assertTimeoutPreemptively(LIMIT, () -> {
            for (int round = 0; round < 200; round++) {
                DockReservations desk = new DockReservations(List.of(new CargoOnlyAtDepot()));
                List<ReservationRequest> requests = new ArrayList<>();
                for (int rider = 1; rider <= 4; rider++) {
                    requests.add(request("rider-" + rider, "uptown-9", BikeType.CLASSIC));
                }
                long winners = race(desk, requests).stream().filter(ReservationResult::reserved).count();
                assertEquals(1, winners, "In round " + round + ", " + winners
                        + " riders got uptown-9. Exactly one may hold a dock");
                assertEquals(1, desk.heldCount(), "Only one dock was asked for, so only one may be held");
            }
        }, "200 small races should finish well within the time limit");
    }
}
