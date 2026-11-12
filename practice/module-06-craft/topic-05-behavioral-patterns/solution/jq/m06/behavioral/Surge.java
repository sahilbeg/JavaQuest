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
        return (long) Math.floor(inner.priceCents(minutes) * multiplier);
    }
}
