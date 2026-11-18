package jq.m11.incident;

/** Thrown when a rollout plan would let fewer pods serve than Spoke's safety floor allows. */
public class UnsafeRolloutException extends RuntimeException {

    public UnsafeRolloutException(String message) {
        super(message);
    }
}
