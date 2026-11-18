package jq.m09.hexagonal;

import java.util.Optional;

/** Core, driven port: what the core needs from storage. The core owns this interface. */
public interface RentalStore {

    Optional<Rental> findById(String rentalId);

    void save(Rental rental);
}
