package jq.m09.layered;

/**
 * Service layer: what RentalService returns for one rental. It is a DTO: only the fields a client needs.
 * The web layer just turns it into JSON. The payment token stays inside the entity.
 */
public record RentalResponse(String id, String riderId, String bikeId, String stationId, String status) {

    public static RentalResponse from(Rental rental) {
        // TODO 6: Copy id, riderId, bikeId, stationId, and the status as text (rental.getStatus().name()).
        return null;
    }
}
