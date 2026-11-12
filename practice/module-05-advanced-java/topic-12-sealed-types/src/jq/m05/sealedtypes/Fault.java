package jq.m05.sealedtypes;

public record Fault(String station, String code) implements DockEvent {
}
