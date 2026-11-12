package jq.m05.virtualthreads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class MarathonPayments {

    public static boolean pay(int rider) {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    public static int payAll(int riders) {
        AtomicInteger paid = new AtomicInteger();
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < riders; i++) {
                int rider = i;
                pool.submit(() -> {
                    if (pay(rider)) {
                        paid.incrementAndGet();
                    }
                });
            }
        }
        return paid.get();
    }

    public static boolean ranOnVirtualThread(Runnable task) throws InterruptedException {
        boolean[] seen = new boolean[1];
        Thread t = Thread.ofVirtual().start(() -> {
            task.run();
            seen[0] = Thread.currentThread().isVirtual();
        });
        t.join();
        return seen[0];
    }

    public static List<String> payNamed(List<String> riderIds) throws InterruptedException, ExecutionException {
        List<Future<String>> receipts = new ArrayList<>();
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (String id : riderIds) {
                receipts.add(pool.submit(() -> {
                    pay(0);
                    return "paid " + id;
                }));
            }
        }
        List<String> lines = new ArrayList<>();
        for (Future<String> receipt : receipts) {
            lines.add(receipt.get());
        }
        return lines;
    }

    public static int countTaps(int taps) {
        AtomicInteger count = new AtomicInteger();
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < taps; i++) {
                pool.submit(() -> {
                    count.incrementAndGet();
                });
            }
        }
        return count.get();
    }

    public static void main(String[] args) {
        System.out.println("Marathon riders paid: " + payAll(10_000));
    }
}
