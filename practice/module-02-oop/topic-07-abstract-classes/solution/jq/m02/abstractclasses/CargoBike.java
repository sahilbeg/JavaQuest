package jq.m02.abstractclasses;

public class CargoBike extends Vehicle {

    public CargoBike(String id) {
        super(id);
    }

    @Override
    public double pricePerMinute() {
        return 0.25;
    }

    @Override
    public String kind() {
        return "cargo";
    }
}
