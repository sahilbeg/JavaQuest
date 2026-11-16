package jq.m08.validation;

/** Thrown when no bike has the id the rider typed. */
public class BikeNotFoundException extends RuntimeException {

    public BikeNotFoundException(String bikeId) {
        super("No bike has the id " + bikeId + ". Check the number on the bike's frame.");
    }
}
