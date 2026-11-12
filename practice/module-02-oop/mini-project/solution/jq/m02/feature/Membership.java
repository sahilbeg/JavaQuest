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
        return freeMinutes;
    }

    public boolean paysUnlockFee() {
        return paysUnlockFee;
    }
}
