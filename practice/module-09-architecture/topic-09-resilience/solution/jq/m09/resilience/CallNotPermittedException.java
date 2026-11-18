package jq.m09.resilience;

/** Thrown by an open circuit breaker instead of calling the provider. Given to you. */
public class CallNotPermittedException extends RuntimeException {

    public CallNotPermittedException(String message) {
        super(message);
    }
}
