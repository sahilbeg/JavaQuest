package jq.m04.bigo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StepCounterTest {

    @Test
    @DisplayName("countLinear does one step per item")
    void linear() {
        assertEquals(10, StepCounter.countLinear(10), "countLinear(10) should be 10: one step for each of 10 items");
        assertEquals(1000, StepCounter.countLinear(1000), "countLinear(1000) should be 1000: doubling the data doubles the steps");
        assertEquals(0, StepCounter.countLinear(0), "countLinear(0) should be 0: no items, no steps");
    }

    @Test
    @DisplayName("countNested does n times n steps")
    void nested() {
        assertEquals(100, StepCounter.countNested(10), "countNested(10) should be 100: a loop of 10 inside a loop of 10");
        assertEquals(10000, StepCounter.countNested(100), "countNested(100) should be 10000: 100 * 100");
        assertEquals(1, StepCounter.countNested(1), "countNested(1) should be 1: 1 * 1");
    }

    @Test
    @DisplayName("countHalving counts how many times n halves down to 1")
    void halving() {
        assertEquals(3, StepCounter.countHalving(8), "countHalving(8) should be 3: 8, 4, 2, 1");
        assertEquals(3, StepCounter.countHalving(10), "countHalving(10) should be 3: 10, 5, 2, 1 (integer division)");
        assertEquals(9, StepCounter.countHalving(1000), "countHalving(1000) should be 9: a thousand items need only 9 halvings");
        assertEquals(0, StepCounter.countHalving(1), "countHalving(1) should be 0: n is already 1");
        assertEquals(0, StepCounter.countHalving(0), "countHalving(0) should be 0: the loop never starts when n is not above 1");
    }

    @Test
    @DisplayName("bigO names the growth of each method")
    void labels() {
        assertEquals("O(n)", StepCounter.bigO("linear"), "bigO(\"linear\") should be \"O(n)\"");
        assertEquals("O(n^2)", StepCounter.bigO("nested"), "bigO(\"nested\") should be \"O(n^2)\"");
        assertEquals("O(log n)", StepCounter.bigO("halving"), "bigO(\"halving\") should be \"O(log n)\" with one space");
        assertEquals("O(1)", StepCounter.bigO("hashset"), "bigO(\"hashset\") should be \"O(1)\": a HashSet lookup does not grow with n");
    }

    @Test
    @DisplayName("containsSlow loops over the list itself")
    void slowCheck() {
        List<String> ids = new ArrayList<>(List.of("B-1001", "B-1042", "B-2001")) {
            @Override
            public boolean contains(Object o) {
                throw new AssertionError("containsSlow should loop over the ids with a for loop, not call ids.contains");
            }

            @Override
            public int indexOf(Object o) {
                throw new AssertionError("containsSlow should loop over the ids with a for loop, not call ids.contains");
            }

            @Override
            public int lastIndexOf(Object o) {
                throw new AssertionError("containsSlow should loop over the ids with a for loop, not call ids.contains");
            }
        };
        assertTrue(BikeCheck.containsSlow(ids, "B-1042"), "containsSlow should find B-1042 in the list");
        assertTrue(BikeCheck.containsSlow(ids, "B-2001"), "containsSlow should find B-2001, the last id");
        assertFalse(BikeCheck.containsSlow(ids, "B-9999"), "containsSlow should return false for B-9999, which is not out");
        assertFalse(BikeCheck.containsSlow(List.of(), "B-1042"), "containsSlow should return false for an empty list");
    }

    @Test
    @DisplayName("containsFast asks the set directly without looping")
    void fastCheck() {
        HashSet<String> ids = new HashSet<>(List.of("B-1001", "B-1042", "B-2001")) {
            @Override
            public Iterator<String> iterator() {
                throw new AssertionError("containsFast should call ids.contains(id) instead of looping over the set");
            }
        };
        assertTrue(BikeCheck.containsFast(ids, "B-1042"), "containsFast should find B-1042 in the set");
        assertFalse(BikeCheck.containsFast(ids, "B-9999"), "containsFast should return false for B-9999, which is not out");
    }
}
