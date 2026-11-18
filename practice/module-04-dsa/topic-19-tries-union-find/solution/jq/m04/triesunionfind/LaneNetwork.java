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
        for (int i = 0; i < stations; i++) {
            parent[i] = i;
            size[i] = 1;
        }
        groups = stations;
    }

    /** The root (leader) of the station's group. Points every station on the way straight at the root. */
    public int find(int station) {
        int root = station;
        while (parent[root] != root) {
            root = parent[root];
        }
        while (parent[station] != root) {
            int next = parent[station];
            parent[station] = root;
            station = next;
        }
        return root;
    }

    /** Joins the groups of a and b. Returns true if two groups merged, false if they were already one. */
    public boolean connect(int a, int b) {
        int rootA = find(a);
        int rootB = find(b);
        if (rootA == rootB) {
            return false;
        }
        if (size[rootA] < size[rootB]) {
            int swap = rootA;
            rootA = rootB;
            rootB = swap;
        }
        parent[rootB] = rootA;
        size[rootA] += size[rootB];
        groups--;
        return true;
    }

    public boolean connected(int a, int b) {
        return find(a) == find(b);
    }

    /** How many separate groups there are. */
    public int groups() {
        return groups;
    }

    /** How many stations are in the same group as this one, itself included. */
    public int groupSize(int station) {
        return size[find(station)];
    }

    /** The index of the first lane whose two stations were already connected, or -1 if every lane is needed. */
    public static int firstRedundantLane(int stations, int[][] lanes) {
        LaneNetwork network = new LaneNetwork(stations);
        for (int i = 0; i < lanes.length; i++) {
            if (!network.connect(lanes[i][0], lanes[i][1])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Kruskal's algorithm. Each lane is {a, b, costCents}. Returns the lowest total cost of lanes
     * that joins every station into one group, or -1 if the lanes cannot join them all.
     */
    public static long cheapestNetworkCost(int stations, int[][] lanes) {
        int[][] byCost = lanes.clone();
        Arrays.sort(byCost, Comparator.comparingInt(lane -> lane[2]));
        LaneNetwork network = new LaneNetwork(stations);
        long total = 0;
        for (int[] lane : byCost) {
            if (network.connect(lane[0], lane[1])) {
                total += lane[2];
            }
        }
        return network.groups() <= 1 ? total : -1;
    }
}
