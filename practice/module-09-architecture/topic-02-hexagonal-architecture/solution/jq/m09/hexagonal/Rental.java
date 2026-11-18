package jq.m09.hexagonal;

import java.time.Instant;

/**
 * Core (domain): one rental. While the ride is on, endedAt and payment are null.
 * It is a record, so end(...) returns a new, ended copy instead of changing this one.
 */
public record Rental(String id, String riderId, String bikeId, BikeType type, Instant startedAt,
                     Instant endedAt, long fareCents, PaymentResult payment) {

    public static Rental started(String id, String riderId, String bikeId, BikeType type, Instant startedAt) {
        return new Rental(id, riderId, bikeId, type, startedAt, null, 0, null);
    }

    public boolean isEnded() {
        return endedAt != null;
    }

    public Rental end(Instant at, long fare, PaymentResult result) {
        return new Rental(id, riderId, bikeId, type, startedAt, at, fare, result);
    }
}
