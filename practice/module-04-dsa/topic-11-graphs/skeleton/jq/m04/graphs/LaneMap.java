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
        // TODO 1: add b to a's neighbour list and a to b's neighbour list.
        //         Create an empty ArrayList for a station the first time you see it.
    }

    public List<String> neighbours(String station) {
        // TODO 2: return the neighbour list of station, or an empty list for an unknown station.
        return new ArrayList<>();
    }

    public List<String> reachable(String start) {
        // TODO 3: visit every station you can reach from start (DFS or BFS) with a visited set.
        //         Return the visited stations as a list sorted A to Z. start is in the list.
        return new ArrayList<>();
    }

    public List<String> fewestStops(String from, String to) {
        // TODO 4: run BFS from "from" with a queue, a visited set and a parent map.
        //         When you take "to" out of the queue, walk the parents back to "from",
        //         then reverse the list. Return an empty list when "to" is never reached.
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
