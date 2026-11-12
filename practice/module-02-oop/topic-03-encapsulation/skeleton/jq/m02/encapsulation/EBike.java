package jq.m02.encapsulation;

public class EBike {

    private final String id;
    private int battery;

    public EBike(String id, int battery) {
        // TODO 1: store the id in this.id, then call setBattery(battery) so the value is clamped
        this.id = "";
    }

    public String getId() {
        // TODO 2: return the id field
        return "";
    }

    public int getBattery() {
        // TODO 2: return the battery field
        return 0;
    }

    public void setBattery(int battery) {
        // TODO 3: below 0 becomes 0, above 100 becomes 100, then store it in this.battery
    }

    public void charge(int percent) {
        // TODO 4: add percent to the battery, never above 100 (setBattery can help)
    }

    public void ride(int minutes) {
        // TODO 5: use 1 percent for every 2 whole minutes (minutes / 2), never below 0
    }

    public boolean canUnlock() {
        // TODO 6: return true when the battery is at least 20
        return false;
    }
}
