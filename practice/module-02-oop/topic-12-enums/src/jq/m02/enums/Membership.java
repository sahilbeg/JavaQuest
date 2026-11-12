package jq.m02.enums;

public enum Membership {
    BASIC, PLUS, PRO;

    public int freeMinutes() {
        // TODO 3: return 0 for BASIC, 30 for PLUS and 60 for PRO (a switch on this works well)
        return 0;
    }

    public boolean paysUnlockFee() {
        // TODO 4: return true only for BASIC (compare this with == BASIC)
        return false;
    }
}
