package jq.m09.hexagonal;

/** Core (domain): the ride has already ended, so it cannot end, or be charged, again. */
public class RideAlreadyEndedException extends RuntimeException {

    public RideAlreadyEndedException(String rentalId) {
        super("Rental " + rentalId + " has already ended.");
    }
}
