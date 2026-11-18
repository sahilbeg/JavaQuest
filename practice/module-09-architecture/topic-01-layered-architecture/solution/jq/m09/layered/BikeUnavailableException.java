package jq.m09.layered;

/** Domain layer: the bike is already out on another rental. It knows nothing about HTTP. */
public class BikeUnavailableException extends RuntimeException {

    private final String bikeId;

    public BikeUnavailableException(String bikeId) {
        super("Bike " + bikeId + " is already out on a ride.");
        this.bikeId = bikeId;
    }

    public String bikeId() {
        return bikeId;
    }
}
