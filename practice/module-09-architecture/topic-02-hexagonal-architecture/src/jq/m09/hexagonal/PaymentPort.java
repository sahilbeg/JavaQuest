package jq.m09.hexagonal;

/**
 * Core, driven port: what the core needs from a payment provider, in Spoke's words.
 * Amounts are whole cents. No provider's types, formats or status codes appear here.
 */
public interface PaymentPort {

    PaymentResult charge(String riderId, String rentalId, long amountCents);
}
