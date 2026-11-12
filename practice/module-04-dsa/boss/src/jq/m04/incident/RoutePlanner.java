package jq.m04.incident;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RoutePlanner {

    private final Map<String, List<String>> lanes = new LinkedHashMap<>();
    private final Map<String, Integer> freeDocks;

    public RoutePlanner(Map<String, Integer> freeDocks) {
        this.freeDocks = freeDocks;
    }

    public void addLane(String a, String b) {
        lanes.computeIfAbsent(a, key -> new ArrayList<>()).add(b);
        lanes.computeIfAbsent(b, key -> new ArrayList<>()).add(a);
    }

    public Optional<String> nearestFree(String from) {
        List<String> level = List.of(from);
        Set<String> seen = new HashSet<>(level);
        while (!level.isEmpty()) {
            for (String station : level) {
                // TODO bronze: a station with 0 free docks is full
                // TODO silver: a station missing from freeDocks crashes here
                if (freeDocks.get(station) >= 0) {
                    // TODO gold: compare every free station in this level before choosing
                    return Optional.of(station);
                }
            }
            List<String> next = new ArrayList<>();
            for (String station : level) {
                for (String neighbour : lanes.getOrDefault(station, List.of())) {
                    if (seen.add(neighbour)) {
                        next.add(neighbour);
                    }
                }
            }
            level = next;
        }
        return Optional.empty();
    }

    public static void main(String[] args) {
        RoutePlanner planner = new RoutePlanner(Map.of("Harbor", 0, "Uptown", 0, "Riverside", 3, "Old Mill", 5));
        planner.addLane("Harbor", "Uptown");
        planner.addLane("Harbor", "Riverside");
        planner.addLane("Uptown", "Depot");
        planner.addLane("Riverside", "Old Mill");
        System.out.println("From Harbor: " + planner.nearestFree("Harbor").orElse("nowhere"));
    }
}
