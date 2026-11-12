package jq.m05.synchronization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class DockTest {

    @Test
    @DisplayName("UnlockCounter counts 80,000 when 8 threads each increment 10,000 times")
    void counterIsThreadSafe() throws InterruptedException {
        UnlockCounter counter = new UnlockCounter();
        Thread[] threads = new Thread[8];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10_000; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }
        assertEquals(80_000, counter.get(),
                "get() should be 80000. A lower number means updates were lost: count++ is read, add, write, "
                        + "so make increment() and get() synchronized");
    }

    @Test
    @DisplayName("A Harbor dock with 2 bikes unlocks twice, then says no")
    void dockStopsAtZero() {
        Dock dock = new Dock("Harbor", 2);
        assertTrue(dock.tryUnlock(), "The first tryUnlock() should return true: there were 2 bikes");
        assertTrue(dock.tryUnlock(), "The second tryUnlock() should return true: there was 1 bike left");
        assertFalse(dock.tryUnlock(), "The third tryUnlock() should return false: the dock is empty");
        assertEquals(0, dock.bikesLeft(), "bikesLeft() should be 0 after both bikes were taken");
    }

    @Test
    @DisplayName("A new dock reports the bikes it was given")
    void newDockCount() {
        assertEquals(12, new Dock("Uptown", 12).bikesLeft(),
                "bikesLeft() should be 12 for new Dock(\"Uptown\", 12). Store bikes in the constructor");
    }

    @Test
    @DisplayName("50 riders racing for 5 bikes get exactly 5 unlocks, every time")
    void rushHour() throws InterruptedException {
        for (int round = 1; round <= 20; round++) {
            Dock dock = new Dock("Harbor", 5);
            int unlocked = Dock.unlockMany(dock, 50);
            assertEquals(5, unlocked,
                    "Round " + round + ": unlockMany should report 5 successes for 5 bikes. More means two riders "
                            + "got the same bike: put the check and the take in one synchronized method");
            assertEquals(0, dock.bikesLeft(), "Round " + round + ": the dock should have 0 bikes left, never fewer");
        }
    }

    @Test
    @DisplayName("8 threads each calling tryUnlock 10,000 times take exactly 80,000 bikes")
    void hotLoopUnlocks() throws InterruptedException {
        Dock dock = new Dock("Harbor", 100_000);
        CountDownLatch go = new CountDownLatch(1);
        Thread[] threads = new Thread[8];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    go.await();
                } catch (InterruptedException e) {
                    return;
                }
                for (int j = 0; j < 10_000; j++) {
                    dock.tryUnlock();
                }
            });
            threads[i].start();
        }
        go.countDown();
        for (Thread t : threads) {
            t.join();
        }
        assertEquals(20_000, dock.bikesLeft(), "updates to bikes were lost: make tryUnlock synchronized");
    }
}
