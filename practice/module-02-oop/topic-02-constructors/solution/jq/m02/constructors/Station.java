package jq.m02.constructors;

public class Station {
    public String name;
    public int docks;

    public Station(String name) {
        this(name, 12);
    }

    public Station(String name, int docks) {
        this.name = name;
        this.docks = docks;
    }

    public int freeDocks(int bikesDocked) {
        return docks - bikesDocked;
    }
}
