package jq.m10.launch;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Core: the rental use cases for ONE city. Everything city-specific comes from its CityConfig.
 * It talks only to its ports and a Clock: no Spring, no SQL, no HTTP.
 */
public class RentalService implements RentalUseCases {

    private final CityConfig city;
    private final RentalRepository rentals;
    private final BikeInventory bikes;
    private final EventPublisher events;
    private final Clock clock;
    private final FareCalculator fares;

    public RentalService(CityConfig city, RentalRepository rentals, BikeInventory bikes, EventPublisher events,
                         Clock clock) {
        this.city = city;
        this.rentals = rentals;
        this.bikes = bikes;
        this.events = events;
        this.clock = clock;
        this.fares = new FareCalculator(city);
    }

    @Override
    public Rental startRental(String riderId, String bikeId, String stationId, String idempotencyKey) {
        if (idempotencyKey != null) {
            Optional<Rental> earlier = rentals.findByIdempotencyKey(riderId, idempotencyKey);
            if (earlier.isPresent()) {
                Rental first = earlier.get();
                if (first.bikeId().equals(bikeId) && first.startStationId().equals(stationId)) {
                    return first;
                }
                throw new RentalException.IdempotencyKeyReused(idempotencyKey);
            }
        }

        if (!city.hasStation(stationId)) {
            throw new RentalException.StationNotFound(stationId, city.id());
        }
        Bike bike = bikes.findById(bikeId)
                .filter(b -> b.cityId().equals(city.id()))
                .orElseThrow(() -> new RentalException.BikeNotFound(bikeId, city.id()));
        if (!city.offers(bike.type())) {
            throw new RentalException.BikeTypeNotOffered(bike.type(), city.id());
        }
        if (!bike.isAvailableAt(stationId)) {
            throw new RentalException.BikeUnavailable(bikeId, stationId);
        }
        Optional<Rental> riding = rentals.findActiveByRider(riderId);
        if (riding.isPresent()) {
            throw new RentalException.ActiveRentalExists(riderId, riding.get().id());
        }

        Rental rental = Rental.start(rentals.nextId(), city.id(), riderId, bikeId, bike.type(), stationId,
                clock.instant(), idempotencyKey);
        rentals.save(rental);
        bikes.markRented(bikeId);
        return rental;
    }

    @Override
    public Rental endRental(String rentalId, String endStationId) {
        Rental rental = findRental(rentalId);
        if (!rental.isActive()) {
            throw new RentalException.RentalAlreadyEnded(rentalId);
        }
        if (!city.hasStation(endStationId)) {
            throw new RentalException.StationNotFound(endStationId, city.id());
        }

        Instant now = clock.instant();
        Duration ride = Duration.between(rental.startedAt(), now);
        long fare = fares.fareCents(rental.bikeType(), ride);
        Rental ended = rental.end(endStationId, now, fare);
        rentals.save(ended);
        bikes.dock(rental.bikeId(), endStationId);

        LocalDate serviceDate = rental.startedAt().atZone(city.zone()).toLocalDate();
        events.publish(new RideEnded("ride-ended:" + rental.id(), city.id(), rental.id(), rental.riderId(),
                rental.bikeId(), rental.bikeType(), ride.toMinutes(), fare, serviceDate));
        return ended;
    }

    @Override
    public Rental findRental(String rentalId) {
        return rentals.findById(rentalId)
                .filter(r -> r.cityId().equals(city.id()))
                .orElseThrow(() -> new RentalException.RentalNotFound(rentalId));
    }

    @Override
    public StationAvailability availability(String stationId) {
        if (!city.hasStation(stationId)) {
            throw new RentalException.StationNotFound(stationId, city.id());
        }
        List<Bike> docked = bikes.dockedAt(stationId);
        int rentable = (int) docked.stream().filter(b -> city.offers(b.type())).count();
        int ebikes = (int) docked.stream().filter(b -> b.type() == BikeType.EBIKE && city.offers(BikeType.EBIKE)).count();
        int docksFree = Math.max(0, city.docksPerStation() - docked.size());
        return new StationAvailability(stationId, city.stations().get(stationId), city.docksPerStation(), rentable,
                ebikes, docksFree);
    }
}
