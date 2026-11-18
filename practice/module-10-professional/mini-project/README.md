# CAPSTONE: Launch Spoke in Bramford

Wrap Bramford's rentals core in a real Spring Boot service: a web adapter, a JDBC adapter, Problem Details errors, idempotency keys, a readiness probe and a rate limit. Everything is in package `jq.m10.launch`.

**The checks are six stages. Do them in order:** `Stage1RentalApiTest`, `Stage2StationsTest`, `Stage3ErrorsTest`, `Stage4IdempotencyTest`, `Stage5PersistenceTest`, `Stage6LaunchReadinessTest`. Each stage starts its own app, with its own H2 database and a fresh fleet. A later stage may use what an earlier one built. The TODO comments say which stage they belong to.

## Already written: do not change

- **The core**, a copy of the last ticket's solution: `BikeType`, `BikeStatus`, `Bike`, `CityConfig`, `FareCalculator`, `Rental`, `RideEnded`, `StationAvailability`, `RentalException`, the ports `RentalUseCases`, `RentalRepository`, `BikeInventory`, `EventPublisher`, and `RentalService`. Minutes round **down**. Bramford: 120 cents to unlock, CLASSIC 15, EBIKE 35 cents a minute, no CARGO.
- **Adapters:** `InMemoryRentalRepository`, `InMemoryBikeInventory`, `RecordingEventPublisher` (a stand-in for the outbox).
- `LaunchApplication` (JPA is switched off: this service uses plain JDBC), `LaunchWiring` (the composition root, with the launch-day fleet), `StationDirectory`, `RentalResponse`, `RateLimitedException`, and in `LaunchProblems` the handlers for database conflicts and unexpected errors.
- Actuator is not on this project's classpath, so the health checks are a small hand-made `HealthController`. `/health/live` is done.

**Launch-day fleet** (the same as in the capstone ticket). Every station has 10 docks.

| Station | CLASSIC | EBIKE |
|---|---|---|
| `castle-hill` | BR-2001, BR-2002 | BR-2003, BR-2004 |
| `canal-street` | BR-2011, BR-2012 | BR-2013, BR-2014 |
| `market-square` | BR-2021, BR-2022 | BR-2023, BR-2024 |
| `university` | BR-2031, BR-2032 | BR-2033, BR-2034 |
| `bramford-central` | BR-2041, BR-2042 | BR-2043, BR-2044 |

Ids ending in 1 or 2 are CLASSIC, ids ending in 3 or 4 are EBIKE. The truck also dropped `BR-2900`, a CARGO bike, at `market-square` by mistake. Bramford does not rent CARGO.

## Stage 1: the rentals API (TODOs 1 to 3, `RentalController`)

The controller only translates HTTP to calls on `RentalUseCases`, and back with `RentalResponse.from(rental)`.

| Request | Answer |
|---|---|
| `POST /rentals` with `{"riderId":"rider-7","bikeId":"BR-2003","stationId":"castle-hill"}` | `201 Created`, header `Location: /rentals/{id}`, the rental as JSON |
| `GET /rentals/{id}` | `200` and the rental |
| `POST /rentals/{id}/end` with `{"stationId":"canal-street"}` | `200` and the ended rental |

The rental JSON has exactly these fields: `id`, `cityId`, `riderId`, `bikeId`, `bikeType`, `startStationId`, `startedAt`, `endStationId`, `endedAt`, `fareCents`, `status` (`ACTIVE` or `ENDED`). Times are UTC text such as `"2026-11-18T09:00:00Z"`; `endStationId` and `endedAt` are `null` while the ride is on. A 12 min 40 s EBIKE ride costs 540 cents.

## Stage 2: station availability (TODO 4, `StationController`)

`GET /stations/{id}/availability` answers `200` with the core's `StationAvailability`: `{"id":"bramford-central","name":"Bramford Central","docks":10,"bikesAvailable":4,"ebikesAvailable":2,"docksFree":6}`. `bikesAvailable` counts docked bikes a rider can take (types Bramford offers), `ebikesAvailable` is its EBIKE part, and `docksFree` is 10 minus every bike docked there. So the CARGO bike takes a dock at Market Square but is not available.

## Stage 3: errors as Problem Details (TODOs 5 to 7)

Every error is `application/problem+json` with `type` = `https://api.spoke.example/problems/` + the slug below, and this `title` and `status`. Map them all in `LaunchProblems`, the one advice class.

| Error | Status | Type slug | Title |
|---|---|---|---|
| `RentalNotFound` | 404 | `rental-not-found` | Rental not found |
| `StationNotFound` (a station Bramford does not have, in a URL or a body) | 404 | `station-not-found` | Station not found |
| `BikeNotFound` (no such bike in Bramford) | 404 | `bike-not-found` | Bike not found |
| `BikeTypeNotOffered` (the CARGO bike) | 422 | `bike-type-not-offered` | Bike type not offered |
| `BikeUnavailable` | 409 | `bike-unavailable` | Bike unavailable |
| `ActiveRentalExists` | 409 | `active-rental-exists` | Rider already has an active rental |
| `RentalAlreadyEnded` | 409 | `rental-already-ended` | Rental already ended |
| `IdempotencyKeyReused` | 422 | `idempotency-key-reused` | Idempotency key reused |
| a missing or blank body field | 400 | `invalid-request` | Invalid request |

