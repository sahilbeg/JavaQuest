# List, ArrayList, LinkedList

Theo tidies the Harbor evening ride list every night. His old code removed short rides inside a for-each loop and crashed. Build a `RideQueue` that edits its list safely.

`Ride.java` is done: `record Ride(String bikeId, String station, String type, int minutes)`. In `RideQueue.java`, `add`, `size` and `get` are done too. Finish the rest:

1. **TODO 1:** `addFirst(Ride ride)` puts the ride at the front of the list (position 0).
2. **TODO 2:** `totalMinutes()` returns the sum of `minutes()` over every ride. An empty queue gives 0.
3. **TODO 3:** `removeShortRides(int underMinutes)` removes every ride with fewer minutes than `underMinutes`, and returns how many it removed. A ride of exactly `underMinutes` stays. Do not remove inside a for-each loop: loop backwards with an index.
4. **TODO 4:** `ridesFrom(String station)` returns a **new** list with the rides from that station, in their original order. The queue itself does not change.
5. **TODO 5:** `longest()` returns the ride with the most minutes. If two rides tie, return the one nearer the front. Return `null` when the queue is empty.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
