# Validation and error responses

Make `POST /rentals` check its input, and turn every error into a clear `ProblemDetail` answer that never shows Java internals.

`RentalController` and `BikeCatalog` are already written. The catalog knows these bikes:

| Bike | Where | Status |
|---|---|---|
| `B-1042`, `B-1043` | harbor | available |
| `B-2001` | riverside | rented |
| `B-3003` | depot | in maintenance |
| `B-0000` | a test dock with a broken cable | asking about it throws a surprise `IllegalStateException` |

Any other id throws `BikeNotFoundException`. A bike that is not available throws `BikeUnavailableException`.

1. **TODO 1:** in `StartRentalRequest`, add `@NotBlank` to `riderId` and `station`. Add `@NotNull` and `@Pattern(regexp = "B-\\d{4}", message = "must look like B-1042")` to `bikeId`.
2. **TODO 2:** in `RentalController`, put `@Valid` in front of `@RequestBody`.
3. **TODO 3 and 4:** in `SpokeErrorHandler.handleMethodArgumentNotValid`, set the type to `https://api.spoke.example/problems/invalid-request` and the title to `Invalid request`. Add an `errors` property: a map from each field name to its message.
4. **TODO 5:** answer `BikeNotFoundException` with 404, type `https://api.spoke.example/problems/bike-not-found` and title `Bike not found`.
5. **TODO 6:** answer `BikeUnavailableException` with 409, type `https://api.spoke.example/problems/bike-unavailable` and title `Bike unavailable`.
6. **TODO 7:** add a catch-all for `Exception` that logs the exception and answers 500 with the detail `Something went wrong on our side`.

For 404 and 409, use the exception's message as the detail. It names the bike, and it is safe to show. The 500 answer must not contain the exception's message, an exception class name or a package name.

`SpokeErrorHandler` extends Spring's `ResponseEntityExceptionHandler`. Keep it that way. The base class answers Spring's own errors, like broken JSON, with a 4xx status. Your catch-all then only sees real surprises.

A bad request should get an answer like this:

```json
{"detail":"Some fields are missing or wrong. See errors for each one.","instance":"/rentals","status":400,"title":"Invalid request","type":"https://api.spoke.example/problems/invalid-request","errors":{"bikeId":"must look like B-1042"}}
```

## How to read the tests

Open `RentalErrorsTest.java`. It uses `@WebMvcTest`, so only the web layer starts: the controller and your error handler. The test adds the real `BikeCatalog` with `@Import(BikeCatalog.class)`. Each check sends one `POST /rentals` with MockMvc. Then it reads the status, the Content-Type (`application/problem+json`) and the JSON fields.

A message like "POST /rentals crashed with BikeNotFoundException and nothing turned it into an answer" means no handler caught that exception yet. The surprise-bug checks print an ERROR log line with a stack trace. That is your catch-all doing its job, and one check makes sure it happens.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own. A Spring test takes a few seconds, because Spring starts first.
