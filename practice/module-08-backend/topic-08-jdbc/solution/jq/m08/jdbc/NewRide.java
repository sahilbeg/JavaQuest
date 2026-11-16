package jq.m08.jdbc;

/** A ride that is not saved yet, so it has no id. save() turns it into a Ride. */
public record NewRide(String riderId, String bikeId, int minutes, long fareCents) {}
