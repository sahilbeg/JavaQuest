# Static fields and methods

Give every new bike its own id automatically, count the bikes, and keep the prices in one place.

1. **TODO 1:** in `Prices.java`, give the constants their real values: `UNLOCK_FEE` 1.00, `CLASSIC` 0.15, `EBIKE` 0.30, `CARGO` 0.25.
2. **TODO 2:** make `Prices.fare(perMinute, minutes)` return the unlock fee plus `perMinute` times `minutes`.
3. **TODO 3:** in `Bike.java`, make the static method `nextId()` add 1 to `counter` and return `"B-"` followed by `1000 + counter`. The first id is `B-1001`.
4. **TODO 4:** make the constructor store the type and set the id with `nextId()`.
5. **TODO 5:** make `getId()` and `getType()` return their fields, and make the static `created()` return `counter`.

`resetCounter()` is already done. The tests call it before each test, so every test starts from `B-1001`.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
