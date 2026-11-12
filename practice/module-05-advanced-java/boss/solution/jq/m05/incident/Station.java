package jq.m05.incident;

public class Station {

    private final String name;
    private int bikes;

    public Station(String name, int bikes) {
        this.name = name;
        this.bikes = bikes;
    }

    public String name() {
        return name;
    }

    public int bikes() {
        return bikes;
    }

    void take() {
        bikes--;
    }

    void put() {
        bikes++;
    }
}
