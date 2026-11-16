package jq.m08.apidesign;

/** Thrown when a rental id does not look like rental-17 at all. */
public class InvalidRentalIdException extends RuntimeException {

    private final String rentalId;

    public InvalidRentalIdException(String rentalId) {
        super("Not a rental id: " + rentalId);
        this.rentalId = rentalId;
    }

    public String rentalId() {
        return rentalId;
    }
}
