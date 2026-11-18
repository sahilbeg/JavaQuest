package jq.m05.coordination;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class PaymentGate {

    private final Semaphore permits;

    public PaymentGate(int maxConcurrent) {
        // TODO 1: throw IllegalArgumentException when maxConcurrent is less than 1
        this.permits = new Semaphore(maxConcurrent);
    }

    public <T> T call(Supplier<T> work) throws InterruptedException {
        // TODO 2: acquire a permit, return work.get() inside try, and release the permit in finally
        return null;
    }

    public boolean tryCall(Runnable work, long timeoutMillis) throws InterruptedException {
        // TODO 3: wait at most timeoutMillis for a permit with tryAcquire. No permit: return false.
        //         Got one: run the work in try, release in finally, and return true.
        return false;
    }

    public int freeSlots() {
        // TODO 4: return how many permits are free right now (availablePermits)
        return 0;
    }
}
