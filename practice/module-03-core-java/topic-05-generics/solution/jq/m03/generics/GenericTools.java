package jq.m03.generics;

import java.util.List;

public class GenericTools {

    public static <T> T lastOf(List<T> items) {
        if (items.isEmpty()) {
            return null;
        }
        return items.get(items.size() - 1);
    }

    public static <T extends Comparable<T>> T maxOf(List<T> items) {
        if (items.isEmpty()) {
            return null;
        }
        T best = items.get(0);
        for (T item : items) {
            if (item.compareTo(best) > 0) {
                best = item;
            }
        }
        return best;
    }

    public static <T extends Number> double sum(List<T> numbers) {
        double total = 0;
        for (T n : numbers) {
            total += n.doubleValue();
        }
        return total;
    }
}
