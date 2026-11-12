package jq.m05.concurrentcollections;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TelemetryPipe {

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public void send(String item) throws InterruptedException {
        // TODO 6: put item into the queue
    }

    public static List<String> drain(TelemetryPipe pipe, int expected) throws InterruptedException {
        // TODO 7: take exactly expected items from pipe.queue, in order, and return them in a list
        return new ArrayList<>();
    }
}
