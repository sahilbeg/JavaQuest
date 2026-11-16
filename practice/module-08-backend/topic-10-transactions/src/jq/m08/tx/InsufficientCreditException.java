package jq.m08.tx;

/** Thrown when a rider tries to give more credit than they have. A checked exception: callers must handle it. */
public class InsufficientCreditException extends Exception {

    public InsufficientCreditException(String riderId, long wanted, long balance) {
        super(riderId + " wants to give " + wanted + " cents but has only " + balance);
    }
}
