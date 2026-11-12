package jq.m03.maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StationStats {

    public static TreeMap<String, Integer> minutesPerStation(List<Ride> rides) {
        TreeMap<String, Integer> totals = new TreeMap<>();
        for (Ride ride : rides) {
            int old = totals.getOrDefault(ride.station(), 0);
            totals.put(ride.station(), old + ride.minutes());
        }
        return totals;
    }

    public static Map<String, Integer> ridesPerType(List<Ride> rides) {
        Map<String, Integer> counts = new HashMap<>();
        for (Ride ride : rides) {
            counts.put(ride.type(), counts.getOrDefault(ride.type(), 0) + 1);
        }
        return counts;
    }

    public static String busiestStation(List<Ride> rides) {
        String best = null;
        int bestMinutes = 0;
        for (Map.Entry<String, Integer> e : minutesPerStation(rides).entrySet()) {
            if (best == null || e.getValue() > bestMinutes) {
                best = e.getKey();
                bestMinutes = e.getValue();
            }
        }
        return best;
    }

    public static String report(List<Ride> rides) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : minutesPerStation(rides).entrySet()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(e.getKey()).append(": ").append(e.getValue()).append(" min");
        }
        return sb.toString();
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
