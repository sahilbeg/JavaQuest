package jq.m06.oodesign;

/** Spoke's normal price: 100 cents to unlock, plus the bike type's rate for every minute. */
public class StandardPricing implements PricingRule {

    @Override
    public long fareCents(Bike bike, int minutes) {
        return 100 + bike.type().perMinuteCents() * minutes;
    }
}
