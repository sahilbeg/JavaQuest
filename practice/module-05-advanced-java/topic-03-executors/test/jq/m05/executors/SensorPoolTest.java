package jq.m05.executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class SensorPoolTest {

    private static List<String> docks(String station, int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            ids.add(station + "-" + i);
        }
        return ids;
    }

    @Test
    @DisplayName("checkAll returns one result per dock, in the same order as the input")
    void checkAllKeepsOrder() throws Exception {
        List<String> result = SensorPool.checkAll(List.of("Harbor-1", "Harbor-2", "Harbor-3"), 2);
        assertEquals(List.of("Harbor-1: ok", "Harbor-2: ok", "Harbor-3: ok"), result,
                "checkAll should return readSensor(id) for each dock. Call get() on the futures in the order you submitted them");
    }

    @Test
    @DisplayName("checkAll handles all 60 docks with a pool of 4 threads")
    void checkAllSixtyDocks() throws Exception {
        List<String> ids = new ArrayList<>();
        for (String station : List.of("Riverside", "Old Mill", "Harbor", "Uptown", "Depot")) {
            ids.addAll(docks(station, 12));
        }
        List<String> result = SensorPool.checkAll(ids, 4);
        assertEquals(60, result.size(), "checkAll should return 60 lines for 60 docks, one per dock");
        assertEquals("Riverside-1: ok", result.get(0), "the first line should be for the first dock, Riverside-1");
        assertEquals("Depot-12: ok", result.get(59), "the last line should be for the last dock, Depot-12");
    }

    @Test
    @DisplayName("maxThreadsSeen never sees more threads than the pool size")
    void threadsStayWithinPool() throws Exception {
        int seen = SensorPool.maxThreadsSeen(docks("Uptown", 12), 3);
        assertTrue(seen >= 1,
                "maxThreadsSeen should return at least 1: add Thread.currentThread().getName() to the set in every task. It returned " + seen);
        assertTrue(seen <= 3,
                "a pool of 3 threads should run the tasks on at most 3 threads, but " + seen + " names were seen. Use Executors.newFixedThreadPool(poolSize)");
    }

    @Test
    @DisplayName("checkAllSafely marks a broken sensor as failed and keeps checking the rest")
    void brokenSensorIsReported() throws Exception {
        List<String> result = SensorPool.checkAllSafely(List.of("Harbor-1", "Harbor-X", "Harbor-3"), 2);
        assertEquals(List.of("Harbor-1: ok", "Harbor-X: failed (sensor offline)", "Harbor-3: ok"), result,
                "catch ExecutionException around get() and add \"Harbor-X: failed (\" + e.getCause().getMessage() + \")\"");
    }
}
