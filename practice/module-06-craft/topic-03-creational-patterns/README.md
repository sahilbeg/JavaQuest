# Creational patterns: factory, builder, singleton

Jiraiya's ticket: a receipt said Uptown to Harbor because two Strings were swapped in a long constructor. The app also makes a new price table for every request. Make tickets hard to build wrong. Money is in whole cents, stored as `int`. PriceTable rates are small whole cents, so `int` is enough.

`Bike`, `ClassicBike`, `EBike` and `CargoBike` are finished. Work in `RideTicket.java`, `PriceTable.java` and `BikeFactory.java`.

1. **TODO 1:** in `RideTicket.Builder`, add a private field for each value: seven fields, in the record's order (bike, from, to, type, minutes, rider, helmet). `type` starts as `"classic"` and `helmet` starts as `false`.
2. **TODO 2:** make every setter store its value and return `this`.
3. **TODO 3:** in `build()`, check `bike`, `from`, `to` and `rider` in that order. Throw `IllegalStateException("missing required field: bike")` (with the right name) for the first one missing.
4. **TODO 4:** throw an `IllegalStateException` when minutes is below 0.
5. **TODO 5:** return a new `RideTicket` made from the fields.
6. **TODO 6:** make the static factory `RideTicket.ebike(...)` use the builder with type `"ebike"`.
7. **TODO 7:** turn `PriceTable` into an enum with one constant, `INSTANCE`. Classic is 15 cents a minute, e-bike 30, cargo 25.
8. **TODO 8 and 9:** make `BikeFactory.create` return the right bike class for `"classic"`, `"ebike"` and `"cargo"`, and throw `IllegalArgumentException` for anything else.

This should work when you are done:

```java
RideTicket t = RideTicket.builder().bike("B-1042").from("Harbor").to("Uptown").minutes(25).rider("r1").build();
```

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
