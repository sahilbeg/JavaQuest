package jq.m05.threads;

import java.util.List;

public class SensorCheck {

    // Already written: pretends to ask one dock sensor over a slow network.
    public static String readSensor(String dockId) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            return dockId + ": interrupted";
        }
        return dockId + ": ok";
    }

    public static List<String> checkAll(List<String> dockIds) throws InterruptedException {
        String[] results = new String[dockIds.size()];
        Thread[] threads = new Thread[dockIds.size()];
        for (int i = 0; i < dockIds.size(); i++) {
            int slot = i;
            String id = dockIds.get(i);
            threads[i] = new Thread(() -> results[slot] = readSensor(id), "check-" + id);
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }
        return List.of(results);
    }

    public static String runOnNewThread(Runnable task) throws InterruptedException {
        String[] name = new String[1];
        Thread worker = new Thread(() -> {
            task.run();
            name[0] = Thread.currentThread().getName();
        }, "sensor-worker");
        worker.start();
        worker.join();
        return name[0];
    }

    public static void main(String[] args) throws InterruptedException {
        List<String> docks = List.of("Harbor-1", "Harbor-2", "Harbor-3", "Harbor-4");
        for (String line : checkAll(docks)) {
            System.out.println(line);
        }
    }
}
