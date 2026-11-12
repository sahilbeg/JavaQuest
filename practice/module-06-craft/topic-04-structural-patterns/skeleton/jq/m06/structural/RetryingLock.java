package jq.m06.structural;

public class RetryingLock implements Lock {

    // TODO 4: keep the wrapped Lock in a private final field.
    public RetryingLock(Lock inner) {
    }

    @Override
    public boolean open(String bikeId) {
        // TODO 5: open the inner lock. If that fails, try exactly once more and return that answer.
        return false;
    }

    @Override
    public boolean close(String bikeId) {
        // TODO 6: pass close straight on to the inner lock.
        return false;
    }
}
