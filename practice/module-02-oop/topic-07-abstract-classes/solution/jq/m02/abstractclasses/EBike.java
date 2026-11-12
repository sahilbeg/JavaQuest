package jq.m02.abstractclasses;

public class EBike extends Vehicle {

    public EBike(String id) {
        super(id);
    }

    @Override
    public double pricePerMinute() {
        return 0.30;
    }

    @Override
    public String kind() {
        return "ebike";
    }
}
