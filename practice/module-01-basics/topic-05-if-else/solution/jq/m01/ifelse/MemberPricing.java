package jq.m01.ifelse;

public class MemberPricing {

    public static boolean isFreeRide(boolean member, int minutes) {
        return member && minutes <= 30;
    }

    public static double fare(boolean member, int minutes) {
        if (!member) {
            return 1.00 + minutes * 0.15;
        } else if (minutes <= 30) {
            return 0.0;
        } else {
            return (minutes - 30) * 0.15;
        }
    }

    public static String rideLabel(int minutes) {
        if (minutes < 15) {
            return "short";
        } else if (minutes < 60) {
            return "normal";
        } else {
            return "long";
        }
    }

    public static void main(String[] args) {
        System.out.println("Member, 12 minutes: $" + fare(true, 12));
        System.out.println("Member, 42 minutes: $" + fare(true, 42));
        System.out.println("Non-member, 12 minutes: $" + fare(false, 12));
        System.out.println("12 minutes is a " + rideLabel(12) + " ride");
    }
}
