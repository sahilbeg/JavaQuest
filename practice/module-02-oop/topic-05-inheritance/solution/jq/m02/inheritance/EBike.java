package jq.m02.inheritance;

public class EBike extends Bike {

    private int battery;

    public EBike(String id, int battery) {
        super(id);
        this.battery = battery;
    }

    public int getBattery() {
        return battery;
    }

    @Override
    public double pricePerMinute() {
        return 0.30;
    }
}
