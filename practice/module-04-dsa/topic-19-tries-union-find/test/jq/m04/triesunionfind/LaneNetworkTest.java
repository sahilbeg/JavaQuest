package jq.m04.triesunionfind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LaneNetworkTest {

    // Stations used in these tests: 0 Riverside, 1 Old Mill, 2 Harbor, 3 Uptown, 4 Depot.

    @Test
    @DisplayName("a new network has every station in a group of its own")
    void startsSeparate() {
        LaneNetwork network = new LaneNetwork(5);
        assertEquals(5, network.groups(), "groups() should be 5 before any lane is built: each station is its own group");
        assertFalse(network.connected(0, 1), "connected(0, 1) should be false before any lane is built");
        assertTrue(network.connected(3, 3), "connected(3, 3) should be true: a station is always connected to itself");
        assertEquals(1, network.groupSize(4), "groupSize(4) should be 1: Depot is alone");
    }

    @Test
    @DisplayName("connect joins two groups and returns false when they are already joined")
    void connectMergesGroups() {
        LaneNetwork network = new LaneNetwork(5);
        assertTrue(network.connect(0, 1), "connect(0, 1) should return true: Riverside and Old Mill were apart");
        assertTrue(network.connect(2, 3), "connect(2, 3) should return true: Harbor and Uptown were apart");
        assertEquals(3, network.groups(), "groups() should be 3 after two joins: {0,1}, {2,3} and {4}");
        assertTrue(network.connect(1, 3), "connect(1, 3) should return true: it joins the group {0,1} with {2,3}");
        assertFalse(network.connect(0, 2), "connect(0, 2) should return false: Riverside already reaches Harbor");
        assertFalse(network.connect(4, 4), "connect(4, 4) should return false: Depot is already in its own group");
        assertEquals(2, network.groups(), "groups() should be 2: {0,1,2,3} and {4}");
    }

    @Test
    @DisplayName("connected follows lanes through other stations, not just direct lanes")
    void connectedIsTransitive() {
        LaneNetwork network = new LaneNetwork(5);
        network.connect(0, 1);
        network.connect(0, 2);
        assertTrue(network.connected(1, 2),
                "connected(1, 2) should be true: Old Mill reaches Harbor through Riverside. "
                        + "In connect, link the two ROOTS, not the two stations");
        network.connect(3, 2);
        assertTrue(network.connected(1, 3), "connected(1, 3) should be true: Old Mill, Riverside, Harbor, Uptown");
        assertFalse(network.connected(4, 0), "connected(4, 0) should be false: no lane reaches Depot");
        assertEquals(4, network.groupSize(3), "groupSize(3) should be 4: Uptown's group is {0,1,2,3}");
    }

    @Test
    @DisplayName("100,000 stations joined in either order stay fast")
    void staysFast() {
        joinAllAndCheck(true);
        joinAllAndCheck(false);
    }

    private static void joinAllAndCheck(boolean zeroFirst) {
        int stations = 100_000;
        String call = zeroFirst ? "connect(0, i)" : "connect(i, 0)";
        LaneNetwork network = new LaneNetwork(stations);
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            for (int i = 1; i < stations; i++) {
                if (zeroFirst) {
                    network.connect(0, i);
                } else {
                    network.connect(i, 0);
                }
            }
            for (int i = 0; i < stations; i++) {
                if (!network.connected(i, stations - 1 - i)) {
                    throw new AssertionError("After " + call + " for every station, connected(" + i + ", "
                            + (stations - 1 - i) + ") should be true: every station joined station 0");
                }
            }
        }, "100,000 joins with " + call + " took more than 2 seconds. "
                + "Use union by size and path compression so the trees stay flat");
        assertEquals(1, network.groups(), "groups() should be 1 after " + call + " for every station");
        assertEquals(stations, network.groupSize(12_345),
                "groupSize should be 100000 after " + call + " for every station: everyone is in one group");
    }

    @Test
    @DisplayName("firstRedundantLane finds the first lane that closes a loop")
    void redundantLane() {
        int[][] lanes = {{0, 1}, {1, 2}, {3, 4}, {2, 0}, {2, 3}};
        assertEquals(3, LaneNetwork.firstRedundantLane(5, lanes),
                "Lane 3 (Harbor to Riverside) is redundant: Riverside already reaches Harbor through Old Mill");
        int[][] tree = {{0, 1}, {1, 2}, {2, 3}, {3, 4}};
        assertEquals(-1, LaneNetwork.firstRedundantLane(5, tree), "No lane closes a loop here, so the answer is -1");
        assertEquals(-1, LaneNetwork.firstRedundantLane(3, new int[0][]), "With no lanes at all, the answer is -1");
    }

    @Test
    @DisplayName("firstRedundantLane catches a repeated lane and a lane from a station to itself")
    void redundantLaneEdgeCases() {
        assertEquals(1, LaneNetwork.firstRedundantLane(3, new int[][] {{0, 1}, {1, 0}, {1, 2}}),
                "Lane 1 joins Old Mill back to Riverside, which lane 0 already did, so it is redundant");
        assertEquals(1, LaneNetwork.firstRedundantLane(3, new int[][] {{0, 1}, {2, 2}}),
                "Lane 1 goes from Harbor to Harbor. A station already reaches itself, so it is redundant");
    }

    @Test
    @DisplayName("cheapestNetworkCost picks the cheapest lanes that join every station")
    void kruskal() {
        int[][] lanes = {
            {0, 1, 400}, {1, 2, 300}, {0, 2, 200}, {2, 3, 900}, {1, 3, 500}, {3, 4, 100}, {0, 4, 800}
        };
        assertEquals(1100, LaneNetwork.cheapestNetworkCost(5, lanes),
                "The cheapest network uses 3-4 (100), 0-2 (200), 1-2 (300) and 1-3 (500): 1100 cents. "
                        + "Sort the lanes by cost and skip any lane whose stations are already connected");
        assertEquals(0, LaneNetwork.cheapestNetworkCost(1, new int[0][]), "One station needs no lanes, so the cost is 0");
    }

    @Test
    @DisplayName("cheapestNetworkCost returns -1 when the lanes cannot reach every station")
    void kruskalDisconnected() {
        int[][] lanes = {{0, 1, 400}, {1, 2, 300}, {0, 2, 200}};
        assertEquals(-1, LaneNetwork.cheapestNetworkCost(4, lanes),
                "No lane reaches station 3 (Uptown), so no network joins all four stations: return -1");
    }
}
