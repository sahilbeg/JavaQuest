package jq.m10.capstone;

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
        // TODO 3: Idempotency. If idempotencyKey is not null, look for rentals.findByIdempotencyKey(riderId, key).
        //         Found, with the same bikeId and stationId: return that rental (a retry). Found, with a different
        //         bike or station: throw new RentalException.IdempotencyKeyReused(key).
        // TODO 4: The checks, in this order. Each throws the RentalException named here:
        //         the city has no such station -> StationNotFound(stationId, city.id());
        //         no such bike, or the bike is in another city -> BikeNotFound(bikeId, city.id());
        //         the city does not offer the bike's type -> BikeTypeNotOffered(type, city.id());
        //         the bike is not AVAILABLE at this station (bike.isAvailableAt) -> BikeUnavailable(bikeId, stationId);
        //         the rider already has an active rental -> ActiveRentalExists(riderId, thatRental.id()).
        // TODO 5: Make Rental.start(rentals.nextId(), city.id(), riderId, bikeId, bike.type(), stationId,
        //         clock.instant(), idempotencyKey), save it, mark the bike rented, and return the rental.
        return null;
    }

    @Override
    public Rental endRental(String rentalId, String endStationId) {
        // TODO 6: Find the rental with findRental(rentalId) (it throws RentalNotFound for you).
        //         If it is not active, throw new RentalException.RentalAlreadyEnded(rentalId).
        //         If the city has no station endStationId, throw new RentalException.StationNotFound(endStationId, city.id()).
        // TODO 7: now = clock.instant(). The fare is fares.fareCents(type, Duration.between(startedAt, now)).
        //         Save rental.end(endStationId, now, fare), and dock the bike at endStationId.
        // TODO 8: Publish one RideEnded: eventId "ride-ended:" + rental id, the city id, rental, rider, bike, type,
        //         whole minutes (Duration.toMinutes()), the fare, and the serviceDate: the day the ride STARTED
        //         in the city's zone, rental.startedAt().atZone(city.zone()).toLocalDate(). Return the ended rental.
        return null;
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
