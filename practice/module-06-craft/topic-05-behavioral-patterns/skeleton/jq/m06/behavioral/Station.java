package jq.m06.behavioral;

public class Station {
    private final String name;
    private int docks;

    public Station(String name, int docks) {
        this.name = name;
        this.docks = docks;
    }

    public String name() {
        return name;
    }

    public int docks() {
        return docks;
    }

    public void setDocks(int docks) {
        this.docks = docks;
    }
}
