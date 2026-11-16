package jq.m08.di;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** Priya's Sunday promo: half the unlock fee and half the per-minute price, rounded down. */
@Component
@Qualifier("sunday")
public class SundayPriceList implements PriceList {

    @Override
    public long unlockCents() {
        return 50;
    }

    @Override
    public long perMinuteCents(BikeType type) {
        return switch (type) {
            case CLASSIC -> 7;   // 15 / 2, rounded down
            case EBIKE -> 15;    // 30 / 2
            case CARGO -> 12;    // 25 / 2, rounded down
        };
    }
}
