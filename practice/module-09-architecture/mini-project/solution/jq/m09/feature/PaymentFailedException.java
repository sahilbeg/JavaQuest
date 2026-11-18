package jq.m09.feature;

/** One charge attempt failed, for example a timeout at the bank. Ready to use: do not change. */
public class PaymentFailedException extends RuntimeException {

    public PaymentFailedException(String message) {
        super(message);
    }
}
