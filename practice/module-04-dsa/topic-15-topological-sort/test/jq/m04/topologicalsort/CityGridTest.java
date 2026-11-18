package jq.m04.topologicalsort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

class CityGridTest {

    private static <T> T quickly(ThrowingSupplier<T> work) {
        return assertTimeoutPreemptively(Duration.ofSeconds(3), work,
                "This took more than 3 seconds. Mark a cell visited when it joins the queue, so no cell joins twice");
    }

    private static String show(int[][] grid) {
        return Arrays.deepToString(grid);
    }

    @Test
    @DisplayName("isLane is true for '#' and 'D', false for '.', and false outside the grid")
    void isLaneChecksBounds() {
        CityGrid grid = new CityGrid(
                "#.D",
                "..#");
        assertTrue(grid.isLane(0, 0), "Cell (0, 0) is '#', a lane block, so isLane(0, 0) should be true");
        assertTrue(grid.isLane(0, 2), "Cell (0, 2) is 'D', a dock on a lane, so isLane(0, 2) should be true");
        assertFalse(grid.isLane(0, 1), "Cell (0, 1) is '.', a block with no lane, so isLane(0, 1) should be false");
        assertFalse(grid.isLane(-1, 0), "Row -1 is above the grid, so isLane(-1, 0) should be false, not crash");
        assertFalse(grid.isLane(2, 0), "The grid has rows 0 and 1 only, so isLane(2, 0) should be false, not crash");
        assertFalse(grid.isLane(1, -1), "Column -1 is left of the grid, so isLane(1, -1) should be false, not crash");
        assertFalse(grid.isLane(1, 3), "The grid has columns 0 to 2 only, so isLane(1, 3) should be false, not crash");
    }

    @Test
    @DisplayName("laneZones counts groups of lane cells joined up, down, left or right")
    void countsZones() {
        CityGrid grid = new CityGrid(
                "##..#",
                "#..##",
                "..#..",
                "#.#.#");
        assertEquals(5, quickly(grid::laneZones),
                "This map has 5 separate lane zones: top left, top right, the middle pair, and the two lone bottom corners");
    }

    @Test
    @DisplayName("Cells that only touch at a corner are in different zones")
    void diagonalDoesNotJoin() {
        CityGrid grid = new CityGrid(
                "#.",
                ".#");
        assertEquals(2, quickly(grid::laneZones),
                "The two '#' cells only touch corner to corner. A rider cannot cut across a corner, so expect 2 zones, not 1");
    }

    @Test
    @DisplayName("laneZones handles an empty grid, a grid with no lanes and a one-cell grid")
    void smallGrids() {
        assertEquals(0, quickly(() -> new CityGrid().laneZones()), "A grid with no rows has 0 lane zones");
        assertEquals(0, quickly(() -> new CityGrid("....").laneZones()), "A grid with no '#' or 'D' has 0 lane zones");
        assertEquals(1, quickly(() -> new CityGrid("D").laneZones()), "A single dock cell is one lane zone on its own");
    }

    @Test
    @DisplayName("laneZones works on a 600 x 600 city without running out of stack")
    void bigGridNeedsNoRecursion() {
        String[] solid = new String[600];
        String[] stripes = new String[600];
        for (int r = 0; r < 600; r++) {
            solid[r] = "#".repeat(600);
            stripes[r] = (r % 2 == 0 ? "#" : ".").repeat(600);
        }
        try {
            assertEquals(1, quickly(() -> new CityGrid(solid).laneZones()),
                    "A 600 x 600 grid full of lanes is one big zone");
            assertEquals(300, quickly(() -> new CityGrid(stripes).laneZones()),
                    "600 rows that alternate lane and no lane make 300 separate zones");
        } catch (StackOverflowError e) {
            fail("StackOverflowError: a recursive flood fill went 360,000 calls deep. "
                    + "Use an ArrayDeque as your own queue or stack instead of recursion");
        }
    }

    @Test
    @DisplayName("distanceToDock gives each lane cell its moves to the nearest dock")
    void distancesFromNearestDock() {
        CityGrid corridor = new CityGrid("D#####D");
        assertEquals(show(new int[][] {{0, 1, 2, 3, 2, 1, 0}}), show(quickly(corridor::distanceToDock)),
                "Docks sit at both ends. Each cell counts moves to the NEARER dock, so the middle cell is 3. "
                        + "Put every dock in the queue at the start");
        CityGrid grid = new CityGrid(
                "D#..#",
                "##..#",
                "...#D");
        int[][] expected = {
                {0, 1, -1, -1, 2},
                {1, 2, -1, -1, 1},
                {-1, -1, -1, 1, 0}};
        assertEquals(show(expected), show(quickly(grid::distanceToDock)),
                "Docks are 0, '.' cells are -1, and lane cells count moves along lanes to the nearest dock");
    }

    @Test
    @DisplayName("distanceToDock marks cut-off lanes -1, and every cell -1 when there is no dock")
    void unreachableCells() {
        CityGrid grid = new CityGrid(
                "D#.#",
                "....");
        int[][] expected = {
                {0, 1, -1, -1},
                {-1, -1, -1, -1}};
        assertEquals(show(expected), show(quickly(grid::distanceToDock)),
                "The '#' at (0, 3) is a lane, but no lane joins it to the dock, so its distance is -1");
        CityGrid noDock = new CityGrid("##", "#.");
        assertEquals(show(new int[][] {{-1, -1}, {-1, -1}}), show(quickly(noDock::distanceToDock)),
                "With no dock on the map, no cell can reach one, so every cell is -1");
    }
}
