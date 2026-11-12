package jq.m02.enums;

public enum Membership {
    BASIC, PLUS, PRO;

    public int freeMinutes() {
        switch (this) {
            case PLUS:
                return 30;
            case PRO:
                return 60;
            default:
                return 0;
        }
    }

    public boolean paysUnlockFee() {
        return this == BASIC;
    }
}
