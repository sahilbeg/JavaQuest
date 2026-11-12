package jq.m05.incident;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UnlockService {

    private final Map<String, String> reservations = new ConcurrentHashMap<>();
    private final AtomicInteger payments = new AtomicInteger();

    public boolean unlock(String bikeId, String riderId) {
        writeAuditLine(bikeId);
        return reservations.putIfAbsent(bikeId, riderId) == null;
    }

    public void recordPayment() {
        payments.incrementAndGet();
    }

    public int payments() {
        return payments.get();
    }

    public static void moveBike(Station from, Station to) {
        Station first = from;
        Station second = to;
        if (to.name().compareTo(from.name()) < 0) {
            first = to;
            second = from;
        }
        synchronized (first) {
            writeAuditLine(first.name());
            synchronized (second) {
                if (from.bikes() > 0) {
                    from.take();
                    to.put();
                }
            }
        }
    }

    static void writeAuditLine(String text) {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        UnlockService service = new UnlockService();
        Thread ana = new Thread(() -> System.out.println("Ana unlocked: " + service.unlock("B-1042", "ana")));
        Thread ben = new Thread(() -> System.out.println("Ben unlocked: " + service.unlock("B-1042", "ben")));
        ana.start();
        ben.start();
        ana.join();
        ben.join();
    }
}
