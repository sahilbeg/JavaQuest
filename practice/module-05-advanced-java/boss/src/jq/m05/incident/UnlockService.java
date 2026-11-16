package jq.m05.incident;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UnlockService {

    private final Map<String, String> reservations = new HashMap<>();
    private int payments;

    public boolean unlock(String bikeId, String riderId) {
        // TODO bronze: another rider can unlock the bike between the check and the put
        if (!reservations.containsKey(bikeId)) {
            writeAuditLine(bikeId);
            reservations.put(bikeId, riderId);
            return true;
        }
        return false;
    }

    public void recordPayment() {
        // TODO silver: payments++ loses counts when threads run it at the same time
        payments++;
    }

    public int payments() {
        return payments;
    }

    public static void moveBike(Station from, Station to) {
        // TODO gold: two threads moving in opposite directions lock in opposite orders
        synchronized (from) {
            writeAuditLine(from.name());
            synchronized (to) {
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
