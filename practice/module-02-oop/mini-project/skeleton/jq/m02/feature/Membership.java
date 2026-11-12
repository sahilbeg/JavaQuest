package jq.m02.feature;

public enum Membership {
    BASIC(0, true), PLUS(30, false), PRO(60, false);

    private final int freeMinutes;
    private final boolean paysUnlockFee;

    Membership(int freeMinutes, boolean paysUnlockFee) {
        this.freeMinutes = freeMinutes;
        this.paysUnlockFee = paysUnlockFee;
    }

    public int freeMinutes() {
        // TODO 2: return the free minutes per ride
        return 0;
    }

    public boolean paysUnlockFee() {
        // TODO 3: return whether this level pays the 1.00 unlock fee
        return false;
    }
}
