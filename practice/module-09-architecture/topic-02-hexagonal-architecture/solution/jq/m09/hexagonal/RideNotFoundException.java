package jq.m09.hexagonal;

/** Core (domain): there is no rental with this id. */
public class RideNotFoundException extends RuntimeException {

    public RideNotFoundException(String rentalId) {
        super("No rental with id " + rentalId + " exists.");
    }
}
