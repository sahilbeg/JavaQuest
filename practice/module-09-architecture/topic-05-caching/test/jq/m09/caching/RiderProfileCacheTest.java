package jq.m09.caching;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RiderProfileCacheTest {

    private static final RiderProfile ADA = new RiderProfile("rider-7", "Ada", "harbor");
    private static final RiderProfile ADA_MOVED = new RiderProfile("rider-7", "Ada", "uptown");

    private final MutableClock clock = new MutableClock();
    private final FakeProfileStore store = new FakeProfileStore();
    private RiderProfileCache cache;

    @BeforeEach
    void seed() {
        store.seed(ADA);
        cache = new RiderProfileCache(store, clock);
    }

    @Test
    @DisplayName("The first find loads from the store, and the next finds are served from the cache")
    void cacheAside() {
        assertEquals(Optional.of(ADA), cache.find("rider-7"), "find should return rider-7's profile from the store");
        assertEquals(Optional.of(ADA), cache.find("rider-7"), "the second find should return the same profile");
        assertEquals(Optional.of(ADA), cache.find("rider-7"), "the third find should return the same profile");

        assertEquals(1, store.loads, "only the first find may load from the slow store; the others must be cache hits");
    }

    @Test
    @DisplayName("A cached profile is loaded again once its 10 minute TTL is over")
    void profileTtl() {
        cache.find("rider-7");
        clock.advance(Duration.ofMinutes(9));
        cache.find("rider-7");
        assertEquals(1, store.loads, "after 9 minutes the profile is still fresh, so no second load");

        clock.advance(Duration.ofMinutes(1));
        assertEquals(Optional.of(ADA), cache.find("rider-7"), "after the TTL, find should still return the profile");
        assertEquals(2, store.loads, "after 10 minutes the cached profile has expired, so find must load it again");
    }

    @Test
    @DisplayName("An unknown rider is remembered as unknown for 1 minute, then checked again")
    void negativeCaching() {
        for (int i = 0; i < 5; i++) {
            assertEquals(Optional.empty(), cache.find("rider-404"), "rider-404 does not exist, so find should return Optional.empty()");
        }
        assertEquals(1, store.loads, "five finds for an unknown rider should reach the store once: cache the 'not found' too");

        clock.advance(Duration.ofSeconds(59));
        cache.find("rider-404");
        assertEquals(1, store.loads, "59 seconds later the 'not found' answer is still fresh");

        clock.advance(Duration.ofSeconds(1));
        cache.find("rider-404");
        assertEquals(2, store.loads, "after UNKNOWN_TTL (1 minute) the store should be asked again, in case the rider signed up");
    }

    @Test
    @DisplayName("update saves to the store, and the next find loads the new profile instead of the cached one")
    void updateInvalidates() {
        cache.find("rider-7");
        cache.update(ADA_MOVED);

        assertEquals(1, store.saves, "update should save the profile to the store");
        assertEquals(Optional.of(ADA_MOVED), cache.find("rider-7"),
                "after update, find must not return the old cached profile with home station harbor");
        assertEquals(2, store.loads, "update should delete the cached entry, so the next find loads the new profile once");
    }

    @Test
    @DisplayName("A rider who was unknown and then signs up is found at once")
    void updateClearsTheNegativeEntry() {
        RiderProfile newRider = new RiderProfile("rider-404", "Bea", "depot");
        assertEquals(Optional.empty(), cache.find("rider-404"), "rider-404 does not exist yet");

        cache.update(newRider);

        assertEquals(Optional.of(newRider), cache.find("rider-404"),
                "update must also delete the 'unknown rider' entry, or the new rider looks missing for up to a minute");
    }

    @Test
    @DisplayName("A read that arrives while the store is being written does not leave the old profile cached")
    void deleteAfterWriting() {
        cache.find("rider-7");
        // A rider opens the app while the save is in flight: the store still holds the old profile.
        store.beforeWrite = () -> cache.find("rider-7");

        cache.update(ADA_MOVED);

        assertEquals(Optional.of(ADA_MOVED), cache.find("rider-7"),
                "update must write the store first and delete the cache entry after. Deleting first lets a read in between put the old profile back");
    }

    @Test
    @DisplayName("Two updates whose cache steps land in the wrong order still leave the newest profile")
    void deleteBeatsUpdate() {
        RiderProfile adaRiverside = new RiderProfile("rider-7", "Ada", "riverside");
        cache.find("rider-7");
        // Update 1 (riverside) is written to the store, then update 2 (uptown) runs completely, then update 1 finishes.
        store.afterWrite = () -> cache.update(ADA_MOVED);

        cache.update(adaRiverside);

        assertEquals(Optional.of(ADA_MOVED), cache.find("rider-7"),
                "the store holds uptown, written last. If update puts its own profile into the cache instead of deleting, the slower update leaves riverside cached");
    }
}
