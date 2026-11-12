package jq.m05.virtualthreads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class MarathonPayments {

    // Provided: the slow payment service. Do not change it.
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
        // TODO 1: open Executors.newVirtualThreadPerTaskExecutor() in try-with-resources
        // TODO 2: submit one task per rider (0 to riders - 1) that calls pay(rider) and counts each true in an AtomicInteger
        // TODO 3: after the try block, return the count
        return 0;
    }

    public static boolean ranOnVirtualThread(Runnable task) throws InterruptedException {
        // TODO 4: start a virtual thread that runs task and records Thread.currentThread().isVirtual()
        // TODO 5: join the thread and return what it recorded
        return false;
    }

    public static List<String> payNamed(List<String> riderIds) throws InterruptedException, ExecutionException {
        // TODO 6: submit one task per id that calls pay(0) and returns "paid " + id; keep each Future in a list
        // TODO 7: after the executor closes, call get() on the futures in order and return the lines
        return new ArrayList<>();
    }

    public static int countTaps(int taps) {
        // TODO 8: in a virtual-thread executor, submit `taps` tasks that each add 1 to an AtomicInteger
        // TODO 9: after the try block, return the count
        return 0;
    }

    public static void main(String[] args) {
        System.out.println("Marathon riders paid: " + payAll(10_000));
    }
}
