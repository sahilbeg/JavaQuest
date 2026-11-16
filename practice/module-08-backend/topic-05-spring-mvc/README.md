# Controllers and request mapping

Finish `RentalController` so the rider app can start, find, list and end rentals over HTTP.

`RentalService` is already written. It keeps rentals in memory and hands out ids `rental-1`, `rental-2`, and so on. Your controller reads each request, calls the service, and picks the answer's status. Keep every method short: no rules about rentals belong in the controller.

| Request | Answer |
|---|---|
| `POST /rentals` with a JSON body | 201 Created, a `Location` header of `/rentals/{id}`, and the new rental |
| `GET /rentals/{id}` | 200 with the rental, or 404 when there is no such id |
| `GET /rentals?riderId=r-17` | 200 with that rider's rentals; every rental when `riderId` is left out |
| `POST /rentals/{id}/end` | 200 with the ended rental, 404 for an unknown id, 409 when it already ended |

The JSON body for a new rental looks like this:

```json
{"riderId": "r-17", "bikeId": "B-1042", "station": "harbor"}
```

For now the app also sends the start station, and ending a rental takes no body. End stations and fares come in later tickets.

1. **TODO 1 and 2:** map `start` to `POST /rentals`, read the body with `@RequestBody`, and answer with `ResponseEntity.created(...)`.
2. **TODO 3 and 4:** map `find` to `GET /rentals/{id}`. Throw a `ResponseStatusException` with `HttpStatus.NOT_FOUND` when the service finds nothing.
3. **TODO 5 and 6:** map `list` to `GET /rentals` with an optional `riderId` query parameter.
4. **TODO 7 and 8:** map `end` to `POST /rentals/{id}/end`. Turn an empty answer into 404 and a `RentalAlreadyEndedException` into 409 Conflict.

## How to read the tests

Open `RentalControllerTest.java`. It uses `@WebMvcTest`, so only the web layer starts, with your controller. The test adds the real `RentalService` with `@Import(RentalService.class)`, plus a clock frozen at 08:00 UTC. Before each check, the service forgets every rental, so ids start at `rental-1` again.

Each check sends a pretend request with MockMvc, then reads the status, the headers and the JSON body. A message like "no method in RentalController answered GET /rentals" means a mapping annotation is missing or has the wrong path.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own. A Spring test takes a few seconds, because Spring starts first.
