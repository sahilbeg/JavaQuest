package jq.m09.hexagonal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// The core runs with test adapters: an in-memory store, a fake payment port and a recording notifier.
// No PayRiver, no network, and a clock frozen at a known time.
class EndRideServiceTest {

    static final Instant START = Instant.parse("2026-11-16T08:00:00Z");

    /** A fake payment adapter: it records every charge and answers with a result chosen by the test. */
    static class FakePayments implements PaymentPort {
        final List<String> charges = new ArrayList<>();
        PaymentResult answer = PaymentResult.PAID;

        @Override
        public PaymentResult charge(String riderId, String rentalId, long amountCents) {
            charges.add(riderId + " " + rentalId + " " + amountCents);
            return answer;
        }
    }

    /** A fake notifier: it remembers every receipt it was given. */
    static class RecordingNotifier implements RiderNotifier {
        final List<RideReceipt> sent = new ArrayList<>();

        @Override
        public void rideEnded(RideReceipt receipt) {
            sent.add(receipt);
        }
    }

    final InMemoryRentalStore store = new InMemoryRentalStore();
    final FakePayments payments = new FakePayments();
    final RecordingNotifier notifier = new RecordingNotifier();

    /** A service whose clock says the time is START plus the given seconds. */
    EndRideUseCase serviceAt(long secondsAfterStart) {
        Clock clock = Clock.fixed(START.plusSeconds(secondsAfterStart), ZoneOffset.UTC);
        return new EndRideService(store, payments, notifier, clock);
    }

    void startRide(String rentalId, String riderId, String bikeId, BikeType type) {
        store.save(Rental.started(rentalId, riderId, bikeId, type, START));
    }

    @Test
    @DisplayName("Ending a 12-minute EBIKE ride returns a receipt with a fare of 460 cents")
    void endsAnEbikeRide() {
        startRide("rental-17", "rider-7", "B-1042", BikeType.EBIKE);

        RideReceipt receipt = serviceAt(12 * 60).endRide("rental-17");

        assertNotNull(receipt, "endRide should return a RideReceipt, not null");
        assertEquals("rental-17", receipt.rentalId(), "The receipt should name the rental");
        assertEquals("rider-7", receipt.riderId(), "The receipt should name the rider");
        assertEquals(12, receipt.minutes(), "08:00 to 08:12 is 12 minutes");
        assertEquals(460, receipt.fareCents(), "An EBIKE costs 100 to unlock plus 30 a minute: 100 + 12 x 30 = 460");
        assertEquals(PaymentResult.PAID, receipt.payment(), "The payment port answered PAID, so the receipt should say PAID");
    }

    @Test
    @DisplayName("Part of a minute is dropped: 20 minutes 50 seconds on a CLASSIC bike costs 400 cents")
    void partialMinutesRoundDown() {
        startRide("rental-18", "rider-8", "B-2001", BikeType.CLASSIC);

        RideReceipt receipt = serviceAt(20 * 60 + 50).endRide("rental-18");

        assertNotNull(receipt, "endRide should return a RideReceipt, not null");
        assertEquals(20, receipt.minutes(),
                "Count whole minutes only: Duration.between(start, now).toMinutes() gives 20 for 20 min 50 s");
        assertEquals(400, receipt.fareCents(), "A CLASSIC bike costs 100 + 20 x 15 = 400 cents");
    }

    @Test
    @DisplayName("The core charges once with the fare in cents, and notifies the rider once with the receipt")
    void chargesOnceAndNotifiesOnce() {
        startRide("rental-19", "rider-9", "B-3007", BikeType.CARGO);

        RideReceipt receipt = serviceAt(10 * 60).endRide("rental-19");

        assertEquals(List.of("rider-9 rental-19 350"), payments.charges,
                "The core should call payments.charge(riderId, rentalId, fareCents) once. "
                        + "A CARGO bike for 10 minutes is 100 + 10 x 25 = 350 cents");
        assertEquals(1, notifier.sent.size(), "notifier.rideEnded should be called exactly once");
        assertEquals(receipt, notifier.sent.get(0), "The notifier should get the same receipt that endRide returns");
    }

    @Test
    @DisplayName("The ended ride is saved in the store with its end time, fare and payment result")
    void savesTheEndedRide() {
        startRide("rental-17", "rider-7", "B-1042", BikeType.EBIKE);

        serviceAt(12 * 60).endRide("rental-17");

        Rental saved = store.findById("rental-17").orElseThrow();
        assertTrue(saved.isEnded(), "After endRide, the stored rental should be ended. Save rental.end(...) in the store.");
        assertEquals(START.plusSeconds(12 * 60), saved.endedAt(), "endedAt should be the clock's time");
        assertEquals(460, saved.fareCents(), "The stored rental should keep the fare, 460 cents");
        assertEquals(PaymentResult.PAID, saved.payment(), "The stored rental should keep the payment result");
    }

    @Test
    @DisplayName("A declined payment still ends the ride, records DECLINED, notifies the rider and does not retry")
    void declinedPaymentIsHandled() {
        payments.answer = PaymentResult.DECLINED;
        startRide("rental-17", "rider-7", "B-1042", BikeType.EBIKE);

        RideReceipt receipt = serviceAt(12 * 60).endRide("rental-17");

        assertNotNull(receipt, "endRide should return a receipt even when the card is declined");
        assertEquals(PaymentResult.DECLINED, receipt.payment(), "The receipt should say DECLINED");
        assertEquals(1, payments.charges.size(), "A declined card should be charged once, not retried in a loop");
        Rental saved = store.findById("rental-17").orElseThrow();
        assertTrue(saved.isEnded(), "The bike is docked, so the ride ends even when the payment is declined");
        assertEquals(PaymentResult.DECLINED, saved.payment(), "The stored rental should record DECLINED, so support can follow up");
        assertEquals(1, notifier.sent.size(), "The rider should still be told the ride ended");
    }

    @Test
    @DisplayName("An unknown rental throws RideNotFoundException and charges nobody")
    void unknownRentalChargesNobody() {
        EndRideUseCase service = serviceAt(60);

        assertThrows(RideNotFoundException.class, () -> service.endRide("rental-404"),
                "endRide of an unknown id should throw RideNotFoundException");
        assertTrue(payments.charges.isEmpty(), "Nobody should be charged for a rental that does not exist");
    }

    @Test
    @DisplayName("Ending a ride twice throws RideAlreadyEndedException and does not charge again")
    void endingTwiceDoesNotChargeTwice() {
        startRide("rental-17", "rider-7", "B-1042", BikeType.EBIKE);
        EndRideUseCase service = serviceAt(12 * 60);
        service.endRide("rental-17");

        assertThrows(RideAlreadyEndedException.class, () -> service.endRide("rental-17"),
                "Ending rental-17 a second time should throw RideAlreadyEndedException");
        assertEquals(1, payments.charges.size(), "The rider should be charged once, not once per tap on End");
    }
}