- **Why 422 for CARGO:** the request is well formed, but Bramford can never do it. Waiting will not help, so it is not a 409 conflict with the current state.
- **Validation (TODOs 6 and 7):** every field of `StartRentalRequest` and `EndRentalRequest` is `@NotBlank`, and both `@RequestBody` parameters get `@Valid`. The 400 problem has the same shape as in module 8: type `invalid-request`, title `Invalid request`, and a property `errors`, a map from each bad field name to its message, sorted by field name (keep the first message per field). A body with no `riderId` and a blank `bikeId` gives `errors` with exactly the keys `bikeId` and `riderId`.
- Given: a `DataIntegrityViolationException` from the database becomes `409` type `conflict`, title `Conflict`, and anything unexpected becomes `500` type `internal-error`.

## Stage 4: the Idempotency-Key header (TODO 8)

`POST /rentals` takes an optional header `Idempotency-Key`. Pass it to the core (no header means `null`).

- Same rider, same key, same bike and station: the answer is **`201` again, with the same rental and the same Location**. Nothing new is created and no second bike leaves.
- Same rider, same key, different bike or station: `422` `idempotency-key-reused`.
- Keys belong to one rider: two riders may send the same key text and get two rentals.

## Stage 5: rentals in H2 with JDBC (TODOs 9 to 12, `JdbcRentalRepository`, `LaunchWiring`)

The schema is created in code, in the constructor. Table `rentals`: `id` (primary key), `city_id`, `rider_id`, `bike_id`, `bike_type`, `start_station_id`, `started_at`, `end_station_id`, `ended_at`, `fare_cents`, `idempotency_key`, `active_bike_id`.

- **TODO 9:** add `CONSTRAINT one_active_rental_per_bike UNIQUE (active_bike_id)` and `CONSTRAINT one_rental_per_rider_key UNIQUE (rider_id, idempotency_key)`. `active_bike_id` holds the bike id while the ride is on and `NULL` after it ends. In H2 (and PostgreSQL) a UNIQUE column allows many NULLs, so the database refuses a second **active** rental of one bike, even one written by plain SQL from another server, but allows any number of ended ones.
- **TODO 10, `save`:** `UPDATE` the row's `end_station_id`, `ended_at`, `fare_cents` and `active_bike_id`; if no row changed, `INSERT` all 12 columns. `save` then `findById` must give back an equal `Rental`.
- **TODO 11:** `findActiveByRider` (`ended_at IS NULL`) and `findByIdempotencyKey(riderId, key)`, like the given `findById`.
- **TODO 12:** in `LaunchWiring.rentalRepository`, return `new JdbcRentalRepository(jdbc)` instead of the in-memory map. Stages 1 to 4 must still pass: that is the point of a port.
- When another server already holds an active row for a bike, `POST /rentals` for it answers `409` type `conflict`, and the bike stays docked here.

## Stage 6: launch readiness (TODOs 13 to 16)

- **TODO 13, `GET /health/ready`:** `200` with `{"status":"UP","stationsLoaded":5}` when every one of the city's stations is loaded (`loadedCount()` equals `expectedCount()`, and is not 0). Otherwise `503` with `{"status":"DOWN","stationsLoaded":n}`. `LaunchWiring` loads the stations once the app has started.
- **TODOs 14 to 16, rate limit:** at most `RiderRateLimiter.LIMIT` (5) `POST /rentals` per rider in any 60 seconds, counted by the injected `Clock`. The controller calls `limiter.check(riderId)` first, right after validation, so every valid request counts, even one the core later refuses. A request counts for exactly 60 seconds: one made at 09:00:00 no longer counts at 09:01:00. A refused request does not count.
- The 6th request answers `429` type `rate-limited`, title `Too many requests`, and the header `Retry-After`: the seconds until the oldest counted request leaves the window, rounded **up**, at least 1. Five requests from 09:00:00 to 09:00:40, then one at 09:00:50: `Retry-After: 10`. At 09:00:59.4 it would be `1`, never `0`.
- The limit is per rider: one busy rider never slows down another.

## How the checks work

Each stage class uses `@SpringBootTest` with its own `spring.datasource.url` (an H2 database in memory), `@AutoConfigureMockMvc` to send requests without a network, and a test clock that starts every check at `2026-11-18T09:00:00Z`. Stage 5 also reads the `rentals` table with `JdbcTemplate`, and Stage 6 builds a `HealthController` by hand to see it before the stations are loaded.
