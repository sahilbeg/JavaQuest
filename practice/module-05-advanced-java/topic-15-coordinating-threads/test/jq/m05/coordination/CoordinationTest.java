package jq.m05.coordination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 7, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class CoordinationTest {

    private static final long WAIT = 2;

    private static void await(CountDownLatch latch) {
        await(latch, WAIT);
    }

    private static void await(CountDownLatch latch, long seconds) {
        try {
            latch.await(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static Thread start(Runnable work) {
        Thread t = new Thread(work);
        t.setDaemon(true);
        t.start();
        return t;
    }

    // ---------- PaymentGate ----------

    @Test
    @DisplayName("A gate with fewer than 1 slot is refused")
    void gateNeedsAtLeastOneSlot() {
        assertThrows(IllegalArgumentException.class, () -> new PaymentGate(0),
                "new PaymentGate(0) should throw IllegalArgumentException: a gate needs at least 1 slot");
    }

    @Test
    @DisplayName("call returns the provider's answer and gives its slot back")
    void callReturnsResult() throws InterruptedException {
        PaymentGate gate = new PaymentGate(3);
        String answer = gate.call(() -> "paid rental-17");
        assertEquals("paid rental-17", answer, "call(work) should return what work.get() returned");
        assertEquals(3, gate.freeSlots(), "After the call finishes, all 3 slots should be free again");
    }

    @Test
    @DisplayName("A slot is given back even when the provider throws")
    void slotReleasedOnFailure() {
        PaymentGate gate = new PaymentGate(3);
        assertThrows(IllegalStateException.class, () -> gate.call(() -> {
            throw new IllegalStateException("provider down");
        }), "call should let the provider's exception travel up to the caller");
        assertEquals(3, gate.freeSlots(),
                "The slot was never given back after the exception. Call permits.release() in a finally block");
    }

    @Test
    @DisplayName("12 riders paying at once: never more than 3 calls inside, and 3 really can run together")
    void atMostThreeAtOnce() throws InterruptedException {
        PaymentGate gate = new PaymentGate(3);
        AtomicInteger inside = new AtomicInteger();
        AtomicInteger most = new AtomicInteger();
        AtomicInteger ran = new AtomicInteger();
        CountDownLatch threeInside = new CountDownLatch(3);
        AtomicBoolean gaveUp = new AtomicBoolean(false);
        List<Thread> riders = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            riders.add(start(() -> {
                try {
                    gate.call(() -> {
                        int now = inside.incrementAndGet();
                        most.accumulateAndGet(now, Math::max);
                        threeInside.countDown();
                        if (!gaveUp.get()) {
                            try {
                                if (!threeInside.await(WAIT, TimeUnit.SECONDS)) {
                                    gaveUp.set(true);
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        inside.decrementAndGet();
                        ran.incrementAndGet();
                        return null;
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        for (Thread t : riders) {
            t.join();
        }
        assertEquals(12, ran.get(), "All 12 payment calls should run. call(work) must run work.get()");
        assertTrue(most.get() <= 3,
                "At most 3 calls may be inside at once, but " + most.get() + " were. Acquire a permit before work.get()");
        assertEquals(3, most.get(), "3 calls should be able to run at the same time with new PaymentGate(3)");
        assertEquals(3, gate.freeSlots(), "When every call has finished, all 3 slots should be free");
    }

    @Test
    @DisplayName("tryCall gives up after its timeout while all slots are busy, and works once a slot is free")
    void tryCallTimesOut() throws InterruptedException {
        PaymentGate gate = new PaymentGate(3);
        CountDownLatch entered = new CountDownLatch(3);
        CountDownLatch hold = new CountDownLatch(1);
        List<Thread> busy = new ArrayList<>();
        try {
            for (int i = 0; i < 3; i++) {
                busy.add(start(() -> {
                    try {
                        gate.call(() -> {
                            entered.countDown();
                            await(hold, 7);
                            return null;
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }));
            }
            assertTrue(entered.await(WAIT, TimeUnit.SECONDS),
                    "3 calls should get inside a gate with 3 slots. call(work) must run work.get()");
            AtomicBoolean ran = new AtomicBoolean(false);
            boolean got = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> gate.tryCall(() -> ran.set(true), 100),
                    "tryCall(work, 100) should give up after 100 ms. Use permits.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS), not acquire()");
            assertFalse(got, "With all 3 slots busy, tryCall(work, 100) should wait 100 ms and return false");
            assertFalse(ran.get(), "tryCall must not run the work when it gets no slot");
        } finally {
            hold.countDown();
        }
        for (Thread t : busy) {
            t.join();
        }
        AtomicBoolean ran = new AtomicBoolean(false);
        assertTrue(gate.tryCall(() -> ran.set(true), 1_000),
                "Once the busy calls finished, tryCall should get a slot and return true");
        assertTrue(ran.get(), "tryCall should run the work when it gets a slot");
        assertEquals(3, gate.freeSlots(), "tryCall should give its slot back when the work is done");
    }

    // ---------- StartupCheck ----------

    @Test
    @DisplayName("5 quick station checks all run, and the answer is true")
    void allChecksPass() throws InterruptedException {
        AtomicInteger ran = new AtomicInteger();
        List<Runnable> checks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            checks.add(ran::incrementAndGet);
        }
        assertTrue(StartupCheck.allPassed(checks, 2_000),
                "Every check finished without an error, so allPassed should return true");
        assertEquals(5, ran.get(), "allPassed should run each of the 5 checks exactly once");
    }

    @Test
    @DisplayName("With no checks at all, the answer is true straight away")
    void noChecks() throws InterruptedException {
        assertTrue(StartupCheck.allPassed(List.of(), 2_000),
                "An empty list has nothing to fail, so allPassed should return true. new CountDownLatch(0) is already open");
    }

    @Test
    @DisplayName("A check that throws makes the answer false quickly, and the other checks still run")
    void failingCheck() {
        CountDownLatch othersDone = new CountDownLatch(2);
        List<Runnable> checks = List.of(
                () -> {
                    throw new IllegalStateException("Harbor dock 3 does not answer");
                },
                othersDone::countDown,
                othersDone::countDown);
        boolean result = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> StartupCheck.allPassed(checks, 5_000),
                "allPassed waited for the full timeout after a check threw. Call countDown() in a finally block, so a crashed check still counts");
        assertFalse(result, "One check threw an exception, so allPassed should return false");
        assertTrue(await2(othersDone), "The other 2 checks should still run, each on its own thread");
    }

    @Test
    @DisplayName("A check that hangs makes the answer false after the timeout, and the other checks still run")
    void hangingCheck() {
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch othersDone = new CountDownLatch(2);
        List<Runnable> checks = List.of(() -> await(release, 6), othersDone::countDown, othersDone::countDown);
        try {
            boolean result = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> StartupCheck.allPassed(checks, 200),
                    "allPassed should stop waiting after 200 ms. Use await(timeoutMillis, TimeUnit.MILLISECONDS), never plain await()");
            assertFalse(result, "One check did not finish within 200 ms, so allPassed should return false");
            assertTrue(await2(othersDone), "The other 2 checks should still run, each on its own thread");
        } finally {
            release.countDown();
        }
    }

    @Test
    @DisplayName("The checks run at the same time, one thread each")
    void checksRunInParallel() {
        CyclicBarrier allThree = new CyclicBarrier(3);
        Runnable meet = () -> {
            try {
                allThree.await(2, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException("the 3 checks never ran together", e);
            }
        };
        boolean result = assertTimeoutPreemptively(Duration.ofSeconds(6),
                () -> StartupCheck.allPassed(List.of(meet, meet, meet), 4_000),
                "allPassed took too long. Start every check on its own thread before you wait");
        assertTrue(result, "These 3 checks only pass when they run at the same moment. Start one new Thread per check");
    }

    private static boolean await2(CountDownLatch latch) {
        try {
            return latch.await(WAIT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    // ---------- PriceBoard ----------

    @Test
    @DisplayName("A fare is 100 cents to unlock plus the per-minute rate, and an unknown type is refused")
    void fares() {
        PriceBoard board = new PriceBoard();
        assertEquals(400, board.fareCents("CLASSIC", 20), "A 20-minute CLASSIC ride costs 100 + 15 x 20 = 400 cents");
        assertEquals(400, board.fareCents("EBIKE", 10), "A 10-minute EBIKE ride costs 100 + 30 x 10 = 400 cents");
        assertEquals(100, board.fareCents("CARGO", 0), "A 0-minute CARGO ride costs just the 100-cent unlock");
        assertThrows(IllegalArgumentException.class, () -> board.fareCents("SCOOTER", 5),
                "fareCents should throw IllegalArgumentException for a bike type it does not know");
    }

    @Test
    @DisplayName("raiseAll and setRate change the prices, and snapshot lists the types in alphabetical order")
    void updatesAndSnapshot() {
        PriceBoard board = new PriceBoard();
        board.raiseAll(5);
        Map<String, Long> snap = board.snapshot();
        assertEquals(Map.of("CARGO", 30L, "CLASSIC", 20L, "EBIKE", 35L), snap,
                "After raiseAll(5), every rate should be 5 cents higher");
        assertEquals(List.of("CARGO", "CLASSIC", "EBIKE"), new ArrayList<>(snap.keySet()),
                "snapshot() should list types in alphabetical order. Return new TreeMap<>(perMinuteCents)");
        board.setRate("EBIKE", 40);
        assertEquals(500, board.fareCents("EBIKE", 10), "After setRate(\"EBIKE\", 40), 10 minutes should cost 100 + 400 = 500");
        assertEquals(35L, snap.get("EBIKE"), "An earlier snapshot is a copy, so it should still say 35 for EBIKE");
    }

    @Test
    @DisplayName("Readers never see a rush-hour raise half done")
    void noHalfDoneRaise() throws InterruptedException {
        PriceBoard board = new PriceBoard();
        AtomicInteger torn = new AtomicInteger();
        List<Thread> threads = new ArrayList<>();
        threads.add(start(() -> {
            for (int i = 0; i < 500; i++) {
                board.raiseAll(1);
            }
        }));
        for (int r = 0; r < 4; r++) {
            threads.add(start(() -> {
                for (int i = 0; i < 1_000; i++) {
                    Map<String, Long> snap = board.snapshot();
                    if (snap.size() != 3
                            || snap.get("EBIKE") - snap.get("CLASSIC") != 15
                            || snap.get("CARGO") - snap.get("CLASSIC") != 10) {
                        torn.incrementAndGet();
                    }
                }
            }));
        }
        for (Thread t : threads) {
            t.join(5_000);
            assertFalse(t.isAlive(), "A thread was still stuck after 5 seconds. Unlock every lock in a finally block");
        }
        assertEquals(0, torn.get(),
                torn.get() + " snapshots showed a raise half done, or were empty. raiseAll must hold the write lock, snapshot the read lock");
        assertEquals(Map.of("CARGO", 525L, "CLASSIC", 515L, "EBIKE", 530L), board.snapshot(),
                "After 500 raises of 1 cent, CLASSIC should be 515, CARGO 525 and EBIKE 530");
    }

    @Test
    @DisplayName("PriceBoard guards its prices with a ReadWriteLock")
    void usesReadWriteLock() {
        boolean found = false;
        for (Field f : PriceBoard.class.getDeclaredFields()) {
            if (ReadWriteLock.class.isAssignableFrom(f.getType())) {
                found = true;
            }
        }
        assertTrue(found, "PriceBoard should have a field: private final ReadWriteLock lock = new ReentrantReadWriteLock();");
    }
}
