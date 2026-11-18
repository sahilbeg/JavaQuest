package jq.m09.feature;

/** One successful charge, as the billing database stores it. Ready to use: do not change. */
public record Charge(String eventId, String riderId, long cents, String paymentId) {
}
