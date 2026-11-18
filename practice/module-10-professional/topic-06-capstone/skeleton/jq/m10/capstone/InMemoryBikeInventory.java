package jq.m10.capstone;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Adapter (driven side): the fleet, in a map. Bikes of every city can live in one inventory. */
public class InMemoryBikeInventory implements BikeInventory {

    private final Map<String, Bike> bikes = new LinkedHashMap<>();

    /** Adds a bike, or replaces the one with the same id. Returns this, so calls can be chained. */
    public synchronized InMemoryBikeInventory add(Bike bike) {
        bikes.put(bike.id(), bike);
        return this;
    }

    @Override
    public synchronized Optional<Bike> findById(String bikeId) {
        return Optional.ofNullable(bikes.get(bikeId));
    }

    @Override
    public synchronized void markRented(String bikeId) {
        Bike bike = bikes.get(bikeId);
        bikes.put(bikeId, new Bike(bike.id(), bike.cityId(), bike.type(), null, BikeStatus.RENTED));
    }

    @Override
    public synchronized void dock(String bikeId, String stationId) {
        Bike bike = bikes.get(bikeId);
        bikes.put(bikeId, new Bike(bike.id(), bike.cityId(), bike.type(), stationId, BikeStatus.AVAILABLE));
    }

    @Override
    public synchronized List<Bike> dockedAt(String stationId) {
        return bikes.values().stream()
                .filter(b -> b.status() == BikeStatus.AVAILABLE && stationId.equals(b.stationId()))
                .toList();
    }
}
