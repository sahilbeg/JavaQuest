package jq.m04.simplesorts;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SimpleSortsTest {

    private static final int[] RIVERSIDE = {42, 7, 18, 95, 3, 61, 25, 12, 88, 30, 5, 70};
    private static final int[] RIVERSIDE_SORTED = {3, 5, 7, 12, 18, 25, 30, 42, 61, 70, 88, 95};
    private static final int[] WITH_DUPLICATES = {12, 5, 12, 30, 5, 5, 1};
    private static final int[] WITH_DUPLICATES_SORTED = {1, 5, 5, 5, 12, 12, 30};

    private static void checkSort(String name, Consumer<int[]> sort) {
        int[] a = RIVERSIDE.clone();
        sort.accept(a);
        assertArrayEquals(RIVERSIDE_SORTED, a,
                name + " should sort the Riverside minutes in place, smallest first. You got " + Arrays.toString(a));

        int[] dup = WITH_DUPLICATES.clone();
        sort.accept(dup);
        assertArrayEquals(WITH_DUPLICATES_SORTED, dup,
                name + " should keep every duplicate. You got " + Arrays.toString(dup));

        int[] backwards = {9, 8, 7, 6, 5, 4, 3, 2, 1};
        sort.accept(backwards);
        assertArrayEquals(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9}, backwards,
                name + " should sort a backwards array. You got " + Arrays.toString(backwards));

        int[] empty = {};
        sort.accept(empty);
        assertEquals(0, empty.length, name + " should leave an empty array empty");

        int[] single = {42};
        sort.accept(single);
        assertArrayEquals(new int[] {42}, single, name + " should leave a one-item array alone");
    }

    @Test
    @DisplayName("bubble sorts ride minutes in place, with duplicates and edge cases")
    void bubbleSorts() {
        checkSort("bubble", SimpleSorts::bubble);
    }

    @Test
    @DisplayName("selection sorts ride minutes in place, with duplicates and edge cases")
    void selectionSorts() {
        checkSort("selection", SimpleSorts::selection);
    }

    @Test
    @DisplayName("insertion sorts ride minutes in place, with duplicates and edge cases")
    void insertionSorts() {
        checkSort("insertion", SimpleSorts::insertion);
    }

    @Test
    @DisplayName("bubblePasses sorts the array and counts passes with an early stop")
    void bubblePassCount() {
        int[] sorted = {3, 5, 7, 12};
        assertEquals(1, SimpleSorts.bubblePasses(sorted),
                "already sorted data should take 1 pass: the first pass makes no swap, so stop");

        int[] oneSwap = {2, 1, 3, 4};
        assertEquals(2, SimpleSorts.bubblePasses(oneSwap),
                "{2, 1, 3, 4} should take 2 passes: one swap, then one pass that proves it is sorted. Reset swapped at the start of each pass");
        assertArrayEquals(new int[] {1, 2, 3, 4}, oneSwap, "bubblePasses should also sort the array");

        int[] trace = {5, 2, 8, 1};
        assertEquals(3, SimpleSorts.bubblePasses(trace),
                "{5, 2, 8, 1} should take 3 passes, like the trace table in the lesson");
        assertArrayEquals(new int[] {1, 2, 5, 8}, trace, "bubblePasses should sort {5, 2, 8, 1} to {1, 2, 5, 8}");

        assertEquals(0, SimpleSorts.bubblePasses(new int[0]), "an empty array needs 0 passes");
        assertEquals(0, SimpleSorts.bubblePasses(new int[] {42}), "a one-item array needs 0 passes");
    }
}
