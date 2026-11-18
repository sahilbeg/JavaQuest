package jq.m12.incident;

/**
 * One line of the finance report: a station's rides and fares for one month.
 * revenueCents is a Long, not a long, so a NULL from SQL shows up as null instead of hiding as 0.
 */
public record StationRevenue(String stationId, String stationName, long rides, Long revenueCents) {}
