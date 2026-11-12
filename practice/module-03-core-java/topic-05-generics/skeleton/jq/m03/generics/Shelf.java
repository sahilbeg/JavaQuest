package jq.m03.generics;

import java.util.ArrayList;
import java.util.List;

public class Shelf<T> {

    private int capacity;
    private final List<T> items = new ArrayList<>();

    public Shelf(int capacity) {
        // TODO 1: store the capacity in the field
    }

    public boolean add(T item) {
        // TODO 2: return false when the shelf is full, otherwise add the item and return true
        return false;
    }

    public T get(int index) {
        // TODO 3: return the item at this index
        return null;
    }

    public int size() {
        // TODO 4: return how many items are on the shelf
        return 0;
    }

    public boolean isFull() {
        // TODO 5: return true when size() has reached the capacity
        return false;
    }
}
