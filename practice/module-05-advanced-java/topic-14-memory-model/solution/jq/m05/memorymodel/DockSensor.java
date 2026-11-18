package jq.m05.memorymodel;

public class DockSensor implements Runnable {

    private volatile boolean running = true;
    private long readings;

    @Override
    public void run() {
        while (running) {
            readings++;
        }
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
