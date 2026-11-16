package jq.m08.di;

/** Prices in whole cents. A ride costs unlockCents() plus perMinuteCents(type) for every minute. */
public interface PriceList {

    long unlockCents();

    long perMinuteCents(BikeType type);
}
