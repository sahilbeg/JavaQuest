package jq.m09.hexagonal;

/** Thrown by PayRiver's client when PayRiver cannot be reached or times out. */
public class PayRiverException extends RuntimeException {

    public PayRiverException(String message) {
        super(message);
    }
}
