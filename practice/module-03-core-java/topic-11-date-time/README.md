# java.time: dates, times, durations

The kiosk used to count time as minutes since midnight. That broke on rides after midnight and on monthly passes. Rewrite `RideTimes.java` so it works with real dates and times.

1. **TODO 1:** make `minutesBetween(start, end)` return the whole minutes from `start` to `end`. It must work when a ride crosses midnight or lasts more than a day. Use `Duration.between(start, end).toMinutes()`.
2. **TODO 2:** make `passExpiry(bought, months)` return the date that is `months` months after `bought`. A pass bought on 31 January for 1 month ends on the last day of February.
3. **TODO 3:** make `isWeekend(day)` return `true` when the day is a Saturday or a Sunday.
4. **TODO 4:** make `label(start)` return text like `Fri 13 Nov 23:50`. Use `DateTimeFormatter.ofPattern("EEE d MMM HH:mm", Locale.ENGLISH)`.

The tests pass fixed dates in. Your code never needs to read the clock.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
