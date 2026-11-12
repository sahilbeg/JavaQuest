package jq.m04.feature;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class TruckPlanner {

    record Need(String name, int amount) {
    }

    public static List<Trip> plan(List<Station> stations, int capacity) {
        // TODO 2: fill two priority queues, then plan trips until one queue is empty
        return new ArrayList<>();
    }

    public static String report(List<Trip> trips) {
        // TODO 3: one line per trip, then "Moved N bikes in T trips"
        return "";
    }

    public static void main(String[] args) {
        List<Station> stations = List.of(
                new Station("Riverside", 12, 6),
                new Station("Harbor", 2, 9),
                new Station("Uptown", 10, 6),
                new Station("Old Mill", 5, 8));
        System.out.println(report(plan(stations, 5)));
    }
}
