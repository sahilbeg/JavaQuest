package jq.m09.feature;

/** The payment provider. Ready to use: do not change. The checks use a fake. */
public interface PaymentGateway {

    /**
     * Charges the rider's card.
     *
     * @param idempotencyKey the provider charges at most once per key, even if it is sent again
     * @return the payment id, for example "pay-301"
     * @throws PaymentFailedException when this attempt failed; another attempt may work
     */
    String charge(String riderId, long cents, String idempotencyKey);
}
