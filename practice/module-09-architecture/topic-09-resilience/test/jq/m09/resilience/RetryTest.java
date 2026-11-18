package jq.m09.resilience;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RetryTest {

    /** The fake sleeper never waits: it only writes down how long it was asked to wait. */
    private final List<Long> sleeps = new ArrayList<>();
    private final Sleeper sleeper = sleeps::add;
    private final AtomicInteger calls = new AtomicInteger();

    /** Base 200 ms, cap 5 s, and a "random" that always picks the ceiling, so the delays are 200, 400, 800... */
    private Retry retry(int maxAttempts) {
        return new Retry(maxAttempts, new Backoff(200, 5_000, new FakeRandom()), sleeper);
    }

    /** A fake payment provider: throws the given failures in order, then answers "pay-301". */
    private Supplier<String> provider(RuntimeException... failures) {
        return () -> {
            int call = calls.incrementAndGet();
            if (call <= failures.length) {
                throw failures[call - 1];
            }
            return "pay-301";
        };
    }

    private <T> T within2s(Supplier<T> work) {
        return assertTimeoutPreemptively(Duration.ofSeconds(2), work::get, "the retry loop should stop. Does it ever end?");
    }

    @Test
    @DisplayName("Timeouts, 429 and 5xx are retryable; 400, 401, 404, 409 and plain bugs are not")
    void whichFailuresAreRetryable() {
        assertTrue(Retry.isRetryable(new ProviderTimeoutException("no answer in 2 s")), "a timeout may pass next time: retryable");
        for (int status : new int[] {429, 500, 502, 503, 504}) {
            assertTrue(Retry.isRetryable(new ProviderException(status, "try later")), status + " may pass next time: retryable");
        }
        for (int status : new int[] {400, 401, 403, 404, 409, 422}) {
            assertFalse(Retry.isRetryable(new ProviderException(status, "refused")),
                    status + " will fail the same way every time: not retryable");
        }
        assertFalse(Retry.isRetryable(new IllegalStateException("bug in our code")),
                "a bug in our own code is not a provider failure: not retryable");
    }

    @Test
    @DisplayName("Two 503s, then success: the call is tried 3 times, with backoff waits of 200 and 400 ms")
    void recoversAfterTwoFailures() {
        String first = within2s(() -> retry(3).call(provider()));
        assertEquals("pay-301", first, "a call that works the first time should return its answer");
        assertEquals(List.of(), sleeps, "a call that works the first time should never wait");

        calls.set(0);
        String answer = within2s(() -> retry(3).call(provider(
                new ProviderException(503, "busy"), new ProviderException(503, "busy"))));
        assertEquals("pay-301", answer, "the third try works, so call should return its answer");
        assertEquals(3, calls.get(), "two failures and one success: 3 calls to the provider");
        assertEquals(List.of(200L, 400L), sleeps,
                "wait backoff.delayMillis(1) after the 1st failure and delayMillis(2) after the 2nd, through the Sleeper");
    }

    @Test
    @DisplayName("Always failing: stop after maxAttempts, never wait after the last try, and throw the last failure")
    void givesUpAfterMaxAttempts() {
        RuntimeException[] timeouts = {
            new ProviderTimeoutException("try 1"), new ProviderTimeoutException("try 2"),
            new ProviderTimeoutException("try 3"), new ProviderTimeoutException("try 4")
        };
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> within2s(() -> retry(3).call(provider(timeouts))),
                "after 3 failed tries, call should throw");

        assertEquals(3, calls.get(), "maxAttempts 3 means 3 calls in all, the first try included");
        assertSame(timeouts[2], thrown, "throw the failure from the LAST try, unchanged");
        assertEquals(List.of(200L, 400L), sleeps, "wait between tries only: 2 waits for 3 tries, none after the last");
    }

    @Test
    @DisplayName("A 409 is thrown at once: one call, no waiting, no retry")
    void nonRetryableIsThrownAtOnce() {
        ProviderException conflict = new ProviderException(409, "payment pay-301 already captured");
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> within2s(() -> retry(5).call(provider(conflict))),
                "a 409 should be thrown to the caller");

        assertSame(conflict, thrown, "throw the 409 itself, unchanged");
        assertEquals(1, calls.get(), "a 409 will fail the same way again, so call the provider only once");
        assertEquals(List.of(), sleeps, "no retry means no waiting");

        assertThrows(IllegalArgumentException.class, () -> new Retry(0, new Backoff(200, 5_000, new FakeRandom()), sleeper),
                "maxAttempts 0 would never even try: throw IllegalArgumentException");
    }

    @Test
    @DisplayName("A 503, then a 400: the 503 is retried once, then the 400 is thrown at once")
    void retryableThenNonRetryable() {
        ProviderException badRequest = new ProviderException(400, "card number missing");
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> within2s(() -> retry(5).call(provider(new ProviderException(503, "busy"), badRequest))),
                "the 400 on the second try should be thrown");

        assertSame(badRequest, thrown, "the caller should get the 400 itself");
        assertEquals(2, calls.get(), "one retry after the 503, then stop at the 400");
        assertEquals(List.of(200L), sleeps, "one wait, after the 503 only");
    }
}
