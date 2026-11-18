package jq.m06.oodesign;

/** A strategy that prices a ride. New rules are new classes: the desk never changes. */
public interface PricingRule {
    long fareCents(Bike bike, int minutes);
}
