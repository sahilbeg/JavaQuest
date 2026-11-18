# Clean code: names, functions, comments

`Billing.calc(int t, int m, int k)` works, but nobody can read it. Rewrite the fare rule so the next person can change a price in one minute. Money is in **long cents** everywhere: $1.00 is `100`.

The fare rule: an unlock fee of 100 cents, plus each paid minute at the bike's price. BASIC riders pay everything. PLUS riders get 30 free minutes and no unlock fee. PRO riders get 60 free minutes and no unlock fee.

1. **TODO 1:** in `BikeType`, give each constant a `centsPerMinute` field: CLASSIC 15, EBIKE 30, CARGO 25.
2. **TODO 2:** in `Membership`, give each constant its free minutes (BASIC 0, PLUS 30, PRO 60) and whether it pays the unlock fee (only BASIC does).
3. **TODO 3:** mark `calc` with `@Deprecated` and replace its body with one line that calls `fareCents`. Old callers must still get the same answers.
4. **TODO 4:** in `fareCents`, throw `IllegalArgumentException` for negative minutes. Put the bad value in the message.
5. **TODO 5:** finish `fareCents` with guard clauses, a named constant for the unlock fee, and small helper methods.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
