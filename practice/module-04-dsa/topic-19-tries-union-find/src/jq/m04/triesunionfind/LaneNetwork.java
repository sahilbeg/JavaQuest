package jq.m04.triesunionfind;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Which stations are joined by bike lanes, as a union-find (disjoint set union).
 * Stations are numbered 0 to stations - 1. Every station starts in a group of its own.
 * find uses path compression and connect uses union by size, so each call is close to O(1).
 */
public class LaneNetwork {

    private final int[] parent;
    private final int[] size;
    private int groups;

    public LaneNetwork(int stations) {
        parent = new int[stations];
        size = new int[stations];
        // TODO 5: make every station its own root (parent[i] = i) with a group size of 1.
        // There are as many groups as stations.
    }

    /** The root (leader) of the station's group. Points every station on the way straight at the root. */
    public int find(int station) {
        // TODO 6: follow parent links until parent[root] == root.
        // Then walk the path again and point each station straight at the root (path compression).
        return station;
    }

    /** Joins the groups of a and b. Returns true if two groups merged, false if they were already one. */
    public boolean connect(int a, int b) {
        // TODO 7: find both roots. If they are the same, return false.
        // Otherwise hang the smaller group's root under the larger one (union by size),
        // add the sizes, take 1 off groups and return true.
        return false;
    }

    public boolean connected(int a, int b) {
        // TODO 8: two stations are connected when they have the same root.
        return false;
    }

    /** How many separate groups there are. */
    public int groups() {
        return groups;
    }

    /** How many stations are in the same group as this one, itself included. */
    public int groupSize(int station) {
        // TODO 9: the size is stored at the group's root.
        return 0;
    }

    /** The index of the first lane whose two stations were already connected, or -1 if every lane is needed. */
    public static int firstRedundantLane(int stations, int[][] lanes) {
        // TODO 10: build a new LaneNetwork and connect the lanes in order.
        // The first lane where connect returns false closes a loop: return its index.
        return -1;
    }

    /**
     * Kruskal's algorithm. Each lane is {a, b, costCents}. Returns the lowest total cost of lanes
     * that joins every station into one group, or -1 if the lanes cannot join them all.
     */
    public static long cheapestNetworkCost(int stations, int[][] lanes) {
        // TODO 11: copy the lanes and sort the copy by cost, cheapest first.
        // Connect them in that order and add a lane's cost only when connect returns true.
        // At the end, return the total if at most one group is left, otherwise -1.
        return 0;
    }
}
