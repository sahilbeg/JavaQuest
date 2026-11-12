package jq.m02.interfaces;

public class Scooter implements Chargeable {
    private final String id;
    private int battery;

    public Scooter(String id, int battery) {
        this.id = id;
        this.battery = clamp(battery);
    }

    public String getId() {
        return id;
    }

    @Override
    public int batteryLevel() {
        return battery;
    }

    @Override
    public void charge(int percent) {
        battery = clamp(battery + percent);
    }

    private static int clamp(int value) {
        if (value < 0) {
            return 0;
        }
        if (value > 100) {
            return 100;
        }
        return value;
    }
}
