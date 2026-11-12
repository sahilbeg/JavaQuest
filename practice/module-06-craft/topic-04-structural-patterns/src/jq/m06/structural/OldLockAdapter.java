package jq.m06.structural;

public class OldLockAdapter implements Lock {

    // TODO 1: keep the OldLock in a private final field and store it in the constructor.
    public OldLockAdapter(OldLock old) {
    }

    @Override
    public boolean open(String bikeId) {
        // TODO 2: call disengage on the old lock. Code 0 means it opened.
        return false;
    }

    @Override
    public boolean close(String bikeId) {
        // TODO 3: call engage on the old lock. Code 0 means it closed.
        return false;
    }
}
