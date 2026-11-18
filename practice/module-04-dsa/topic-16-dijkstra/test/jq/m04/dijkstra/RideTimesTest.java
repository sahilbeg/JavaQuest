package jq.m04.dijkstra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

class RideTimesTest {

    private static <T> T quickly(ThrowingSupplier<T> work) {
        return assertTimeoutPreemptively(Duration.ofSeconds(2), work,
                "This took more than 2 seconds. Check that the loop polls the queue and that every station settles once");
    }

    private static int fast(ThrowingSupplier<Integer> work) {
        return assertTimeoutPreemptively(Duration.ofSeconds(2), work,
                "30,000 docks took more than 2 seconds. Take the next station from a PriorityQueue, "
                        + "instead of scanning every station to find the closest one");
    }

    /** Riverton, with riding minutes on every lane. */
    private static RideTimes riverton() {
        RideTimes map = new RideTimes();
        map.addLane("Riverside", "Old Mill", 4);
        map.addLane("Riverside", "Harbor", 2);
        map.addLane("Harbor", "Old Mill", 1);
        map.addLane("Old Mill", "Uptown", 3);
        map.addLane("Harbor", "Uptown", 7);
        map.addLane("Uptown", "Depot", 5);
        map.addLane("Harbor", "Depot", 12);
        return map;
    }

    @Test
    @DisplayName("addLane rejects negative minutes but accepts a 0-minute lane")
    void negativeMinutesAreRejected() {
        RideTimes map = new RideTimes();
        assertThrows(IllegalArgumentException.class, () -> map.addLane("Riverside", "Harbor", -3),
                "A lane cannot take -3 minutes. addLane should throw IllegalArgumentException, because Dijkstra breaks on negative weights");
        map.addLane("Riverside", "Harbor", 2);
        map.addLane("Harbor", "Old Mill", 0);
        assertEquals(2, quickly(() -> map.fastestMinutes("Riverside", "Old Mill")),
                "Harbor and Old Mill share a 0-minute lane, so Riverside to Old Mill takes 2 + 0 = 2 minutes");
    }

    @Test
    @DisplayName("A lane can be ridden both ways in the same time")
    void lanesWorkBothWays() {
        RideTimes map = riverton();
        assertEquals(2, quickly(() -> map.fastestMinutes("Riverside", "Harbor")),
                "Riverside to Harbor is one 2-minute lane");
        assertEquals(2, quickly(() -> map.fastestMinutes("Harbor", "Riverside")),
                "Harbor to Riverside uses the same lane the other way, so it is 2 minutes too. addLane must add both directions");
    }

    @Test
    @DisplayName("The fastest route can have more stops than the route with the fewest stops")
    void fastestIsNotFewestStops() {
        assertEquals(11, quickly(() -> riverton().fastestMinutes("Riverside", "Depot")),
                "Riverside to Depot should take 11 minutes: Harbor 2, Old Mill 1, Uptown 3, Depot 5. "
                        + "The 2-lane route through Harbor takes 14, which is what BFS would pick");
    }

    @Test
    @DisplayName("A station's best time can still improve after it was first reached")
    void bestTimesCanImprove() {
        RideTimes map = riverton();
        assertEquals(3, quickly(() -> map.fastestMinutes("Riverside", "Old Mill")),
                "Riverside to Old Mill should be 3 minutes via Harbor, not 4 on the direct lane. "
                        + "Do not lock in a station when it is first offered; a station is final only when it is polled");
        assertEquals(6, quickly(() -> map.fastestMinutes("Riverside", "Uptown")),
                "Riverside to Uptown should be 6 minutes: Harbor 2, Old Mill 1, Uptown 3");
    }

    @Test
    @DisplayName("fastestRoute lists the stations of the fastest route, both ends included")
    void routeListsStations() {
        assertEquals(List.of("Riverside", "Harbor", "Old Mill", "Uptown", "Depot"),
                quickly(() -> riverton().fastestRoute("Riverside", "Depot")),
                "The 11-minute route is [Riverside, Harbor, Old Mill, Uptown, Depot]. Walk the parents back from Depot, then reverse");
        assertEquals(List.of("Depot", "Uptown", "Old Mill", "Harbor", "Riverside"),
                quickly(() -> riverton().fastestRoute("Depot", "Riverside")),
                "From Depot the fastest route back is [Depot, Uptown, Old Mill, Harbor, Riverside]");
    }

