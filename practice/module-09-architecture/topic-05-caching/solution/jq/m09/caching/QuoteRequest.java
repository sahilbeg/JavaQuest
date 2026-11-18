package jq.m09.caching;

/** What a fare quote depends on: the bike type and the planned minutes. A record, so equals and hashCode come free. Given to you. */
public record QuoteRequest(BikeType bikeType, int minutes) {}
