package jq.m09.performance;

import java.util.AbstractList;
import java.util.List;

/**
 * A read-only list for the checks. It counts every element read, and stops the code under test
 * when the check's time limit has passed, so slow code never keeps running in the background.
 */
final class WatchedList<T> extends AbstractList<T> {

    private final List<T> items;
    long reads;

    WatchedList(List<T> items) {
        this.items = List.copyOf(items);
    }

    @Override
    public T get(int index) {
        if (Thread.currentThread().isInterrupted()) {
            throw new IllegalStateException("stopped by the check: the report took longer than its time limit");
        }
        reads++;
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }
}
