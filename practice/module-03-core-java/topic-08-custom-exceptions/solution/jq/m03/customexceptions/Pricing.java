package jq.m03.customexceptions;

public class Pricing {

    public static double pricePerMinute(String type) {
        if (type.equals("classic")) {
            return 0.15;
        }
        if (type.equals("ebike")) {
            return 0.30;
        }
        if (type.equals("cargo")) {
            return 0.25;
        }
        throw new UnknownBikeTypeException(type);
    }
}
