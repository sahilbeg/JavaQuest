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
        // TODO 1: Find the rental in the store, or throw new RideNotFoundException(rentalId).
        //         If it has already ended, throw new RideAlreadyEndedException(rentalId).
        // TODO 2: Read the time from the clock. Count whole minutes since startedAt with Duration.between(...).toMinutes(),
        //         and get the fare from rental.type().fareCents(minutes).
        // TODO 3: Charge once through the payment port: payments.charge(riderId, rentalId, fare).
        //         Save rental.end(now, fare, result) in the store, whatever the result was.
        // TODO 4: Build the RideReceipt, pass it to notifier.rideEnded, and return it.
        return null;
    }

    // TODO 5: Delete this leftover from the old code. It ties the core to PayRiver's status codes.
    //         Turning "ok" into PAID is the adapter's job now.
    private static boolean wasPaid(PayRiverResponse response) {
        return response != null && "ok".equals(response.status());
    }
}
