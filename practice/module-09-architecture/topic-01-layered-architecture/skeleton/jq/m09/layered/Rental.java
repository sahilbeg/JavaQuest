package jq.m09.layered;

/**
 * Domain layer: one rental. This is the "entity", the object the system stores.
 * It holds internal data, such as the payment token, that must never reach the JSON.
 * It is a rich model: the rule "a rental ends only once" lives here, in end().
 */
public class Rental {

    private final String id;
    private final String riderId;
    private final String bikeId;
    private final String stationId;
    private final String paymentToken;
    private RentalStatus status = RentalStatus.ACTIVE;

    public Rental(String id, String riderId, String bikeId, String stationId, String paymentToken) {
        this.id = id;
        this.riderId = riderId;
        this.bikeId = bikeId;
        this.stationId = stationId;
        this.paymentToken = paymentToken;
    }

    /** Ends the rental. Throws RentalAlreadyEndedException when it has already ended. */
    public void end() {
        if (status == RentalStatus.ENDED) {
            throw new RentalAlreadyEndedException(id);
        }
        status = RentalStatus.ENDED;
    }

    public boolean isActive() {
        return status == RentalStatus.ACTIVE;
    }

    public String getId() {
        return id;
    }

    public String getRiderId() {
        return riderId;
    }

    public String getBikeId() {
        return bikeId;
    }

    public String getStationId() {
        return stationId;
    }

    /** Internal: used later to charge the rider. Never send it to a client. */
    public String getPaymentToken() {
        return paymentToken;
    }

    public RentalStatus getStatus() {
        return status;
    }
}
