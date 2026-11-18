package jq.m09.layered;

import java.util.Optional;

/** Persistence layer: what the service needs from storage. Any storage can sit behind it. */
public interface RentalRepository {

    /** The id for the next new rental: rental-1, then rental-2, and so on. */
    String nextId();

    /** Stores the rental, replacing any rental with the same id. */
    void save(Rental rental);

    /** The rental with this id, or empty when there is none. */
    Optional<Rental> findById(String id);

    /** The ACTIVE rental of this bike, or empty when the bike is free. */
    Optional<Rental> findActiveByBikeId(String bikeId);
}
