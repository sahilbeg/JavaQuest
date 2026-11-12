package jq.m06.structural;

class FlakyLock implements Lock {

    private int failuresLeft;
    int opens = 0;

    FlakyLock(int failuresBeforeSuccess) {
        this.failuresLeft = failuresBeforeSuccess;
    }

    @Override
    public boolean open(String bikeId) {
        opens++;
        if (failuresLeft > 0) {
            failuresLeft--;
            return false;
        }
        return true;
    }

    @Override
    public boolean close(String bikeId) {
        return true;
    }
}
