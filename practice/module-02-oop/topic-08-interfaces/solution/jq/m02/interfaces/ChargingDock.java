package jq.m02.interfaces;

public class ChargingDock {

    public static void chargeAll(Chargeable[] items, int percent) {
        for (Chargeable item : items) {
            item.charge(percent);
        }
    }

    public static int countNeedingCharge(Chargeable[] items) {
        int count = 0;
        for (Chargeable item : items) {
            if (item.needsCharge()) {
                count++;
            }
        }
        return count;
    }
}
