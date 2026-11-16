package jq.m03.streamsadvanced;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Dashboard {

    public static Map<String, Long> ridesPerStation(List<Ride> rides) {
        // TODO 1: group the rides by station and count each group with Collectors.counting()
        return new HashMap<>();
    }

    public static Map<String, Integer> minutesPerType(List<Ride> rides) {
        // TODO 2: group the rides by type and add up the minutes with Collectors.summingInt(...)
        return new HashMap<>();
    }

    public static Map<Boolean, List<Ride>> longOrShort(List<Ride> rides, int limit) {
        // TODO 3: use Collectors.partitioningBy so rides longer than limit go under true
        return new HashMap<>();
    }

    public static List<String> allBikes(List<List<Ride>> stationLogs) {
        // TODO 4: flatMap the logs into one stream, keep the bike ids, remove repeats and sort them
        return List.of();
    }

    public static String summary(List<Ride> rides) {
        // TODO 5: list each station once, in alphabetical order, joined with ", "
        return "";
    }
}
