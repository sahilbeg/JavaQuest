# Coordinating threads: latches, semaphores and barriers

Build three rush-hour tools. Every wait in this ticket has a time limit, so nothing can freeze for ever.

1. **TODO 1 to 4: `PaymentGate`** lets at most `maxConcurrent` payment calls run at the same moment. Use the `Semaphore` field `permits`.
   - `new PaymentGate(n)` throws `IllegalArgumentException` when `n` is less than 1.
   - `call(work)` waits for a permit with `acquire()`, returns `work.get()`, and gives the permit back with `release()` in a `finally` block. If the work throws, the exception travels up to the caller, and the permit is still given back.
   - `tryCall(work, timeoutMillis)` waits at most `timeoutMillis` for a permit with `tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)`. With no permit in time, it returns `false` and does **not** run the work. With a permit, it runs the work, releases in `finally`, and returns `true`.
   - `freeSlots()` returns `permits.availablePermits()`.
   - The check sends 12 calls through a gate of 3. All 12 must run, never more than 3 at once, and 3 must be able to run together.
2. **TODO 5 to 7: `StartupCheck.allPassed(checks, timeoutMillis)`** runs every station check at the same time and waits for them with a `CountDownLatch`.
   - Start one new `Thread` per check, so the checks run together.
   - Each thread runs its check, sets an `AtomicBoolean failed` when the check throws a `RuntimeException`, and calls `countDown()` in `finally`.
   - Wait with `await(timeoutMillis, TimeUnit.MILLISECONDS)`. Return `true` only when every check finished in time and none threw.
   - An empty list returns `true` at once. A check that throws makes the answer `false` as soon as every check has finished, without waiting for the timeout. A check that hangs makes the answer `false` after the timeout.
3. **TODO 8 to 12: `PriceBoard`** holds the per-minute rates (CLASSIC 15, EBIKE 30, CARGO 25 cents) and guards them with a `ReadWriteLock`.
   - Add the field `private final ReadWriteLock lock = new ReentrantReadWriteLock();`.
   - `fareCents(type, minutes)` returns `100 + rate * minutes` under the read lock. An unknown type throws `IllegalArgumentException`.
   - `snapshot()` returns `new TreeMap<>(perMinuteCents)` under the read lock, so types come out in alphabetical order. It is a copy: later changes do not reach it.
   - `setRate(type, cents)` and `raiseAll(extraCents)` change the rates under the write lock. `raiseAll` adds `extraCents` to every rate in one go, so no reader ever sees half a raise.
   - Unlock in `finally` every time.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
