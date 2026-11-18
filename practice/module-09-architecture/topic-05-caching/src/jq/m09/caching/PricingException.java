package jq.m09.caching;

/** Thrown by the pricing service when it cannot answer. Given to you. */
public class PricingException extends RuntimeException {

    public PricingException(String message) {
        super(message);
    }
}
