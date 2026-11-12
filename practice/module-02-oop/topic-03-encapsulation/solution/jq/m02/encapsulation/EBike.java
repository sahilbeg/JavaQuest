package jq.m02.encapsulation;

public class EBike {

    private final String id;
    private int battery;

    public EBike(String id, int battery) {
        this.id = id;
        setBattery(battery);
    }

    public String getId() {
        return id;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        if (battery < 0) {
            battery = 0;
        }
        if (battery > 100) {
            battery = 100;
        }
        this.battery = battery;
    }

    public void charge(int percent) {
        setBattery(battery + percent);
    }

    public void ride(int minutes) {
        setBattery(battery - minutes / 2);
    }

    public boolean canUnlock() {
        return battery >= 20;
    }
}
