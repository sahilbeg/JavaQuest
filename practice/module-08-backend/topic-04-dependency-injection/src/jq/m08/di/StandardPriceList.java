package jq.m08.di;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/** The everyday prices: 100 cents to unlock, then 15, 30 or 25 cents a minute. */
// TODO 1: put @Component and @Primary above the class, so Spring makes it a bean and picks it by default
public class StandardPriceList implements PriceList {

    @Override
    public long unlockCents() {
        return 100;
    }

    @Override
    public long perMinuteCents(BikeType type) {
        return switch (type) {
            case CLASSIC -> 15;
            case EBIKE -> 30;
            case CARGO -> 25;
        };
    }
}
