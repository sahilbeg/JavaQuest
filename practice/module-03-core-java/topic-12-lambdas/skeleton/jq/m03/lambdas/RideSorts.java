package jq.m03.lambdas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RideSorts {

    public static List<Ride> byMinutes(List<Ride> rides) {
        List<Ride> sorted = new ArrayList<>(rides);
        // TODO 1: sort the copy, shortest ride first, with sorted.sort(Comparator.comparing(Ride::minutes))
        return new ArrayList<>();
    }

    public static List<Ride> byStationThenMinutes(List<Ride> rides) {
        List<Ride> sorted = new ArrayList<>(rides);
        // TODO 2: sort the copy by station name, then by minutes, using Comparator.comparing(...).thenComparing(...)
        return new ArrayList<>();
    }

    public static void removeShort(List<Ride> rides, int under) {
        // TODO 3: remove every ride with fewer minutes than under, using rides.removeIf(r -> ...)
    }

    public static List<String> upperIds(List<Ride> rides) {
        List<String> ids = new ArrayList<>();
        // TODO 4: add each ride's bike id in upper case to ids, using a loop or rides.forEach(...)
        return ids;
    }
}
