package jq.m10.launch;

/**
 * Web adapter: the JSON for one rental. Times are ISO-8601 text in UTC, like "2026-11-18T09:00:00Z".
 * The idempotency key stays inside: clients never need it back.
 */
public record RentalResponse(String id, String cityId, String riderId, String bikeId, String bikeType,
                             String startStationId, String startedAt, String endStationId, String endedAt,
                             long fareCents, String status) {

    public static RentalResponse from(Rental rental) {
        return new RentalResponse(rental.id(), rental.cityId(), rental.riderId(), rental.bikeId(),
                rental.bikeType().name(), rental.startStationId(), rental.startedAt().toString(),
                rental.endStationId(), rental.endedAt() == null ? null : rental.endedAt().toString(),
                rental.fareCents(), rental.status());
    }
}
