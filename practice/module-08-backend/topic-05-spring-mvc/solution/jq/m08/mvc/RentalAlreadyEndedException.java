package jq.m08.mvc;

/** Thrown by RentalService.end when the rental was already ended. */
public class RentalAlreadyEndedException extends RuntimeException {

    public RentalAlreadyEndedException(String rentalId) {
        super("Rental " + rentalId + " has already ended");
    }
}
