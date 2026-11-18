package jq.m09.layered;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

/** Persistence layer: keeps rentals in a map. A JPA or JDBC version could replace it later. */
@Repository
public class InMemoryRentalRepository implements RentalRepository {

    // TODO 7: Move the map and the counter here from the controller, and fill in the four methods.
    //         Make each method synchronized: two requests can arrive at once.

    @Override
    public synchronized String nextId() {
        return "";
    }

    @Override
    public synchronized void save(Rental rental) {
    }

    @Override
    public synchronized Optional<Rental> findById(String id) {
        return Optional.empty();
    }

    @Override
    public synchronized Optional<Rental> findActiveByBikeId(String bikeId) {
        return Optional.empty();
    }
}
