package jq.m03.streamsadvanced;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Dashboard {

    public static Map<String, Long> ridesPerStation(List<Ride> rides) {
        return rides.stream()
                .collect(Collectors.groupingBy(Ride::station, Collectors.counting()));
    }

    public static Map<String, Integer> minutesPerType(List<Ride> rides) {
        return rides.stream()
                .collect(Collectors.groupingBy(Ride::type, Collectors.summingInt(Ride::minutes)));
    }

    public static Map<Boolean, List<Ride>> longOrShort(List<Ride> rides, int limit) {
        return rides.stream()
                .collect(Collectors.partitioningBy(r -> r.minutes() > limit));
    }

    public static List<String> allBikes(List<List<Ride>> stationLogs) {
        return stationLogs.stream()
                .flatMap(List::stream)
                .map(Ride::bikeId)
                .distinct()
                .sorted()
                .toList();
    }

    public static String summary(List<Ride> rides) {
        return rides.stream()
                .map(Ride::station)
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
