package jq.m11.incident;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Sits in front of every request a spoke-api pod handles, and decides what happens when the pod is told
 * to stop (SIGTERM during a rollout).
 *
 * This is the code that ran on Friday.
 */
public final class RequestGate {

    private final Duration gracePeriod;
    private final AtomicInteger inFlight = new AtomicInteger();
    private volatile boolean ready = true;
    private volatile boolean stopped = false;

    public RequestGate(Duration gracePeriod) {
        if (gracePeriod == null || gracePeriod.isNegative()) {
            throw new IllegalArgumentException("gracePeriod must be zero or more");
        }
        this.gracePeriod = gracePeriod;
    }

    /** What the readiness probe reports: true while this pod should get new requests. */
    public boolean ready() {
        return ready;
    }

    /** True once the server has stopped. */
    public boolean stopped() {
        return stopped;
    }

    /** Requests running right now. */
    public int inFlight() {
        return inFlight.get();
    }

    /** Runs one request's work, unless the pod is shutting down. */
    public Response handle(Supplier<Response> work) {
        // TODO 5 (gold): once shutdown has started, answer Response.shuttingDown() without running the work.
        inFlight.incrementAndGet();
        try {
            Response response = work.get();
            return stopped ? Response.cutOff() : response;
        } finally {
            inFlight.decrementAndGet();
        }
    }

    /** Stops the pod gracefully: readiness off, let running requests finish, at most gracePeriod, then stop. */
    public ShutdownReport shutdown() throws InterruptedException {
        int atStart = inFlight.get();
        // TODO 6 (gold): turn readiness off first, so new requests get a 503.
        // TODO 7 (gold): wait until the running requests have finished, but never longer than gracePeriod.
        // Use a bounded wait such as CountDownLatch.await(timeout, unit), never an endless one.
        // Then stop, and report how many requests were still running.
        stopped = true;
        return new ShutdownReport(atStart, 0);
    }
}
