package jq.m09.caching;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FareQuoteLoaderTest {

    private static final QuoteRequest EBIKE_20 = new QuoteRequest(BikeType.EBIKE, 20);
    private static final QuoteRequest CLASSIC_20 = new QuoteRequest(BikeType.CLASSIC, 20);
    private static final QuoteRequest CARGO_5 = new QuoteRequest(BikeType.CARGO, 5);
    private static final int RIDERS = 8;

    private final FakePricing pricing = new FakePricing();
    private final FareQuoteLoader loader = new FareQuoteLoader(pricing);

    /** Starts one thread per rider, each asking for the same quote. Each result is a Long or the Throwable it threw. */
    private List<Thread> askAtOnce(QuoteRequest request, ConcurrentLinkedQueue<Object> results) {
        List<Thread> riders = new ArrayList<>();
        for (int i = 0; i < RIDERS; i++) {
            Thread rider = new Thread(() -> {
                try {
                    results.add(loader.quote(request));
                } catch (Throwable t) {
                    results.add(t);
                }
            }, "rider-" + i);
            riders.add(rider);
            rider.start();
        }
        return riders;
    }

    /** Waits until every rider thread is parked (waiting for the load) or finished, so all of them have already asked. */
    private static void awaitAllParked(List<Thread> riders) {
        while (riders.stream().anyMatch(t -> t.getState() == Thread.State.NEW || t.getState() == Thread.State.RUNNABLE)) {
            Thread.onSpinWait();
        }
    }

    private static void joinAll(List<Thread> riders) throws InterruptedException {
        for (Thread rider : riders) {
            rider.join();
        }
    }

    @Test
    @DisplayName("A quote is computed once and then reused, and different quotes are computed separately")
    void remembersQuotes() {
        assertEquals(700, loader.quote(EBIKE_20), "an EBIKE for 20 minutes costs 100 + 20 x 30 = 700 cents");
        assertEquals(700, loader.quote(EBIKE_20), "the second ask should give the same quote");
        assertEquals(400, loader.quote(CLASSIC_20), "a CLASSIC for 20 minutes costs 100 + 20 x 15 = 400 cents");

        assertEquals(1, pricing.callsFor(EBIKE_20), "the EBIKE quote should reach the pricing service once, then be reused");
        assertEquals(1, pricing.callsFor(CLASSIC_20), "the CLASSIC quote is a different key, so it needs its own call");
    }

    @Test
    @DisplayName("invalidate makes the next ask call the pricing service again")
    void invalidates() {
        loader.quote(EBIKE_20);
        loader.quote(CLASSIC_20);
        loader.invalidate(EBIKE_20);
        loader.quote(EBIKE_20);
        loader.quote(CLASSIC_20);
        assertEquals(2, pricing.callsFor(EBIKE_20), "after invalidate(EBIKE_20), the next ask must compute the quote again");
        assertEquals(1, pricing.callsFor(CLASSIC_20), "invalidate(EBIKE_20) must not touch the CLASSIC quote");

        loader.invalidateAll();
        loader.quote(CLASSIC_20);
        assertEquals(2, pricing.callsFor(CLASSIC_20), "after invalidateAll(), every quote is computed again");
    }

    @Test
    @DisplayName("Eight riders asking for the same quote at once cause exactly one call to the pricing service")
    void singleFlight() {
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            loader.quote(CARGO_5);   // warm up, so every class is loaded before the race
            pricing.gatedKey = EBIKE_20;
            ConcurrentLinkedQueue<Object> results = new ConcurrentLinkedQueue<>();

            List<Thread> riders = askAtOnce(EBIKE_20, results);
            awaitAllParked(riders);  // every rider has asked; the first load is still held at the gate
            pricing.gate.countDown();
            joinAll(riders);

            assertEquals(1, pricing.callsFor(EBIKE_20), RIDERS + " riders asked for the same quote while it was loading. "
                    + "The pricing service should be called exactly once, but was called " + pricing.callsFor(EBIKE_20) + " times");
            assertEquals(RIDERS, results.size(), "every rider should get an answer");
            for (Object result : results) {
                assertEquals(700L, result, "every rider, including the ones that waited, should get the 700 cent quote");
            }
        }, "the quotes did not finish within 3 seconds. Does a waiting rider ever wake up?");
    }

    @Test
    @DisplayName("A failed load reaches every waiting rider as a PricingException and is not remembered")
    void failureIsNotCached() {
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            loader.quote(CARGO_5);
            pricing.gatedKey = EBIKE_20;
            pricing.failNext = true;
            ConcurrentLinkedQueue<Object> results = new ConcurrentLinkedQueue<>();

            List<Thread> riders = askAtOnce(EBIKE_20, results);
            awaitAllParked(riders);
            pricing.gate.countDown();
            joinAll(riders);

            assertEquals(1, pricing.callsFor(EBIKE_20), "the riders who waited must share the one failed call, not each call again");
            assertEquals(RIDERS, results.size(), "every rider should get an answer, even if it is an error");
            for (Object result : results) {
                assertInstanceOf(PricingException.class, result,
                        "every rider should get the PricingException itself. A waiting rider gets it wrapped in a CompletionException: unwrap it with getCause()");
            }

            long retried = assertDoesNotThrow(() -> loader.quote(EBIKE_20),
                    "a failed load must not be remembered: the next ask should call the pricing service again, not throw the old failure");
            assertEquals(700, retried, "after a failure, the next ask should try again and get 700");
            assertEquals(2, pricing.callsFor(EBIKE_20), "a failed load must be removed, so the next ask calls the pricing service again");
        }, "the quotes did not finish within 3 seconds. Does a waiting rider ever wake up after a failure?");
    }

    @Test
    @DisplayName("A failure on a single ask is thrown to the caller, and the next ask tries again")
    void failureThenRetry() {
        PricingService flaky = new PricingService() {
            int calls;

            @Override
            public long quoteCents(QuoteRequest request) {
                calls++;
                if (calls == 1) {
                    throw new PricingException("pricing service timed out");
                }
                return FakePricing.fare(request);
            }
        };
        FareQuoteLoader flakyLoader = new FareQuoteLoader(flaky);

        assertThrows(PricingException.class, () -> flakyLoader.quote(EBIKE_20), "the first call fails, so quote should throw the PricingException");
        long retried = assertDoesNotThrow(() -> flakyLoader.quote(EBIKE_20),
                "the failure must not be remembered: the second ask should call the pricing service again, not throw the old failure");
        assertEquals(700, retried, "the second ask should succeed with 700");
    }

    @Test
    @DisplayName("A slow load for one quote does not block a different quote")
    void slowKeyDoesNotBlockOthers() throws InterruptedException {
        pricing.gatedKey = EBIKE_20;
        ConcurrentLinkedQueue<Object> results = new ConcurrentLinkedQueue<>();
        Thread slowRider = new Thread(() -> {
            try {
                results.add(loader.quote(EBIKE_20));
            } catch (Throwable t) {
                results.add(t);
            }
        });
        slowRider.start();
        try {
            assertTrue(pricing.entered.await(2, TimeUnit.SECONDS), "quote(EBIKE_20) should call the pricing service");
            long classic = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> loader.quote(CLASSIC_20),
                    "the CLASSIC quote waited for the slow EBIKE load. Do not call the pricing service inside a lock "
                    + "or inside computeIfAbsent: one slow key must not block the others");
            assertEquals(400, classic, "the CLASSIC quote is 100 + 20 x 15 = 400 cents");
        } finally {
            pricing.gate.countDown();
            slowRider.join(2000);
        }
        assertEquals(List.of(700L), new ArrayList<>(results), "the slow EBIKE quote should still finish with 700 cents");
    }
}
