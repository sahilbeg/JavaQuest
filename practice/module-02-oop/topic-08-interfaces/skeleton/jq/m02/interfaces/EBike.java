package jq.m02.interfaces;

public class EBike implements Chargeable {
    private final String id;
    private int battery;

    public EBike(String id, int battery) {
        this.id = id;
        // TODO 2: store battery, kept between 0 and 100
    }

    public String getId() {
        return id;
    }

    @Override
    public int batteryLevel() {
        // TODO 3: return the battery field
        return 0;
    }

    @Override
    public void charge(int percent) {
        // TODO 4: add percent to battery, but never go above 100
    }
}