    @Test
    @DisplayName("When two routes take the same minutes, the queue's A-to-Z order and a strict < decide")
    void tiesFollowTheRule() {
        RideTimes map = new RideTimes();
        map.addLane("Riverside", "Old Mill", 2);
        map.addLane("Riverside", "Harbor", 2);
        map.addLane("Old Mill", "Uptown", 3);
        map.addLane("Harbor", "Uptown", 3);
        assertEquals(5, quickly(() -> map.fastestMinutes("Riverside", "Uptown")),
                "Both routes to Uptown take 5 minutes");
        assertEquals(List.of("Riverside", "Harbor", "Uptown"), quickly(() -> map.fastestRoute("Riverside", "Uptown")),
                "Harbor and Old Mill are both 2 minutes away. Harbor comes first A to Z, so it is polled first and sets "
                        + "Uptown's parent. Old Mill's equal 5 is not strictly smaller, so it must not replace it");
    }

    @Test
    @DisplayName("Unreachable or unknown stations give -1 and an empty route")
    void unreachableStations() {
        RideTimes map = new RideTimes();
        map.addLane("Riverside", "Harbor", 2);
        map.addLane("Uptown", "Depot", 5);
        assertEquals(-1, quickly(() -> map.fastestMinutes("Riverside", "Depot")),
                "No lanes join Riverside to Depot, so fastestMinutes should return -1");
        assertEquals(List.of(), quickly(() -> map.fastestRoute("Riverside", "Depot")),
                "No lanes join Riverside to Depot, so fastestRoute should return an empty list");
        assertEquals(-1, quickly(() -> map.fastestMinutes("Airport", "Harbor")),
                "Airport has no lanes at all, so fastestMinutes(\"Airport\", \"Harbor\") should be -1, not a crash");
        assertEquals(-1, quickly(() -> map.fastestMinutes("Harbor", "Airport")),
                "Airport is not on the map, so fastestMinutes(\"Harbor\", \"Airport\") should be -1");
    }

    @Test
    @DisplayName("From a station to itself takes 0 minutes and the route is just that station, even off the map")
    void sameStation() {
        RideTimes map = riverton();
        assertEquals(0, quickly(() -> map.fastestMinutes("Harbor", "Harbor")),
                "Harbor to Harbor needs no riding, so 0 minutes");
        assertEquals(List.of("Harbor"), quickly(() -> map.fastestRoute("Harbor", "Harbor")),
                "Harbor to Harbor is the route [Harbor]");
        assertEquals(0, quickly(() -> map.fastestMinutes("Airport", "Airport")),
                "Airport is not on the map, but from a station to itself is always 0 minutes");
        assertEquals(List.of("Airport"), quickly(() -> map.fastestRoute("Airport", "Airport")),
                "From a station to itself the route is just that station, even one not on the map: [Airport]");
    }

    @Test
    @DisplayName("When two lanes join the same stations, the faster lane counts")
    void parallelLanes() {
        RideTimes map = new RideTimes();
        map.addLane("Riverside", "Harbor", 9);
        map.addLane("Riverside", "Harbor", 4);
        assertEquals(4, quickly(() -> map.fastestMinutes("Riverside", "Harbor")),
                "There is a 9-minute lane and a 4-minute lane between Riverside and Harbor, so the answer is 4");
        assertEquals(List.of("Harbor", "Riverside"), quickly(() -> map.fastestRoute("Harbor", "Riverside")),
                "The route from Harbor to Riverside is [Harbor, Riverside]");
    }

    @Test
    @DisplayName("A city of 30,000 docks is answered quickly with a PriorityQueue")
    void bigCityIsFast() {
        RideTimes map = new RideTimes();
        for (int i = 0; i + 1 < 30_000; i++) {
            map.addLane("Dock-" + i, "Dock-" + (i + 1), 1);
        }
        map.addLane("Dock-0", "Dock-29999", 5);
        assertEquals(1004, fast(() -> map.fastestMinutes("Dock-0", "Dock-29000")),
                "Dock-0 to Dock-29000 is fastest through the 5-minute shortcut to Dock-29999, then 999 one-minute lanes back: 1004");
        assertEquals(15000, fast(() -> map.fastestMinutes("Dock-0", "Dock-15000")),
                "Dock-0 to Dock-15000 is 15000 one-minute lanes; the shortcut way would take 15004");
    }
}
