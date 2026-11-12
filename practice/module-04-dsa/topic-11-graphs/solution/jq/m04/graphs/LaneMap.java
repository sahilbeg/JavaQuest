package jq.m04.graphs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class LaneMap {

    private final Map<String, List<String>> lanes = new LinkedHashMap<>();

    public void addLane(String a, String b) {
        lanes.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
        lanes.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
    }

    public List<String> neighbours(String station) {
        return lanes.getOrDefault(station, new ArrayList<>());
    }

    public List<String> reachable(String start) {
        Set<String> visited = new HashSet<>();
        visit(start, visited);
        List<String> result = new ArrayList<>(visited);
        Collections.sort(result);
        return result;
    }

    private void visit(String station, Set<String> visited) {
        if (visited.contains(station)) {
            return;
        }
        visited.add(station);
        for (String next : neighbours(station)) {
            visit(next, visited);
        }
    }

    public List<String> fewestStops(String from, String to) {
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        visited.add(from);
        queue.offer(from);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(to)) {
                List<String> route = new ArrayList<>();
                String step = to;
                while (step != null) {
                    route.add(step);
                    step = parent.get(step);
                }
                Collections.reverse(route);
                return route;
            }
            for (String next : neighbours(current)) {
                if (!visited.contains(next)) {
                    visited.add(next);
                    parent.put(next, current);
                    queue.offer(next);
                }
            }
        }
        return new ArrayList<>();
    }

    public static void main(String[] args) {
        LaneMap map = new LaneMap();
        map.addLane("Riverside", "Old Mill");
        map.addLane("Riverside", "Harbor");
        map.addLane("Old Mill", "Uptown");
        map.addLane("Harbor", "Uptown");
        map.addLane("Uptown", "Depot");
        System.out.println(map.fewestStops("Riverside", "Depot"));
    }
}
