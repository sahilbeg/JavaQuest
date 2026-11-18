package jq.m09.caching;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

/** Remembers fare quotes, and makes sure only one call per quote ever reaches the slow pricing service at a time. */
public final class FareQuoteLoader {

    private final PricingService pricing;
    private final ConcurrentHashMap<QuoteRequest, CompletableFuture<Long>> quotes = new ConcurrentHashMap<>();

    public FareQuoteLoader(PricingService pricing) {
        this.pricing = pricing;
    }

    public long quote(QuoteRequest request) {
        // TODO 11: single flight.
        //          Make a new CompletableFuture<Long> called mine, and putIfAbsent(request, mine).
        //          Someone else's future came back? Return waitFor(it): that quote is loaded, or loading right now.
        //          Otherwise you are the loader: call the pricing service OUTSIDE any lock, complete mine with the
        //          result and return it.
        // TODO 12: if the pricing service throws a RuntimeException: remove(request, mine) so the failure is not
        //          remembered, completeExceptionally(e) so the waiting riders wake up, then throw e.
        return 0;
    }

    public void invalidate(QuoteRequest request) {
        // TODO 14: forget this one quote
    }

    public void invalidateAll() {
        // TODO 15: forget every quote
    }

    private static long waitFor(CompletableFuture<Long> future) {
        // TODO 13: return future.join(). join() wraps a failure in a CompletionException:
        //          catch it, and throw its cause instead when the cause is a RuntimeException.
        return 0;
    }
}
