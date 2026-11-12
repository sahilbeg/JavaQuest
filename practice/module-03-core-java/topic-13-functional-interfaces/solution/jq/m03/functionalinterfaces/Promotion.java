package jq.m03.functionalinterfaces;

@FunctionalInterface
public interface Promotion {

    double apply(Ride ride, double fare);
}
