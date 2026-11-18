# Strings and their methods

Riders type bike codes in odd ways, like `" b-1042 "` or `B1042`. Make `BikeCode.java` clean them up and check them.

1. **TODO 1 and 2:** `normalize(raw)` removes the spaces at both ends and makes the text upper case. If the result starts with `B` but not `B-`, put a dash after the `B`. So `"b1042"` becomes `"B-1042"` and `" b-1042 "` becomes `"B-1042"`.
2. **TODO 3 and 4:** `isValid(code)` returns `true` only when the code is `B-` followed by exactly 4 digits. Check `length()` is 6 and `startsWith("B-")`. Then loop over indexes 2 to 5 and check each character with `Character.isDigit`.
3. **TODO 5:** `number(code)` returns the 4 digits as text. `number("B-1042")` gives `"1042"`.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
