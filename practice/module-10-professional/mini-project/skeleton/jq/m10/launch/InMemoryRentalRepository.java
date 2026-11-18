package jq.m10.launch;

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
        return rentals.values().stream()
                .filter(r -> r.riderId().equals(riderId) && r.isActive())
                .findFirst();
    }

    @Override
    public synchronized Optional<Rental> findByIdempotencyKey(String riderId, String idempotencyKey) {
        return rentals.values().stream()
                .filter(r -> r.riderId().equals(riderId) && idempotencyKey.equals(r.idempotencyKey()))
                .findFirst();
    }

    /** How many rentals are stored. Handy in tests. */
    public synchronized int size() {
        return rentals.size();
    }
}
