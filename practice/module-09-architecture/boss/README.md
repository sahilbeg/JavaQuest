# INC-09: The live map falls over at peak

At 8 a.m. the rider app's live map shows spinners, and the station-status service runs at 100% CPU. When it fell over, the map showed errors everywhere instead of slightly old numbers.

Fix `LiveMapCache` in package `jq.m09.incident`. Change only that class. `StationStatus`, `MapRequest`, `StationStatusSource` and `StatusUnavailableException` are ready.

`get(MapRequest request)` returns the `StationStatus` for `request.stationId()`. The cache reads the time only from its `Clock`. The checks use a fake clock that starts at 8:00:00 and a fake source that counts its calls. Every check fails with a message that says what went wrong.

## Bronze: the cache must hit (TODOs 1 and 2)

- **Key by station id.** Every request has a new `requestId` and a rider id. Two requests for the same station share one entry, whoever asks.
- **Fresh while `now` is before `loadedAt + TTL`.** `loadedAt` is the clock's time when the cache asked the source. `TTL` is 5 seconds. An entry loaded at 8:00:00 is a hit at 8:00:04.999 and has expired at exactly 8:00:05. An expired entry is loaded again, and the new numbers are cached from that moment.
- Each station keeps its own entry. Riverside's numbers never show up for Harbor.

## Silver: no stampede (TODO 3)

- When an entry is missing or expired, **one** request per station calls the source. Every other request for that station that arrives during the load waits, then returns the same result. 20 riders at once cost 1 call.
- If that load throws, every waiting request gets an exception (any `RuntimeException`). The failure is **not** kept in the cache: a later request, once the source works again, loads the station as normal.
- Stations do not wait for each other. While Riverside's load is slow, a request for Harbor still gets its answer. Do not lock the whole cache.

## Gold: old numbers beat an error (TODO 4)

- **Stale if error.** When the source throws and the station has an old entry, return `entry.status().asStale()`: the same numbers with `stale()` equal to `true`. A status loaded from the source has `stale()` equal to `false`. Serving a stale copy does not change its `loadedAt`.
- **A limit on staleness.** A stale copy may be shown while `now` is before `loadedAt + TTL + MAX_STALE`. `MAX_STALE` is 60 seconds. An entry loaded at 8:00:00 can be shown stale until 8:01:04.999. From exactly 8:01:05, throw `StatusUnavailableException`.
- **No hammering.** After a failed load, do not call the source again for that station until `RETRY_INTERVAL` (1 second) has passed. A failure at 8:00:06 means no calls before 8:00:07; at exactly 8:00:07 the cache may try again. Meanwhile, answer with the stale copy, or throw `StatusUnavailableException` when there is none to show.
- **Nothing to show.** With no old entry, or one past the limit, throw `new StatusUnavailableException(stationId, cause)`. When this request called the source, `cause` is the source's exception. When it skipped the call because of the retry interval, `cause` may be `null`.
- **Throw it unwrapped.** Every request, loader and waiters alike, must throw `StatusUnavailableException` itself. `join()` on a failed `CompletableFuture` wraps the exception in a `CompletionException`. Catch that and rethrow `e.getCause()`, or let the loading request throw its own exception directly and unwrap only in the waiters.
- When the source works again, the fresh status is cached as normal, with `stale()` equal to `false`.

The waiting requests from silver get whatever the loading request ends up with: the fresh status, the stale copy, or the exception.
