package jq.m09.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A fake payment provider that records every attempt and fails on command. */
final class FakeGateway implements PaymentGateway {

    /** One call to charge, successful or not. */
    record Attempt(String riderId, long cents, String idempotencyKey, boolean succeeded) {
    }

    private final List<Attempt> attempts = new ArrayList<>();
    private final Map<String, Integer> failuresLeft = new HashMap<>();
    private Runnable beforeEachCharge = () -> { };
    private int nextPayment = 301;

    /** The next `times` attempts with this idempotency key fail. */
    void fail(String idempotencyKey, int times) {
        failuresLeft.put(idempotencyKey, times);
    }

    void beforeEachCharge(Runnable spy) {
        beforeEachCharge = spy;
    }

    List<Attempt> attempts() {
        return attempts;
    }

    List<Attempt> successes() {
        return attempts.stream().filter(Attempt::succeeded).toList();
    }

    long attemptsFor(String idempotencyKey) {
        return attempts.stream().filter(a -> a.idempotencyKey().equals(idempotencyKey)).count();
    }

    @Override
    public String charge(String riderId, long cents, String idempotencyKey) {
        beforeEachCharge.run();
        int left = failuresLeft.getOrDefault(idempotencyKey, 0);
        if (left > 0) {
            failuresLeft.put(idempotencyKey, left - 1);
            attempts.add(new Attempt(riderId, cents, idempotencyKey, false));
            throw new PaymentFailedException("bank timed out");
        }
        attempts.add(new Attempt(riderId, cents, idempotencyKey, true));
        return "pay-" + nextPayment++;
    }
}
