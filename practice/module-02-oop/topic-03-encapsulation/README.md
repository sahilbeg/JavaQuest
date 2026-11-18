# Encapsulation: private fields and getters

Protect the battery in `EBike.java` so it can never leave the range 0 to 100.

1. **TODO 1:** in the constructor, store the id and set the battery through `setBattery`, so 140 becomes 100.
2. **TODO 2:** make `getId()` and `getBattery()` return their fields.
3. **TODO 3:** make `setBattery(int)` clamp the value: below 0 becomes 0, above 100 becomes 100.
4. **TODO 4:** make `charge(int percent)` add the percent, never going above 100.
5. **TODO 5:** make `ride(int minutes)` use 1 percent for every 2 whole minutes, never going below 0. A 5 minute ride uses 2 percent.
6. **TODO 6:** make `canUnlock()` return `true` when the battery is at least 20.

The fields are already `private`, and `id` has no setter, so nobody outside can change it.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
