package jq.m03.streamsbasics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StreamReportsTest {

    private final List<Ride> rides = List.of(
            new Ride("B-1042", "Riverside", "ebike", 25),
            new Ride("B-2001", "Harbor", "classic", 10),
            new Ride("B-3300", "Uptown", "cargo", 40),
            new Ride("B-1042", "Harbor", "ebike", 15),
            new Ride("B-4100", "Old Mill", "classic", 30));

    @Test
    @DisplayName("eBikeRides keeps only the e-bike rides, in their original order")
    void eBikeRidesOnly() {
        assertEquals(List.of(rides.get(0), rides.get(3)), StreamReports.eBikeRides(rides),
                "eBikeRides should return the two \"ebike\" rides: the 25 minute one, then the 15 minute one");
    }

    @Test
    @DisplayName("totalMinutes adds up every ride")
    void totalMinutesSum() {
        assertEquals(120, StreamReports.totalMinutes(rides),
                "totalMinutes should be 25 + 10 + 40 + 15 + 30 = 120");
        assertEquals(0, StreamReports.totalMinutes(List.of()),
                "totalMinutes of an empty list should be 0");
    }

    @Test
    @DisplayName("stationsVisited lists each station once, in alphabetical order")
    void stationsSortedDistinct() {
        assertEquals(List.of("Harbor", "Old Mill", "Riverside", "Uptown"), StreamReports.stationsVisited(rides),
                "Harbor appears twice in the rides but should appear once, and the list should be sorted A to Z");
    }

    @Test
    @DisplayName("topThreeLongest gives the bike ids of the three longest rides")
    void topThree() {
        assertEquals(List.of("B-3300", "B-4100", "B-1042"), StreamReports.topThreeLongest(rides),
                "The longest rides are 40 (B-3300), 30 (B-4100) and 25 (B-1042) minutes, longest first");
        assertEquals(List.of("B-2001"), StreamReports.topThreeLongest(List.of(rides.get(1))),
                "With only one ride, topThreeLongest should return just that bike id");
    }

    @Test
    @DisplayName("averageMinutes works out the mean, and 0.0 for no rides")
    void average() {
        assertEquals(24.0, StreamReports.averageMinutes(rides), 0.001,
                "averageMinutes should be 120 minutes / 5 rides = 24.0");
        assertEquals(0.0, StreamReports.averageMinutes(List.of()), 0.001,
                "averageMinutes of an empty list should be 0.0: use average().orElse(0.0)");
    }
}
