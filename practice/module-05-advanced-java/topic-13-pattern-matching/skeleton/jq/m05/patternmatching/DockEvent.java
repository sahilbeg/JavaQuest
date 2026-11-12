package jq.m05.patternmatching;

public sealed interface DockEvent permits BikeDocked, BikeUndocked, Fault, Rebalanced {
}
