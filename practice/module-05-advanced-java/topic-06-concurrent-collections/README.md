# Concurrent collections and atomics

Make the live station map safe when many dock sensor threads update it at once.

1. **TODO 1 to 3:** in `LiveMap`, `dockEvent(station, change)` adds `change` with `docks.merge(station, change, Integer::sum)`. `free(station)` returns the count, or `0` for an unknown station. `snapshot()` returns a `TreeMap` copy, so stations come out in alphabetical order.
2. **TODO 4 and 5:** in `RideCounter`, `record()` adds 1 to the `AtomicInteger` and `total()` returns its value.
3. **TODO 6 and 7:** in `TelemetryPipe`, `send(item)` puts the item on the `LinkedBlockingQueue`. `drain(pipe, expected)` takes exactly `expected` items and returns them in the order they arrived.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
