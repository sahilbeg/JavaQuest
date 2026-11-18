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
        if (row < 0 || row >= rows() || col < 0 || col >= cols()) {
            return false;
        }
        return cells[row][col] == '#' || cells[row][col] == 'D';
    }

    public int laneZones() {
        boolean[][] visited = new boolean[rows()][cols()];
        int zones = 0;
        for (int r = 0; r < rows(); r++) {
            for (int c = 0; c < cols(); c++) {
                if (isLane(r, c) && !visited[r][c]) {
                    zones++;
                    Deque<int[]> stack = new ArrayDeque<>();
                    visited[r][c] = true;
                    stack.push(new int[] {r, c});
                    while (!stack.isEmpty()) {
                        int[] cell = stack.pop();
                        for (int d = 0; d < 4; d++) {
                            int nr = cell[0] + ROW_STEP[d];
                            int nc = cell[1] + COL_STEP[d];
                            if (isLane(nr, nc) && !visited[nr][nc]) {
                                visited[nr][nc] = true;
                                stack.push(new int[] {nr, nc});
                            }
                        }
                    }
                }
            }
        }
        return zones;
    }

    public int[][] distanceToDock() {
        int[][] distance = new int[rows()][cols()];
        Deque<int[]> queue = new ArrayDeque<>();
        for (int r = 0; r < rows(); r++) {
            Arrays.fill(distance[r], -1);
            for (int c = 0; c < cols(); c++) {
                if (cells[r][c] == 'D') {
                    distance[r][c] = 0;
                    queue.offer(new int[] {r, c});
                }
            }
        }
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            for (int d = 0; d < 4; d++) {
                int nr = cell[0] + ROW_STEP[d];
                int nc = cell[1] + COL_STEP[d];
                if (isLane(nr, nc) && distance[nr][nc] == -1) {
                    distance[nr][nc] = distance[cell[0]][cell[1]] + 1;
                    queue.offer(new int[] {nr, nc});
                }
            }
        }
        return distance;
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
