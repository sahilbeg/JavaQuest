package jq.m05.sealedtypes;

public sealed interface DockEvent permits BikeDocked, BikeUndocked, Fault {
}
