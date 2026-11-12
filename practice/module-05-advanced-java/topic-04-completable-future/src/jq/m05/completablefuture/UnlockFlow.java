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
        // TODO 1: start charge(riderId) and releaseLock(bikeId) with supplyAsync, so both run at once
        // TODO 2: chain sendReceipt on the payment id with thenCompose
        // TODO 3: combine the lock and the receipt with thenCombine into "released B-1042 | receipt for P-r1"
        return CompletableFuture.completedFuture("");
    }

    public static CompletableFuture<String> unlockOrFallback(String riderId, String bikeId) {
        // TODO 4: add exceptionally to unlock(...) and return "unlock failed: <message>"
        //         use getCause() when the exception is a CompletionException
        return CompletableFuture.completedFuture("");
    }

    public static List<String> unlockAll(List<String> riderIds, String bikeIdPrefix) {
        // TODO 5: start unlockOrFallback for rider i with bike id bikeIdPrefix + (i + 1)
        // TODO 6: wait with CompletableFuture.allOf(...).join(), then join each future in order
        return new ArrayList<>();
    }

    public static void main(String[] args) {
        System.out.println(unlockOrFallback("r1", "B-1042").join());
        System.out.println(unlockOrFallback("BLOCKED", "B-1043").join());
        System.out.println(unlockAll(List.of("r1", "r2", "r3"), "B-104"));
    }
}
