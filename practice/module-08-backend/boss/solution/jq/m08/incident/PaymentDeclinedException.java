package jq.m08.incident;

/** The bank said no. A checked exception: callers must decide what to tell the rider. */
public class PaymentDeclinedException extends Exception {

    public PaymentDeclinedException(String riderId) {
        super("payment declined for " + riderId);
    }
}
