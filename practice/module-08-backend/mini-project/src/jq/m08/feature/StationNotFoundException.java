package jq.m08.feature;

public class StationNotFoundException extends RuntimeException {

    private final String stationId;

    public StationNotFoundException(String stationId) {
        super("No station with id " + stationId);
        this.stationId = stationId;
    }

    public String getStationId() {
        return stationId;
    }
}
