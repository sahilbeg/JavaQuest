package jq.m10.capstone;

import java.util.Optional;

/** Core, driven port: what the core needs from rental storage. A map, JDBC or JPA adapter implements it. */
public interface RentalRepository {

    /** A new, never used rental id, like "rental-1". */
    String nextId();

    /** Inserts a new rental, or replaces the stored one with the same id. */
    void save(Rental rental);

    Optional<Rental> findById(String rentalId);

    /** The rider's rental that has not ended yet, if any. */
    Optional<Rental> findActiveByRider(String riderId);

    /** The rental this rider started with this idempotency key, if any. Keys belong to one rider. */
    Optional<Rental> findByIdempotencyKey(String riderId, String idempotencyKey);
}
