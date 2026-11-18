package jq.m09.layered;

/**
 * Service layer: what RentalService returns for one rental. It is a DTO: only the fields a client needs.
 * The web layer just turns it into JSON. The payment token stays inside the entity.
 */
public record RentalResponse(String id, String riderId, String bikeId, String stationId, String status) {

    public static RentalResponse from(Rental rental) {
        return new RentalResponse(rental.getId(), rental.getRiderId(), rental.getBikeId(),
                rental.getStationId(), rental.getStatus().name());
    }
}
