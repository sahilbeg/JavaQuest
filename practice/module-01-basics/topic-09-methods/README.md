# Methods: parameters, return values, scope

Split the fare rules in `FareRules.java` into small methods that each do one job.

1. **TODO 1:** make `pricePerMinute(type)` return `0.15` for `"classic"`, `0.30` for `"ebike"`, `0.25` for `"cargo"`, and `0` for any other type.
2. **TODO 2:** make `unlockFee(member)` return `0` when `member` is `true` and `1.00` when it is `false`.
3. **TODO 3:** make `fare(type, minutes, member)` call `pricePerMinute` and `unlockFee`. A non-member pays the unlock fee plus every minute. A member rides the first 30 minutes free, then pays per minute, with no unlock fee.
4. **TODO 4:** make the overloaded `fare(minutes)` return the fare for a classic bike and a non-member. Call the other `fare` method to do it.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
