package jq.m03.sets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class BikeUsage {

    public static int distinctBikes(List<String> bikeIds) {
        Set<String> unique = new HashSet<>(bikeIds);
        return unique.size();
    }

    public static List<String> sortedIds(List<String> bikeIds) {
        Set<String> sorted = new TreeSet<>(bikeIds);
        return new ArrayList<>(sorted);
    }

    public static List<String> usedAtBoth(List<String> stationA, List<String> stationB) {
        Set<String> both = new TreeSet<>(stationA);
        both.retainAll(new HashSet<>(stationB));
        return new ArrayList<>(both);
    }

    public static String firstDuplicate(List<String> bikeIds) {
        Set<String> seen = new HashSet<>();
        for (String id : bikeIds) {
            if (!seen.add(id)) {
                return id;
            }
        }
        return null;
    }
}
