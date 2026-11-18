package jq.m06.oodesign;

/** Spoke's normal price: 100 cents to unlock, plus the bike type's rate for every minute. */
public class StandardPricing implements PricingRule {

    @Override
    public long fareCents(Bike bike, int minutes) {
        // TODO 2: return 100 plus the bike type's perMinuteCents() times minutes
        return 0;
    }
}
