package jq.m09.layered;

/** Domain layer: no rental has this id. It knows nothing about HTTP. */
public class RentalNotFoundException extends RuntimeException {

    private final String rentalId;

    public RentalNotFoundException(String rentalId) {
        super("No rental with id " + rentalId + " exists.");
        this.rentalId = rentalId;
    }

    public String rentalId() {
        return rentalId;
    }
}
