package jq.m01.arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DockMapTest {

    private static final boolean[] RIVERSIDE =
            {true, true, false, true, false, false, true, true, true, false, true, true};

    @Test
    @DisplayName("countBikes counts the docks that hold a bike")
    void countBikes() {
        assertEquals(8, DockMap.countBikes(RIVERSIDE),
                "countBikes should be 8 for Riverside: 8 of the 12 docks are true");
        assertEquals(0, DockMap.countBikes(new boolean[12]),
                "countBikes should be 0 when all 12 docks are false");
    }

    @Test
    @DisplayName("firstEmptyDock gives the painted dock number, which starts at 1")
    void firstEmptyDock() {
        assertEquals(3, DockMap.firstEmptyDock(RIVERSIDE),
                "firstEmptyDock should be 3 for Riverside: index 2 is the first false, and the dock number is index + 1");
        assertEquals(1, DockMap.firstEmptyDock(new boolean[12]),
                "firstEmptyDock should be 1 when every dock is empty: index 0 is dock number 1");
    }

    @Test
    @DisplayName("firstEmptyDock returns -1 when every dock holds a bike")
    void noEmptyDock() {
        boolean[] full = {true, true, true, true};
        assertEquals(-1, DockMap.firstEmptyDock(full),
                "firstEmptyDock should be -1 when no dock is empty. Return -1 after the loop ends");
    }

    @Test
    @DisplayName("totalMinutes adds up every ride")
    void totalMinutes() {
        assertEquals(87, DockMap.totalMinutes(new int[] {12, 45, 30}),
                "totalMinutes({12, 45, 30}) should be 87");
        assertEquals(0, DockMap.totalMinutes(new int[0]),
                "totalMinutes of an empty array should be 0");
    }

    @Test
    @DisplayName("longestRide finds the biggest number, or 0 for no rides")
    void longestRide() {
        assertEquals(45, DockMap.longestRide(new int[] {12, 45, 30}),
                "longestRide({12, 45, 30}) should be 45");
        assertEquals(60, DockMap.longestRide(new int[] {60, 5, 20}),
                "longestRide({60, 5, 20}) should be 60. Check the first element too");
        assertEquals(0, DockMap.longestRide(new int[0]),
                "longestRide of an empty array should be 0");
    }

    @Test
    @DisplayName("bikesPerStation gives one bike count per station row")
    void bikesPerStation() {
        boolean[][] stations = {
            RIVERSIDE,
            new boolean[12],
            {true, false, true},
            {true, true, true, true}
        };
        assertArrayEquals(new int[] {8, 0, 2, 4}, DockMap.bikesPerStation(stations),
                "bikesPerStation should be {8, 0, 2, 4}: one count for each row, in the same order");
    }
}
