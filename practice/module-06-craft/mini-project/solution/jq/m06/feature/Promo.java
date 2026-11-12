package jq.m06.feature;

public interface Promo {

    String name();

    boolean appliesTo(RideContext ride);

    long discountCents(RideContext ride, long fareCents);
}
