# Interfaces and default methods

Sam says the charging dock only knows about `EBike`. The new electric scooters need charging too. Teach the dock to charge anything that promises to be `Chargeable`.

1. **TODO 1:** in `Chargeable`, make the default method `needsCharge()` return `true` when `batteryLevel()` is below 20.
2. **TODO 2 to 4:** in `EBike`, store the starting battery between 0 and 100, return it from `batteryLevel()`, and make `charge(percent)` add to it without going above 100.
3. **TODO 5 to 7:** do the same in `Scooter`.
4. **TODO 8:** in `ChargingDock`, make `chargeAll(items, percent)` call `charge(percent)` on every item.
5. **TODO 9:** make `countNeedingCharge(items)` return how many items need a charge.

A small private helper method that keeps a number between 0 and 100 saves you from writing the same `if` checks twice.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
