package jq.m06.feature;

import java.time.DayOfWeek;

public class SundayHalfPrice implements Promo {

    @Override
    public String name() {
        return "sunday-half-price";
    }

    @Override
    public boolean appliesTo(RideContext ride) {
        // TODO 1: Sundays only; half the fare off, rounded down
        return false;
    }

    @Override
    public long discountCents(RideContext ride, long fareCents) {
        return 0;
    }
}
