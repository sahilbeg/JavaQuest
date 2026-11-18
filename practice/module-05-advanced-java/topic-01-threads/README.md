# Threads and Runnable

The Harbor kiosk checks its dock sensors one after another. Make `SensorCheck.java` check them all at the same time.

`readSensor(dockId)` is already written. It waits a little, like a slow network, then returns text such as `Harbor-3: ok`.

1. **TODO 1:** in `checkAll`, make a `String` array with one slot per dock id.
2. **TODO 2:** start one `Thread` per dock. Thread number `i` calls `readSensor` for dock `i` and stores the text in slot `i`.
3. **TODO 3:** wait for every thread with `join()`, then return the array as a `List`, in the same order as `dockIds`.
4. **TODO 4:** in `runOnNewThread`, run `task` on a new thread named `sensor-worker`, wait for it, and return the name of the thread that ran it.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
