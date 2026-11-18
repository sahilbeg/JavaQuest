package jq.m04.dppatterns;

import java.util.Arrays;

public class PlanningDesk {

    // Take or skip: best[i] = max(best[i - 1], best[i - 2] + riders[i]).
    // Two rolling variables replace the whole array. O(n) time, O(1) space.
    public static int maxRidersWithoutNeighbours(int[] riders) {
        int twoBack = 0;
        int oneBack = 0;
        for (int r : riders) {
            int best = Math.max(oneBack, twoBack + r);
            twoBack = oneBack;
            oneBack = best;
        }
        return oneBack;
    }

    // Unbounded choices, minimum: fewest[a] = 1 + min over coins of fewest[a - coin].
    // O(amount * coins) time, O(amount) space.
    public static int fewestCoins(int[] coins, int amount) {
        int impossible = amount + 1;
        int[] fewest = new int[amount + 1];
        Arrays.fill(fewest, impossible);
        fewest[0] = 0;
        for (int a = 1; a <= amount; a++) {
            for (int coin : coins) {
                if (coin <= a && fewest[a - coin] + 1 < fewest[a]) {
                    fewest[a] = fewest[a - coin] + 1;
                }
            }
        }
        return fewest[amount] == impossible ? -1 : fewest[amount];
    }

    // Unbounded choices, counting: coins in the OUTER loop, so each mix is counted once.
    // O(amount * coins) time, O(amount) space.
    public static long waysToPay(int[] coins, int amount) {
        long[] ways = new long[amount + 1];
        ways[0] = 1;
        for (int coin : coins) {
            for (int a = coin; a <= amount; a++) {
                ways[a] += ways[a - coin];
            }
        }
        return ways[amount];
    }

    // Grid paths: routes[r][c] = routes from above + routes from the left, 0 on a blocked cell.
    // One rolling row: O(rows * cols) time, O(cols) space.
    public static long routesThroughGrid(boolean[][] blocked) {
        if (blocked.length == 0 || blocked[0].length == 0) {
            return 0;
        }
        int cols = blocked[0].length;
        long[] routes = new long[cols];
        routes[0] = 1;
        for (boolean[] row : blocked) {
            for (int c = 0; c < cols; c++) {
                if (row[c]) {
                    routes[c] = 0;
                } else if (c > 0) {
                    routes[c] += routes[c - 1];
                }
            }
        }
        return routes[cols - 1];
    }

    // 0/1 knapsack: best[w] = max(best[w], best[w - weight] + value).
    // Capacity goes DOWN so each crate is used at most once. O(n * capacity) time, O(capacity) space.
    public static int bestTruckLoad(int[] weights, int[] values, int capacity) {
        int[] best = new int[capacity + 1];
        for (int i = 0; i < weights.length; i++) {
            for (int w = capacity; w >= weights[i]; w--) {
                best[w] = Math.max(best[w], best[w - weights[i]] + values[i]);
            }
        }
        return best[capacity];
    }

    // Two strings: dist[i][j] = edits to turn the first i letters of a into the first j letters of b.
    // O(n * m) time and space.
    public static int editDistance(String a, String b) {
        int n = a.length();
        int m = b.length();
        int[][] dist = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) {
            dist[i][0] = i;
        }
        for (int j = 0; j <= m; j++) {
            dist[0][j] = j;
        }
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dist[i][j] = dist[i - 1][j - 1];
                } else {
                    int replace = dist[i - 1][j - 1];
                    int delete = dist[i - 1][j];
                    int insert = dist[i][j - 1];
                    dist[i][j] = 1 + Math.min(replace, Math.min(delete, insert));
                }
            }
        }
        return dist[n][m];
    }

    public static void main(String[] args) {
        System.out.println("Riders reached: " + maxRidersWithoutNeighbours(new int[] {5, 1, 1, 5}));
        System.out.println("Fewest coins for 6: " + fewestCoins(new int[] {1, 3, 4}, 6));
        System.out.println("Ways to pay 5: " + waysToPay(new int[] {1, 2, 5}, 5));
        boolean[][] map = new boolean[3][3];
        map[1][1] = true;
        System.out.println("Routes around a closed square: " + routesThroughGrid(map));
        System.out.println("Best truck load: " + bestTruckLoad(new int[] {1, 3, 4, 5}, new int[] {1, 4, 5, 7}, 7));
        System.out.println("Edits from Harbour to Harbor: " + editDistance("Harbour", "Harbor"));
    }
}
