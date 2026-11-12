# Logging with SLF4J

The kiosk's `Checkout` prints everything with `System.out.println`, including full card numbers. Swap the prints for real log lines.

1. **TODO 1:** in `PaymentLog.mask`, keep only the digits and return `**** **** **** ` plus the last 4. `"4242 4242 4242 4242"` becomes `"**** **** **** 4242"`.
2. **TODO 2:** when there are fewer than 4 digits, return `"****"`.
3. **TODO 3:** give `Checkout` a `private static final Logger` from `LoggerFactory.getLogger(Checkout.class)`.
4. **TODO 4:** when `cardNumber` is `null`, log ERROR `payment failed for {}` with a new `IllegalArgumentException` as the last argument. Return `false`.
5. **TODO 5:** mask the card number before you log it.
6. **TODO 6:** when `cents` is over 50,000, log WARN `payment declined for {} card {}` and return `false`.
7. **TODO 7:** otherwise log INFO `payment ok for {} card {} amount {}` and return `true`.

Use `{}` placeholders for every value. The tests read the log lines and check the level, the text, the attached exception, and that no line holds a full card number.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
