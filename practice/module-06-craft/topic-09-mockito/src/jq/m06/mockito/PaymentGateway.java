package jq.m06.mockito;

/** Talks to the card payment provider. */
public interface PaymentGateway {

    /** Charges the rider and returns a payment id such as "pay-77". Throws PaymentDeclinedException when the card says no. */
    String charge(String riderId, long cents);

    /** Gives back the money from an earlier charge. */
    void refund(String paymentId);
}
