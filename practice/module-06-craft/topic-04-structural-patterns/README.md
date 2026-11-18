# Structural patterns: adapter, decorator, facade

Itachi's ticket: the second-hand locks at Depot speak `engage` and `disengage` with number codes. Support wants every unlock retried once and recorded, without changing the lock classes.

`Lock`, `OldLock` and `RiderRegistry` are finished. Do not change `OldLock`. Every class you write keeps another object in a field and passes calls on to it.

1. **TODO 1 to 3:** `OldLockAdapter` wraps an `OldLock` and implements `Lock`. `open` is true when `disengage` returns 0. `close` is true when `engage` returns 0.
2. **TODO 4 to 6:** `RetryingLock` wraps any `Lock`. When `open` fails, it tries exactly once more. `close` is passed straight on.
3. **TODO 7 to 10:** `RecordingLock` wraps any `Lock` and adds a line to its history for every call, like `open B-1042: true` or `close B-1042: false`.
4. **TODO 11 to 14:** `UnlockFacade.unlock(riderId, bikeId)` returns `rider r9 not allowed` when the registry says no (and leaves the lock alone), `lock failed for B-1042` when the lock does not open, and `unlocked B-1042 for r1` when all is well.

When you are done, this stack should work:

```java
Lock lock = new RecordingLock(new RetryingLock(new OldLockAdapter(new OldLock())));
```

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
