package jq.m05.sealedtypes;

public record BikeDocked(String station, String bikeId) implements DockEvent {
}
