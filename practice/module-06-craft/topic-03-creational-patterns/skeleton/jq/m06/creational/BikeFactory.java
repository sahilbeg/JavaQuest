package jq.m06.creational;

public class BikeFactory {

    public static Bike create(String type) {
        // TODO 8: return a ClassicBike for "classic", an EBike for "ebike" and a CargoBike for "cargo".
        // TODO 9: throw an IllegalArgumentException for any other type.
        return new ClassicBike();
    }
}
