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
        CompletableFuture<Long> mine = new CompletableFuture<>();
        CompletableFuture<Long> existing = quotes.putIfAbsent(request, mine);
        if (existing != null) {
            return waitFor(existing);
        }
        try {
            long cents = pricing.quoteCents(request);
            mine.complete(cents);
            return cents;
        } catch (RuntimeException e) {
            quotes.remove(request, mine);
            mine.completeExceptionally(e);
            throw e;
        }
    }

    public void invalidate(QuoteRequest request) {
        quotes.remove(request);
    }

    public void invalidateAll() {
        quotes.clear();
    }

    private static long waitFor(CompletableFuture<Long> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw e;
        }
    }
}
