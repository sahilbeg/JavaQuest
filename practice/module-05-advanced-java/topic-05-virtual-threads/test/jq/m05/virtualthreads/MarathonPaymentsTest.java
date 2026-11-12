package jq.m05.virtualthreads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class MarathonPaymentsTest {

    @Test
    @DisplayName("payAll pays a small group of 3 riders")
    void paysThree() {
        assertEquals(3, MarathonPayments.payAll(3),
                "payAll(3) should return 3: submit one task per rider and count each pay(...) that returns true");
    }

    @Test
    @DisplayName("payAll pays all 10,000 marathon riders in time")
    void paysTenThousand() {
        assertEquals(10_000, MarathonPayments.payAll(10_000),
                "payAll(10_000) should return 10000. Use newVirtualThreadPerTaskExecutor in try-with-resources so close() waits for every rider, and count with an AtomicInteger so no update is lost");
    }

    @Test
    @DisplayName("countTaps counts all 10,000 taps from 10,000 virtual threads")
    void countsTenThousandTaps() {
        assertEquals(10_000, MarathonPayments.countTaps(10_000),
                "countTaps(10_000) should return 10000. A lower number means updates were lost: count with an AtomicInteger and incrementAndGet()");
    }

    @Test
    @DisplayName("ranOnVirtualThread runs the task on a virtual thread")
    void runsOnVirtualThread() throws InterruptedException {
        boolean[] ran = new boolean[1];
        boolean virtual = MarathonPayments.ranOnVirtualThread(() -> ran[0] = true);
        assertTrue(ran[0], "ranOnVirtualThread should call task.run() on the new thread");
        assertTrue(virtual,
                "ranOnVirtualThread should return true: start the thread with Thread.ofVirtual().start(...) and read isVirtual() inside it, then join()");
    }

    @Test
    @DisplayName("payNamed returns one paid line per rider in input order")
    void paysNamedInOrder() throws Exception {
        List<String> lines = MarathonPayments.payNamed(List.of("r1", "r2", "r3", "r4"));
        assertEquals(List.of("paid r1", "paid r2", "paid r3", "paid r4"), lines,
                "payNamed should return \"paid \" + id for every rider, in the same order as the input. Read the futures in the order you submitted them");
    }
}
