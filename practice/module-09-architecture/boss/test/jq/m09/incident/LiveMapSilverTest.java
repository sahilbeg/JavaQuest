package jq.m09.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LiveMapSilverTest {

    private static final Duration LIMIT = Duration.ofSeconds(4);

    private final TestClock clock = new TestClock();
    private final FakeSource source = new FakeSource().set("riverside", 7, 5).set("harbor", 3, 9);
    private final LiveMapCache cache = new LiveMapCache(source, clock);

    @Test
    @DisplayName("Silver: 20 riders open the map at once on an empty cache, and Riverside is loaded once")
    void oneLoadForACrowd() {
        assertTimeoutPreemptively(LIMIT, () -> {
            source.holdLoadsFor("riverside");
            Crowd crowd = new Crowd(20, cache, "riverside");
            crowd.go();
            crowd.awaitEveryoneWaiting();
            source.release();
            crowd.join();
            assertEquals(1, source.calls("riverside"),
                    "20 riders asked at once; one should load Riverside and the other 19 wait for its result");
            for (int i = 0; i < 20; i++) {
                assertNull(crowd.errors()[i], "rider " + i + " should get Riverside's numbers, not an error");
                assertEquals(7, crowd.results()[i].bikesAvailable(), "rider " + i + " should see Riverside's 7 bikes");
            }
        }, "the 20 riders should all get an answer; did a waiting rider never wake up?");
    }

    @Test
    @DisplayName("Silver: when Riverside's entry expires during rush hour, it is reloaded once, not once per rider")
    void oneReloadWhenAnEntryExpires() {
        assertTimeoutPreemptively(LIMIT, () -> {
            cache.get(new MapRequest("rider-7", "req-1", "riverside"));
            source.set("riverside", 6, 6);
            clock.at(5_000);
            source.holdLoadsFor("riverside");
            Crowd crowd = new Crowd(20, cache, "riverside");
            crowd.go();
            crowd.awaitEveryoneWaiting();
            source.release();
            crowd.join();
            assertEquals(2, source.calls("riverside"),
                    "one load at 8:00:00 and one reload at 8:00:05, however many riders ask at that moment");
            for (int i = 0; i < 20; i++) {
                assertNull(crowd.errors()[i], "rider " + i + " should get Riverside's numbers, not an error");
                assertEquals(6, crowd.results()[i].bikesAvailable(), "rider " + i + " should see the reloaded 6 bikes");
            }
        }, "the 20 riders should all get an answer; did a waiting rider never wake up?");
    }

    @Test
    @DisplayName("Silver: a failed load reaches every waiting rider once, and is not kept in the cache")
    void aFailedLoadIsNotCached() {
        assertTimeoutPreemptively(LIMIT, () -> {
            source.goDown(new IllegalStateException("station-status service timed out"));
            source.holdLoadsFor("riverside");
            Crowd crowd = new Crowd(10, cache, "riverside");
            crowd.go();
            crowd.awaitEveryoneWaiting();
            source.release();
            crowd.join();
            assertEquals(1, source.calls("riverside"), "the failing load should still be shared: one call, not 10");
            for (int i = 0; i < 10; i++) {
                assertNotNull(crowd.errors()[i], "rider " + i + " should get an error, because the load failed");
                assertEquals(true, crowd.errors()[i] instanceof RuntimeException,
                        "rider " + i + " should get a RuntimeException, but got " + crowd.errors()[i]);
            }

            source.comeBack();
            clock.at(2_000);
            StationStatus later = cache.get(new MapRequest("rider-7", "req-1", "riverside"));
            assertEquals(7, later.bikesAvailable(),
                    "2 seconds later the source works again, so the map should load Riverside's 7 bikes; a failed load must not stay cached");
            assertEquals(2, source.calls("riverside"), "the failed load, then one new load");
        }, "every rider should get an answer, even when the load fails");
    }

    @Test
    @DisplayName("Silver: a slow load for Riverside does not hold up the map for Harbor")
    void stationsDoNotWaitForEachOther() {
        assertTimeoutPreemptively(LIMIT, () -> {
            source.holdLoadsFor("riverside");
            Crowd crowd = new Crowd(3, cache, "riverside");
            crowd.go();
            crowd.awaitEveryoneWaiting();
            StationStatus harbor = cache.get(new MapRequest("rider-8", "req-9", "harbor"));
            assertEquals(3, harbor.bikesAvailable(), "Harbor has 3 bikes");
            source.release();
            crowd.join();
            assertEquals(7, crowd.results()[0].bikesAvailable(), "Riverside has 7 bikes");
        }, "Harbor's request waited for Riverside's slow load; lock or wait per station, not for the whole cache");
    }
}
