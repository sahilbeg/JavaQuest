package jq.m03.generics;

import java.util.ArrayList;
import java.util.List;

public class Shelf<T> {

    private final int capacity;
    private final List<T> items = new ArrayList<>();

    public Shelf(int capacity) {
        this.capacity = capacity;
    }

    public boolean add(T item) {
        if (isFull()) {
            return false;
        }
        items.add(item);
        return true;
    }

    public T get(int index) {
        return items.get(index);
    }

    public int size() {
        return items.size();
    }

    public boolean isFull() {
        return items.size() >= capacity;
    }
}
