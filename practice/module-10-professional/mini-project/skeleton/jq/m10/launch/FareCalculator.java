package jq.m10.launch;

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
        if (!city.offers(type)) {
            throw new RentalException.BikeTypeNotOffered(type, city.id());
        }
        if (ride.isNegative()) {
            throw new IllegalArgumentException("A ride cannot last " + ride);
        }
        long minutes = ride.toMinutes();
        return city.unlockCents() + city.perMinuteCents().get(type) * minutes;
    }
}
