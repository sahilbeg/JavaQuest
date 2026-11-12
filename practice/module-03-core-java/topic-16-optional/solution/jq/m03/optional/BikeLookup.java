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
        return Optional.ofNullable(docks.get(bikeId));
    }

    public String stationOrUnknown(String bikeId) {
        return findStation(bikeId).orElse("unknown");
    }

    public Optional<Ride> longestRide(List<Ride> rides) {
        return rides.stream()
                .max((a, b) -> Integer.compare(a.minutes(), b.minutes()));
    }

    public String longestRideLabel(List<Ride> rides) {
        return longestRide(rides)
                .map(r -> r.bikeId() + ": " + r.minutes() + " min")
                .orElse("no rides");
    }
}
