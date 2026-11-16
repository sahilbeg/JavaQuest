package jq.m08.tx;

/** Thrown when no wallet exists for a rider id. An unchecked exception. */
public class UnknownRiderException extends RuntimeException {

    public UnknownRiderException(String riderId) {
        super("no wallet for rider " + riderId);
    }
}
