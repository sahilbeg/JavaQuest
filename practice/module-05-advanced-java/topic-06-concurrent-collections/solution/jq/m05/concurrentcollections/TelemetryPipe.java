package jq.m05.concurrentcollections;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TelemetryPipe {

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public void send(String item) throws InterruptedException {
        queue.put(item);
    }

    public static List<String> drain(TelemetryPipe pipe, int expected) throws InterruptedException {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < expected; i++) {
            items.add(pipe.queue.take());
        }
        return items;
    }
}
