package jq.m05.reflection;

public class Station {

    private String name;
    private int docks;
    private int bikesDocked;

    public Station() {
        this("Riverside", 12, 0);
    }

    public Station(String name, int docks, int bikesDocked) {
        this.name = name;
        this.docks = docks;
        this.bikesDocked = bikesDocked;
    }

    public String getName() {
        return name;
    }

    public int freeDocks() {
        return docks - bikesDocked;
    }
}
