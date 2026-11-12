package jq.m03.optional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BikeLookup {

    private final Map<String, String> docks;

    public BikeLookup(Map<String, String> docks) {
        this.docks = docks;
    }

    public Optional<String> findStation(String bikeId) {
        // TODO 1: wrap docks.get(bikeId) with Optional.ofNullable(...)
        return Optional.empty();
    }

    public String stationOrUnknown(String bikeId) {
        // TODO 2: call findStation and return "unknown" when the box is empty
        return "";
    }

    public Optional<Ride> longestRide(List<Ride> rides) {
        // TODO 3: use rides.stream().max(...) to find the ride with the most minutes
        return Optional.empty();
    }

    public String longestRideLabel(List<Ride> rides) {
        // TODO 4: map the longest ride to "B-1042: 80 min", or return "no rides" with orElse
        return "";
    }
}
