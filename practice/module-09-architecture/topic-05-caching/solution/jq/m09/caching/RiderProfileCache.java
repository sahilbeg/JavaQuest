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
        Optional<RiderProfile> cached = profiles.get(riderId);
        if (cached.isPresent()) {
            return cached;
        }
        if (unknownRiders.get(riderId).isPresent()) {
            return Optional.empty();
        }
        Optional<RiderProfile> loaded = store.load(riderId);
        if (loaded.isPresent()) {
            profiles.put(riderId, loaded.get());
        } else {
            unknownRiders.put(riderId, Boolean.TRUE);
        }
        return loaded;
    }

    public void update(RiderProfile profile) {
        store.save(profile);
        profiles.invalidate(profile.riderId());
        unknownRiders.invalidate(profile.riderId());
    }
}
