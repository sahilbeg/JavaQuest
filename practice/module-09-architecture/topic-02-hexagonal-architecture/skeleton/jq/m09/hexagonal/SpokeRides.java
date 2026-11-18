package jq.m09.hexagonal;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * The composition root: the one place that knows every class. It builds the adapters,
 * plugs them into the core, and drives the use case. Press Run to try it.
 */
public class SpokeRides {

    public static void main(String[] args) {
        Instant start = Instant.parse("2026-11-16T08:00:00Z");
        Clock clock = Clock.fixed(start.plusSeconds(12 * 60 + 40), ZoneOffset.UTC);

        InMemoryRentalStore store = new InMemoryRentalStore();
        store.save(Rental.started("rental-17", "rider-7", "B-1042", BikeType.EBIKE, start));

        PayRiverClient payRiverSandbox = request -> {
            System.out.println("PayRiver got " + request);
            return new PayRiverResponse("ok", "pr_9001");
        };
        PaymentPort payments = new PayRiverPaymentAdapter(payRiverSandbox);
        RiderNotifier pushNotifier = receipt -> System.out.println("Push to " + receipt.riderId()
                + ": your ride cost " + receipt.fareCents() + " cents");

        EndRideUseCase endRide = new EndRideService(store, payments, pushNotifier, clock);
        System.out.println(endRide.endRide("rental-17"));
    }
}
