package jq.m06.oodesign;

import java.time.Instant;

/** One rider's hold on one bike, until expiresAt. A value: it never changes after it is made. */
public record Reservation(String riderId, Bike bike, int dockId, Instant expiresAt) {

    /** Active while now is strictly before expiresAt. At expiresAt exactly, it has expired. */
    public boolean isActiveAt(Instant now) {
        return now.isBefore(expiresAt);
    }
}
