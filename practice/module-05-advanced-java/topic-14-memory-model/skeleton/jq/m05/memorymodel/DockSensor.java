package jq.m05.memorymodel;

public class DockSensor implements Runnable {

    // TODO 1: make running volatile, so the sensor thread sees stop() from another thread
    private boolean running = true;
    private long readings;

    @Override
    public void run() {
        while (running) {
            readings++;
        }
    }

    public void stop() {
        // TODO 2: set running to false
    }

    public boolean isRunning() {
        return running;
    }
}
