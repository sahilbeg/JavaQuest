package jq.m09.incident;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LiveMapGoldTest {

    private final TestClock clock = new TestClock();
    private final FakeSource source = new FakeSource().set("riverside", 7, 5);
    private final LiveMapCache cache = new LiveMapCache(source, clock);
    private final IllegalStateException down = new IllegalStateException("station-status service is down");

    private StationStatus riverside(int request) {
        return cache.get(new MapRequest("rider-7", "req-" + request, "riverside"));
    }

    @Test
    @DisplayName("Gold: when the source is down, the map shows Riverside's last numbers, marked stale")
    void servesStaleWhenTheSourceIsDown() {
        StationStatus fresh = riverside(1);
        assertFalse(fresh.stale(), "a status straight from the source is not stale");
        source.goDown(down);
        clock.at(6_000);
        StationStatus shown = assertDoesNotThrow(() -> riverside(2),
                "the source is down, but Riverside was loaded 6 seconds ago; show that copy instead of an error");
        assertTrue(shown.stale(), "the old copy must be marked stale, so the app can grey it out");
        assertEquals(7, shown.bikesAvailable(), "the stale copy keeps Riverside's last numbers");
        assertEquals(5, shown.docksFree(), "the stale copy keeps Riverside's last numbers");
        assertEquals(2, source.calls(), "the cache should still have tried the source once after the entry expired");
    }

    @Test
    @DisplayName("Gold: while the source is failing, it is asked at most once per second")
    void retriesAtMostOncePerInterval() {
        riverside(1);
        source.goDown(down);
        clock.at(6_000);
        assertDoesNotThrow(() -> riverside(2), "show the stale copy at 8:00:06");
        assertEquals(2, source.calls(), "the first request after expiry tries the source");
        for (long millis : new long[] {6_200, 6_500, 6_999}) {
            clock.at(millis);
            StationStatus shown = assertDoesNotThrow(() -> riverside(3), "show the stale copy at " + millis + " ms");
            assertTrue(shown.stale(), "the copy shown while the source is down is stale");
        }
        assertEquals(2, source.calls(),
                "within 1 second of a failed load, serve the stale copy without calling the failing source again");
        clock.at(7_000);
        assertDoesNotThrow(() -> riverside(4), "show the stale copy at 8:00:07");
        assertEquals(3, source.calls(), "exactly 1 second after the failed load, the cache may try the source again");
    }

    @Test
    @DisplayName("Gold: when the source comes back, the map shows fresh numbers again and caches them")
    void recoversWhenTheSourceComesBack() {
        riverside(1);
        source.goDown(down);
        clock.at(6_000);
        assertDoesNotThrow(() -> riverside(2), "show the stale copy while the source is down");
        source.comeBack();
        source.set("riverside", 4, 8);
        clock.at(7_000);
        StationStatus back = assertDoesNotThrow(() -> riverside(3), "the source works again");
        assertFalse(back.stale(), "a status loaded from the source again is not stale");
        assertEquals(4, back.bikesAvailable(), "the map should show the new 4 bikes");
        clock.at(11_999);
        riverside(4);
        assertEquals(3, source.calls(), "the new status was loaded at 8:00:07, so it is fresh until 8:00:12");
    }

    @Test
    @DisplayName("Gold: a stale copy is shown for at most 60 seconds after it expired")
    void staleHasALimit() {
        riverside(1);
        source.goDown(down);
        clock.at(64_999);
        StationStatus last = assertDoesNotThrow(() -> riverside(2),
                "loaded at 8:00:00, expired at 8:00:05, so it may be shown stale until just before 8:01:05");
        assertTrue(last.stale(), "the copy shown while the source is down is stale");
        clock.at(65_000);
        StatusUnavailableException error = assertThrows(StatusUnavailableException.class, () -> riverside(3),
                "at exactly expiry + 60 seconds the copy is too old to show; throw StatusUnavailableException (if you got a CompletionException, unwrap it: rethrow e.getCause())");
        assertEquals("riverside", error.stationId(), "the exception names the station");
    }

    @Test
    @DisplayName("Gold: with no copy at all, a failing source gives StatusUnavailableException")
    void nothingToShow() {
        source.goDown(down);
        StatusUnavailableException error = assertThrows(StatusUnavailableException.class, () -> riverside(1),
                "Riverside was never loaded and the source is down; throw StatusUnavailableException (if you got a CompletionException, unwrap it: rethrow e.getCause())");
        assertEquals("riverside", error.stationId(), "the exception names the station");
        assertSame(down, error.getCause(), "keep the source's exception as the cause, so the logs show what went wrong");
        clock.at(500);
        assertThrows(StatusUnavailableException.class, () -> riverside(2),
                "still nothing to show half a second later; throw StatusUnavailableException (if you got a CompletionException, unwrap it: rethrow e.getCause())");
        assertEquals(1, source.calls(), "within 1 second of a failed load, do not call the failing source again");
    }
}
