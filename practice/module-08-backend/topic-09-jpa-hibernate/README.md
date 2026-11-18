# JPA and Hibernate

Move stations and bikes from hand-written SQL to JPA, and answer three questions for the ops team.

`Station` and `Bike` are already entities, and `SpokeApiApplication` starts Spring. `StationRepository` is complete. `BikeRepository` is empty, and `StationReport` returns placeholders.

1. **TODO 1 (`Bike.java`):** put `@Enumerated(EnumType.STRING)` on the `type` and `status` fields, so the database stores words like `RENTED`, not numbers.
2. **TODO 2 (`BikeRepository.java`):** declare a derived query that finds bikes by station id and status, sorted by id. The name starts with `findByStationId...`.
3. **TODO 3 (`BikeRepository.java`):** declare a derived query that counts the bikes with one status. It returns `long`.
4. **TODO 4 (`BikeRepository.java`):** declare a JPQL `@Query` that returns the ids of the stations with at least `:min` bikes of a given `:type` and `:status`, sorted by station id. Use `group by` and `having`.
5. **TODO 5 (`StationReport.java`):** `freeBikeIds` calls your TODO 2 method with `BikeStatus.AVAILABLE` and returns the bike ids.
6. **TODO 6 (`StationReport.java`):** `countByStatus` puts one count per `BikeStatus` value into the map.
7. **TODO 7 (`StationReport.java`):** `stationsWithFreeEbikes` calls your TODO 4 query with `BikeType.EBIKE` and `BikeStatus.AVAILABLE`.

Repository methods have no body. Spring Data writes the code when the app starts. If a method name has a typo, the tests stop with `No property ... found for type 'Bike'`. Read the field names in `Bike` and try again.

## How to read the tests

Open `StationReportTest.java`. It uses `@DataJpaTest`, which starts only the JPA part of Spring with an empty in-memory database.

- `@BeforeEach` saves four stations and twelve bikes with `TestEntityManager`. Harbor has two free e-bikes, Riverside has three and Uptown has one.
- Each test calls one `StationReport` method and checks the answer.
- The last test reads the `bike` table with plain SQL, to check that `type` and `status` hold words.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own. A Spring test takes a few seconds to start.
