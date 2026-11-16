package jq.m08.apidesign;

/** Thrown when a well-formed rental id, such as rental-99, matches no rental. */
public class RentalNotFoundException extends RuntimeException {

    private final String rentalId;

    public RentalNotFoundException(String rentalId) {
        super("No rental with id " + rentalId);
        this.rentalId = rentalId;
    }

    public String rentalId() {
        return rentalId;
    }
}
