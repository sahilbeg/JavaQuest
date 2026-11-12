package jq.m04.feature;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class TruckPlanner {

    record Need(String name, int amount) {
    }

    public static List<Trip> plan(List<Station> stations, int capacity) {
        Comparator<Need> largestFirst = Comparator.comparingInt(Need::amount).reversed().thenComparing(Need::name);
        PriorityQueue<Need> givers = new PriorityQueue<>(largestFirst);
        PriorityQueue<Need> takers = new PriorityQueue<>(largestFirst);
        for (Station station : stations) {
            if (station.surplus() > 0) {
                givers.add(new Need(station.name(), station.surplus()));
            } else if (station.surplus() < 0) {
                takers.add(new Need(station.name(), -station.surplus()));
            }
        }
        List<Trip> trips = new ArrayList<>();
        while (!givers.isEmpty() && !takers.isEmpty()) {
            Need giver = givers.poll();
            Need taker = takers.poll();
            int bikes = Math.min(Math.min(giver.amount(), taker.amount()), capacity);
            trips.add(new Trip(giver.name(), taker.name(), bikes));
            if (giver.amount() > bikes) {
                givers.add(new Need(giver.name(), giver.amount() - bikes));
            }
            if (taker.amount() > bikes) {
                takers.add(new Need(taker.name(), taker.amount() - bikes));
            }
        }
        return trips;
    }

    public static String report(List<Trip> trips) {
        StringBuilder text = new StringBuilder();
        int moved = 0;
        for (Trip trip : trips) {
            String unit = " bikes";
            if (trip.bikes() == 1) {
                unit = " bike";
            }
            text.append(trip.from()).append(" -> ").append(trip.to()).append(": ").append(trip.bikes()).append(unit).append("\n");
            moved += trip.bikes();
        }
        text.append("Moved ").append(moved).append(" bikes in ").append(trips.size()).append(" trips");
        return text.toString();
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
