package jq.m09.caching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TtlLruCacheTest {

    private final MutableClock clock = new MutableClock();

    private TtlLruCache<String, String> cache(int capacity) {
        return new TtlLruCache<>(capacity, Duration.ofMinutes(10), clock);
    }

    @Test
    @DisplayName("A stored value is a hit, a missing key is a miss, and the hit ratio counts both")
    void hitsAndMisses() {
        TtlLruCache<String, String> cache = cache(3);
        assertEquals(0.0, cache.hitRatio(), 1e-9, "with no lookups yet, hitRatio() should be 0.0, not a division by zero");
        cache.put("rider-7", "Ada");

        assertEquals(Optional.of("Ada"), cache.get("rider-7"), "get should return the value put for rider-7");
        assertEquals(Optional.of("Ada"), cache.get("rider-7"), "a second get should hit again");
        assertEquals(Optional.of("Ada"), cache.get("rider-7"), "a third get should hit again");
        assertEquals(Optional.empty(), cache.get("rider-8"), "rider-8 was never put, so get should return Optional.empty()");

        assertEquals(3, cache.hits(), "three gets found a value, so hits() should be 3");
        assertEquals(1, cache.misses(), "one get found nothing, so misses() should be 1");
        assertEquals(0.75, cache.hitRatio(), 1e-9, "3 hits out of 4 lookups is a hit ratio of 0.75");
    }

    @Test
    @DisplayName("When full, the least recently used key is evicted, and a get counts as a use")
    void evictsLeastRecentlyUsed() {
        TtlLruCache<String, String> cache = cache(3);
        cache.put("rider-1", "Ada");
        cache.put("rider-2", "Ben");
        cache.put("rider-3", "Cy");
        cache.get("rider-1");               // rider-1 is now the most recently used; rider-2 is the oldest
        cache.put("rider-4", "Dee");        // one too many: evict the least recently used

        assertEquals(Optional.empty(), cache.get("rider-2"),
                "rider-2 was the least recently used (rider-1 was read after it), so it should have been evicted");
        assertEquals(Optional.of("Ada"), cache.get("rider-1"),
                "rider-1 was read just before the put, so it must survive. Did you create the LinkedHashMap with accessOrder = true?");
        assertEquals(Optional.of("Cy"), cache.get("rider-3"), "rider-3 should still be cached");
        assertEquals(Optional.of("Dee"), cache.get("rider-4"), "rider-4 was just put, so it should be cached");
        assertEquals(3, cache.size(), "the cache must never hold more than its capacity of 3");
    }

    @Test
    @DisplayName("Putting a key that is already there replaces its value, refreshes it, and evicts nothing")
    void putReplaces() {
        TtlLruCache<String, String> cache = cache(2);
        cache.put("rider-1", "Ada");
        cache.put("rider-2", "Ben");
        cache.put("rider-1", "Ada Lovelace");   // replace, and rider-1 becomes the most recently used
        cache.put("rider-3", "Cy");             // evicts rider-2, the least recently used

        assertEquals(Optional.of("Ada Lovelace"), cache.get("rider-1"), "the second put for rider-1 should replace its value");
        assertEquals(Optional.empty(), cache.get("rider-2"), "putting rider-1 again made it recent, so rider-2 was the one to evict");
        assertEquals(2, cache.size(), "replacing a value must not add a second entry");
    }

    @Test
    @DisplayName("An entry is fresh just before its TTL and expired exactly at it (now >= put time + ttl)")
    void expiresAtTheBoundary() {
        TtlLruCache<String, String> cache = cache(3);
        cache.put("rider-7", "Ada");

        clock.advance(Duration.ofMinutes(10).minusNanos(1));
        assertEquals(Optional.of("Ada"), cache.get("rider-7"), "1 nanosecond before the 10 minute TTL, the entry is still fresh");

        clock.advance(Duration.ofNanos(1));
        assertEquals(Optional.empty(), cache.get("rider-7"), "at exactly put time + ttl the entry is expired, so get is a miss");
        assertEquals(1, cache.hits(), "only the first get was a hit");
        assertEquals(1, cache.misses(), "reading an expired entry counts as a miss");
        assertEquals(0, cache.size(), "the expired entry should be gone");
    }

    @Test
    @DisplayName("A put restarts the TTL, and size() does not count expired entries")
    void putRestartsTheClock() {
        TtlLruCache<String, String> cache = cache(3);
        cache.put("rider-1", "Ada");
        cache.put("rider-2", "Ben");            // both expire at minute 10
        clock.advance(Duration.ofMinutes(6));
        cache.put("rider-1", "Ada");            // put again at minute 6: now expires at minute 16
        clock.advance(Duration.ofMinutes(6));   // minute 12

        assertEquals(1, cache.size(),
                "at minute 12, rider-2 (put at minute 0) has expired, while rider-1 (put again at minute 6) is fresh: size() should be 1");
        assertEquals(Optional.of("Ada"), cache.get("rider-1"), "rider-1 was put again at minute 6, so at minute 12 it is still fresh");
        assertEquals(Optional.empty(), cache.get("rider-2"), "rider-2 expired at minute 10");
    }

    @Test
    @DisplayName("invalidate removes one key and leaves the others")
    void invalidates() {
        TtlLruCache<String, String> cache = cache(3);
        cache.put("rider-1", "Ada");
        cache.put("rider-2", "Ben");
        cache.invalidate("rider-1");
        cache.invalidate("rider-99");           // not there: nothing happens

        assertEquals(Optional.empty(), cache.get("rider-1"), "rider-1 was invalidated, so get should miss");
        assertEquals(Optional.of("Ben"), cache.get("rider-2"), "invalidating rider-1 must not touch rider-2");
        assertEquals(1, cache.size(), "one entry should be left");
    }

    @Test
    @DisplayName("Capacity 1 keeps only the newest key, and capacity 0 stores nothing")
    void tinyCapacities() {
        TtlLruCache<String, String> one = cache(1);
        one.put("rider-1", "Ada");
        one.put("rider-2", "Ben");
        assertEquals(Optional.empty(), one.get("rider-1"), "with capacity 1, putting rider-2 must evict rider-1");
        assertEquals(Optional.of("Ben"), one.get("rider-2"), "with capacity 1, the newest key stays");

        TtlLruCache<String, String> none = cache(0);
        none.put("rider-1", "Ada");
        assertEquals(Optional.empty(), none.get("rider-1"), "capacity 0 means caching is off: every get is a miss");
        assertEquals(0, none.size(), "capacity 0 stores nothing");
    }

    @Test
    @DisplayName("A negative capacity, or a TTL that is zero or negative, is refused")
    void refusesBadSettings() {
        assertThrows(IllegalArgumentException.class, () -> new TtlLruCache<String, String>(-1, Duration.ofMinutes(1), clock),
                "capacity -1 makes no sense: throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new TtlLruCache<String, String>(10, Duration.ZERO, clock),
                "a TTL of zero would expire every entry at once: throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new TtlLruCache<String, String>(10, Duration.ofSeconds(-5), clock),
                "a negative TTL makes no sense: throw IllegalArgumentException");
    }
}
