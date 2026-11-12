package jq.m06.incident;

public enum Membership {
    BASIC(0), PLUS(30), PRO(60);

    private final int freeMinutes;

    Membership(int freeMinutes) {
        this.freeMinutes = freeMinutes;
    }

    public int freeMinutes() {
        return freeMinutes;
    }
}
