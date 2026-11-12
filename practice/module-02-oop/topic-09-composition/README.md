# Composition over inheritance

Someone made `Station` extend `Dock`, so the code says every station "is a dock". A station really **has** 12 docks, and each dock **has** at most one bike. Build those classes with objects that hold other objects.

`Bike.java` is already done.

1. **TODO 1:** in `Dock`, make `isFree()` return `true` when no bike is parked.
2. **TODO 2:** make `park(bike)` store the bike.
3. **TODO 3:** make `release()` empty the dock and return the bike that was in it.
4. **TODO 4:** in the `Station` constructor, put a `new Dock()` in each of the 12 slots.
5. **TODO 5:** `dock(bike)` parks the bike in the first free dock and returns its number, from 1 to 12. It returns -1 when the station is full.
6. **TODO 6:** `undock(bikeId)` releases and returns the bike with that id. It returns `null` when no dock holds it.
7. **TODO 7:** `freeDocks()` counts the free docks.
8. **TODO 8:** `findDock(bikeId)` returns the number of the dock holding that bike, or -1.

Let each `Dock` do its own work. `Station` asks its docks with `isFree()`, `park`, `getBike()` and `release()`.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
