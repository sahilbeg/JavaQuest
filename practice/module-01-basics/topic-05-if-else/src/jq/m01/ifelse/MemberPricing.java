package jq.m01.ifelse;

public class MemberPricing {

    public static boolean isFreeRide(boolean member, int minutes) {
        // TODO 1: return true only when the rider is a member AND rode 30 minutes or less
        return false;
    }

    public static double fare(boolean member, int minutes) {
        // TODO 2: non-members pay 1.00 + minutes * 0.15
        // TODO 3: members pay 0 for the first 30 minutes, then 0.15 per extra minute, with no unlock fee
        return 0.0;
    }

    public static String rideLabel(int minutes) {
        // TODO 4: return "short" under 15 minutes, "normal" from 15 to 59, and "long" from 60
        return "";
    }

    public static void main(String[] args) {
        System.out.println("Member, 12 minutes: $" + fare(true, 12));
        System.out.println("Member, 42 minutes: $" + fare(true, 42));
        System.out.println("Non-member, 12 minutes: $" + fare(false, 12));
        System.out.println("12 minutes is a " + rideLabel(12) + " ride");
    }
}
