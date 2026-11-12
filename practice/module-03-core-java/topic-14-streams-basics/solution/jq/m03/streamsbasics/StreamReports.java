package jq.m03.streamsbasics;

import java.util.List;

public class StreamReports {

    public static List<Ride> eBikeRides(List<Ride> rides) {
        return rides.stream()
                .filter(ride -> ride.type().equals("ebike"))
                .toList();
    }

    public static int totalMinutes(List<Ride> rides) {
        return rides.stream()
                .mapToInt(Ride::minutes)
                .sum();
    }

    public static List<String> stationsVisited(List<Ride> rides) {
        return rides.stream()
                .map(Ride::station)
                .distinct()
                .sorted()
                .toList();
    }

    public static List<String> topThreeLongest(List<Ride> rides) {
        return rides.stream()
                .sorted((a, b) -> Integer.compare(b.minutes(), a.minutes()))
                .limit(3)
                .map(Ride::bikeId)
                .toList();
    }

    public static double averageMinutes(List<Ride> rides) {
        return rides.stream()
                .mapToInt(Ride::minutes)
                .average()
                .orElse(0.0);
    }
}
