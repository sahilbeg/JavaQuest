package jq.m05.completablefuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class UnlockFlow {

    public static String charge(String riderId) {
        pause(20);
        if (riderId.equals("BLOCKED")) {
            throw new IllegalStateException("card declined");
        }
        return "P-" + riderId;
    }

    public static String releaseLock(String bikeId) {
        pause(10);
        return "released " + bikeId;
    }

    public static String sendReceipt(String paymentId) {
        pause(10);
        return "receipt for " + paymentId;
    }

    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static CompletableFuture<String> unlock(String riderId, String bikeId) {
        CompletableFuture<String> payment = CompletableFuture.supplyAsync(() -> charge(riderId));
        CompletableFuture<String> lock = CompletableFuture.supplyAsync(() -> releaseLock(bikeId));
        CompletableFuture<String> receipt =
                payment.thenCompose(id -> CompletableFuture.supplyAsync(() -> sendReceipt(id)));
        return lock.thenCombine(receipt, (lockLine, receiptLine) -> lockLine + " | " + receiptLine);
    }

    public static CompletableFuture<String> unlockOrFallback(String riderId, String bikeId) {
        return unlock(riderId, bikeId).exceptionally(ex -> {
            Throwable real = ex;
            if (ex instanceof CompletionException && ex.getCause() != null) {
                real = ex.getCause();
            }
            return "unlock failed: " + real.getMessage();
        });
    }

    public static List<String> unlockAll(List<String> riderIds, String bikeIdPrefix) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (int i = 0; i < riderIds.size(); i++) {
            futures.add(unlockOrFallback(riderIds.get(i), bikeIdPrefix + (i + 1)));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        List<String> results = new ArrayList<>();
        for (CompletableFuture<String> future : futures) {
            results.add(future.join());
        }
        return results;
    }

    public static void main(String[] args) {
        System.out.println(unlockOrFallback("r1", "B-1042").join());
        System.out.println(unlockOrFallback("BLOCKED", "B-1043").join());
        System.out.println(unlockAll(List.of("r1", "r2", "r3"), "B-104"));
    }
}
