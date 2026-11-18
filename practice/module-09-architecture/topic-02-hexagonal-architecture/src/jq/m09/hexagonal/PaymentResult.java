package jq.m09.hexagonal;

/** Core (domain): how a payment went, in Spoke's own words. No provider's codes appear here. */
public enum PaymentResult {
    PAID,
    DECLINED,
    FAILED
}
