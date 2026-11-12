# Executors and thread pools

Make the dock sensor check in `SensorPool.java` reuse a small pool of threads. `readSensor` is already written. It throws an `IllegalStateException("sensor offline")` for any dock id that ends in `-X`.

1. **TODO 1 to 3:** write `checkAll(dockIds, poolSize)`. Make a pool with `Executors.newFixedThreadPool(poolSize)` inside try-with-resources. Submit one `Callable` per dock that returns `readSensor(id)`. Call `get()` on the futures in the order you submitted them, so the results match the input order.
2. **TODO 4 to 6:** write `maxThreadsSeen(dockIds, poolSize)`. Make a plain `HashSet`. Each task adds `Thread.currentThread().getName()` inside `synchronized (names) { names.add(...); }`. Add a one-line comment saying why the lock is needed. Wait for every task, then return the size of the set. It must never be more than `poolSize`.
3. **TODO 7:** write `checkAllSafely(dockIds, poolSize)`. It works like `checkAll`, but a broken sensor gives the line `Harbor-X: failed (sensor offline)`. Catch `ExecutionException` and use `e.getCause().getMessage()`.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
