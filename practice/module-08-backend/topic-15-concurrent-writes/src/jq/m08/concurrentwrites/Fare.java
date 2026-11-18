package jq.m08.concurrentwrites;

/** One row of the fare table: the per-minute price of a bike type, and the row's version number. */
public record Fare(String bikeType, long centsPerMinute, long version) {}
