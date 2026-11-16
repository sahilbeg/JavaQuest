package dev.javaquest.runner;

import java.time.Duration;

public record RunLimits(Duration timeout, int heapMegabytes, int maxOutputBytes) {

    public RunLimits {
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive: " + timeout);
        }
        if (heapMegabytes < 16) {
            throw new IllegalArgumentException("heapMegabytes must be at least 16: " + heapMegabytes);
        }
        if (maxOutputBytes < 1) {
            throw new IllegalArgumentException("maxOutputBytes must be positive: " + maxOutputBytes);
        }
    }

    public RunLimits withTimeout(Duration newTimeout) {
        return new RunLimits(newTimeout, heapMegabytes, maxOutputBytes);
    }

    public RunLimits withHeapMegabytes(int newHeapMegabytes) {
        return new RunLimits(timeout, newHeapMegabytes, maxOutputBytes);
    }
}
