package jq.m05.incident;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

final class Crowd {

    private Crowd() {
    }

    static void runTogether(List<Runnable> tasks) throws InterruptedException {
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();
        for (Runnable task : tasks) {
            Thread thread = new Thread(() -> {
                try {
                    start.await();
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            thread.setDaemon(true);
            threads.add(thread);
            thread.start();
        }
        start.countDown();
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
