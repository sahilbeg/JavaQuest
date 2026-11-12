package jq.m03.streamsbasics;

import java.util.List;

public class StreamReports {

    public static List<Ride> eBikeRides(List<Ride> rides) {
        // TODO 1: stream the rides, filter to type "ebike", and collect them with toList()
        return List.of();
    }

    public static int totalMinutes(List<Ride> rides) {
        // TODO 2: stream the rides, mapToInt to the minutes, and sum them
        return 0;
    }

    public static List<String> stationsVisited(List<Ride> rides) {
        // TODO 3: map each ride to its station, then use distinct and sorted before toList()
        return List.of();
    }

    public static List<String> topThreeLongest(List<Ride> rides) {
        // TODO 4: sort the rides longest first, limit to 3, and map each one to its bike id
        return List.of();
    }

    public static double averageMinutes(List<Ride> rides) {
        // TODO 5: mapToInt to the minutes, then average().orElse(0.0)
        return -1.0;
    }
}
