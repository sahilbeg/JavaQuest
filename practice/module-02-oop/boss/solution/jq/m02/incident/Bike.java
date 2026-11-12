package jq.m02.incident;

public class Bike {

    protected double price = 0.15;
    private final String id;

    public Bike(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String type() {
        return "classic";
    }

    public double pricePerMinute() {
        return price;
    }

    public double fare(int minutes) {
        return 1.00 + minutes * pricePerMinute();
    }
}
