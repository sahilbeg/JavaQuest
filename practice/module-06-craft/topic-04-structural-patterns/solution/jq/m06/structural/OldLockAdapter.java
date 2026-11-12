package jq.m06.structural;

public class OldLockAdapter implements Lock {

    private final OldLock old;

    public OldLockAdapter(OldLock old) {
        this.old = old;
    }

    @Override
    public boolean open(String bikeId) {
        return old.disengage(bikeId) == 0;
    }

    @Override
    public boolean close(String bikeId) {
        return old.engage(bikeId) == 0;
    }
}
