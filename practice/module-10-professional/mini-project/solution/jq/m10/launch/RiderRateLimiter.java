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
        Instant now = clock.instant();
        Deque<Instant> times = recent.computeIfAbsent(riderId, id -> new ArrayDeque<>());
        Instant windowStart = now.minus(WINDOW);
        while (!times.isEmpty() && !times.peekFirst().isAfter(windowStart)) {
            times.pollFirst();
        }
        if (times.size() >= LIMIT) {
            long waitMillis = Duration.between(now, times.peekFirst().plus(WINDOW)).toMillis();
            long seconds = Math.max(1, (waitMillis + 999) / 1000);
            throw new RateLimitedException(riderId, seconds);
        }
        times.addLast(now);
    }
}
