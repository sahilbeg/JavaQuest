package jq.m02.inheritance;

public class Bike {

    private String id;

    public Bike(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public double pricePerMinute() {
        return 0.15;
    }

    public double fare(int minutes) {
        return 1.00 + minutes * pricePerMinute();
    }

    public String describe() {
        return "Bike " + id;
    }
}
