package jq.m02.abstractclasses;

public abstract class Vehicle {
    private final String id;

    public Vehicle(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract double pricePerMinute();

    public abstract String kind();

    public double fare(int minutes) {
        // TODO 1: return the unlock fee 1.00 plus minutes times pricePerMinute()
        return 0.0;
    }

    public String label() {
        // TODO 2: return kind(), then a space, then the id, for example "ebike B-1042"
        return "";
    }
}
