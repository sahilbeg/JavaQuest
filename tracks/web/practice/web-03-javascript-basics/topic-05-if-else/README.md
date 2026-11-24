# Decisions: if, else and logical operators

Fix the website's rules in `src/rules.js`. Days are `"Mon"` to `"Sun"`, and hours are whole numbers from 0 to 23.

1. **TODO 1:** `deliveryFee(subtotalPaise, pickup)` is `0` for a pickup. Otherwise it's `6000` paise, but free above `99900` paise.
2. **TODO 2:** `stallOpen(day, hour)` is `true` on `"Sat"` and `"Sun"` from 10:00 until 20:00. At 20:00 the stall is closed.
3. **TODO 3:** `noteLine(note)` returns `"Note: "` followed by the note, or `"No note"` when the note is `""`, `undefined` or `null`.
4. **TODO 4:** `priceBand(paise)` returns `"under ₹200"` below `20000`, `"₹200 to ₹499"` below `50000`, and `"₹500 and up"` otherwise.

Press **Submit** to run the checks.
