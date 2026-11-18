# Records

Replace the long `Ride` class with a short record, then add up a list of rides.

`Ride` is already a record with three components: `bikeId`, `type` and `minutes`. Java writes the accessors, `equals`, `hashCode` and `toString` for you.

1. **TODO 1:** in the compact constructor of `Ride`, turn negative minutes into 0.
2. **TODO 2:** make `fare()` return the $1.00 unlock fee plus `minutes` times the price per minute. Classic is 0.15, ebike is 0.30, cargo is 0.25.
3. **TODO 3:** in `RideLog`, make `totalMinutes` add up the minutes of every ride.
4. **TODO 4:** make `totalFare` add up the fare of every ride.
5. **TODO 5:** make `longest` return the ride with the most minutes. Return `null` when the array is empty.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
