package jq.m02.equalshashcode;

import java.util.Objects;

public class Bike {
    private final String id;
    private final String type;

    public Bike(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        // TODO 1: return text like Bike[B-1042, ebike] built from id and type
        return "";
    }

    @Override
    public boolean equals(Object other) {
        // TODO 2: return true when other is a Bike with the same id (use the instanceof pattern and equals on the ids)
        return false;
    }

    @Override
    public int hashCode() {
        // TODO 3: return a hash code built from id only, for example Objects.hash(id)
        return super.hashCode();
    }
}
