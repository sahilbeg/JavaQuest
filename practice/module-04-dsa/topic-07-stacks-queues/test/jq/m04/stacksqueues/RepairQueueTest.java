package jq.m04.stacksqueues;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RepairQueueTest {

    @Test
    @DisplayName("The repair queue serves bikes in the order they were reported")
    void queueIsFirstInFirstOut() {
        RepairQueue repairs = new RepairQueue();
        repairs.report("B-1042");
        repairs.report("B-2210");
        repairs.report("B-3307");
        assertEquals(3, repairs.waiting(), "Three bikes were reported, so waiting() should be 3");
        assertEquals("B-1042", repairs.next(), "next() should serve B-1042 first: it was reported first");
        assertEquals("B-2210", repairs.next(), "next() should serve B-2210 second");
        assertEquals("B-3307", repairs.next(), "next() should serve B-3307 last");
        assertNull(repairs.next(), "next() on an empty queue should return null");
        assertEquals(0, repairs.waiting(), "After serving every bike, waiting() should be 0");
    }
}
