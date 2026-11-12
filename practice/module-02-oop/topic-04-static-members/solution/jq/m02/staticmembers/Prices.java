package jq.m02.staticmembers;

public class Prices {

    public static final double UNLOCK_FEE = 1.00;
    public static final double CLASSIC = 0.15;
    public static final double EBIKE = 0.30;
    public static final double CARGO = 0.25;

    public static double fare(double perMinute, int minutes) {
        return UNLOCK_FEE + perMinute * minutes;
    }
}
