package jq.m01.methods;

public class FareRules {

    public static double pricePerMinute(String type) {
        // TODO 1: return 0.15 for "classic", 0.30 for "ebike", 0.25 for "cargo", and 0 for anything else
        return 0.0;
    }

    public static double unlockFee(boolean member) {
        // TODO 2: return 0 for members and 1.00 for everyone else
        return 0.0;
    }

    public static double fare(String type, int minutes, boolean member) {
        // TODO 3: call pricePerMinute(type) and unlockFee(member), then apply the member rule (first 30 minutes free)
        return 0.0;
    }

    public static double fare(int minutes) {
        // TODO 4: return the fare for a classic bike and a non-member by calling the other fare method
        return 0.0;
    }

    public static void main(String[] args) {
        System.out.println("Classic, 10 min: $" + fare(10));
        System.out.println("E-bike, 20 min: $" + fare("ebike", 20, false));
        System.out.println("Member cargo, 40 min: $" + fare("cargo", 40, true));
    }
}
