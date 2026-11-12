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
        // TODO 1: make a fixed pool of poolSize threads inside try-with-resources
        // TODO 2: submit one Callable per dock that returns readSensor(id), and keep each Future in a list
        // TODO 3: call get() on the futures in submit order and collect the results
        return new ArrayList<>();
    }

    public static int maxThreadsSeen(List<String> dockIds, int poolSize)
            throws InterruptedException, ExecutionException {
        // TODO 4: make a HashSet called names
        // TODO 5: in each task, inside synchronized (names) { ... }, add Thread.currentThread().getName(), then read the sensor
        // TODO 6: wait for every task, then return the size of the set
        return 0;
    }

    public static List<String> checkAllSafely(List<String> dockIds, int poolSize)
            throws InterruptedException {
        // TODO 7: like checkAll, but catch ExecutionException around get()
        //         and add "<id>: failed (<message of getCause()>)" for that dock
        return new ArrayList<>();
    }

    public static void main(String[] args) throws Exception {
        List<String> docks = List.of("Harbor-1", "Harbor-2", "Harbor-X", "Depot-1");
        for (String line : checkAllSafely(docks, 2)) {
            System.out.println(line);
        }
    }
}
