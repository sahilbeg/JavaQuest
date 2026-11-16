package jq.m08.jdbc;

/** One row of the ride table. The id comes from the database. */
public record Ride(long id, String riderId, String bikeId, int minutes, long fareCents) {}
