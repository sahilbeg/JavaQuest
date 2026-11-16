package jq.m08.incident;

public class BikeUnavailableException extends RuntimeException {

    public BikeUnavailableException(String bikeId) {
        super(bikeId + " is not available");
    }
}
