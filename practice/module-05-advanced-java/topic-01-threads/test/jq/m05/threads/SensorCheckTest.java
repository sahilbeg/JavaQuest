package jq.m05.threads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class SensorCheckTest {

    @Test
    @DisplayName("checkAll returns one result per dock, in the same order as the dock ids")
    void keepsOrder() throws InterruptedException {
        List<String> docks = List.of("Harbor-1", "Harbor-2", "Harbor-3", "Harbor-4", "Harbor-5");
        List<String> expected = List.of("Harbor-1: ok", "Harbor-2: ok", "Harbor-3: ok", "Harbor-4: ok", "Harbor-5: ok");
        assertEquals(expected, SensorCheck.checkAll(docks),
                "checkAll should return readSensor's text for every dock, in dock order. "
                        + "Let thread i write into slot i of an array, join all threads, then return the array as a list");
    }

    @Test
    @DisplayName("checkAll checks all 12 docks of a kiosk")
    void checksEveryDock() throws InterruptedException {
        List<String> docks = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            docks.add("Uptown-" + i);
        }
        List<String> results = SensorCheck.checkAll(docks);
        assertEquals(12, results.size(), "checkAll should return 12 results for 12 docks");
        for (int i = 0; i < 12; i++) {
            assertEquals("Uptown-" + (i + 1) + ": ok", results.get(i),
                    "Result " + i + " is missing or wrong. Did you join every thread before returning?");
        }
    }

    @Test
    @DisplayName("runOnNewThread runs the task on a thread named sensor-worker")
    void runsOnSensorWorker() throws InterruptedException {
        String[] seen = new String[1];
        String name = SensorCheck.runOnNewThread(() -> seen[0] = Thread.currentThread().getName());
        assertEquals("sensor-worker", seen[0],
                "The task should run on a thread named sensor-worker. Use new Thread(..., \"sensor-worker\") and call start(), then join()");
        assertEquals("sensor-worker", name, "runOnNewThread should return the name of the thread that ran the task");
    }

    @Test
    @DisplayName("runOnNewThread does not run the task on the calling thread")
    void notOnCaller() throws InterruptedException {
        String caller = Thread.currentThread().getName();
        String[] seen = new String[1];
        SensorCheck.runOnNewThread(() -> seen[0] = Thread.currentThread().getName());
        assertTrue(seen[0] != null, "The task never ran. Call start() on the thread and wait with join()");
        assertNotEquals(caller, seen[0],
                "The task ran on the calling thread. Calling run() does not start a new thread; call start() instead");
    }
}
