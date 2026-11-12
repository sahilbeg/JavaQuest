package jq.m06.mockito;

/** Sends a receipt to the rider's phone. */
public interface Receipts {

    void send(String riderId, String paymentId);
}
