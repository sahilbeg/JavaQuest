# API docs, versioning and error design

Ship version 2 of the rentals API next to version 1, and mark v1 as deprecated. Give both versions errors that partner code can handle.

`RentalService` is already written, and both controllers share it. `find(id)` returns a `Rental`. It throws `InvalidRentalIdException` for an id that does not look like `rental-17`. It throws `RentalNotFoundException` for a rental that does not exist. You change three files.

| Request | Answer |
|---|---|
| `GET /api/v1/rentals/rental-17` | `{"id":"rental-17","bikeId":"B-1042","fareCents":460}` plus two deprecation headers |
| `GET /api/v2/rentals/rental-17` | `{"id":"rental-17","bikeId":"B-1042","fare":{"amountCents":460,"currency":"USD"}}` |
| `GET /api/v2/rentals/rental-99` (either version) | 404 problem of type `https://api.spoke.example/problems/rental-not-found` |
| `GET /api/v2/rentals/B-1042` (either version) | 400 problem of type `https://api.spoke.example/problems/invalid-rental-id` |

1. **TODO 1** in `RentalV1Controller`: send the headers `Deprecation` and `Sunset`, using the constants `DEPRECATION` and `SUNSET`. Use `ResponseEntity.ok().header(...).header(...).body(...)`. Do not change the v1 JSON.
2. **TODO 2** in `RentalV2Controller`: build the `Fare` from `rental.fareCents()` with the currency `"USD"`.
3. **TODO 3 to 5** in `ApiErrors.rentalNotFound`: return a 404 `ProblemDetail` with
   - a `detail` that names the missing id,
   - type `PROBLEMS + "rental-not-found"` and title `Rental not found`,
   - instance `URI.create(request.getRequestURI())`,
   - the extension property `rentalId`, added with `setProperty`.
4. **TODO 6** in `ApiErrors.invalidRentalId`: return a 400 `ProblemDetail` with type `PROBLEMS + "invalid-rental-id"`, title `Invalid rental id`, a detail and the instance.

## How to read the tests

Open `RentalApiTest.java`. It starts the app with `@SpringBootTest` and sends requests through MockMvc.

- `json(r, "$.fare.amountCents")` reads one value from the JSON body, or `null` when it is missing.
- `fields(r)` gives the top-level field names, so a test can check that v1 has no `fare` and v2 has no `fareCents`.
- `assertProblemJson(r)` checks that an error arrives as `application/problem+json`.

The tests check both JSON shapes and the deprecation headers, which only v1 sends. They also check every problem field: `type`, `title`, `status`, `detail`, `instance` and `rentalId`.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
