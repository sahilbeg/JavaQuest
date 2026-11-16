package jq.m08.mvc;

import java.time.Instant;

/** One bike rental. endedAt stays null while the rider is still riding. */
public record Rental(String id, String riderId, String bikeId, String startStation, Instant startedAt, Instant endedAt) {

    /** A copy of this rental that ended at the given time. */
    public Rental endedAt(Instant time) {
        return new Rental(id, riderId, bikeId, startStation, startedAt, time);
    }
}
