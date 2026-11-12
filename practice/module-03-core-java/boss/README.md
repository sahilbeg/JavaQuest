# INC-03: The monthly ride report is wrong

1. **Bronze:** `removeTestRides(rides)` removes rides whose bike id starts with `TEST-`, without crashing, and returns how many it removed.
2. **Silver:** `averageMinutesPerStation(rides)` keeps the decimals: Harbor's 20 and 25 minutes average 22.5.
3. **Gold:** `topStations(rides, n)` returns the `n` stations with the most total minutes, largest first, ties in alphabetical order.
