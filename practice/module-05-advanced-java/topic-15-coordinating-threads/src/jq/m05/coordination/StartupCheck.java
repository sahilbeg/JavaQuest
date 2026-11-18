package jq.m05.coordination;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class StartupCheck {

    private StartupCheck() {
    }

    public static boolean allPassed(List<Runnable> checks, long timeoutMillis) throws InterruptedException {
        // TODO 5: make a CountDownLatch with one count per check, and an AtomicBoolean called failed
        // TODO 6: start one new Thread per check. It runs the check, sets failed if the check throws
        //         a RuntimeException, and calls countDown() in finally
        // TODO 7: wait with await(timeoutMillis, TimeUnit.MILLISECONDS).
        //         Return true only if every check finished in time and none failed
        return false;
    }
}
