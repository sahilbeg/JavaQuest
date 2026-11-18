package jq.m10.launch;

import java.time.Instant;

/**
 * Core (domain): one rental. While the ride is on, endStationId and endedAt are null and fareCents is 0.
 * idempotencyKey is the key the rider's app sent with the start request, or null.
 */
public record Rental(String id, String cityId, String riderId, String bikeId, BikeType bikeType,
                     String startStationId, Instant startedAt, String endStationId, Instant endedAt,
                     long fareCents, String idempotencyKey) {

    public static Rental start(String id, String cityId, String riderId, String bikeId, BikeType bikeType,
                               String startStationId, Instant startedAt, String idempotencyKey) {
        return new Rental(id, cityId, riderId, bikeId, bikeType, startStationId, startedAt, null, null, 0,
                idempotencyKey);
    }

    public boolean isActive() {
        return endedAt == null;
    }

    /** "ACTIVE" while the ride is on, "ENDED" after. */
    public String status() {
        return isActive() ? "ACTIVE" : "ENDED";
    }

    /** An ended copy of this rental. The record itself never changes. */
    public Rental end(String atStationId, Instant at, long fare) {
        return new Rental(id, cityId, riderId, bikeId, bikeType, startStationId, startedAt, atStationId, at, fare,
                idempotencyKey);
    }
}
