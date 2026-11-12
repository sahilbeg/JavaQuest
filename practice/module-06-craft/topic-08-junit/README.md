# Testing with JUnit

Build a safety net under Spoke's fare code. `StandardFare.java` already works. Your job is `FareChecks.java`: five checks that pass on today's fare rule and fail the moment someone breaks it.

The fare rule, in cents:

| | Unlock fee | Free minutes |
|---|---|---|
| BASIC | 100 | 0 |
| PLUS | 0 | 30 |
| PRO | 0 | 60 |

Per minute after the free minutes: classic 15, ebike 30, cargo 25. A negative number of minutes throws `IllegalArgumentException`.

Each check takes a `FareCalculator` and uses `assertEquals`, `assertAll` or `assertThrows`. `checkBasicClassic` is a worked example to copy.

1. **TODO 1:** in `checkBasicClassic`, also check a 0 minute BASIC classic ride. It costs only the unlock fee.
2. **TODO 2:** in `checkMembershipFreeMinutes`, check that PLUS and PRO riders pay 0 inside their free minutes.
3. **TODO 3:** in `checkAfterFreeMinutes`, check the first paid minute for PLUS and for PRO.
4. **TODO 4:** in `checkEbikeAndCargo`, check a BASIC e-bike ride and a BASIC cargo ride.
5. **TODO 5:** in `checkRejectsNegativeMinutes`, check that -1 minutes throws `IllegalArgumentException`.

## How your checks are graded

The tests first run every check against the real `StandardFare`. All of them must pass.

Then the tests run your checks against five broken copies of the fare code. Each copy has one bug:

- it forgets the unlock fee for BASIC riders
- it gives PLUS riders 29 free minutes instead of 30
- it charges e-bikes like classic bikes
- it gives PRO riders 30 free minutes instead of 60
- it returns a price for negative minutes

At least one of your checks must fail on each broken copy. A broken copy counts as caught only when one of your assertions fails. Any other exception does not count. Test the edges: zero, both sides of each boundary, and negative minutes.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
