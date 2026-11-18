# The Java Memory Model: visibility and volatile

Make four small Spoke classes safe to share between threads. The checks look at results, and at the modifiers you declare, because a visibility bug may not show up on every run.

1. **TODO 1 and 2:** in `DockSensor`, the stop flag must be a field named `running`, declared `private volatile boolean running = true;`. `stop()` sets `running` to `false`. `isRunning()` returns it. Once `stop()` is called from any thread, the loop in `run()` must end (the check allows 2 seconds).
2. **TODO 3 to 6:** make `StationSnapshot` immutable:
   - Declare the class `final`.
   - Make every field `private final`.
   - In the constructor, store `List.copyOf(bikeIds)`, so a later change to the caller's list does not reach the snapshot.
   - `bikeIds()` returns a list that cannot be changed. Adding to it throws `UnsupportedOperationException`, which `List.copyOf` gives you for free.
   - `withFreeDocks(n)` returns a **new** snapshot with the same station and bikes and `n` free docks. The old snapshot keeps its old number.
3. **TODO 7 to 11:** `RideStats` counts rides and remembers the most riders online at once. No field may be a plain number (`int`, `long`), volatile or not.
   - Count rides in a field of type `LongAdder`: `rides.increment()` in `rideStarted`, and `rides()` returns `rides.sum()`.
   - Keep the peak in a field of type `AtomicInteger`: `peakOnline.accumulateAndGet(ridersOnline, Math::max)`. `peakOnline()` returns its value.
   - The peak never goes down: after `rideStarted(40)` and `rideStarted(12)`, `peakOnline()` is still 40.
   - 6 threads calling `rideStarted` 5,000 times each must give exactly 30,000 rides and the right peak.
4. **TODO 12 and 13:** `FareTable.get()` must return the same table to every thread, and the constructor must run only once. Use the holder-class idiom: a `private static final class Holder` with `static final FareTable INSTANCE = new FareTable();`, and return `Holder.INSTANCE`. (Double-checked locking with a `volatile` field also passes.) `timesCreated()` and `fareCents` are already written.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
