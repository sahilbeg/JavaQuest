# Capstone: Bramford's rentals core

Bramford's council said yes. Before anyone writes a controller or a table, Spoke needs the **core** of the rentals service: plain Java that knows each city's stations, fares and time zone, and enforces every rental rule. You build it hexagon-style, in package `jq.m10.capstone`, with in-memory adapters. The next ticket, the CAPSTONE feature, wraps this same core in a Spring Boot service.

```text
 driving side                core (no Spring, no SQL)                          driven side
 tests, CapstoneDemo  →  RentalUseCases ─ RentalService ─┬→ RentalRepository  ← InMemoryRentalRepository
                                          │              ├→ BikeInventory     ← InMemoryBikeInventory
                                    CityConfig           ├→ EventPublisher    ← RecordingEventPublisher
                                    FareCalculator       └→ Clock
```

## Already written

- **Domain:** `BikeType` (`CLASSIC`, `EBIKE`, `CARGO`), `BikeStatus` (`AVAILABLE`, `RENTED`), `Bike`, `Rental` (`isActive()`, `status()`, `end(...)` returns an ended copy), `RideEnded`, `StationAvailability`.
- **Errors:** `RentalException`, a sealed class. Each kind is a nested class: `RentalNotFound`, `StationNotFound`, `BikeNotFound`, `BikeTypeNotOffered`, `BikeUnavailable`, `ActiveRentalExists`, `RentalAlreadyEnded`, `IdempotencyKeyReused`. Throw them as `new RentalException.BikeUnavailable(bikeId, stationId)`.
- **Ports:** `RentalUseCases` (driving), `RentalRepository`, `BikeInventory`, `EventPublisher` (driven), plus `java.time.Clock`.
- **Adapters:** `InMemoryBikeInventory`, `RecordingEventPublisher`, and most of `InMemoryRentalRepository`.
- `CityConfig.riverton()`, and `RentalService.findRental` and `availability`.
- `CapstoneDemo`: a composition root with a fixed clock. Press **Run** once your TODOs are done.

## What to do

1. **TODO 1, `CityConfig.bramford()`:** id `bramford`, name `Bramford`, zone `Europe/London`, unlock 120 cents, 10 docks per station. Per-minute prices: `CLASSIC` 15, `EBIKE` 35. Leave `CARGO` out: a type missing from `perMinuteCents` is not offered. Stations in this order: `castle-hill` (Castle Hill), `canal-street` (Canal Street), `market-square` (Market Square), `university` (University), `bramford-central` (Bramford Central).
2. **TODO 2, `FareCalculator.fareCents(type, ride)`:**
   - If the city does not offer the type, throw `new RentalException.BikeTypeNotOffered(type, city.id())`. A negative ride throws `IllegalArgumentException`.
   - Minutes are whole minutes, **rounded down**: `ride.toMinutes()`. 12 min 59 s is 12 minutes; 59 s is 0.
   - Fare = `unlockCents + perMinuteCents(type) × minutes`. Riverton EBIKE 12 min: 100 + 30 × 12 = 460. Bramford EBIKE 12 min: 120 + 35 × 12 = 540.
3. **TODOs 3 to 5, `RentalService.startRental(riderId, bikeId, stationId, idempotencyKey)`**, in this order:
   1. If `idempotencyKey` is not null and `rentals.findByIdempotencyKey(riderId, key)` finds a rental: with the same `bikeId` and `stationId`, return that rental and change nothing (a retry). Otherwise throw `IdempotencyKeyReused(key)`.
   2. The city has no such station: `StationNotFound(stationId, city.id())`.
   3. No such bike, or the bike's `cityId` is another city: `BikeNotFound(bikeId, city.id())`.
   4. The city does not offer the bike's type: `BikeTypeNotOffered(type, city.id())`.
   5. The bike is not `AVAILABLE` at that station (`bike.isAvailableAt(stationId)`): `BikeUnavailable(bikeId, stationId)`.
   6. The rider already has an active rental: `ActiveRentalExists(riderId, activeRental.id())`.
   7. Otherwise: `Rental.start(rentals.nextId(), city.id(), riderId, bikeId, bike.type(), stationId, clock.instant(), idempotencyKey)`, save it, `bikes.markRented(bikeId)`, and return it.
   A rejected request saves nothing and changes no bike.
4. **TODOs 6 to 8, `RentalService.endRental(rentalId, endStationId)`:**
   - `findRental(rentalId)` (it throws `RentalNotFound` for an unknown id or another city's rental). Not active: `RentalAlreadyEnded(rentalId)`. Unknown end station: `StationNotFound(endStationId, city.id())`.
   - `now = clock.instant()`, fare from `fares.fareCents(type, Duration.between(startedAt, now))`. Save `rental.end(endStationId, now, fare)` and `bikes.dock(bikeId, endStationId)`.
   - Publish one `RideEnded`: eventId `"ride-ended:" + rentalId`, cityId, rentalId, riderId, bikeId, bikeType, whole minutes (rounded down), fare, and `serviceDate`: the day the ride **started**, in the city's zone: `rental.startedAt().atZone(city.zone()).toLocalDate()`. Return the ended rental.
5. **TODOs 9 and 10, `InMemoryRentalRepository`:** `findActiveByRider` returns the rider's rental that is still active; `findByIdempotencyKey(riderId, key)` returns the rental with that rider **and** that key. The same key from another rider is a different key.

## Rules the checks enforce

- No core class (domain, errors, ports, `FareCalculator`, `RentalService`) mentions `org.springframework`, `java.sql`, `javax.sql`, `jakarta` or an adapter class. The check reads the compiled classes, so a mention inside a method counts.
- `RentalService` implements `RentalUseCases`, and its one constructor takes `(CityConfig, RentalRepository, BikeInventory, EventPublisher, Clock)`.
- `RentalService` and `FareCalculator` never write a city's name (`"bramford"`, `"riverton"`): every city difference comes from `CityConfig`.

## How to read the checks

Bramford's launch-day fleet, the same one the CAPSTONE feature uses:

| Station | CLASSIC | EBIKE |
|---|---|---|
| `castle-hill` | BR-2001, BR-2002 | BR-2003, BR-2004 |
| `canal-street` | BR-2011, BR-2012 | BR-2013, BR-2014 |
| `market-square` | BR-2021, BR-2022 | BR-2023, BR-2024 |
| `university` | BR-2031, BR-2032 | BR-2033, BR-2034 |
| `bramford-central` | BR-2041, BR-2042 | BR-2043, BR-2044 |

Ids ending in 1 or 2 are CLASSIC, ids ending in 3 or 4 are EBIKE. The truck also dropped `BR-2900`, a CARGO bike, at `market-square` by mistake. Bramford does not rent CARGO.

`World` builds the test world: a clock at 2026-11-18 09:00 UTC, and one inventory with bikes of both cities. From Bramford's fleet it docks `BR-2002` (CLASSIC) and `BR-2003` (EBIKE) at `castle-hill`, `BR-2011` (CLASSIC) at `canal-street`, and the CARGO bike `BR-2900` at `market-square`. From Riverton: `B-1042` EBIKE and `B-2001` CLASSIC at `harbor`. `CityFaresTest` checks the configs and fares, `StartRentalTest` and `EndRentalTest` the use cases, and `CoreRulesTest` the shape of the hexagon.
