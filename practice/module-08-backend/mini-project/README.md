# FEAT-08: The station availability API

Fill in the numbered TODOs in package `jq.m08.feature`:

1. TODOs 1 to 4: a sorted station query in `StationRepository` and three derived count queries in `BikeRepository`. Count in the database, never in a Java loop over every bike.
2. TODOs 5 to 7: `StationService` builds each station's numbers, filters by `minBikes` and `type`, and throws `StationNotFoundException` for an unknown id.
3. TODOs 8 and 9: `StationController` answers `GET /api/v1/stations` and `GET /api/v1/stations/{id}`, with `minBikes` from 0 to 12.
4. TODOs 10 to 12: `StationApiErrors` turns an unknown station into a 404 problem, and a bad `minBikes` or `type` into a 400 problem that names the parameter.

A `RENTED` bike has no station, so it takes no dock. The JSON shapes and the test data are in the KB page.
