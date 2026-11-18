package jq.m05.coordination;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class StartupCheck {

    private StartupCheck() {
    }

    public static boolean allPassed(List<Runnable> checks, long timeoutMillis) throws InterruptedException {
        CountDownLatch done = new CountDownLatch(checks.size());
        AtomicBoolean failed = new AtomicBoolean(false);
        for (Runnable check : checks) {
            new Thread(() -> {
                try {
                    check.run();
                } catch (RuntimeException e) {
                    failed.set(true);
                } finally {
                    done.countDown();
                }
            }).start();
        }
        boolean finishedInTime = done.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return finishedInTime && !failed.get();
    }
}
