package jq.m04.searching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SearchTest {

    private static final int[] UNSORTED = {1042, 2001, 1777, 3300, 1500};
    private static final int[] SORTED = {1003, 1042, 1500, 1777, 2001, 2450, 3300};

    @Test
    @DisplayName("linear finds a bike id in an unsorted array and returns -1 for a missing one")
    void linearSearch() {
        assertEquals(3, Search.linear(UNSORTED, 3300), "linear(.., 3300) should be 3: 3300 sits at index 3");
        assertEquals(0, Search.linear(UNSORTED, 1042), "linear(.., 1042) should be 0: it is the first item");
        assertEquals(-1, Search.linear(UNSORTED, 9999), "linear(.., 9999) should be -1: that id is not in the array");
        assertEquals(-1, Search.linear(new int[0], 1042), "linear on an empty array should be -1");
    }

    @Test
    @DisplayName("binary finds every id in a sorted array and returns -1 for missing ones")
    void binarySearch() {
        for (int i = 0; i < SORTED.length; i++) {
            assertEquals(i, Search.binary(SORTED, SORTED[i]),
                    "binary(.., " + SORTED[i] + ") should be " + i + ", the index where that id sits");
        }
        assertEquals(-1, Search.binary(SORTED, 1000), "binary(.., 1000) should be -1: it is smaller than every id");
        assertEquals(-1, Search.binary(SORTED, 2000), "binary(.., 2000) should be -1: it falls between two ids");
        assertEquals(-1, Search.binary(SORTED, 9999), "binary(.., 9999) should be -1: it is bigger than every id");
        assertEquals(-1, Search.binary(new int[0], 5), "binary on an empty array should be -1");
    }

    @Test
    @DisplayName("binary searches 1024 sorted items in at most 11 steps")
    void binaryIsLogarithmic() {
        int[] ids = new int[1024];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = i * 2;
        }
        int[] targets = {0, 2046, 1000, 1001, -5, 5000};
        for (int target : targets) {
            int expected = -1;
            if (target >= 0 && target % 2 == 0 && target < 2048) {
                expected = target / 2;
            }
            assertEquals(expected, Search.binary(ids, target), "binary(ids, " + target + ") should be " + expected);
            int steps = Search.lastSteps();
            assertTrue(steps >= 1 && steps <= 11,
                    "searching 1024 items for " + target + " took " + steps
                            + " steps. Set steps to 0 at the start, add 1 on every loop pass, and halve the range each time");
        }
    }

    @Test
    @DisplayName("binaryRecursive gives the same answers as binary")
    void recursiveSearch() {
        for (int i = 0; i < SORTED.length; i++) {
            assertEquals(i, Search.binaryRecursive(SORTED, SORTED[i]),
                    "binaryRecursive(.., " + SORTED[i] + ") should be " + i);
        }
        assertEquals(-1, Search.binaryRecursive(SORTED, 2000), "binaryRecursive(.., 2000) should be -1: that id is missing");
        assertEquals(-1, Search.binaryRecursive(new int[0], 5), "binaryRecursive on an empty array should be -1");
    }

    @Test
    @DisplayName("firstAtLeast finds the first ride of at least the given minutes")
    void firstAtLeastMinutes() {
        int[] minutes = {5, 8, 12, 12, 12, 30, 45};
        assertEquals(2, Search.firstAtLeast(minutes, 12), "firstAtLeast(.., 12) should be 2: the first 12 is at index 2");
        assertEquals(2, Search.firstAtLeast(minutes, 10), "firstAtLeast(.., 10) should be 2: 12 is the first value >= 10");
        assertEquals(0, Search.firstAtLeast(minutes, 1), "firstAtLeast(.., 1) should be 0: every ride is at least 1 minute");
        assertEquals(6, Search.firstAtLeast(minutes, 45), "firstAtLeast(.., 45) should be 6");
        assertEquals(7, Search.firstAtLeast(minutes, 60), "firstAtLeast(.., 60) should be 7, the array length: no ride is that long");
        assertEquals(0, Search.firstAtLeast(new int[0], 10), "firstAtLeast on an empty array should be 0, the length");
    }

    @Test
    @DisplayName("firstAtLeast searches 1024 sorted items in at most 11 steps")
    void firstAtLeastIsLogarithmic() {
        int[] minutes = new int[1024];
        for (int i = 0; i < minutes.length; i++) {
            minutes[i] = i * 2;
        }
        assertEquals(500, Search.firstAtLeast(minutes, 999), "firstAtLeast(minutes, 999) should be 500: 1000 is the first value >= 999");
        int steps = Search.lastSteps();
        assertTrue(steps >= 1 && steps <= 11,
                "firstAtLeast on 1024 items took " + steps
                        + " steps. Set steps to 0 at the start, add 1 on every loop pass, and halve the range each time");
    }
}
