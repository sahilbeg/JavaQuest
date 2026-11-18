package jq.m10.launch;

/** Thrown by RiderRateLimiter when a rider sends too many POST /rentals. LaunchProblems turns it into a 429. */
public class RateLimitedException extends RuntimeException {

    private final String riderId;
    private final long retryAfterSeconds;

    public RateLimitedException(String riderId, long retryAfterSeconds) {
        super(riderId + " sent too many rental requests. Try again in " + retryAfterSeconds + " s.");
        this.riderId = riderId;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public String riderId() {
        return riderId;
    }

    /** Whole seconds until the rider may try again, at least 1. */
    public long retryAfterSeconds() {
        return retryAfterSeconds;
    }
}
