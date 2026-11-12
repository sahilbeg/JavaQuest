package jq.m05.reflection;

public class Bike {

    private static int bikesBuilt = 0;

    private String id;
    private int battery;

    public Bike() {
        this("B-0000", 100);
    }

    public Bike(String id, int battery) {
        this.id = id;
        this.battery = battery;
        bikesBuilt++;
    }

    public String getId() {
        return id;
    }

    public int getBattery() {
        return battery;
    }

    public boolean isLow() {
        return battery < 20;
    }

    private String secretCode() {
        return "unlock-" + id;
    }
}
