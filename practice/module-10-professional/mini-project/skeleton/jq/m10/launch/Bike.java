package jq.m10.launch;

/**
 * Core (domain): one bike in one city. While it is RENTED, stationId is null.
 * It is a record, so the inventory stores a new copy when a bike changes.
 */
public record Bike(String id, String cityId, BikeType type, String stationId, BikeStatus status) {

    public static Bike docked(String id, String cityId, BikeType type, String stationId) {
        return new Bike(id, cityId, type, stationId, BikeStatus.AVAILABLE);
    }

    public boolean isAvailableAt(String station) {
        return status == BikeStatus.AVAILABLE && station.equals(stationId);
    }
}
