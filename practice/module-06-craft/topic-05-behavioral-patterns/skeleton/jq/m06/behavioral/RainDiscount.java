package jq.m06.behavioral;

public class RainDiscount implements PricingStrategy {
    private final PricingStrategy inner;

    public RainDiscount(PricingStrategy inner) {
        this.inner = inner;
    }

    @Override
    public long priceCents(int minutes) {
        // TODO 2: ask inner for its price and return half of it (whole cents, rounded down)
        return 0;
    }
}
