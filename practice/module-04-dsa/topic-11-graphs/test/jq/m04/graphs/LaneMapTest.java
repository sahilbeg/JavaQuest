package jq.m04.graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 2, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class LaneMapTest {

    private LaneMap split() {
        LaneMap map = new LaneMap();
        map.addLane("Riverside", "Old Mill");
        map.addLane("Old Mill", "Harbor");
        map.addLane("Uptown", "Depot");
        return map;
    }

    private LaneMap riverton() {
        LaneMap map = new LaneMap();
        map.addLane("Riverside", "Old Mill");
        map.addLane("Riverside", "Harbor");
        map.addLane("Old Mill", "Uptown");
        map.addLane("Harbor", "Uptown");
        map.addLane("Uptown", "Depot");
        return map;
    }

    @Test
    @DisplayName("A lane works in both directions and keeps neighbours in the order they were added")
    void lanesAreUndirected() {
        LaneMap map = riverton();
        assertEquals(List.of("Old Mill", "Harbor"), map.neighbours("Riverside"),
                "neighbours(\"Riverside\") should be [Old Mill, Harbor], in the order the lanes were added");
        assertEquals(List.of("Old Mill", "Harbor", "Depot"), map.neighbours("Uptown"),
                "neighbours(\"Uptown\") should be [Old Mill, Harbor, Depot]. addLane(a, b) must also add a to b's list");
        LaneMap small = new LaneMap();
        small.addLane("Riverside", "Old Mill");
        assertEquals(List.of(), small.neighbours("Depot"),
                "Depot has no lanes on this map, so neighbours(\"Depot\") should be an empty list");
    }

    @Test
    @DisplayName("reachable lists every station you can ride to, sorted A to Z")
    void reachableIsSorted() {
        assertEquals(List.of("Depot", "Harbor", "Old Mill", "Riverside", "Uptown"), riverton().reachable("Depot"),
                "reachable(\"Depot\") should hold all five Riverton stations, sorted A to Z");
        LaneMap map = split();
        assertEquals(List.of("Harbor", "Old Mill", "Riverside"), map.reachable("Harbor"),
                "With no lane to Uptown, reachable(\"Harbor\") should be [Harbor, Old Mill, Riverside]");
        assertEquals(List.of("Depot", "Uptown"), map.reachable("Uptown"),
                "reachable(\"Uptown\") should be [Depot, Uptown]: the start counts too");
    }

    @Test
    @DisplayName("fewestStops finds the route with the fewest stops, including both ends")
    void shortestRoute() {
        assertEquals(List.of("Riverside", "Old Mill", "Uptown", "Depot"), riverton().fewestStops("Riverside", "Depot"),
                "Riverside to Depot should be [Riverside, Old Mill, Uptown, Depot]: 3 lanes is the fewest");
        assertEquals(List.of("Harbor", "Riverside"), riverton().fewestStops("Harbor", "Riverside"),
                "Harbor to Riverside is one lane, so the route is [Harbor, Riverside]");
    }

    @Test
    @DisplayName("When two routes tie, the neighbour added first wins")
    void tieGoesToInsertionOrder() {
        assertEquals(List.of("Riverside", "Old Mill", "Uptown"), riverton().fewestStops("Riverside", "Uptown"),
                "Via Old Mill and via Harbor both take 2 lanes. BFS meets Old Mill first because its lane was added first");
        LaneMap flipped = new LaneMap();
        flipped.addLane("Riverside", "Harbor");
        flipped.addLane("Riverside", "Old Mill");
        flipped.addLane("Old Mill", "Uptown");
        flipped.addLane("Harbor", "Uptown");
        assertEquals(List.of("Riverside", "Harbor", "Uptown"), flipped.fewestStops("Riverside", "Uptown"),
                "With the Harbor lane added first, the tie should go to [Riverside, Harbor, Uptown]");
    }

    @Test
    @DisplayName("fewestStops handles the same station and unreachable stations")
    void edgeCases() {
        assertEquals(List.of("Harbor"), riverton().fewestStops("Harbor", "Harbor"),
                "From Harbor to Harbor the route is just [Harbor]");
        assertEquals(List.of(), split().fewestStops("Riverside", "Depot"),
                "No lanes join Riverside to Depot on this map, so fewestStops should return an empty list");
    }
}
