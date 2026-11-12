package jq.m06.cleancode;

public enum Membership {
    BASIC, PLUS, PRO;

    // TODO 2: give each membership its free minutes (0, 30, 60) and whether it pays the unlock fee.
    public int freeMinutes() {
        return 0;
    }

    public boolean paysUnlockFee() {
        return false;
    }
}
