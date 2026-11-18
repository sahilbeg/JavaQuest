package jq.m04.dijkstra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class RideTimes {

    /** One lane leaving a station: where it goes and how many minutes it takes. */
    record Lane(String to, int minutes) {
    }

    /** One entry in the priority queue: a station and the minutes of the route that reached it. */
    record Visit(String station, int minutes) {
    }

    private final Map<String, List<Lane>> lanes = new HashMap<>();

    public void addLane(String a, String b, int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("minutes cannot be negative: " + minutes);
        }
        lanes.computeIfAbsent(a, k -> new ArrayList<>()).add(new Lane(b, minutes));
        lanes.computeIfAbsent(b, k -> new ArrayList<>()).add(new Lane(a, minutes));
    }

    private void search(String from, Map<String, Integer> best, Map<String, String> parent) {
        PriorityQueue<Visit> queue = new PriorityQueue<>(
                Comparator.comparingInt(Visit::minutes).thenComparing(Visit::station));
        best.put(from, 0);
        queue.offer(new Visit(from, 0));
        while (!queue.isEmpty()) {
            Visit visit = queue.poll();
            if (visit.minutes() > best.get(visit.station())) {
                continue; // stale: a faster route to this station was found after this entry went in
            }
            for (Lane lane : lanes.getOrDefault(visit.station(), List.of())) {
                int minutes = visit.minutes() + lane.minutes();
                Integer known = best.get(lane.to());
                if (known == null || minutes < known) {
                    best.put(lane.to(), minutes);
                    parent.put(lane.to(), visit.station());
                    queue.offer(new Visit(lane.to(), minutes));
                }
            }
        }
    }

    public int fastestMinutes(String from, String to) {
        Map<String, Integer> best = new HashMap<>();
        search(from, best, new HashMap<>());
        return best.getOrDefault(to, -1);
    }

    public List<String> fastestRoute(String from, String to) {
        Map<String, Integer> best = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        search(from, best, parent);
        List<String> route = new ArrayList<>();
        if (!best.containsKey(to)) {
            return route;
        }
        for (String step = to; step != null; step = parent.get(step)) {
            route.add(step);
        }
        Collections.reverse(route);
        return route;
    }

    public static void main(String[] args) {
        RideTimes map = new RideTimes();
        map.addLane("Riverside", "Old Mill", 4);
        map.addLane("Riverside", "Harbor", 2);
        map.addLane("Harbor", "Old Mill", 1);
        map.addLane("Old Mill", "Uptown", 3);
        map.addLane("Harbor", "Uptown", 7);
        map.addLane("Uptown", "Depot", 5);
        map.addLane("Harbor", "Depot", 12);
        System.out.println(map.fastestMinutes("Riverside", "Depot") + " minutes");
        System.out.println(map.fastestRoute("Riverside", "Depot"));
    }
}
