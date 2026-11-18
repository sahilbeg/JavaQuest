package jq.m09.incident;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/** Many riders whose map asks for the same station at the same moment. */
final class Crowd {

    private final List<Thread> threads = new ArrayList<>();
    private final StationStatus[] results;
    private final Throwable[] errors;
    private final AtomicInteger arrived = new AtomicInteger();
    private final CountDownLatch start = new CountDownLatch(1);

    Crowd(int riders, LiveMapCache cache, String stationId) {
        results = new StationStatus[riders];
        errors = new Throwable[riders];
        for (int i = 0; i < riders; i++) {
            int index = i;
            MapRequest request = new MapRequest("rider-" + (100 + i), "req-" + (5000 + i), stationId);
            Thread thread = new Thread(() -> {
                try {
                    start.await();
                    arrived.incrementAndGet();
                    results[index] = cache.get(request);
                } catch (Throwable e) {
                    errors[index] = e;
                }
            });
            thread.setDaemon(true);
            threads.add(thread);
            thread.start();
        }
    }

    /** Lets every rider ask at once. */
    void go() {
        start.countDown();
    }

    /**
     * Returns once every rider is inside get() and is waiting: in the source, or for another rider's load.
     * A thread can be BLOCKED for a moment on a short lock, so the state must hold for many checks in a row.
     */
    void awaitEveryoneWaiting() {
        int calmChecks = 0;
        while (calmChecks < 200) {
            if (everyoneWaiting()) {
                calmChecks++;
            } else {
                calmChecks = 0;
            }
            Thread.yield();
        }
    }

    private boolean everyoneWaiting() {
        if (arrived.get() < threads.size()) {
            return false;
        }
        for (Thread thread : threads) {
            Thread.State state = thread.getState();
            boolean parked = state == Thread.State.WAITING
                    || state == Thread.State.TIMED_WAITING
                    || state == Thread.State.BLOCKED
                    || state == Thread.State.TERMINATED;
            if (!parked) {
                return false;
            }
        }
        return true;
    }

    void join() throws InterruptedException {
        for (Thread thread : threads) {
            thread.join();
        }
    }

    StationStatus[] results() {
        return results;
    }

    Throwable[] errors() {
        return errors;
    }
}
