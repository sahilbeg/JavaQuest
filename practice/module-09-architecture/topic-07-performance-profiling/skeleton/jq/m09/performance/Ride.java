package jq.m09.performance;

/** One finished ride, as the ride-history report sees it. Finished for you. */
public record Ride(String rentalId, String riderId, int minutes, long fareCents) {}
