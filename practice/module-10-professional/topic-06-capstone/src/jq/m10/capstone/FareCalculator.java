package jq.m10.capstone;

import java.time.Duration;

/**
 * Core (domain): one city's fare rule. Money stays in whole cents, as a long.
 * Minutes are whole minutes, rounded DOWN: 12 min 59 s is 12 minutes.
 */
public class FareCalculator {

    private final CityConfig city;

    public FareCalculator(CityConfig city) {
        this.city = city;
    }

    /** The unlock price plus the per-minute price times the whole minutes ridden. */
    public long fareCents(BikeType type, Duration ride) {
        // TODO 2: If the city does not offer this type, throw new RentalException.BikeTypeNotOffered(type, city.id()).
        //         If the ride is negative, throw an IllegalArgumentException.
        //         Whole minutes round DOWN: ride.toMinutes(). Return the unlock price plus the per-minute price times minutes.
        //         Read every price from city: never write a city's name or a price here.
        return 0;
    }
}
