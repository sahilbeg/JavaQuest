package jq.m03.incident;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthlyReport {

    public static int removeTestRides(List<Ride> rides) {
        // TODO bronze: this loop crashes with ConcurrentModificationException
        int removed = 0;
        for (Ride ride : rides) {
            if (ride.bikeId().startsWith("TEST-")) {
                rides.remove(ride);
                removed++;
            }
        }
        return removed;
    }

    public static Map<String, Double> averageMinutesPerStation(List<Ride> rides) {
        Map<String, Integer> total = new HashMap<>();
        Map<String, Integer> count = new HashMap<>();
        for (Ride ride : rides) {
            total.put(ride.station(), total.getOrDefault(ride.station(), 0) + ride.minutes());
            count.put(ride.station(), count.getOrDefault(ride.station(), 0) + 1);
        }
        Map<String, Double> average = new HashMap<>();
        for (String station : total.keySet()) {
            // TODO silver: Harbor's 45 minutes over 2 rides comes out as 22.0
            average.put(station, (double) (total.get(station) / count.get(station)));
        }
        return average;
    }

    public static List<String> topStations(List<Ride> rides, int n) {
        Map<String, Integer> total = new HashMap<>();
        for (Ride ride : rides) {
            total.put(ride.station(), total.getOrDefault(ride.station(), 0) + ride.minutes());
        }
        // TODO gold: the order of a HashMap's keys is not the busiest-first order
        List<String> stations = new ArrayList<>(total.keySet());
        return stations.subList(0, Math.min(n, stations.size()));
    }

    public static void main(String[] args) {
        List<Ride> rides = new ArrayList<>(List.of(
                new Ride("B-1042", "Harbor", "ebike", 20),
                new Ride("TEST-1", "Harbor", "classic", 5),
                new Ride("B-2001", "Harbor", "classic", 25),
                new Ride("B-3001", "Uptown", "cargo", 60)));
        System.out.println("Removed: " + removeTestRides(rides));
        System.out.println("Averages: " + averageMinutesPerStation(rides));
        System.out.println("Top: " + topStations(rides, 2));
    }
}
