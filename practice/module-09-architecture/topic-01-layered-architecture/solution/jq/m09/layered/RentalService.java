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
        if (rentals.findActiveByBikeId(bikeId).isPresent()) {
            throw new BikeUnavailableException(bikeId);
        }
        Rental rental = new Rental(rentals.nextId(), riderId, bikeId, stationId, paymentToken);
        rentals.save(rental);
        return RentalResponse.from(rental);
    }

    public RentalResponse find(String id) {
        return RentalResponse.from(load(id));
    }

    public RentalResponse end(String id) {
        Rental rental = load(id);
        rental.end();
        rentals.save(rental);
        return RentalResponse.from(rental);
    }

    private Rental load(String id) {
        return rentals.findById(id).orElseThrow(() -> new RentalNotFoundException(id));
    }
}
