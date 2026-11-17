# Polymorphism and overriding

Shizune wants one fleet report for every kind of bike. `Bike`, `EBike` and `CargoBike` are finished. Fill in the four methods in `FleetReport.java`. Each one takes a `Bike[]` that can hold any mix of bikes.

1. **TODO 1:** `totalFare(bikes, minutes)` adds up `b.fare(minutes)` for every bike. Each bike uses its own price.
2. **TODO 2:** `countEBikes(bikes)` counts the bikes where `b instanceof EBike` is true.
3. **TODO 3:** `lowBatteryIds(bikes, threshold)` returns the ids of e-bikes whose battery is below `threshold`, separated by `", "`. For example `B-2001, B-2003`. Return `""` when there are none. Use `if (b instanceof EBike e)` to reach `getBattery()`.
4. **TODO 4:** `cheapest(bikes)` returns the bike with the lowest `pricePerMinute()`. Return `null` when the array is empty.

Press **Run** to see the report from `main`. Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
