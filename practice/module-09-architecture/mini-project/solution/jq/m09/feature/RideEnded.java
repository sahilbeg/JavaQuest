package jq.m09.feature;

/**
 * The event the rentals service writes to the "ride-events" log when a ride ends.
 * Ready to use: do not change.
 *
 * @param eventId   unique for every event, for example "evt-1001"
 * @param rentalId  for example "rental-17"
 * @param riderId   for example "rider-7"; also the event's key in the log
 * @param bikeId    for example "B-1042"
 * @param type      the bike's type, which sets the price per minute
 * @param minutes   how long the ride took
 * @param fareCents the fare the rentals service worked out, in cents
 */
public record RideEnded(String eventId, String rentalId, String riderId, String bikeId,
        BikeType type, int minutes, long fareCents) {
}
