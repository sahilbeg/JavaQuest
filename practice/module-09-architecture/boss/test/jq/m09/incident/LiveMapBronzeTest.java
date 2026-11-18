package jq.m09.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LiveMapBronzeTest {

    private final TestClock clock = new TestClock();
    private final FakeSource source = new FakeSource().set("riverside", 7, 5).set("harbor", 3, 9);
    private final LiveMapCache cache = new LiveMapCache(source, clock);

    @Test
    @DisplayName("Bronze: two riders who look at Riverside within 5 seconds share one load")
    void twoRidersShareOneLoad() {
        StationStatus first = cache.get(new MapRequest("rider-7", "req-1", "riverside"));
        clock.at(2_000);
        StationStatus second = cache.get(new MapRequest("rider-8", "req-2", "riverside"));
        assertEquals(1, source.calls(),
                "the second rider should get Riverside from the cache; key the cache by station id, not by the whole request");
        assertEquals(7, first.bikesAvailable(), "Riverside has 7 bikes");
        assertEquals(second, first, "both riders should see the same Riverside numbers");
    }

    @Test
    @DisplayName("Bronze: 100 map refreshes for Riverside in one second load it only once")
    void hundredRefreshesOneLoad() {
        for (int i = 0; i < 100; i++) {
            clock.at(i * 10L);
            cache.get(new MapRequest("rider-" + i, "req-" + i, "riverside"));
        }
        assertEquals(1, source.calls(),
                "100 requests for one station within the TTL should cost 1 load, not " + source.calls());
    }

    @Test
    @DisplayName("Bronze: each station keeps its own entry, and its own numbers")
    void eachStationHasItsOwnEntry() {
        StationStatus riverside = cache.get(new MapRequest("rider-7", "req-1", "riverside"));
        StationStatus harbor = cache.get(new MapRequest("rider-7", "req-2", "harbor"));
        clock.at(1_000);
        StationStatus riversideAgain = cache.get(new MapRequest("rider-9", "req-3", "riverside"));
        StationStatus harborAgain = cache.get(new MapRequest("rider-9", "req-4", "harbor"));
        assertEquals(7, riverside.bikesAvailable(), "Riverside has 7 bikes");
        assertEquals(3, harbor.bikesAvailable(), "Harbor has 3 bikes, not Riverside's number");
        assertEquals(riverside, riversideAgain, "the second Riverside request should show Riverside's numbers");
        assertEquals(harbor, harborAgain, "the second Harbor request should show Harbor's numbers");
        assertEquals(2, source.calls(), "one load for Riverside and one for Harbor, then two cache hits");
    }

    @Test
    @DisplayName("Bronze: an entry is still fresh 4.999 seconds after it was loaded")
    void freshJustBeforeTheTtl() {
        cache.get(new MapRequest("rider-7", "req-1", "riverside"));
        clock.at(4_999);
        cache.get(new MapRequest("rider-8", "req-2", "riverside"));
        assertEquals(1, source.calls(), "at 4.999 seconds the entry is still fresh, so the source should not be called again");
    }

    @Test
    @DisplayName("Bronze: at exactly 5 seconds the entry has expired, and the new numbers are loaded once")
    void expiresAtTheTtl() {
        StationStatus before = cache.get(new MapRequest("rider-7", "req-1", "riverside"));
        source.set("riverside", 6, 6);
        clock.at(5_000);
        StationStatus after = cache.get(new MapRequest("rider-8", "req-2", "riverside"));
        StationStatus again = cache.get(new MapRequest("rider-9", "req-3", "riverside"));
        assertEquals(7, before.bikesAvailable(), "at 8:00:00 Riverside had 7 bikes");
        assertEquals(6, after.bikesAvailable(),
                "at exactly loadedAt + 5 s the entry has expired, so the map should show the new 6 bikes");
        assertEquals(6, again.bikesAvailable(), "the reloaded entry should be fresh again");
        assertEquals(2, source.calls(), "one load at 8:00:00, one at 8:00:05, then a cache hit");
    }
}
