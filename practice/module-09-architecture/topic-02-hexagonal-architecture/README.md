# Hexagonal architecture: plug PayRiver in at the edge

Spoke is moving to a new payment provider, PayRiver. The old ride code turned PayRiver's status text into results right in the middle of the business logic. Build the "end a ride" use case as a hexagon: a core that only knows its own ports, and an adapter that speaks PayRiver.

```text
            driving side                     core                         driven side
  test / web / SpokeRides  →  EndRideUseCase ─ EndRideService ─┬→ RentalStore    ← InMemoryRentalStore
                                                               ├→ PaymentPort    ← PayRiverPaymentAdapter → PayRiverClient
                                                               ├→ RiderNotifier  ← (push, SMS, a test fake)
                                                               └→ Clock
```

## Already written

- **Core (domain):** `BikeType` (with `fareCents(minutes)`: 100 to unlock plus CLASSIC 15, EBIKE 30, CARGO 25 a minute), `Rental` (a record: `isEnded()`, `end(at, fare, result)` returns an ended copy), `PaymentResult` (`PAID`, `DECLINED`, `FAILED`), `RideReceipt`, `RideNotFoundException`, `RideAlreadyEndedException`.
- **Core (ports):** `EndRideUseCase` (driving), and `RentalStore`, `PaymentPort`, `RiderNotifier` (driven).
- **Adapters:** `InMemoryRentalStore`.
- **PayRiver's library** (someone else's code, do not change it): `PayRiverClient`, `PayRiverChargeRequest(customer, amount, currency, reference)`, `PayRiverResponse(status, chargeId)`, `PayRiverException`.
- **`SpokeRides`:** the composition root. It wires the adapters into the core. Press **Run** to try it.

## What to do

1. **TODO 1 to 4** in `EndRideService.endRide(rentalId)`:
   - Find the rental with `rentals.findById`. If there is none, throw `new RideNotFoundException(rentalId)`. If `rental.isEnded()`, throw `new RideAlreadyEndedException(rentalId)`. Charge nobody in either case.
   - `now` is `clock.instant()`. Minutes are whole minutes, rounded down: `(int) Duration.between(rental.startedAt(), now).toMinutes()`. So 20 min 50 s is 20 minutes.
   - The fare is `rental.type().fareCents(minutes)`. 12 minutes on B-1042, an EBIKE, is 460 cents.
   - Call `payments.charge(rental.riderId(), rental.id(), fare)` **exactly once**. Never retry here.
   - Save `rental.end(now, fare, result)` in the store, whatever the result. The bike is docked, so the ride ends even when the card is declined.
   - Build `new RideReceipt(rentalId, riderId, minutes, fare, result)`, pass it to `notifier.rideEnded`, and return it.
2. **TODO 5:** delete the leftover method `wasPaid`. It names `PayRiverResponse`, so the core still depends on PayRiver.
3. **TODO 6 and 7** in `PayRiverPaymentAdapter.charge`: build `new PayRiverChargeRequest(riderId, toPayRiverAmount(amountCents), CURRENCY, rentalId)`, call `client.createCharge(request)` once, and return `toResult(response)`. If `createCharge` throws `PayRiverException`, return `PaymentResult.FAILED`.
4. **TODO 8** `toPayRiverAmount(cents)`: PayRiver wants text with a dot and exactly two digits after it, whatever the computer's language.

| cents | PayRiver amount |
|---|---|
| 460 | `"4.60"` |
| 100 | `"1.00"` |
| 4605 | `"46.05"` |
| 5 | `"0.05"` |
| 0 | `"0.00"` |
| 70 | `"0.70"` |
| 123456 | `"1234.56"` (no thousands separator) |

5. **TODO 9** `toResult(response)`: exactly `"ok"` becomes `PAID`, exactly `"declined"` becomes `DECLINED`. Any other status, a `null` status, or a `null` response becomes `FAILED`.

## Rules the checks enforce

- No core class (`BikeType`, `Rental`, `PaymentResult`, `RideReceipt`, both exceptions, the four ports, `EndRideService`) mentions a PayRiver type, an adapter, `InMemoryRentalStore`, `SpokeRides`, `java.sql`, Spring or `jakarta`. The check reads the compiled class, so a mention inside a method body counts too.
- `EndRideService` implements `EndRideUseCase`, and its one constructor takes `(RentalStore, PaymentPort, RiderNotifier, Clock)`.
- The currency is `"USD"`, the `CURRENCY` constant.

## How to read the checks

- `EndRideServiceTest` runs the core with test adapters: the in-memory store, a fake `PaymentPort` that records each charge, a notifier that records each receipt, and a clock frozen at 08:00 UTC plus a few minutes.
- `PayRiverPaymentAdapterTest` runs the adapter alone, against a fake PayRiver that records each request. One test switches the computer's language to German for a moment.
- `HexagonRulesTest` checks the dependency rule: arrows point into the core, never out of it.

Press **Submit** in JavaQuest, or just save the file in your IDE. The checks run on their own.
