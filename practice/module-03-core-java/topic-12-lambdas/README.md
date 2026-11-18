# Lambdas and method references

Kakashi's sorter has nine Comparator classes. Replace them with short lambdas in `RideSorts.java`. The `Ride` record is already written for you.

1. **TODO 1:** make `byMinutes(rides)` return a new list sorted from the shortest ride to the longest. Use `Comparator.comparing(Ride::minutes)`. Leave the list you were given in its old order.
2. **TODO 2:** make `byStationThenMinutes(rides)` return a new list sorted by station name. Rides at the same station go shortest first. Use `thenComparing`.
3. **TODO 3:** make `removeShort(rides, under)` remove every ride with fewer minutes than `under` from the list itself. Use `removeIf` with a lambda.
4. **TODO 4:** make `upperIds(rides)` return the bike ids in upper case, in list order. Use a loop or `forEach`.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
