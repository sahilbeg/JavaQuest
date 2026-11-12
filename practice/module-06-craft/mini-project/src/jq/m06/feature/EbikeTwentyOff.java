package jq.m06.feature;

public class EbikeTwentyOff implements Promo {

    @Override
    public String name() {
        return "ebike-20";
    }

    @Override
    public boolean appliesTo(RideContext ride) {
        // TODO 2: ebikes only; 20 percent off, rounded down
        return false;
    }

    @Override
    public long discountCents(RideContext ride, long fareCents) {
        return 0;
    }
}
