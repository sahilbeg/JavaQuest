package jq.m06.feature;

public class TenthRideFree implements Promo {

    @Override
    public String name() {
        return "tenth-ride-free";
    }

    @Override
    public boolean appliesTo(RideContext ride) {
        return ride.ridesThisMonth() % 10 == 9;
    }

    @Override
    public long discountCents(RideContext ride, long fareCents) {
        return fareCents;
    }
}
