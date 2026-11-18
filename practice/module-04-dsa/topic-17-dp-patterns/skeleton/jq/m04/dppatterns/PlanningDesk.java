package jq.m04.dppatterns;

import java.util.Arrays;

public class PlanningDesk {

    public static int maxRidersWithoutNeighbours(int[] riders) {
        // TODO 1: take or skip. best(i) = max(best(i - 1), best(i - 2) + riders[i]).
        //         Two rolling variables are enough. No stations means 0.
        return -1;
    }

    public static int fewestCoins(int[] coins, int amount) {
        // TODO 2: fill fewest[0..amount]. fewest[0] = 0, and every other cell starts as "impossible".
        //         fewest[a] = 1 + the smallest fewest[a - coin], over every coin that fits.
        // TODO 3: return -1 when fewest[amount] is still impossible.
        return 0;
    }

    public static long waysToPay(int[] coins, int amount) {
        // TODO 4: ways[0] = 1. Put the coins in the OUTER loop and the amounts in the inner loop:
        //         ways[a] += ways[a - coin]. Return a long.
        return 0;
    }

    public static long routesThroughGrid(boolean[][] blocked) {
        // TODO 5: return 0 for an empty grid.
        // TODO 6: routes into a square = routes from above + routes from the left.
        //         A blocked square has 0 routes. The start square has 1 route unless it is blocked.
        return -1;
    }

    public static int bestTruckLoad(int[] weights, int[] values, int capacity) {
        // TODO 7: best[w] = most value that fits in w kilograms. For each crate,
        //         loop w from capacity DOWN to the crate's weight, so the crate is used at most once:
        //         best[w] = max(best[w], best[w - weight] + value).
        return -1;
    }

    public static int editDistance(String a, String b) {
        // TODO 8: dist[i][j] = edits to turn the first i letters of a into the first j letters of b.
        //         Row 0 and column 0 count inserts and deletes from empty text.
        //         Same letters: copy the diagonal. Different: 1 + min(replace, delete, insert).
        return -1;
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
