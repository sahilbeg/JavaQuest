package jq.m03.sets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class BikeUsage {

    public static int distinctBikes(List<String> bikeIds) {
        // TODO 1: put the ids in a HashSet and return its size
        return 0;
    }

    public static List<String> sortedIds(List<String> bikeIds) {
        // TODO 2: put the ids in a TreeSet, then return new ArrayList<>(thatSet)
        return new ArrayList<>();
    }

    public static List<String> usedAtBoth(List<String> stationA, List<String> stationB) {
        // TODO 3: copy stationA into a TreeSet, keep only ids in stationB with retainAll, return it as a list
        return new ArrayList<>();
    }

    public static String firstDuplicate(List<String> bikeIds) {
        Set<String> seen = new HashSet<>();
        // TODO 4: loop over the ids; when seen.add(id) returns false, return id
        return null;
    }
}
