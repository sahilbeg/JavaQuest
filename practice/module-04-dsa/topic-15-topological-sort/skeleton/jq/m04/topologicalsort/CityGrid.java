package jq.m04.topologicalsort;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class CityGrid {

    // The four neighbours of a cell: up, down, left, right.
    private static final int[] ROW_STEP = {-1, 1, 0, 0};
    private static final int[] COL_STEP = {0, 0, -1, 1};

    private final char[][] cells;

    public CityGrid(String... rows) {
        cells = new char[rows.length][];
        for (int r = 0; r < rows.length; r++) {
            cells[r] = rows[r].toCharArray();
        }
    }

    public int rows() {
        return cells.length;
    }

    public int cols() {
        return cells.length == 0 ? 0 : cells[0].length;
    }

    public boolean isLane(int row, int col) {
        // TODO 6: return false when (row, col) is outside the grid.
        //         Otherwise return true for a lane block '#' or a dock 'D', and false for '.'.
        return false;
    }

    public int laneZones() {
        // TODO 7: count the groups of lane cells joined up, down, left or right (never diagonally).
        //         Keep a boolean[][] visited. For each lane cell not yet visited, add 1 to the count
        //         and mark its whole group with BFS or DFS. Use an ArrayDeque, not recursion:
        //         a big grid makes recursion run out of stack.
        return 0;
    }

    public int[][] distanceToDock() {
        // TODO 8: multi-source BFS. Fill a rows() x cols() array with -1 (Arrays.fill on each row).
        //         Every dock 'D' gets 0 and joins the queue at the start.
        //         Poll a cell. Each lane neighbour still at -1 gets this cell's distance + 1 and joins the queue.
        return new int[0][0];
    }

    public static void main(String[] args) {
        CityGrid grid = new CityGrid(
                "D#..#",
                "##..#",
                "...#D");
        System.out.println("Lane zones: " + grid.laneZones());
        for (int[] row : grid.distanceToDock()) {
            System.out.println(Arrays.toString(row));
        }
    }
}
