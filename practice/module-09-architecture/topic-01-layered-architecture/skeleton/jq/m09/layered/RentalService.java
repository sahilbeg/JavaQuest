package jq.m09.layered;

import org.springframework.stereotype.Service;

/**
 * Service layer: one method per thing a rider can do. It runs the rules and talks to storage.
 * It knows nothing about HTTP: no ResponseEntity, no status codes, no requests.
 * With a real database, @Transactional would go on these methods.
 */
@Service
public class RentalService {

    private final RentalRepository rentals;

    public RentalService(RentalRepository rentals) {
        this.rentals = rentals;
    }

    public RentalResponse start(String riderId, String bikeId, String stationId, String paymentToken) {
        // TODO 8: If the bike already has an active rental, throw new BikeUnavailableException(bikeId).
        //         Otherwise make a Rental with rentals.nextId(), save it, and return RentalResponse.from(it).
        return null;
    }

    public RentalResponse find(String id) {
        // TODO 9: Load the rental, or throw new RentalNotFoundException(id), and return it as a RentalResponse.
        return null;
    }

    public RentalResponse end(String id) {
        // TODO 10: Load the rental (or throw RentalNotFoundException), call rental.end(), save it, return the response.
        return null;
    }
}
