package jq.m06.structural;

public class RetryingLock implements Lock {

    private final Lock inner;

    public RetryingLock(Lock inner) {
        this.inner = inner;
    }

    @Override
    public boolean open(String bikeId) {
        if (inner.open(bikeId)) {
            return true;
        }
        return inner.open(bikeId);
    }

    @Override
    public boolean close(String bikeId) {
        return inner.close(bikeId);
    }
}
