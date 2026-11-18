# Race conditions, synchronized, locks

At rush hour, many riders tap the Harbor kiosk at once. Make the unlock counter and the dock safe when many threads use them together.

`UnlockCounter.java`:

1. **TODO 1:** make `increment()` add 1 without losing updates when many threads call it.
2. **TODO 2:** make `get()` return the count safely.

`Dock.java`:

3. **TODO 3:** store the number of bikes in the constructor.
4. **TODO 4:** make `tryUnlock()` take a bike and return `true` if one is left, or return `false`. The check and the take must happen under one lock, so the count never drops below 0.
5. **TODO 5:** make `bikesLeft()` return the bikes left, safely.
6. **TODO 6:** make `unlockMany(dock, riders)` start one thread per rider. Each thread calls `tryUnlock()` once. Join them all and return how many unlocks succeeded.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
