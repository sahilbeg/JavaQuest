# CompletableFuture

Make unlocking a bike in `UnlockFlow.java` run payment and the lock at the same time. The services `charge`, `releaseLock` and `sendReceipt` are already written. `charge("BLOCKED")` throws an `IllegalStateException("card declined")`.

1. **TODO 1 to 3:** write `unlock(riderId, bikeId)`. Start `charge` and `releaseLock` with `CompletableFuture.supplyAsync`. Chain `sendReceipt` on the payment id with `thenCompose`. Join the lock line and the receipt line with `thenCombine`. `unlock("r1", "B-1042")` gives `released B-1042 | receipt for P-r1`.
2. **TODO 4:** write `unlockOrFallback(riderId, bikeId)`. It returns the same line as `unlock`, or `unlock failed: card declined` when `charge` throws. Use `exceptionally`, and read `getCause()` when the exception is a `CompletionException`.
3. **TODO 5 to 6:** write `unlockAll(riderIds, bikeIdPrefix)`. Rider number `i` (counting from 0) gets bike `bikeIdPrefix + (i + 1)`. Start every `unlockOrFallback` first, wait with `CompletableFuture.allOf(...).join()`, then `join()` each future in list order.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
