package jq.m06.feature;

public class TenthRideFree implements Promo {

    @Override
    public String name() {
        return "tenth-ride-free";
    }

    @Override
    public boolean appliesTo(RideContext ride) {
        // TODO 3: the 10th, 20th, 30th ... ride of the month is free
        return false;
    }

    @Override
    public long discountCents(RideContext ride, long fareCents) {
        return 0;
    }
}
