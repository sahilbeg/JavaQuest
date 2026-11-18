package jq.m09.caching;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A size-limited cache. The least recently used entry is evicted when it is full,
 * and each entry expires ttl after it was put. Time comes from the injected clock.
 */
public final class TtlLruCache<K, V> {

    /** One cached value and the moment it stops being fresh. */
    private record Entry<V>(V value, Instant expiresAt) {}

    private final int capacity;
    private final Duration ttl;
    private final Clock clock;
    private final LinkedHashMap<K, Entry<V>> entries;
    private long hits;
    private long misses;

    public TtlLruCache(int capacity, Duration ttl, Clock clock) {
        // TODO 1: throw IllegalArgumentException when capacity < 0, or when ttl is null, zero or negative
        this.capacity = capacity;
        this.ttl = ttl;
        this.clock = Objects.requireNonNull(clock, "clock");
        // TODO 2: make this LinkedHashMap keep ACCESS order (the 3-argument constructor), and override
        //         removeEldestEntry so it returns true when size() > capacity (write TtlLruCache.this.capacity)
        this.entries = new LinkedHashMap<>();
    }

    public synchronized Optional<V> get(K key) {
        // TODO 4: look the key up in entries.
        //         Not there: count a miss, return Optional.empty().
        //         There but expired: remove it, count a miss, return Optional.empty().
        //         Otherwise: count a hit and return Optional.of(the value).
        return Optional.empty();
    }

    public synchronized void put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        // TODO 5: store new Entry<>(value, the clock's instant plus ttl) under key
    }

    public synchronized void invalidate(K key) {
        // TODO 6: remove the key, if it is there
    }

    public synchronized int size() {
        // TODO 7: remove every expired entry (entries.values().removeIf(...)), then return how many are left
        return 0;
    }

    public synchronized long hits() {
        return hits;
    }

    public synchronized long misses() {
        return misses;
    }

    public synchronized double hitRatio() {
        // TODO 8: hits divided by (hits + misses) as a double, or 0.0 when there were no lookups yet
        return 0.0;
    }

    private boolean isExpired(Entry<V> entry) {
        // TODO 3: an entry is expired when the clock's instant is NOT before entry.expiresAt(): now >= put time + ttl
        return false;
    }
}
