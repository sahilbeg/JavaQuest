package jq.m09.caching;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** A stand-in for the slow pricing service. It counts calls, can hold one key's calls at a gate, and can fail once. */
final class FakePricing implements PricingService {

    final AtomicInteger calls = new AtomicInteger();
    final Map<QuoteRequest, AtomicInteger> callsPerKey = new ConcurrentHashMap<>();
    /** Counted down each time a call for the gated key starts. */
    final CountDownLatch entered = new CountDownLatch(1);
    /** Calls for this key wait here until the check opens the gate. */
    volatile QuoteRequest gatedKey;
    final CountDownLatch gate = new CountDownLatch(1);
    /** When true, the next call for the gated key throws PricingException (after the gate). */
    volatile boolean failNext;

    static long fare(QuoteRequest request) {
        long perMinute = switch (request.bikeType()) {
            case CLASSIC -> 15;
            case EBIKE -> 30;
            case CARGO -> 25;
        };
        return 100 + perMinute * request.minutes();
    }

    @Override
    public long quoteCents(QuoteRequest request) {
        calls.incrementAndGet();
        callsPerKey.computeIfAbsent(request, k -> new AtomicInteger()).incrementAndGet();
        if (request.equals(gatedKey)) {
            entered.countDown();
            try {
                gate.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (failNext) {
                failNext = false;
                throw new PricingException("pricing service timed out");
            }
        }
        return fare(request);
    }

    int callsFor(QuoteRequest request) {
        AtomicInteger count = callsPerKey.get(request);
        return count == null ? 0 : count.get();
    }
}
