# Layered architecture: split the god controller

`RentalController` does everything. It reads HTTP, checks the rules, keeps rentals in a map, and sends the whole `Rental` entity as JSON, payment token included. Split it into layers:

```text
RentalController  (web: HTTP in, HTTP out)
      ↓
RentalService     (service: the rules, one method per action)
      ↓
RentalRepository  (persistence: an interface)  ←  InMemoryRentalRepository (a map)
```

Dependencies point down only. `RentalResponse` is the service's result record: the service returns it, and the controller only sends it as JSON. `ApiErrors` turns domain exceptions into HTTP errors, in one place.

## Already written

- `Rental`: the entity, with its rule `end()`, which throws `RentalAlreadyEndedException` when called twice.
- `RentalStatus`: `ACTIVE` or `ENDED`.
- `BikeUnavailableException`, `RentalNotFoundException`, `RentalAlreadyEndedException`: domain exceptions, with `bikeId()` or `rentalId()`.
- `StartRentalRequest`: the request body, with `@NotBlank` on all four fields: `riderId`, `bikeId`, `stationId` and `paymentToken`. It is like module 08's request, plus `paymentToken`: the card token the app sends so Spoke can charge the rider when the ride ends. The god controller stores it in the entity, and then leaks it.
- `RentalRepository`: the interface the service uses: `nextId()`, `save(rental)`, `findById(id)`, `findActiveByBikeId(bikeId)`.

## What to do

1. **TODO 1** in `RentalController`: delete the map and the counter. Add one field, `RentalService`, set by the only constructor.
2. **TODO 2 to 5** in `RentalController`: add `@Valid` before `@RequestBody`. Make each method one service call:
   - `POST /rentals` calls `start(riderId, bikeId, stationId, paymentToken)` and answers `201 Created` with the header `Location: /rentals/{id}` and the `RentalResponse` as the body.
   - `GET /rentals/{id}` returns `find(id)`.
   - `POST /rentals/{id}/end` returns `end(id)`.
3. **TODO 6** in `RentalResponse.from`: copy `id`, `riderId`, `bikeId`, `stationId`, and the status as text (`"ACTIVE"` or `"ENDED"`).
4. **TODO 7** in `InMemoryRentalRepository`: move the map and the counter here.
   - `nextId()` returns `rental-1`, then `rental-2`, and so on.
   - `save` puts the rental in the map under its id.
   - `findById` returns the rental, or `Optional.empty()`.
   - `findActiveByBikeId` returns the bike's `ACTIVE` rental (`rental.isActive()`), or `Optional.empty()`.
5. **TODO 8 to 10** in `RentalService`:
   - `start`: if the bike has an active rental, throw `new BikeUnavailableException(bikeId)`. Otherwise make `new Rental(rentals.nextId(), riderId, bikeId, stationId, paymentToken)`, save it, and return `RentalResponse.from(rental)`.
   - `find`: return the rental as a `RentalResponse`, or throw `new RentalNotFoundException(id)`.
   - `end`: load it the same way, call `rental.end()`, save it, and return the response.
6. **TODO 11 to 13** in `ApiErrors`: each handler returns a `ProblemDetail`, made with `ProblemDetail.forStatusAndDetail(status, ex.getMessage())`, then `setType`, `setTitle` and `setProperty`:

| Exception | Status | type (after `PROBLEMS`) | title | property |
|---|---|---|---|---|
| `RentalNotFoundException` | 404 | `rental-not-found` | `Rental not found` | `rentalId` |
| `BikeUnavailableException` | 409 | `bike-unavailable` | `Bike unavailable` | `bikeId` |
| `RentalAlreadyEndedException` | 409 | `rental-already-ended` | `Rental already ended` | `rentalId` |

## Rules the checks enforce

- The rental JSON has exactly five fields: `id`, `riderId`, `bikeId`, `stationId`, `status`. Never `paymentToken`.
- A request with a missing or blank field answers `400`, before the service runs.
- `RentalController` has one constructor, taking only a `RentalService`, and keeps no other field.
- No `RentalService` method, constructor or field uses a type from `org.springframework.http`, `org.springframework.web` or `jakarta.servlet`. So no `ResponseEntity`, no `HttpStatus`, no `HttpServletRequest`.
- `RentalService` takes a `RentalRepository` (the interface) in its one constructor, not the in-memory class.

## How to read the checks

- `RentalApiTest` starts the app and sends requests through MockMvc. Each test uses its own bike, so the order does not matter.
- `RentalServiceTest` builds `new RentalService(new InMemoryRentalRepository())` with no Spring at all. That is how fast a service test can be.
- `LayeringTest` uses reflection to check who depends on whom.

Three checks already pass on the god controller. They make sure your refactor keeps what already worked.

Press **Submit** in Cadence, or just save the file in your IDE. The checks run on their own.
