package jq.m09.incident;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** A fake station-status service that counts its calls, and can be slow or down on command. */
final class FakeSource implements StationStatusSource {

    private final Map<String, StationStatus> statuses = new ConcurrentHashMap<>();
    private final AtomicInteger calls = new AtomicInteger();
    private final Map<String, AtomicInteger> callsPerStation = new ConcurrentHashMap<>();
    private volatile RuntimeException failure;
    private volatile String heldStation;
    private volatile CountDownLatch gate = new CountDownLatch(0);

    FakeSource set(String stationId, int bikes, int docks) {
        statuses.put(stationId, StationStatus.live(stationId, bikes, docks));
        return this;
    }

    /** From now on, every load throws this. */
    void goDown(RuntimeException error) {
        failure = error;
    }

    void comeBack() {
        failure = null;
    }

    /** From now on, loads for this station wait until release() is called. */
    void holdLoadsFor(String stationId) {
        gate = new CountDownLatch(1);
        heldStation = stationId;
    }

    void release() {
        gate.countDown();
    }

    int calls() {
        return calls.get();
    }

    int calls(String stationId) {
        AtomicInteger count = callsPerStation.get(stationId);
        return count == null ? 0 : count.get();
    }

    @Override
    public StationStatus load(String stationId) {
        calls.incrementAndGet();
        callsPerStation.computeIfAbsent(stationId, id -> new AtomicInteger()).incrementAndGet();
        if (stationId.equals(heldStation)) {
            try {
                gate.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        RuntimeException error = failure;
        if (error != null) {
            throw error;
        }
        StationStatus status = statuses.get(stationId);
        if (status == null) {
            throw new IllegalStateException("the fake has no station " + stationId);
        }
        return status;
    }
}
