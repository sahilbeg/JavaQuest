package jq.m08.mvc;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * Keeps rentals in memory and hands out ids rental-1, rental-2, and so on.
 * This class is finished. Your controller calls it and turns its answers into HTTP.
 */
@Service
public class RentalService {

    private final Clock clock;
    private final Map<String, Rental> rentals = new LinkedHashMap<>();
    private int nextNumber = 1;

    public RentalService(Clock clock) {
        this.clock = clock;
    }

    /** Starts a rental now and returns it with its new id. */
    public synchronized Rental start(StartRental request) {
        String id = "rental-" + nextNumber++;
        Rental rental = new Rental(id, request.riderId(), request.bikeId(), request.station(), clock.instant(), null);
        rentals.put(id, rental);
        return rental;
    }

    /** The rental with this id, or an empty Optional when there is none. */
    public synchronized Optional<Rental> find(String id) {
        return Optional.ofNullable(rentals.get(id));
    }

    /** Every rental, oldest first. */
    public synchronized List<Rental> all() {
        return new ArrayList<>(rentals.values());
    }

    /** Only this rider's rentals, oldest first. */
    public synchronized List<Rental> forRider(String riderId) {
        return rentals.values().stream().filter(r -> r.riderId().equals(riderId)).toList();
    }

    /**
     * Ends the rental now. Returns an empty Optional when no rental has this id.
     * Throws RentalAlreadyEndedException when the rental has already ended.
     */
    public synchronized Optional<Rental> end(String id) {
        Rental rental = rentals.get(id);
        if (rental == null) {
            return Optional.empty();
        }
        if (rental.endedAt() != null) {
            throw new RentalAlreadyEndedException(id);
        }
        Rental ended = rental.endedAt(clock.instant());
        rentals.put(id, ended);
        return Optional.of(ended);
    }

    /** Forgets every rental and starts the ids at rental-1 again. The tests call this before each check. */
    public synchronized void clear() {
        rentals.clear();
        nextNumber = 1;
    }
}
