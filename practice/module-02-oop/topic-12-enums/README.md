# Enums with fields and methods

Replace the plain Strings for bike types and membership levels with enums, so a typo can never bill a rider $0.

1. **TODO 1:** in `BikeType`, make the constructor store the price in the `price` field.
2. **TODO 2:** make `pricePerMinute()` return that price.
3. **TODO 3:** in `Membership`, make `freeMinutes()` return 0 for `BASIC`, 30 for `PLUS` and 60 for `PRO`.
4. **TODO 4:** make `paysUnlockFee()` return `true` only for `BASIC`.
5. **TODO 5:** in `FareCalculator.fare`, a level that pays the unlock fee pays $1.00 plus every minute at the bike's price.
6. **TODO 6:** other levels pay no unlock fee. Take off their free minutes first, never going below 0, then charge the rest per minute.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
