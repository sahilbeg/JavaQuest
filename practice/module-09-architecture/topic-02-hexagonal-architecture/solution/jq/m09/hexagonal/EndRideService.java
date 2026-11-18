package jq.m09.hexagonal;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Core: the "end a ride" use case. It talks only to ports and a Clock.
 * It does not know PayRiver, a database or HTTP exist.
 */
public class EndRideService implements EndRideUseCase {

    private final RentalStore rentals;
    private final PaymentPort payments;
    private final RiderNotifier notifier;
    private final Clock clock;

    public EndRideService(RentalStore rentals, PaymentPort payments, RiderNotifier notifier, Clock clock) {
        this.rentals = rentals;
        this.payments = payments;
        this.notifier = notifier;
        this.clock = clock;
    }

    @Override
    public RideReceipt endRide(String rentalId) {
        Rental rental = rentals.findById(rentalId).orElseThrow(() -> new RideNotFoundException(rentalId));
        if (rental.isEnded()) {
            throw new RideAlreadyEndedException(rentalId);
        }
        Instant now = clock.instant();
        int minutes = (int) Duration.between(rental.startedAt(), now).toMinutes();
        long fare = rental.type().fareCents(minutes);

        PaymentResult payment = payments.charge(rental.riderId(), rental.id(), fare);
        rentals.save(rental.end(now, fare, payment));

        RideReceipt receipt = new RideReceipt(rental.id(), rental.riderId(), minutes, fare, payment);
        notifier.rideEnded(receipt);
        return receipt;
    }
}
