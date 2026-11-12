package jq.m05.concurrentcollections;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class LiveMapTest {

    private static void runOnThreads(int threads, Runnable work) throws InterruptedException {
        Thread[] all = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            all[i] = new Thread(work);
            all[i].start();
        }
        for (Thread t : all) {
            t.join();
        }
    }

    @Test
    @DisplayName("free returns the stored count, and 0 for an unknown station")
    void freeAndUnknown() {
        LiveMap map = new LiveMap();
        map.dockEvent("Uptown", 12);
        map.dockEvent("Uptown", -2);
        assertEquals(10, map.free("Uptown"),
                "free(\"Uptown\") should be 10: dockEvent +12 then -2. Use docks.merge(station, change, Integer::sum)");
        assertEquals(0, map.free("Depot"),
                "free(\"Depot\") should be 0 because Depot never had an event. Use getOrDefault(station, 0)");
    }

    @Test
    @DisplayName("8 sensor threads sending +1 and -1 to Harbor end at the right number")
    void harborUnderLoad() throws InterruptedException {
        LiveMap map = new LiveMap();
        map.dockEvent("Harbor", 12);
        runOnThreads(8, () -> {
            for (int i = 0; i < 1_000; i++) {
                map.dockEvent("Harbor", 1);
                map.dockEvent("Harbor", -1);
            }
        });
        map.dockEvent("Harbor", 1);
        assertEquals(13, map.free("Harbor"),
                "Harbor should end at 13: start 12, every +1 has a matching -1, then one more +1. A get then put races and loses updates, so use merge");
    }

    @Test
    @DisplayName("snapshot returns the stations in alphabetical order")
    void snapshotSorted() {
        LiveMap map = new LiveMap();
        map.dockEvent("Uptown", 4);
        map.dockEvent("Harbor", 7);
        map.dockEvent("Old Mill", 2);
        Map<String, Integer> expected = new TreeMap<>(Map.of("Harbor", 7, "Old Mill", 2, "Uptown", 4));
        Map<String, Integer> snap = map.snapshot();
        assertEquals(expected, snap, "snapshot() should hold every station and its count. Return new TreeMap<>(docks)");
        assertEquals(List.of("Harbor", "Old Mill", "Uptown"), new ArrayList<>(snap.keySet()),
                "snapshot() keys should be in alphabetical order, which a TreeMap gives you");
    }

    @Test
    @DisplayName("8 threads x 10,000 record() calls give 80,000 rides")
    void rideCounterUnderLoad() throws InterruptedException {
        RideCounter counter = new RideCounter();
        runOnThreads(8, () -> {
            for (int i = 0; i < 10_000; i++) {
                counter.record();
            }
        });
        assertEquals(80_000, counter.total(),
                "total() should be 80000 after 8 threads each call record() 10,000 times. Use rides.incrementAndGet() so no update is lost");
    }

    @Test
    @DisplayName("drain receives every item a producer thread sends, in order")
    void pipeInOrder() throws InterruptedException {
        TelemetryPipe pipe = new TelemetryPipe();
        List<String> sent = new ArrayList<>();
        for (int i = 1; i <= 500; i++) {
            sent.add("Harbor:dock-" + i);
        }
        Thread sensor = new Thread(() -> {
            try {
                for (String item : sent) {
                    pipe.send(item);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        sensor.start();
        List<String> got = TelemetryPipe.drain(pipe, sent.size());
        sensor.join();
        assertEquals(sent, got,
                "drain(pipe, 500) should return all 500 items in the order they were sent. send uses queue.put, drain calls queue.take() exactly expected times");
    }
}
