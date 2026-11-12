package jq.m06.behavioral;

public class Surge implements PricingStrategy {
    private final PricingStrategy inner;
    private final double multiplier;

    public Surge(PricingStrategy inner, double multiplier) {
        this.inner = inner;
        this.multiplier = multiplier;
    }

    @Override
    public long priceCents(int minutes) {
        // TODO 3: ask inner for its price, multiply it by multiplier, and round down to whole cents
        return 0;
    }
}
