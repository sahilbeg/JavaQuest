package jq.m05.coordination;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class PaymentGate {

    private final Semaphore permits;

    public PaymentGate(int maxConcurrent) {
        if (maxConcurrent < 1) {
            throw new IllegalArgumentException("maxConcurrent must be at least 1, was " + maxConcurrent);
        }
        this.permits = new Semaphore(maxConcurrent);
    }

    public <T> T call(Supplier<T> work) throws InterruptedException {
        permits.acquire();
        try {
            return work.get();
        } finally {
            permits.release();
        }
    }

    public boolean tryCall(Runnable work, long timeoutMillis) throws InterruptedException {
        if (!permits.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) {
            return false;
        }
        try {
            work.run();
            return true;
        } finally {
            permits.release();
        }
    }

    public int freeSlots() {
        return permits.availablePermits();
    }
}
