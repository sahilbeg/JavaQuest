# Sealed types

Close the family of dock sensor messages, so nobody can add a fourth kind by surprise.

1. **TODO 1:** in `DockEvent.java`, make `DockEvent` a `sealed` interface that `permits` exactly `BikeDocked`, `BikeUndocked` and `Fault`.
2. **TODO 2:** in `EventLog.java`, write `describe(DockEvent e)` with `instanceof` and a pattern variable for each type:
   - `BikeDocked("Harbor", "B-1042")` gives `B-1042 docked at Harbor`
   - `BikeUndocked("Harbor", "B-1042")` gives `B-1042 left Harbor`
   - `Fault("Harbor", "E7")` gives `Fault E7 at Harbor`
3. **TODO 3:** write `freeDocksChange(List<DockEvent> events)`. Each `BikeDocked` takes a dock (-1). Each `BikeUndocked` frees a dock (+1). A `Fault` changes nothing (0). Return the total.

The three records are already written for you. Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
