package jq.m02.inheritance;

public class EBike extends Bike {

    private int battery;

    public EBike(String id, int battery) {
        super(id);
        // TODO 1: store the battery parameter in the battery field
    }

    public int getBattery() {
        return battery;
    }

    // TODO 2: override pricePerMinute() with @Override so it returns 0.30
}
