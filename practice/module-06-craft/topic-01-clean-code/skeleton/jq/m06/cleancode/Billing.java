package jq.m06.cleancode;

public class Billing {

    // calculates the fare
    static long calc(int t, int m, int k) {
        // TODO 3: mark calc as @Deprecated and make its body one line that calls fareCents.
        //         BikeType.values()[t] and Membership.values()[k] turn the old codes into enums.
        long r = 0;
        if (m >= 0) {
            if (k == 0) {
                r = r + 100; // add 100
                if (t == 0) {
                    r = r + m * 15;
                } else {
                    if (t == 1) {
                        r = r + m * 30;
                    } else {
                        r = r + m * 25;
                    }
                }
            } else {
                int f = 30;
                if (k == 2) {
                    f = 60;
                }
                if (m > f) {
                    if (t == 0) {
                        r = r + (m - f) * 15;
                    } else {
                        if (t == 1) {
                            r = r + (m - f) * 30;
                        } else {
                            r = r + (m - f) * 25;
                        }
                    }
                }
            }
        }
        return r;
    }

    static long fareCents(BikeType type, int minutes, Membership membership) {
        // TODO 4: reject negative minutes with an IllegalArgumentException that names the bad value.
        // TODO 5: use guard clauses and the enum fields to work out the fare in cents.
        return 0;
    }

    public static void main(String[] args) {
        System.out.println(calc(1, 40, 0));
        System.out.println(fareCents(BikeType.EBIKE, 40, Membership.BASIC));
    }
}
