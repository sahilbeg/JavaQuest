package jq.m05.threads;

import java.util.ArrayList;
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
        // TODO 1: make a String array with one slot per dock id
        // TODO 2: start one Thread per dock; thread i stores readSensor(dockIds.get(i)) in slot i
        // TODO 3: join every thread, then return the array as a List
        return new ArrayList<>();
    }

    public static String runOnNewThread(Runnable task) throws InterruptedException {
        // TODO 4: run task on a new Thread named "sensor-worker", wait for it,
        //         and return the name of the thread that ran the task
        return "";
    }

    public static void main(String[] args) throws InterruptedException {
        List<String> docks = List.of("Harbor-1", "Harbor-2", "Harbor-3", "Harbor-4");
        for (String line : checkAll(docks)) {
            System.out.println(line);
        }
    }
}
