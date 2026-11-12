# Inheritance and super

`EBike` and `CargoBike` should reuse everything in `Bike` and change only what is different. `Bike.java` is already finished. Fill in the two subclasses.

1. **TODO 1:** in the `EBike` constructor, store the `battery` parameter in the `battery` field. The line `super(id);` already passes the id up to `Bike`.
2. **TODO 2:** in `EBike`, override `pricePerMinute()` with `@Override` so it returns `0.30`.
3. **TODO 3:** in `CargoBike`, override `pricePerMinute()` with `@Override` so it returns `0.25`.
4. **TODO 4:** in `CargoBike`, override `describe()` so it returns `super.describe() + " [cargo]"`. For bike `B-1042` that gives `Bike B-1042 [cargo]`.

Do not copy `fare` into the subclasses. `Bike.fare` calls `pricePerMinute()`, so your overrides change the fare on their own.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
