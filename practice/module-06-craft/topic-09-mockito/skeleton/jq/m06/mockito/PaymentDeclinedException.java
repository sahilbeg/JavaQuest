package jq.m06.mockito;

/** Thrown by PaymentGateway.charge when the rider's card is declined. */
public class PaymentDeclinedException extends RuntimeException {

    public PaymentDeclinedException(String message) {
        super(message);
    }
}
