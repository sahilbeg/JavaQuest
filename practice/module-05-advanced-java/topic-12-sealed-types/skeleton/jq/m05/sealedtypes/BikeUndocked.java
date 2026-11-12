package jq.m05.sealedtypes;

public record BikeUndocked(String station, String bikeId) implements DockEvent {
}
