package jq.m08.incident;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RentalService {

    static final long UNLOCK_HOLD_CENTS = 100;

    private final BikeRepository bikes;
    private final RentalRepository rentals;
    private final PaymentGateway payments;
    private final BikeLock locks;

    public RentalService(BikeRepository bikes, RentalRepository rentals, PaymentGateway payments, BikeLock locks) {
        this.bikes = bikes;
        this.rentals = rentals;
        this.payments = payments;
        this.locks = locks;
    }

    /**
     * Pay-and-unlock. The bike is reserved first, so nobody else can take it while the bank answers.
     * Either every step happens, or none of them does.
     */
    // TODO bronze: a jammed lock after the bike was reserved leaves a rented bike and a rental nobody rides.
    //             Make all the steps one transaction.
    // TODO silver: a declined payment still leaves the bike rented. PaymentDeclinedException is a checked
    //             exception, and Spring does not roll back for those unless you ask it to.
    public Rental startRental(String riderId, String bikeId) throws PaymentDeclinedException {
        Bike bike = bikes.findById(bikeId).orElseThrow(() -> new BikeUnavailableException(bikeId));
        if (bike.getStatus() != BikeStatus.AVAILABLE) {
            throw new BikeUnavailableException(bikeId);
        }
        String station = bike.getStationId();
        bike.markRented();
        bikes.save(bike);
        Rental rental = rentals.save(new Rental(riderId, bikeId, station));
        payments.hold(riderId, UNLOCK_HOLD_CENTS);
        locks.open(bikeId);
        return rental;
    }
}
