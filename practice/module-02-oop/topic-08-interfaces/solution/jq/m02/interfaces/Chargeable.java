package jq.m02.interfaces;

public interface Chargeable {
    int batteryLevel();

    void charge(int percent);

    default boolean needsCharge() {
        return batteryLevel() < 20;
    }
}
