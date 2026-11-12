package jq.m06.creational;

public class BikeFactory {

    public static Bike create(String type) {
        return switch (type) {
            case "classic" -> new ClassicBike();
            case "ebike" -> new EBike();
            case "cargo" -> new CargoBike();
            default -> throw new IllegalArgumentException("unknown bike type: " + type);
        };
    }
}
