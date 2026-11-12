package jq.m03.customexceptions;

public class DockFullException extends Exception {

    private final String station;

    public DockFullException(String station) {
        // TODO 1: pass the message "Station full: " + station to super(...)
        this.station = station;
    }

    public String getStation() {
        return station;
    }
}
