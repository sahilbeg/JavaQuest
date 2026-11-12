package jq.m06.structural;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UnlockFacadeTest {

    private static OldLock oldLockReturning(int code) {
        return new OldLock() {
            @Override
            public int engage(String id) {
                return code;
            }

            @Override
            public int disengage(String id) {
                return code;
            }
        };
    }

    @Test
    @DisplayName("The adapter turns code 0 into true and any other code into false")
    void adapterMapsCodes() {
        Lock ok = new OldLockAdapter(oldLockReturning(0));
        assertTrue(ok.open("B-9001"), "open should be true when disengage returns 0");
        assertTrue(ok.close("B-9001"), "close should be true when engage returns 0");

        Lock jammed = new OldLockAdapter(oldLockReturning(4));
        assertFalse(jammed.open("B-9001"), "open should be false when disengage returns error code 4");
        assertFalse(jammed.close("B-9001"), "close should be false when engage returns error code 4");

        Lock real = new OldLockAdapter(new OldLock());
        assertTrue(real.open("B-9001"), "the real OldLock opens B-9001, so the adapter should return true");
        assertFalse(real.open("B-9901"), "the real OldLock jams on B-9901 (code 4), so the adapter should return false");
    }

    @Test
    @DisplayName("RetryingLock tries open exactly once more after a failure")
    void retryOnce() {
        FlakyLock failsOnce = new FlakyLock(1);
        assertTrue(new RetryingLock(failsOnce).open("B-1042"), "a lock that fails once should open on the retry");
        assertEquals(2, failsOnce.opens, "RetryingLock should call open twice when the first try fails");

        FlakyLock works = new FlakyLock(0);
        assertTrue(new RetryingLock(works).open("B-1042"), "a working lock should open");
        assertEquals(1, works.opens, "RetryingLock should not retry when the first try works");

        FlakyLock broken = new FlakyLock(5);
        assertFalse(new RetryingLock(broken).open("B-1042"), "a lock that keeps failing should still report false");
        assertEquals(2, broken.opens, "RetryingLock should give up after one retry: two calls in total");
    }

    @Test
    @DisplayName("RecordingLock writes one history line per call")
    void recordingHistory() {
        RecordingLock lock = new RecordingLock(new FlakyLock(1));
        assertFalse(lock.open("B-1042"), "RecordingLock should pass back the inner lock's answer");
        lock.open("B-1042");
        lock.close("B-1042");
        assertEquals(List.of("open B-1042: false", "open B-1042: true", "close B-1042: true"), lock.history(),
                "history should hold one line per call, like \"open B-1042: true\"");
    }

    @Test
    @DisplayName("Decorators stack in either order")
    void stackingOrder() {
        RecordingLock outside = new RecordingLock(new RetryingLock(new FlakyLock(1)));
        assertTrue(outside.open("B-1042"), "Recording around Retrying should still open a lock that fails once");
        assertEquals(List.of("open B-1042: true"), outside.history(),
                "with RecordingLock outside the retry, it sees one call that worked");

        RecordingLock inside = new RecordingLock(new FlakyLock(1));
        assertTrue(new RetryingLock(inside).open("B-1042"), "Retrying around Recording should still open a lock that fails once");
        assertEquals(List.of("open B-1042: false", "open B-1042: true"), inside.history(),
                "with RecordingLock inside the retry, it sees both attempts");
    }

    @Test
    @DisplayName("UnlockFacade checks the rider first, then opens the lock")
    void facade() {
        RiderRegistry registry = new RiderRegistry(Set.of("r1"));

        FlakyLock good = new FlakyLock(0);
        assertEquals("unlocked B-1042 for r1", new UnlockFacade(registry, good).unlock("r1", "B-1042"),
                "an allowed rider and a working lock should give \"unlocked B-1042 for r1\"");

        FlakyLock untouched = new FlakyLock(0);
        assertEquals("rider r9 not allowed", new UnlockFacade(registry, untouched).unlock("r9", "B-1042"),
                "a rider who is not in the registry should get \"rider r9 not allowed\"");
        assertEquals(0, untouched.opens, "the facade should not touch the lock for a rider who is not allowed");

        FlakyLock broken = new FlakyLock(5);
        assertEquals("lock failed for B-1042", new UnlockFacade(registry, broken).unlock("r1", "B-1042"),
                "a lock that does not open should give \"lock failed for B-1042\"");
    }
}
