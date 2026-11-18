package jq.m11.incident;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Sits in front of every request a spoke-api pod handles, and decides what happens when the pod is told
 * to stop (SIGTERM during a rollout).
 */
public final class RequestGate {

    private final Duration gracePeriod;
    private final AtomicInteger inFlight = new AtomicInteger();
    private final CountDownLatch drained = new CountDownLatch(1);
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
        if (!ready) {
            return Response.shuttingDown();
        }
        inFlight.incrementAndGet();
        try {
            if (!ready) {
                // shutdown started between the first check and the count: do not start the work
                return Response.shuttingDown();
            }
            Response response = work.get();
            return stopped ? Response.cutOff() : response;
        } finally {
            if (inFlight.decrementAndGet() == 0 && !ready) {
                drained.countDown();
            }
        }
    }

    /** Stops the pod gracefully: readiness off, let running requests finish, at most gracePeriod, then stop. */
    public ShutdownReport shutdown() throws InterruptedException {
        ready = false;
        int atStart = inFlight.get();
        if (atStart == 0) {
            drained.countDown();
        }
        boolean finished = drained.await(gracePeriod.toMillis(), TimeUnit.MILLISECONDS);
        stopped = true;
        int cutOff = finished ? 0 : inFlight.get();
        return new ShutdownReport(atStart, cutOff);
    }
}
