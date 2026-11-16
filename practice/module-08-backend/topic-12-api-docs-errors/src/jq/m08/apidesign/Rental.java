package jq.m08.apidesign;

/** One rental as Spoke stores it. The JSON shape partners see is decided by each API version. */
public record Rental(String id, String bikeId, long fareCents) { }
