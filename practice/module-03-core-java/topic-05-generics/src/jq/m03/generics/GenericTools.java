package jq.m03.generics;

import java.util.List;

public class GenericTools {

    public static <T> T lastOf(List<T> items) {
        // TODO 7: return the last item, or null when the list is empty
        return null;
    }

    public static <T extends Comparable<T>> T maxOf(List<T> items) {
        // TODO 8: return the biggest item using compareTo, or null when the list is empty
        return null;
    }

    public static <T extends Number> double sum(List<T> numbers) {
        // TODO 9: add up n.doubleValue() for every number
        return 0.0;
    }
}
