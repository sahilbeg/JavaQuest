# Repeating: while and do-while

The card reader at the kiosk sometimes misses the first tap. Make `CardReader.java` keep trying, and add two more small loops the kiosk needs.

1. **TODO 1 to 3:** `tapsNeeded(firstSuccessfulTap, maxTaps)` counts taps with a `while` loop. Return the tap number that worked. Return `-1` if the card still has not worked after `maxTaps` taps. For example, `tapsNeeded(2, 3)` is `2` and `tapsNeeded(4, 3)` is `-1`.
2. **TODO 4 and 5:** `minutesUntilFull(freeDocks, bikesArrivingPerMinute)` counts the whole minutes until free docks reach 0 or below. `minutesUntilFull(12, 5)` is `3`. If no bikes arrive (0 or less per minute), return `-1` so the loop never runs forever.
3. **TODO 6:** `countdown(from)` builds text like `3 2 1 go` with a loop and `+`. `countdown(0)` is just `go`.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
