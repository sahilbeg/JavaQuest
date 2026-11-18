package jq.m10.launch;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * A sliding-window log per rider: at most LIMIT start requests in any WINDOW.
 * A request counts for exactly 60 seconds. A refused request does not count.
 */
@Component
public class RiderRateLimiter {

    public static final int LIMIT = 5;
    public static final Duration WINDOW = Duration.ofMinutes(1);

    private final Clock clock;
    private final Map<String, Deque<Instant>> recent = new HashMap<>();

    public RiderRateLimiter(Clock clock) {
        this.clock = clock;
    }

    /** Lets the request through, or throws RateLimitedException with the seconds to wait, rounded up. */
    public synchronized void check(String riderId) {
        // TODO 14 (stage 6): a sliding-window log. now = clock.instant(). Take the rider's deque of times
        //          (recent.computeIfAbsent). Drop times from the front that are not after now minus WINDOW.
        //          If LIMIT times are left, throw new RateLimitedException(riderId, seconds): the time until the
        //          oldest one leaves the window, in whole seconds rounded UP, at least 1. Otherwise add now at the end.
    }
}
