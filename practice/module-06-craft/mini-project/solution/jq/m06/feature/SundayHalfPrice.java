package jq.m06.feature;

import java.time.DayOfWeek;

public class SundayHalfPrice implements Promo {

    @Override
    public String name() {
        return "sunday-half-price";
    }

    @Override
    public boolean appliesTo(RideContext ride) {
        return ride.day() == DayOfWeek.SUNDAY;
    }

    @Override
    public long discountCents(RideContext ride, long fareCents) {
        return fareCents / 2;
    }
}
