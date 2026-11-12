package jq.m01.methods;

public class FareRules {

    public static double pricePerMinute(String type) {
        switch (type) {
            case "classic":
                return 0.15;
            case "ebike":
                return 0.30;
            case "cargo":
                return 0.25;
            default:
                return 0;
        }
    }

    public static double unlockFee(boolean member) {
        if (member) {
            return 0;
        }
        return 1.00;
    }

    public static double fare(String type, int minutes, boolean member) {
        double rate = pricePerMinute(type);
        double fee = unlockFee(member);
        if (member) {
            if (minutes > 30) {
                return fee + (minutes - 30) * rate;
            }
            return fee;
        }
        return fee + minutes * rate;
    }

    public static double fare(int minutes) {
        return fare("classic", minutes, false);
    }

    public static void main(String[] args) {
        System.out.println("Classic, 10 min: $" + fare(10));
        System.out.println("E-bike, 20 min: $" + fare("ebike", 20, false));
        System.out.println("Member cargo, 40 min: $" + fare("cargo", 40, true));
    }
}
