package jq.m04.heaps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RepairPlannerTest {

    @Test
    @DisplayName("polling the heap returns urgencies smallest first, matching a sorted copy")
    void heapPollsInSortedOrder() {
        int[] urgencies = {5, 3, 8, 1, 9, 2, 7, 3, 6, 4, 0, 8};
        MinHeap heap = new MinHeap();
        for (int u : urgencies) {
            heap.add(u);
        }
        assertEquals(12, heap.size(), "size() should be 12 after 12 adds; the array must grow past its first 4 slots");
        int[] expected = urgencies.clone();
        Arrays.sort(expected);
        int[] actual = new int[urgencies.length];
        for (int i = 0; i < actual.length; i++) {
            actual[i] = heap.poll();
        }
        assertEquals(Arrays.toString(expected), Arrays.toString(actual),
                "poll() should always return the smallest value left; check sift up in add and sift down in poll");
        assertTrue(heap.isEmpty(), "isEmpty() should be true after polling every value");
    }

    @Test
    @DisplayName("peek shows the smallest value without removing it")
    void peekDoesNotRemove() {
        MinHeap heap = new MinHeap();
        heap.add(4);
        heap.add(2);
        heap.add(6);
        assertEquals(2, heap.peek(), "peek() should return 2, the smallest value");
        assertEquals(3, heap.size(), "peek() must not remove anything: size() should still be 3");
        assertFalse(heap.isEmpty(), "isEmpty() should be false with 3 values inside");
    }

    @Test
    @DisplayName("mixing adds and polls still returns the smallest each time")
    void mixedAddsAndPolls() {
        MinHeap heap = new MinHeap();
        heap.add(7);
        heap.add(3);
        assertEquals(3, heap.poll(), "First poll() should return 3");
        heap.add(1);
        heap.add(5);
        assertEquals(1, heap.poll(), "After adding 1 and 5, poll() should return 1");
        assertEquals(5, heap.poll(), "Then poll() should return 5");
        assertEquals(7, heap.poll(), "Then poll() should return 7");
    }

    @Test
    @DisplayName("poll and peek throw IllegalStateException on an empty heap")
    void emptyHeapThrows() {
        MinHeap heap = new MinHeap();
        assertThrows(IllegalStateException.class, heap::poll,
                "poll() on an empty heap should throw IllegalStateException");
        assertThrows(IllegalStateException.class, heap::peek,
                "peek() on an empty heap should throw IllegalStateException");
    }

    @Test
    @DisplayName("order lists bikes most urgent first and breaks ties by bike id")
    void plannerOrder() {
        List<RepairPlanner.Repair> reports = List.of(
                new RepairPlanner.Repair("B-1042", 3),
                new RepairPlanner.Repair("B-1017", 1),
                new RepairPlanner.Repair("B-1088", 2),
                new RepairPlanner.Repair("B-1003", 1));
        assertEquals(List.of("B-1003", "B-1017", "B-1088", "B-1042"), RepairPlanner.order(reports),
                "Urgency 1 comes first; B-1003 and B-1017 tie, so the smaller bike id goes first");
    }

    @Test
    @DisplayName("order matches a fully sorted copy of the reports")
    void plannerMatchesSort() {
        List<RepairPlanner.Repair> reports = new ArrayList<>();
        String[] ids = {"B-1061", "B-1025", "B-1099", "B-1003", "B-1042", "B-1017"};
        int[] urgency = {2, 2, 0, 5, 0, 1};
        for (int i = 0; i < ids.length; i++) {
            reports.add(new RepairPlanner.Repair(ids[i], urgency[i]));
        }
        assertEquals(List.of("B-1042", "B-1099", "B-1017", "B-1025", "B-1061", "B-1003"),
                RepairPlanner.order(reports),
                "order() should poll a PriorityQueue sorted by urgency, then by bikeId");
        assertEquals(6, reports.size(), "order() should not remove anything from the list it was given");
    }

    @Test
    @DisplayName("MinHeap is built on an int array, with no java.util collection inside")
    void heapBuiltByHand() {
        boolean hasArray = false;
        for (Field field : MinHeap.class.getDeclaredFields()) {
            assertFalse(field.getType().getName().startsWith("java.util"),
                    "MinHeap should not use " + field.getType().getSimpleName() + ". Build it on an int[]");
            if (field.getType() == int[].class) {
                hasArray = true;
            }
        }
        assertTrue(hasArray, "MinHeap should keep its values in an int[] field");
    }
}
