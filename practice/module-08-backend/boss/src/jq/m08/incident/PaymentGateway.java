package jq.m08.incident;

/** Talks to the bank. Holding money reserves it on the rider's card until the ride ends. */
public interface PaymentGateway {

    void hold(String riderId, long cents) throws PaymentDeclinedException;
}
