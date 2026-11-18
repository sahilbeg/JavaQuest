package jq.m11.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeployGoldTest {

    private static final Duration LIMIT = Duration.ofSeconds(4);
    private static final Duration LONG_GRACE = Duration.ofSeconds(30);

    private final List<String> events = new CopyOnWriteArrayList<>();

    private static void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** An unlock request that starts its work, then waits until the check releases it. */
    private final class HeldUnlock {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicReference<Response> response = new AtomicReference<>();
        final Thread thread;

        HeldUnlock(RequestGate gate, String rentalId) {
            thread = new Thread(() -> response.set(gate.handle(() -> {
                started.countDown();
                await(release);
                events.add(rentalId + " unlocked");
                return Response.ok(rentalId + " unlocked");
            })));
            thread.setDaemon(true);
            thread.start();
            await(started);
        }

        Response finish() throws InterruptedException {
            release.countDown();
            thread.join(3_000);
            return response.get();
        }
    }

    /** gate.shutdown() on its own thread, as SIGTERM would start it. */
    private final class ShutdownCall {
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicReference<ShutdownReport> report = new AtomicReference<>();
        final Thread thread;

        ShutdownCall(RequestGate gate) {
            thread = new Thread(() -> {
                try {
                    report.set(gate.shutdown());
                    events.add("stopped");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
            thread.setDaemon(true);
            thread.start();
        }

        ShutdownReport awaitReport(String why) throws InterruptedException {
            assertTrue(done.await(2, TimeUnit.SECONDS), why);
            return report.get();
        }
    }

    /** Waits, at most 2 seconds, until shutdown has turned readiness off. */
    private static void awaitNotReady(RequestGate gate) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (gate.ready()) {
            if (System.nanoTime() > deadline) {
                fail("shutdown() was called, but ready() still says true after 2 seconds. "
                        + "Turn readiness off as the very first step of shutdown, before you wait");
            }
            Thread.yield();
        }
    }

    @Test
    @DisplayName("Gold: shutdown turns readiness off at once, and keeps waiting while a request is still running")
    void readinessGoesOffFirst() {
        assertTimeoutPreemptively(LIMIT, () -> {
            RequestGate gate = new RequestGate(LONG_GRACE);
            assertTrue(gate.ready(), "a new server is ready for requests");
            HeldUnlock unlock = new HeldUnlock(gate, "rental-17");
            ShutdownCall shutdown = new ShutdownCall(gate);
            try {
                awaitNotReady(gate);
                assertEquals(1, shutdown.done.getCount(),
                        "rental-17's unlock is still running, so shutdown() must not return yet");
                assertFalse(gate.stopped(), "the server must not stop while a request is still running");
                assertEquals(1, gate.inFlight(), "one request, rental-17's unlock, is in flight");
            } finally {
                unlock.finish();
                shutdown.done.await(2, TimeUnit.SECONDS);
            }
        }, "the check took too long: does shutdown() wait for ever, or does a request hang?");
    }

    @Test
    @DisplayName("Gold: a request that was already running finishes and gets its 200, then the server stops")
    void runningRequestFinishes() {
        assertTimeoutPreemptively(LIMIT, () -> {
            RequestGate gate = new RequestGate(LONG_GRACE);
            HeldUnlock unlock = new HeldUnlock(gate, "rental-17");
            ShutdownCall shutdown = new ShutdownCall(gate);
            awaitNotReady(gate);
            Response response = unlock.finish();
            ShutdownReport report = shutdown.awaitReport(
                    "once the last request has finished, shutdown() should return within moments");
            assertNotNull(response, "rental-17's unlock should have got a response");
            assertEquals(200, response.status(),
                    "rental-17's unlock was running before shutdown began, so it must finish with 200, not be cut off");
            assertEquals("rental-17 unlocked", response.body(), "the rider should get the unlock's own answer");
            assertEquals(List.of("rental-17 unlocked", "stopped"), events,
                    "the unlock must finish before the server stops");
            assertNotNull(report, "shutdown() should return a ShutdownReport");
            assertEquals(1, report.inFlightAtStart(), "one request was running when shutdown began");
            assertEquals(0, report.cutOff(), "no request was cut off");
            assertTrue(gate.stopped(), "after shutdown() returns, the server has stopped");
        }, "the check took too long: does shutdown() wait for ever, or does a request hang?");
    }

    @Test
    @DisplayName("Gold: a new request that arrives during shutdown gets a 503, and its work never runs")
    void newRequestDuringShutdownIsRefused() {
        assertTimeoutPreemptively(LIMIT, () -> {
            RequestGate gate = new RequestGate(LONG_GRACE);
            HeldUnlock unlock = new HeldUnlock(gate, "rental-17");
            ShutdownCall shutdown = new ShutdownCall(gate);
            AtomicInteger ran = new AtomicInteger();
            try {
                awaitNotReady(gate);
                Response late = gate.handle(() -> {
                    ran.incrementAndGet();
                    return Response.ok("rental-18 unlocked");
                });
                assertEquals(503, late.status(),
                        "a request that arrives after shutdown began must get Response.shuttingDown(), a 503, so the load balancer tries another pod");
                assertEquals(0, ran.get(), "the late request's work must never start: the bike must not unlock");
                assertEquals(1, gate.inFlight(), "a refused request is not counted as in flight");
            } finally {
                unlock.finish();
                shutdown.done.await(2, TimeUnit.SECONDS);
            }
            assertEquals(200, unlock.response.get().status(), "the request that was already running still gets its 200");
        }, "the check took too long: does shutdown() wait for ever, or does a request hang?");
    }

    @Test
    @DisplayName("Gold: with nothing running, shutdown returns at once, and the stopped server refuses new requests")
    void idleShutdown() {
        RequestGate gate = new RequestGate(LONG_GRACE);
        assertEquals(200, gate.handle(() -> Response.ok("rental-16 unlocked")).status(),
                "before shutdown, a request runs as normal");
        ShutdownReport report = assertTimeoutPreemptively(Duration.ofSeconds(2), gate::shutdown,
                "with no request running, shutdown() must not wait for the 30-second grace period");
        assertEquals(0, report.inFlightAtStart(), "nothing was running when shutdown began");
        assertTrue(report.clean(), "nothing was cut off");
        assertFalse(gate.ready(), "a stopped server is not ready");
        assertTrue(gate.stopped(), "after shutdown() returns, the server has stopped");
        AtomicInteger ran = new AtomicInteger();
        Response after = gate.handle(() -> {
            ran.incrementAndGet();
            return Response.ok("rental-18 unlocked");
        });
        assertEquals(503, after.status(), "after shutdown, every request gets Response.shuttingDown(), a 503");
        assertEquals(0, ran.get(), "after shutdown, no request's work runs");
    }

    @Test
    @DisplayName("Gold: shutdown waits only as long as the running requests need, not the whole grace period")
    void doesNotWaitTheWholeGracePeriod() {
        assertTimeoutPreemptively(LIMIT, () -> {
            RequestGate gate = new RequestGate(LONG_GRACE);
            HeldUnlock unlock = new HeldUnlock(gate, "rental-17");
            ShutdownCall shutdown = new ShutdownCall(gate);
            awaitNotReady(gate);
            unlock.finish();
            ShutdownReport report = shutdown.awaitReport(
                    "the only request finished, so shutdown() should return within moments, not after the 30-second grace period");
            assertTrue(report.clean(), "the request finished in time, so nothing was cut off");
        }, "the check took too long: shutdown() should stop waiting as soon as the last request finishes");
    }

    @Test
    @DisplayName("Gold: a request still running when the grace period ends is cut off, and shutdown does not wait for it")
    void gracePeriodEnds() {
        RequestGate gate = new RequestGate(Duration.ofMillis(300));
        HeldUnlock stuck = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> new HeldUnlock(gate, "rental-19"),
                "the request should start at once");
        try {
            ShutdownReport report = assertTimeoutPreemptively(Duration.ofSeconds(2), gate::shutdown,
                    "the grace period is 300 ms, so shutdown() must give up waiting after it, not wait for ever");
            assertEquals(1, report.inFlightAtStart(), "one request was running when shutdown began");
            assertEquals(1, report.cutOff(), "rental-19's request was still running when the grace period ended");
            assertFalse(report.clean(), "a request was cut off, so the shutdown was not clean");
            assertTrue(gate.stopped(), "when the grace period ends, the server stops anyway");
        } finally {
            Response late = assertTimeoutPreemptively(Duration.ofSeconds(4), stuck::finish, "the stuck request should end");
            assertNotNull(late, "the stuck request should get a response");
            assertEquals(502, late.status(),
                    "a request that finishes after the server stopped never reaches the rider: it gets Response.cutOff(), a 502");
        }
    }

    @Test
    @DisplayName("Gold: three unlocks in flight all finish with 200 before the server stops")
    void threeInFlight() {
        assertTimeoutPreemptively(LIMIT, () -> {
            RequestGate gate = new RequestGate(LONG_GRACE);
            List<HeldUnlock> unlocks = new ArrayList<>();
            for (String id : List.of("rental-21", "rental-22", "rental-23")) {
                unlocks.add(new HeldUnlock(gate, id));
            }
            ShutdownCall shutdown = new ShutdownCall(gate);
            awaitNotReady(gate);
            for (HeldUnlock unlock : unlocks) {
                Response response = unlock.finish();
                assertEquals(200, response.status(), "every request that was running before shutdown must finish with 200");
            }
            ShutdownReport report = shutdown.awaitReport("all three finished, so shutdown() should return within moments");
            assertEquals(3, report.inFlightAtStart(), "three requests were running when shutdown began");
            assertEquals(0, report.cutOff(), "none was cut off");
            assertEquals("stopped", events.get(events.size() - 1), "the server stops only after all three unlocks");
            assertEquals(4, events.size(), "three unlocks and one stop");
        }, "the check took too long: does shutdown() wait for ever, or does a request hang?");
    }
}
