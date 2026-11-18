package jq.m09.resilience;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CircuitBreakerTest {

    private static final Duration THIRTY_SECONDS = Duration.ofSeconds(30);

    private final FakeClock clock = new FakeClock(1_000);
    private final AtomicInteger providerCalls = new AtomicInteger();

    /** The SMS provider, up: it counts the call and answers. */
    private Supplier<String> smsSent() {
        return () -> {
            providerCalls.incrementAndGet();
            return "sms sent";
        };
    }

    /** The SMS provider, down: it counts the call and throws this exception. */
    private Supplier<String> smsFails(RuntimeException failure) {
        return () -> {
            providerCalls.incrementAndGet();
            throw failure;
        };
    }

    /** Makes the breaker see one failure, and checks the provider's own exception comes back unchanged. */
    private void fails(CircuitBreaker breaker, String which) {
        RuntimeException timeout = new ProviderTimeoutException("SMS provider did not answer in 2 s");
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> breaker.call(smsFails(timeout)),
                which + ": a failing call should throw");
        assertSame(timeout, thrown, which + ": throw the provider's own exception on, unchanged, so the caller can see what went wrong");
    }

    private void rejected(CircuitBreaker breaker, String why) {
        int before = providerCalls.get();
        assertThrows(CallNotPermittedException.class, () -> breaker.call(smsSent()), why);
        assertEquals(before, providerCalls.get(), "a rejected call must fail fast, without calling the provider at all");
    }

    @Test
    @DisplayName("A closed breaker calls the provider and returns its answer")
    void closedPassesCallsThrough() {
        CircuitBreaker breaker = new CircuitBreaker(3, THIRTY_SECONDS, clock);

        assertEquals(CircuitBreaker.State.CLOSED, breaker.state(), "a new breaker should start CLOSED");
        assertEquals("sms sent", breaker.call(smsSent()), "call should return what the provider returned");
        assertEquals(1, providerCalls.get(), "the provider should be called exactly once");

        assertThrows(IllegalArgumentException.class, () -> new CircuitBreaker(0, THIRTY_SECONDS, clock),
                "a failureThreshold of 0 makes no sense: throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new CircuitBreaker(3, Duration.ZERO, clock),
                "an open time of zero makes no sense: throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Only failures in a row count: a success in between starts the count again")
    void countsFailuresInARow() {
        CircuitBreaker breaker = new CircuitBreaker(3, THIRTY_SECONDS, clock);
        fails(breaker, "failure 1");
        fails(breaker, "failure 2");
        assertEquals(CircuitBreaker.State.CLOSED, breaker.state(), "2 failures with a threshold of 3 should keep the breaker CLOSED");

        assertEquals("sms sent", breaker.call(smsSent()), "the breaker is closed, so this call should go through");
        fails(breaker, "failure 1 after the success");
        fails(breaker, "failure 2 after the success");
        assertEquals(CircuitBreaker.State.CLOSED, breaker.state(),
                "the success reset the count, so 2 new failures should not open it. Did you reset the count on success?");
    }

    @Test
    @DisplayName("The 3rd failure in a row opens the breaker, and then calls fail fast without reaching the provider")
    void opensAndFailsFast() {
        CircuitBreaker breaker = new CircuitBreaker(3, THIRTY_SECONDS, clock);
        fails(breaker, "failure 1");
        fails(breaker, "failure 2");
        fails(breaker, "failure 3");

        assertEquals(CircuitBreaker.State.OPEN, breaker.state(), "3 failures in a row with a threshold of 3 should open the breaker");
        assertEquals(3, providerCalls.get(), "the provider should have been called 3 times so far");
        clock.advance(10_000);
        rejected(breaker, "10 s later the breaker is still open: throw CallNotPermittedException");
        rejected(breaker, "and again: every call is rejected while open");
    }

    @Test
    @DisplayName("With a threshold of 1, the very first failure opens the breaker")
    void thresholdOfOne() {
        CircuitBreaker breaker = new CircuitBreaker(1, THIRTY_SECONDS, clock);
        fails(breaker, "the first call");

        assertEquals(CircuitBreaker.State.OPEN, breaker.state(), "threshold 1: one failure is enough to open");
        rejected(breaker, "the next call should be rejected");
    }

    @Test
    @DisplayName("Still open 1 ms before the open time ends; half-open at exactly 30 s")
    void openDurationBoundary() {
        CircuitBreaker breaker = new CircuitBreaker(1, THIRTY_SECONDS, clock);
        fails(breaker, "the failure that opens it");

        clock.advance(29_999);
        assertEquals(CircuitBreaker.State.OPEN, breaker.state(), "29.999 s after opening, the breaker should still be OPEN");
        rejected(breaker, "29.999 s after opening, calls should still be rejected");

        clock.advance(1);
        assertEquals(CircuitBreaker.State.HALF_OPEN, breaker.state(),
                "exactly 30 s after opening, the breaker should be HALF_OPEN (use >=, not >)");
        assertEquals("sms sent", breaker.call(smsSent()), "the trial call should reach the provider and return its answer");
    }

    @Test
    @DisplayName("A successful trial call closes the breaker, with a fresh failure count")
    void halfOpenSuccessCloses() {
        CircuitBreaker breaker = new CircuitBreaker(2, THIRTY_SECONDS, clock);
        fails(breaker, "failure 1");
        fails(breaker, "failure 2");
        clock.advance(30_000);

        assertEquals("sms sent", breaker.call(smsSent()), "the trial call should go through");
        assertEquals(CircuitBreaker.State.CLOSED, breaker.state(), "a successful trial should close the breaker");
        fails(breaker, "one failure after closing");
        assertEquals(CircuitBreaker.State.CLOSED, breaker.state(),
                "the count starts again after closing, so 1 failure with a threshold of 2 keeps it CLOSED");
        assertEquals("sms sent", breaker.call(smsSent()), "and calls keep going through");
    }

    @Test
    @DisplayName("A failed trial call opens the breaker again, for a whole new open time")
    void halfOpenFailureReopens() {
        CircuitBreaker breaker = new CircuitBreaker(3, THIRTY_SECONDS, clock);
        fails(breaker, "failure 1");
        fails(breaker, "failure 2");
        fails(breaker, "failure 3");
        clock.advance(30_000);
        assertEquals(CircuitBreaker.State.HALF_OPEN, breaker.state(), "30 s after opening, the breaker should be HALF_OPEN");

        fails(breaker, "the trial call");
        assertEquals(CircuitBreaker.State.OPEN, breaker.state(), "one failed trial is enough: the breaker should be OPEN again");
        clock.advance(29_999);
        rejected(breaker, "the new open time started when the trial failed, so 29.999 s later calls are still rejected");
        clock.advance(1);
        assertEquals(CircuitBreaker.State.HALF_OPEN, breaker.state(), "30 s after the failed trial, it should be HALF_OPEN again");
    }

    @Test
    @DisplayName("Half-open lets exactly one trial call through: a second call during the trial is rejected")
    void onlyOneTrialCall() {
        CircuitBreaker breaker = new CircuitBreaker(1, THIRTY_SECONDS, clock);
        fails(breaker, "the failure that opens it");
        clock.advance(30_000);

        String answer = breaker.call(() -> {
            providerCalls.incrementAndGet();
            // While this trial call is still running, another rider's SMS arrives.
            try {
                breaker.call(smsSent());
                fail("a second call while the trial is still running should throw CallNotPermittedException");
            } catch (CallNotPermittedException expected) {
                // right: only one trial at a time
            }
            return "trial sms sent";
        });

        assertEquals("trial sms sent", answer, "the trial call itself should return the provider's answer");
        assertEquals(2, providerCalls.get(), "only the opening failure and the trial should have reached the provider");
        assertEquals(CircuitBreaker.State.CLOSED, breaker.state(), "the trial worked, so the breaker should be CLOSED");
        assertEquals("sms sent", breaker.call(smsSent()), "once closed, calls go through again");
    }
}
