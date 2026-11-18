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
        // TODO 1: throw new IllegalArgumentException when minutes is negative (0 is fine).
        //         A lane works both ways: add new Lane(b, minutes) to a's list
        //         and new Lane(a, minutes) to b's list.
    }

    private void search(String from, Map<String, Integer> best, Map<String, String> parent) {
        // TODO 2: Dijkstra from "from". Fill best (station -> fewest minutes) and parent
        //         (station -> the station just before it on the fastest route).
        //         - Make a PriorityQueue<Visit> ordered by minutes, then by station name A to Z.
        //         - best.put(from, 0) and offer new Visit(from, 0).
        //         - Poll a Visit. If its minutes are more than best.get(its station), skip it: it is stale.
        //         - For each lane out of the station (getOrDefault for a station with no lanes),
        //           work out the new total. Only when there is no best yet, or the new total is
        //           strictly smaller: update best and parent, and offer a new Visit.
    }

    public int fastestMinutes(String from, String to) {
        // TODO 3: run search from "from", then return the best minutes for "to",
        //         or -1 when "to" was never reached.
        return 0;
    }

    public List<String> fastestRoute(String from, String to) {
        // TODO 4: run search from "from". If "to" was never reached, return an empty list.
        //         Otherwise walk the parents back from "to" until null, then reverse the list.
        return new ArrayList<>();
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
