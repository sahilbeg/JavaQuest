package jq.m09.feature;

/** Ride events for the checks. Each fare follows the fare rule unless the name says otherwise. */
final class Rides {

    private Rides() {
    }

    /** rider-7, EBIKE, 12 minutes: 100 + 30 x 12 = 460 cents. */
    static final RideEnded EVT_1001 = new RideEnded("evt-1001", "rental-17", "rider-7", "B-1042", BikeType.EBIKE, 12, 460);

    /** rider-8, CLASSIC, 20 minutes: 100 + 15 x 20 = 400 cents. */
    static final RideEnded EVT_1002 = new RideEnded("evt-1002", "rental-18", "rider-8", "B-2001", BikeType.CLASSIC, 20, 400);

    /** rider-7, CARGO, 10 minutes: 100 + 25 x 10 = 350 cents. */
    static final RideEnded EVT_1003 = new RideEnded("evt-1003", "rental-19", "rider-7", "B-3003", BikeType.CARGO, 10, 350);

    /** rider-7, EBIKE, 10 minutes should be 400 cents, but the event says 350. */
    static final RideEnded EVT_1004_WRONG_FARE = new RideEnded("evt-1004", "rental-20", "rider-7", "B-1042", BikeType.EBIKE, 10, 350);

    /** rider-7, CLASSIC, 4 minutes: 100 + 15 x 4 = 160 cents. */
    static final RideEnded EVT_1005 = new RideEnded("evt-1005", "rental-21", "rider-7", "B-2002", BikeType.CLASSIC, 4, 160);

    /** A ride whose fare follows the rule, for any rider. */
    static RideEnded ride(String eventId, String riderId, int minutes) {
        return new RideEnded(eventId, "rental-" + eventId.substring(4), riderId, "B-2001", BikeType.CLASSIC, minutes,
                Fares.UNLOCK_CENTS + 15L * minutes);
    }

    /** Appends the ride the way the rentals service does: keyed by rider id. */
    static void publish(EventLog log, RideEnded... rides) {
        for (RideEnded ride : rides) {
            log.append(ride.riderId(), ride);
        }
    }
}
