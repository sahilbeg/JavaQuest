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
 * Every public method is synchronized, so two riders can never both win the last bike.
 */
public class ReservationDesk {

    public static final Duration HOLD = Duration.ofMinutes(10);

    private final Clock clock;
    private final PricingRule pricing;
    private final Map<Integer, Bike> docks = new TreeMap<>();
    private final Map<String, Reservation> byRider = new HashMap<>();
    private final List<DockListener> listeners = new ArrayList<>();

    public ReservationDesk(int dockCount, Clock clock, PricingRule pricing) {
        if (dockCount < 1) {
            throw new IllegalArgumentException("a station needs at least 1 dock, got " + dockCount);
        }
        this.clock = clock;
        this.pricing = pricing;
        for (int dockId = 1; dockId <= dockCount; dockId++) {
            docks.put(dockId, null);
        }
    }

    public synchronized void dock(int dockId, Bike bike) {
        checkDockExists(dockId);
        if (docks.get(dockId) != null) {
            throw new IllegalStateException("dock " + dockId + " already holds " + docks.get(dockId).id());
        }
        docks.put(dockId, bike);
    }

    public synchronized Optional<Bike> bikeAt(int dockId) {
        checkDockExists(dockId);
        return Optional.ofNullable(docks.get(dockId));
    }

    public synchronized Optional<Reservation> reserve(String riderId, BikeType type) {
        Instant now = clock.instant();
        dropExpired(now);
        if (byRider.containsKey(riderId)) {
            throw new IllegalStateException(riderId + " already holds a reservation");
        }
        for (Map.Entry<Integer, Bike> dock : docks.entrySet()) {
            Bike bike = dock.getValue();
            if (bike != null && bike.type() == type && !isHeld(dock.getKey())) {
                Reservation reservation = new Reservation(riderId, bike, dock.getKey(), now.plus(HOLD));
                byRider.put(riderId, reservation);
                return Optional.of(reservation);
            }
        }
        return Optional.empty();
    }

    public synchronized Optional<Reservation> reservationOf(String riderId) {
        dropExpired(clock.instant());
        return Optional.ofNullable(byRider.get(riderId));
    }

    public synchronized Bike pickUp(String riderId) {
        Reservation reservation = activeReservation(riderId);
        byRider.remove(riderId);
        docks.put(reservation.dockId(), null);
        for (DockListener listener : listeners) {
            listener.dockFreed(reservation.dockId());
        }
        return reservation.bike();
    }

    public synchronized long quoteCents(String riderId, int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("minutes cannot be negative, got " + minutes);
        }
        return pricing.fareCents(activeReservation(riderId).bike(), minutes);
    }

    public synchronized void onDockFreed(DockListener listener) {
        listeners.add(listener);
    }

    private Reservation activeReservation(String riderId) {
        dropExpired(clock.instant());
        Reservation reservation = byRider.get(riderId);
        if (reservation == null) {
            throw new IllegalStateException(riderId + " has no active reservation");
        }
        return reservation;
    }

    private void dropExpired(Instant now) {
        byRider.values().removeIf(reservation -> !reservation.isActiveAt(now));
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
