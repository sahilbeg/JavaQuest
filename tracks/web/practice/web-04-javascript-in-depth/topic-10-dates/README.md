# Dates and times: opening hours

The website's "Open now" sign is wrong. Fix it in `src/hours.js`. Every function takes the date as a parameter: none of them reads the clock.

1. **TODO 1:** `isShopOpen(date)` is `true` when the Aundh shop is open: Tuesday to Sunday, from 11:00 up to (not including) 21:00. It is closed all day Monday.
2. **TODO 2:** `isStallOpen(date)` is `true` when the FC Road stall is open: Saturday and Sunday only, from 10:00 up to (not including) 20:00.
3. **TODO 3:** `addDays(date, days)` returns a **new** date `days` later (or earlier, when `days` is negative), at the same time of day. The date you were given must not change.
4. **TODO 4:** `formatDay(date)` returns text like `"Sat 31 Oct"`: the short weekday, the day of the month, and the short month. Use the `DAYS` and `MONTHS` arrays.

Remember: `getDay()` gives 0 for Sunday, and `getMonth()` gives 0 for January.

Press **Submit** in Cadence, or just save the file in your editor. The checks run on their own.
