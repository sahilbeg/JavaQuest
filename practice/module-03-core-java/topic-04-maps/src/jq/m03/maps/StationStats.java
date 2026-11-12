package jq.m03.maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StationStats {

    public static TreeMap<String, Integer> minutesPerStation(List<Ride> rides) {
        TreeMap<String, Integer> totals = new TreeMap<>();
        // TODO 1: for each ride, add ride.minutes() to its station's total with getOrDefault and put
        return totals;
    }

    public static Map<String, Integer> ridesPerType(List<Ride> rides) {
        Map<String, Integer> counts = new HashMap<>();
        // TODO 2: for each ride, add 1 to the count for ride.type()
        return counts;
    }

    public static String busiestStation(List<Ride> rides) {
        // TODO 3: loop over minutesPerStation(rides).entrySet() and keep the station with the most minutes
        //         (replace it only when strictly bigger, so ties go to the alphabetically first station)
        return null;
    }

    public static String report(List<Ride> rides) {
        // TODO 4: build one line "Harbor: 45 min" per station, alphabetically, joined with "\n"
        return "";
    }

    public static void main(String[] args) {
        List<Ride> rides = List.of(
                new Ride("B-1042", "Harbor", "ebike", 25),
                new Ride("B-2001", "Uptown", "classic", 12),
                new Ride("B-1042", "Harbor", "ebike", 20),
                new Ride("B-3300", "Riverside", "cargo", 30));
        System.out.println(report(rides));
        System.out.println("Busiest: " + busiestStation(rides));
    }
}
