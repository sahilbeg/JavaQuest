package jq.m09.hexagonal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Adapter (driven side): keeps rentals in a map. Good for tests and demos; a JDBC adapter would replace it. */
public class InMemoryRentalStore implements RentalStore {

    private final Map<String, Rental> rentals = new LinkedHashMap<>();

    @Override
    public synchronized Optional<Rental> findById(String rentalId) {
        return Optional.ofNullable(rentals.get(rentalId));
    }

    @Override
    public synchronized void save(Rental rental) {
        rentals.put(rental.id(), rental);
    }
}
