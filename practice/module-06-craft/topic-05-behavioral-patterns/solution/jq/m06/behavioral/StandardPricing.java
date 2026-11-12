package jq.m06.behavioral;

public class StandardPricing implements PricingStrategy {
    private static final long UNLOCK_FEE_CENTS = 100;
    private static final long CENTS_PER_MINUTE = 15;

    @Override
    public long priceCents(int minutes) {
        return UNLOCK_FEE_CENTS + CENTS_PER_MINUTE * minutes;
    }
}
