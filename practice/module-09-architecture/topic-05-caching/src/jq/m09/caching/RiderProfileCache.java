package jq.m09.caching;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

/** Cache-aside in front of the slow rider-profile store, with negative caching for unknown riders. */
public final class RiderProfileCache {

    public static final int CAPACITY = 10_000;
    public static final Duration PROFILE_TTL = Duration.ofMinutes(10);
    public static final Duration UNKNOWN_TTL = Duration.ofMinutes(1);

    private final RiderProfileStore store;
    private final TtlLruCache<String, RiderProfile> profiles;
    private final TtlLruCache<String, Boolean> unknownRiders;

    public RiderProfileCache(RiderProfileStore store, Clock clock) {
        this.store = store;
        this.profiles = new TtlLruCache<>(CAPACITY, PROFILE_TTL, clock);
        this.unknownRiders = new TtlLruCache<>(CAPACITY, UNKNOWN_TTL, clock);
    }

    public Optional<RiderProfile> find(String riderId) {
        // TODO 9: cache-aside.
        //         1. A profile in profiles? Return it.
        //         2. riderId in unknownRiders? Return Optional.empty() without asking the store.
        //         3. Otherwise load from the store. Found: put it in profiles. Not found: put riderId -> TRUE in unknownRiders.
        return Optional.empty();
    }

    public void update(RiderProfile profile) {
        // TODO 10: save to the store FIRST, then DELETE the rider's entry from profiles and from unknownRiders.
        //          Do not put the new profile into the cache: the next find loads it.
    }
}
