package jq.m06.behavioral;

public class RainDiscount implements PricingStrategy {
    private final PricingStrategy inner;

    public RainDiscount(PricingStrategy inner) {
        this.inner = inner;
    }

    @Override
    public long priceCents(int minutes) {
        return inner.priceCents(minutes) / 2;
    }
}
