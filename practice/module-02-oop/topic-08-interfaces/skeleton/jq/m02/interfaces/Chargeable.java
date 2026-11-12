package jq.m02.interfaces;

public interface Chargeable {
    int batteryLevel();

    void charge(int percent);

    default boolean needsCharge() {
        // TODO 1: return true when batteryLevel() is below 20
        return false;
    }
}
