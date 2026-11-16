package jq.m08.validation;

/** Thrown when the bike exists but is rented or in maintenance. */
public class BikeUnavailableException extends RuntimeException {

    public BikeUnavailableException(String bikeId, BikeStatus status) {
        super("Bike " + bikeId + " is " + status.name().toLowerCase() + " right now. Pick another bike.");
    }
}
