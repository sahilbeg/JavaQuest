package jq.m09.layered;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

/** Persistence layer: keeps rentals in a map. A JPA or JDBC version could replace it later. */
@Repository
public class InMemoryRentalRepository implements RentalRepository {

    private final Map<String, Rental> rentals = new LinkedHashMap<>();
    private int lastNumber = 0;

    @Override
    public synchronized String nextId() {
        lastNumber++;
        return "rental-" + lastNumber;
    }

    @Override
    public synchronized void save(Rental rental) {
        rentals.put(rental.getId(), rental);
    }

    @Override
    public synchronized Optional<Rental> findById(String id) {
        return Optional.ofNullable(rentals.get(id));
    }

    @Override
    public synchronized Optional<Rental> findActiveByBikeId(String bikeId) {
        for (Rental rental : rentals.values()) {
            if (rental.isActive() && rental.getBikeId().equals(bikeId)) {
                return Optional.of(rental);
            }
        }
        return Optional.empty();
    }
}
