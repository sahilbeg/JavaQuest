package jq.m05.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SensorPool {

    public static String readSensor(String dockId) {
        if (dockId.endsWith("-X")) {
            throw new IllegalStateException("sensor offline");
        }
        return dockId + ": ok";
    }

    public static List<String> checkAll(List<String> dockIds, int poolSize)
            throws InterruptedException, ExecutionException {
        List<String> results = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(poolSize)) {
            List<Future<String>> futures = new ArrayList<>();
            for (String id : dockIds) {
                futures.add(pool.submit(() -> readSensor(id)));
            }
            for (Future<String> future : futures) {
                results.add(future.get());
            }
        }
        return results;
    }

    public static int maxThreadsSeen(List<String> dockIds, int poolSize)
            throws InterruptedException, ExecutionException {
        // Several pool threads add names at the same time, so the HashSet needs a lock.
        Set<String> names = new HashSet<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(poolSize)) {
            List<Future<String>> futures = new ArrayList<>();
            for (String id : dockIds) {
                futures.add(pool.submit(() -> {
                    synchronized (names) {
                        names.add(Thread.currentThread().getName());
                    }
                    return readSensor(id);
                }));
            }
            for (Future<String> future : futures) {
                future.get();
            }
        }
        return names.size();
    }

    public static List<String> checkAllSafely(List<String> dockIds, int poolSize)
            throws InterruptedException {
        List<String> results = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(poolSize)) {
            List<Future<String>> futures = new ArrayList<>();
            for (String id : dockIds) {
                futures.add(pool.submit(() -> readSensor(id)));
            }
            for (int i = 0; i < futures.size(); i++) {
                try {
                    results.add(futures.get(i).get());
                } catch (ExecutionException e) {
                    results.add(dockIds.get(i) + ": failed (" + e.getCause().getMessage() + ")");
                }
            }
        }
        return results;
    }

    public static void main(String[] args) throws Exception {
        List<String> docks = List.of("Harbor-1", "Harbor-2", "Harbor-X", "Depot-1");
        for (String line : checkAllSafely(docks, 2)) {
            System.out.println(line);
        }
    }
}
