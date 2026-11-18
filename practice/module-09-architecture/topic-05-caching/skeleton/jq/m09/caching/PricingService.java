package jq.m09.caching;

/** The slow pricing service. Each call can take hundreds of milliseconds. Given to you. */
public interface PricingService {

    /** The fare in cents for this request. Throws PricingException when the service fails. */
    long quoteCents(QuoteRequest request);
}
