package jq.m06.oodesign;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * The reservation desk of one station. It owns the docks, the reservations and the rules.
 * Read README.md for every rule the checks test.
 */
public class ReservationDesk {

    public static final Duration HOLD = Duration.ofMinutes(10);

    private final Clock clock;
    private final PricingRule pricing;
    private final Map<Integer, Bike> docks = new TreeMap<>();               // dock id -> bike, or null when empty
    private final Map<String, Reservation> byRider = new HashMap<>();       // rider id -> their reservation
    private final List<DockListener> listeners = new ArrayList<>();

    public ReservationDesk(int dockCount, Clock clock, PricingRule pricing) {
        // TODO 3: throw IllegalArgumentException when dockCount is below 1,
        //         then put every dock id from 1 to dockCount in docks, each with the value null (empty)
        this.clock = clock;
        this.pricing = pricing;
    }

    // TODO 4: make every public method below synchronized, so two riders can never win the same bike

    public void dock(int dockId, Bike bike) {
        // TODO 5: call checkDockExists(dockId); throw IllegalStateException if the dock already holds a bike;
        //         otherwise put the bike in the dock
    }

    public Optional<Bike> bikeAt(int dockId) {
        // TODO 6: call checkDockExists(dockId), then return the bike in that dock, or Optional.empty() if it is empty
        return Optional.empty();
    }

    public Optional<Reservation> reserve(String riderId, BikeType type) {
        // TODO 7: read now from the clock and call dropExpired(now).
        //         If the rider still holds a reservation, throw IllegalStateException.
        //         Walk the docks in id order. The first dock with a bike of this type that isHeld does not
        //         report wins: make a Reservation that expires at now plus HOLD, store it in byRider, return it.
        //         If no dock matches, return Optional.empty().
        return Optional.empty();
    }

    public Optional<Reservation> reservationOf(String riderId) {
        // TODO 8: drop expired reservations, then return this rider's reservation, or Optional.empty()
        return Optional.empty();
    }

    public Bike pickUp(String riderId) {
        // TODO 9: get the rider's active reservation (activeReservation does the checks), remove it,
        //         empty its dock, tell every listener dockFreed(dockId) in the order they subscribed,
        //         and return the bike
        return null;
    }

    public long quoteCents(String riderId, int minutes) {
        // TODO 10: throw IllegalArgumentException for negative minutes. Otherwise ask the pricing rule
        //          for the fare of the bike in the rider's active reservation
        return 0;
    }

    public void onDockFreed(DockListener listener) {
        // TODO 11: remember the listener
    }

    private Reservation activeReservation(String riderId) {
        // TODO 12: drop expired reservations, then return the rider's reservation,
        //          or throw IllegalStateException when they have none
        return null;
    }

    private void dropExpired(Instant now) {
        // TODO 13: remove every reservation in byRider that is no longer active at now
    }

    private boolean isHeld(int dockId) {
        for (Reservation reservation : byRider.values()) {
            if (reservation.dockId() == dockId) {
                return true;
            }
        }
        return false;
    }

    private void checkDockExists(int dockId) {
        if (!docks.containsKey(dockId)) {
            throw new IllegalArgumentException("no dock " + dockId + " at this station");
        }
    }
}
