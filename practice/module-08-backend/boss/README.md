# INC-08: Half-finished rentals after a crash

Make every unlock all or nothing. Change only `RentalService` and `GroupRentals`.

1. **Bronze:** when the lock jams, `startRental` leaves no trace. The bike is AVAILABLE and docked, with no rental.
2. **Silver:** a declined payment rolls back the same way.
3. **Gold:** in `GroupRentals.unlockAll`, each bike succeeds or fails on its own.
