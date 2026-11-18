# Pattern matching for instanceof, switch and records

Swap the long `instanceof` chain for switch expressions that the compiler checks for you. The sealed `DockEvent` family now has a fourth record, `Rebalanced(String from, String to, int bikes)`, for the truck that moves bikes between stations.

1. **TODO 1:** in `EventHandler.java`, write `describe(DockEvent e)` as a switch with one record pattern per type and **no** `default`:
   - `BikeDocked("Harbor", "B-1042")` gives `B-1042 docked at Harbor`
   - `BikeUndocked("Harbor", "B-1042")` gives `B-1042 left Harbor`
   - `Fault("Harbor", "E7")` gives `Fault E7 at Harbor`
   - `Rebalanced("Riverside", "Harbor", 5)` gives `Truck moved 5 bikes from Riverside to Harbor`
2. **TODO 2:** write `priority(DockEvent e)` with guards (`when`):
   - a `Fault` whose code starts with `"E"` is `1`
   - any other `Fault` is `2`
   - a `Rebalanced` with more than 10 bikes is `3`
   - everything else is `4`
3. **TODO 3:** write `describeOrNone(Object o)`:
   - `null` gives `no event`
   - a `DockEvent` gives the same text as `describe(...)`
   - anything else gives `unknown: ` plus the class's simple name, for example `unknown: Integer`

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
