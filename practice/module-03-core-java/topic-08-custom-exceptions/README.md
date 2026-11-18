# Checked vs unchecked, custom exceptions, try-with-resources

Make a full station impossible to ignore. Replace error codes with exceptions.

1. **TODO 1:** in `DockFullException`, pass the message `"Station full: " + station` to `super(...)`.
2. **TODO 2:** in `UnknownBikeTypeException`, pass the message `"Unknown bike type: " + type` to `super(...)`.
3. **TODO 3 and 4:** in `Station.dock(bikeId)`, throw a `DockFullException` when every dock is used. Otherwise add 1 to `docked` and return the new dock number, starting at 1.
4. **TODO 5 and 6:** in `Pricing.pricePerMinute(type)`, return 0.15 for `classic`, 0.30 for `ebike` and 0.25 for `cargo`. Throw an `UnknownBikeTypeException` for anything else.
5. **TODO 7:** make `KioskSession.close()` remember that the session is closed, so `isClosed()` returns `true`.
6. **TODO 8 and 9:** in `KioskSession.run(s, bikeId)`, open a `KioskSession` in try-with-resources and return `"Docked at "` plus the dock number. Catch `DockFullException` and return its message, such as `Station full: Harbor`.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
