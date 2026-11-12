package jq.m03.lambdas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RideSorts {

    public static List<Ride> byMinutes(List<Ride> rides) {
        List<Ride> sorted = new ArrayList<>(rides);
        sorted.sort(Comparator.comparing(Ride::minutes));
        return sorted;
    }

    public static List<Ride> byStationThenMinutes(List<Ride> rides) {
        List<Ride> sorted = new ArrayList<>(rides);
        sorted.sort(Comparator.comparing(Ride::station).thenComparing(Ride::minutes));
        return sorted;
    }

    public static void removeShort(List<Ride> rides, int under) {
        rides.removeIf(r -> r.minutes() < under);
    }

    public static List<String> upperIds(List<Ride> rides) {
        List<String> ids = new ArrayList<>();
        rides.forEach(r -> ids.add(r.bikeId().toUpperCase()));
        return ids;
    }
}
