package jq.m09.layered;

/** Domain layer: the rental has already ended, so it cannot end again. */
public class RentalAlreadyEndedException extends RuntimeException {

    private final String rentalId;

    public RentalAlreadyEndedException(String rentalId) {
        super("Rental " + rentalId + " has already ended.");
        this.rentalId = rentalId;
    }

    public String rentalId() {
        return rentalId;
    }
}
