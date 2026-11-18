package jq.m10.capstone;

/** Core, driving port: what the outside world (a controller, the kiosk, a test) can ask one city's rental core to do. */
public interface RentalUseCases {

    /**
     * Starts a rental. idempotencyKey may be null. When the same rider sends the same key again
     * for the same bike and station, the first rental comes back and nothing new is created.
     */
    Rental startRental(String riderId, String bikeId, String stationId, String idempotencyKey);

    /** Ends a rental at a station of this city, charges by the minute and publishes RideEnded. */
    Rental endRental(String rentalId, String endStationId);

    Rental findRental(String rentalId);

    StationAvailability availability(String stationId);
}
