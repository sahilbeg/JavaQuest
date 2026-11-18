package jq.m10.capstone;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Adapter (driven side): keeps rentals in a map. Fast for tests and demos; a JDBC adapter replaces it in production. */
public class InMemoryRentalRepository implements RentalRepository {

    private final Map<String, Rental> rentals = new LinkedHashMap<>();
    private int lastId = 0;

    @Override
    public synchronized String nextId() {
        lastId++;
        return "rental-" + lastId;
    }

    @Override
    public synchronized void save(Rental rental) {
        rentals.put(rental.id(), rental);
    }

    @Override
    public synchronized Optional<Rental> findById(String rentalId) {
        return Optional.ofNullable(rentals.get(rentalId));
    }

    @Override
    public synchronized Optional<Rental> findActiveByRider(String riderId) {
        // TODO 9: Return the rider's rental that is still active (isActive()), or Optional.empty().
        return Optional.empty();
    }

    @Override
    public synchronized Optional<Rental> findByIdempotencyKey(String riderId, String idempotencyKey) {
        // TODO 10: Return the rental with this riderId AND this idempotencyKey, or Optional.empty().
        //          The same key from another rider is a different key.
        return Optional.empty();
    }

    /** How many rentals are stored. Handy in tests. */
    public synchronized int size() {
        return rentals.size();
    }
}
