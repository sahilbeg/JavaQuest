package jq.m02.abstractclasses;

public class ClassicBike extends Vehicle {

    public ClassicBike(String id) {
        super(id);
    }

    @Override
    public double pricePerMinute() {
        return 0.15;
    }

    @Override
    public String kind() {
        return "classic";
    }
}
