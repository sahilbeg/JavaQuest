# Object-oriented design: the reservation desk

Riders want to hold a bike from the app before they walk to the station. Jiraiya's requirements are below, already clarified. The design is done: `BikeType`, `Bike`, `PricingRule` and `DockListener` are given, and so are the fields of `ReservationDesk`. You write the behaviour.

Everything is in package `jq.m06.oodesign`. Money is in whole cents, as `long`.

## The requirements

1. **Docks.** `new ReservationDesk(dockCount, clock, pricing)` makes a station with docks numbered `1` to `dockCount`, all empty. A `dockCount` below 1 throws `IllegalArgumentException`.
2. **Docking.** `dock(dockId, bike)` puts a bike in an empty dock. A dock id that does not exist throws `IllegalArgumentException`. A dock that already holds a bike throws `IllegalStateException`.
3. **Looking.** `bikeAt(dockId)` returns the bike in that dock, or `Optional.empty()` when the dock is empty. An unknown dock id throws `IllegalArgumentException`.
4. **Reserving.** `reserve(riderId, type)` holds one free bike of that type for the rider. "Free" means docked and not held by an active reservation. When several bikes fit, the one in the **lowest dock id** wins. The hold starts now (read from the clock) and ends `HOLD` later, which is 10 minutes. Return the new `Reservation(riderId, bike, dockId, expiresAt)`.
5. **No bike free.** When no free bike of that type exists, `reserve` returns `Optional.empty()`. This is a normal outcome, so it is a result, not an exception.
6. **One per rider.** A rider who already holds an active reservation and calls `reserve` again gets an `IllegalStateException`. Their first reservation stays as it was, and no other bike is held.
7. **Expiry.** `Reservation.isActiveAt(now)` is `true` only while `now` is strictly before `expiresAt`. At `expiresAt` exactly, the hold has expired. An expired hold frees the bike for others and frees the rider to reserve again. Expired holds are dropped lazily: at the start of every method that reads reservations, not by a background thread.
8. **Time comes from the clock.** Always read the time with `clock.instant()`. Never call `Instant.now()`: the tests move the clock forward by hand.
9. **Reading a hold.** `reservationOf(riderId)` returns the rider's active reservation, or `Optional.empty()`.
10. **Picking up.** `pickUp(riderId)` needs an active reservation, or it throws `IllegalStateException` and changes nothing. It removes the reservation, empties the dock, calls `dockFreed(dockId)` on every listener in the order they subscribed, and returns the bike.
11. **Listening.** `onDockFreed(listener)` adds a listener (the observer pattern).
12. **Pricing.** `quoteCents(riderId, minutes)` returns the price of a ride of that length on the rider's reserved bike, by asking the `PricingRule` given to the constructor. Negative minutes throw `IllegalArgumentException`. A rider with no active reservation throws `IllegalStateException`.
13. **Standard pricing.** `StandardPricing.fareCents(bike, minutes)` is 100 cents to unlock, plus `bike.type().perMinuteCents()` for every minute: CLASSIC 15, EBIKE 30, CARGO 25.
14. **Extension.** The desk must only talk to the `PricingRule` interface. The checks plug in their own rules, including a lambda, without changing your desk.
15. **Thread safety.** Many riders tap at the same time. Make every public method of `ReservationDesk` `synchronized`, so four riders racing for the last bike give exactly one winner, and a rider's double tap gives exactly one reservation.

Out of scope: cancelling, docking the same bike twice, broken bikes, and returning a bike after a ride.

## Your TODOs

- **TODO 1:** `Reservation.isActiveAt`.
- **TODO 2:** `StandardPricing.fareCents`.
- **TODO 3 to 13:** `ReservationDesk`. The private helpers `isHeld` and `checkDockExists` are written for you. Fill in `activeReservation` and `dropExpired` too, then use them from the public methods.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
