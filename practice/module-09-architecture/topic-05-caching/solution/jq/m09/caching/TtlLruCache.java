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
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be 0 or more, but was " + capacity);
        }
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be longer than zero, but was " + ttl);
        }
        this.capacity = capacity;
        this.ttl = ttl;
        this.clock = Objects.requireNonNull(clock, "clock");
        // accessOrder = true: every get or put moves the key to the "most recently used" end.
        this.entries = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, Entry<V>> eldest) {
                return size() > TtlLruCache.this.capacity;
            }
        };
    }

    public synchronized Optional<V> get(K key) {
        Entry<V> entry = entries.get(key);
        if (entry == null) {
            misses++;
            return Optional.empty();
        }
        if (isExpired(entry)) {
            entries.remove(key);
            misses++;
            return Optional.empty();
        }
        hits++;
        return Optional.of(entry.value());
    }

    public synchronized void put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        entries.put(key, new Entry<>(value, clock.instant().plus(ttl)));
    }

    public synchronized void invalidate(K key) {
        entries.remove(key);
    }

    public synchronized int size() {
        entries.values().removeIf(this::isExpired);
        return entries.size();
    }

    public synchronized long hits() {
        return hits;
    }

    public synchronized long misses() {
        return misses;
    }

    public synchronized double hitRatio() {
        long lookups = hits + misses;
        return lookups == 0 ? 0.0 : (double) hits / lookups;
    }

    private boolean isExpired(Entry<V> entry) {
        return !clock.instant().isBefore(entry.expiresAt());
    }
}
