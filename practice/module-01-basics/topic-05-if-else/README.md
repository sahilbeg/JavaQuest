# Decisions: if, else, boolean logic

Teach `MemberPricing.java` to treat members fairly. Members ride free for their first 30 minutes.

1. **TODO 1:** make `isFreeRide(member, minutes)` return `true` only when the rider is a member and rode 30 minutes or less.
2. **TODO 2:** in `fare(member, minutes)`, charge non-members `1.00 + minutes * 0.15`.
3. **TODO 3:** in the same method, charge members `0` for the first 30 minutes, then `0.15` for each extra minute. Members pay no unlock fee.
4. **TODO 4:** make `rideLabel(minutes)` return `"short"` under 15 minutes, `"normal"` from 15 to 59, and `"long"` from 60.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
