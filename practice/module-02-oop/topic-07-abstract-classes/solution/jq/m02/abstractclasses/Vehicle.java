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
        return 1.00 + minutes * pricePerMinute();
    }

    public String label() {
        return kind() + " " + id;
    }
}
