package jq.m03.streamsadvanced;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DashboardTest {

    private static final List<Ride> RIDES = List.of(
            new Ride("B-1042", "Riverside", "ebike", 25),
            new Ride("B-2001", "Harbor", "classic", 10),
            new Ride("B-1042", "Riverside", "ebike", 40),
            new Ride("B-3300", "Old Mill", "cargo", 15),
            new Ride("B-2001", "Riverside", "classic", 35));

    @Test
    @DisplayName("ridesPerStation counts the rides that started at each station")
    void ridesPerStation() {
        assertEquals(Map.of("Riverside", 3L, "Harbor", 1L, "Old Mill", 1L), Dashboard.ridesPerStation(RIDES),
                "Riverside has 3 rides, Harbor 1 and Old Mill 1. Use groupingBy(Ride::station, Collectors.counting())");
    }

    @Test
    @DisplayName("minutesPerType adds up the minutes for each bike type")
    void minutesPerType() {
        assertEquals(Map.of("ebike", 65, "classic", 45, "cargo", 15), Dashboard.minutesPerType(RIDES),
                "ebike should be 25 + 40 = 65, classic 10 + 35 = 45, cargo 15. Use groupingBy with summingInt(Ride::minutes)");
    }

    @Test
    @DisplayName("longOrShort puts rides over the limit under true and the rest under false")
    void longOrShort() {
        Map<Boolean, List<Ride>> split = Dashboard.longOrShort(RIDES, 30);
        assertEquals(List.of(RIDES.get(2), RIDES.get(4)), split.get(true),
                "With limit 30, the 40 and 35 minute rides should be under true, in their original order");
        assertEquals(List.of(RIDES.get(0), RIDES.get(1), RIDES.get(3)), split.get(false),
                "With limit 30, the 25, 10 and 15 minute rides should be under false");
    }

    @Test
    @DisplayName("allBikes lists every bike from every station log once, sorted")
    void allBikes() {
        List<List<Ride>> logs = List.of(
                List.of(RIDES.get(0), RIDES.get(4)),
                List.of(RIDES.get(1)),
                List.of(RIDES.get(3), RIDES.get(2)));
        assertEquals(List.of("B-1042", "B-2001", "B-3300"), Dashboard.allBikes(logs),
                "Flatten the logs with flatMap(List::stream), then map to bikeId, distinct() and sorted()");
    }

    @Test
    @DisplayName("summary joins each station name once in alphabetical order")
    void summary() {
        assertEquals("Harbor, Old Mill, Riverside", Dashboard.summary(RIDES),
                "summary should be \"Harbor, Old Mill, Riverside\": distinct, sorted, then Collectors.joining(\", \")");
    }
}
