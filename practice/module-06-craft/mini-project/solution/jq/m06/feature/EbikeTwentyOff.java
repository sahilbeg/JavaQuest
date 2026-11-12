package jq.m06.feature;

public class EbikeTwentyOff implements Promo {

    @Override
    public String name() {
        return "ebike-20";
    }

    @Override
    public boolean appliesTo(RideContext ride) {
        return ride.type().equals("ebike");
    }

    @Override
    public long discountCents(RideContext ride, long fareCents) {
        return fareCents * 20 / 100;
    }
}
