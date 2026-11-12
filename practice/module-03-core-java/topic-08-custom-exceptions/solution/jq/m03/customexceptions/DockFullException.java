package jq.m03.customexceptions;

public class DockFullException extends Exception {

    private final String station;

    public DockFullException(String station) {
        super("Station full: " + station);
        this.station = station;
    }

    public String getStation() {
        return station;
    }
}
