package jq.m02.interfaces;

public class Scooter implements Chargeable {
    private final String id;
    private int battery;

    public Scooter(String id, int battery) {
        this.id = id;
        // TODO 5: store battery, kept between 0 and 100
    }

    public String getId() {
        return id;
    }

    @Override
    public int batteryLevel() {
        // TODO 6: return the battery field
        return 0;
    }

    @Override
    public void charge(int percent) {
        // TODO 7: add percent to battery, but never go above 100
    }
}
