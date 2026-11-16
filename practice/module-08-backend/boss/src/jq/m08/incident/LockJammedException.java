package jq.m08.incident;

/** The dock's lock did not open. It can happen at any moment, like a crash halfway through. */
public class LockJammedException extends RuntimeException {

    public LockJammedException(String bikeId) {
        super("lock jammed on " + bikeId);
    }
}
