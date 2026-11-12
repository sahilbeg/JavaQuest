package jq.m02.equalshashcode;

public class LostReport {

    public static int countDistinct(Bike[] bikes) {
        int count = 0;
        for (int i = 0; i < bikes.length; i++) {
            boolean seenBefore = false;
            for (int j = 0; j < i; j++) {
                if (bikes[i].equals(bikes[j])) {
                    seenBefore = true;
                }
            }
            if (!seenBefore) {
                count++;
            }
        }
        return count;
    }

    public static boolean contains(Bike[] bikes, Bike target) {
        for (Bike bike : bikes) {
            if (bike.equals(target)) {
                return true;
            }
        }
        return false;
    }
}
