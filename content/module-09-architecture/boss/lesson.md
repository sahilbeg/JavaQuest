## What happened

The rider app's live map shows every nearby station with its free bikes and empty docks. It asks again every 5 seconds. Each answer comes from `LiveMapCache`, which sits in front of the slow station-status service.

At 8 a.m. the map turned into spinners. Kakashi's dashboard showed three things:

1. The cache's hit rate was 0%. Every single request went through to the station-status service.
2. Every 5 seconds the load jumped again, even on stations that had just been loaded.
3. When the service finally fell over, the map showed an error at every station, instead of numbers that were a few seconds old.

```callout jargon
The **hit rate** is the share of requests a cache answers by itself. A **hit** is a request the cache answers from memory. A **miss** goes through to the slow service behind it.
```

### Why 0% hits hurts so much

At the busiest moment about 5,000 riders have the app open, as in [System design basics](/t/module-09-architecture/topic-06-system-design). Each map asks every 5 seconds, so that is 5,000 ÷ 5 = 1,000 map requests per second. Each map shows about 5 nearby stations, so the cache gets 1,000 × 5 = 5,000 station lookups per second.

With 0% hits, the service does 5,000 loads per second. Riverton has 300 stations. A cache keyed by station needs at most 300 loads every 5 seconds: 300 ÷ 5 = 60 loads per second. That is more than 80 times less work, because 5,000 ÷ 60 is about 83.

### The stampede

Even with good keys, an entry expires. At that moment Riverside is on about 1,000 maps, so it gets 1,000 ÷ 5 = 200 requests per second. A load takes half a second under load, so about 100 requests miss while the first load is still running. Each one starts its own load.

```animate
title: Riverside's entry expires at rush hour
kind: scene
nodes:
  - { id: ana, label: Rider Ana, emoji: "📱", row: 0, col: 0 }
  - { id: ben, label: Rider Ben, emoji: "📱", row: 1, col: 0 }
  - { id: cai, label: Rider Cai, emoji: "📱", row: 2, col: 0 }
  - { id: cache, label: LiveMapCache, emoji: "🗂️", value: "riverside: fresh", row: 1, col: 2 }
  - { id: svc, label: Station-status service, emoji: "🖥️", value: "idle", row: 1, col: 4 }
steps:
  - say: It is 8:00:05. Riverside's entry was loaded at 8:00:00, so it has just expired.
    set: { cache: "riverside: expired" }
    focus: [cache]
  - say: Ana's map asks for Riverside. The cache misses and asks the service.
    send: [{ from: ana, to: cache, label: "riverside?" }, { from: cache, to: svc, label: "load riverside" }]
    set: { svc: "1 load" }
  - say: Ben asks a moment later. Ana's load is still running, so the cache misses again and starts a second load.
    send: [{ from: ben, to: cache, label: "riverside?" }, { from: cache, to: svc, label: "load riverside" }]
    set: { svc: "2 loads" }
  - say: Cai asks too, and a third load starts. All three loads count the same bikes.
    send: [{ from: cai, to: cache, label: "riverside?" }, { from: cache, to: svc, label: "load riverside" }]
    set: { svc: "3 loads" }
  - say: At rush hour this is 100 loads, not 3. The busy service gets slower, so even more requests pile up.
    set: { svc: "100 loads, 100% CPU" }
    focus: [svc]
```

```callout jargon
A **cache stampede** is many requests missing the same expired entry at once, and all of them loading it. It is also called a **thundering herd**.
```

## What you need

You learned each idea in an earlier ticket. Here they meet in one class.

- Cache keys, TTL and single-flight loading: [Caching](/t/module-09-architecture/topic-05-caching).
- Serving a stale copy when the source is down, with a limit on its age and a slow retry pace: [Caching](/t/module-09-architecture/topic-05-caching), the card "Serving stale when the source is down".
- Why you must not hammer a failing dependency: [Resilience](/t/module-09-architecture/topic-09-resilience).
- `ConcurrentHashMap.putIfAbsent`: [Concurrent collections](/t/module-05-advanced-java/topic-06-concurrent-collections).
- `CompletableFuture`, `complete` and `join`: [CompletableFuture](/t/module-05-advanced-java/topic-04-completable-future).

### The classes

| Class | What it is |
|---|---|
| `MapRequest(riderId, requestId, stationId)` | one request from the live map; `requestId` is new every time |
| `StationStatus(stationId, bikesAvailable, docksFree, stale)` | what the map shows; `asStale()` gives a copy marked stale |
| `StationStatusSource` | the slow service: `StationStatus load(String stationId)`, which may throw |
| `StatusUnavailableException(stationId, cause)` | thrown when the cache has nothing it may show |
| `LiveMapCache(source, clock)` | the class you fix; `TTL` 5 s, `MAX_STALE` 60 s, `RETRY_INTERVAL` 1 s |

### What a cache key must be

A key must be equal for every request that should get the same answer. The map's answer depends only on the station. So the key is the station id, and nothing else.

A `MapRequest` record is equal to another only when all three fields are equal. The `requestId` is new every time, so two requests are never equal. A cache keyed by the whole request never hits. It also keeps every request forever, so it slowly eats the server's memory.

### Syntax: comparing times with Instant

```java nocompile
Instant now = clock.instant();
boolean fresh = now.isBefore(loadedAt.plus(TTL));
```

`plus` adds a `Duration` and returns a new `Instant`. `isBefore` is strictly before: at exactly `loadedAt + TTL` it is `false`. So the entry has expired at exactly 5 seconds.

### Single flight

```callout jargon
**Single flight** means one request per key does the slow load. Every other request for that key, arriving during the load, waits for that one result and shares it.
```

```animate
title: One load, three answers
kind: scene
nodes:
  - { id: ana, label: Rider Ana, emoji: "📱", row: 0, col: 0 }
  - { id: ben, label: Rider Ben, emoji: "📱", row: 1, col: 0 }
  - { id: cai, label: Rider Cai, emoji: "📱", row: 2, col: 0 }
  - { id: cache, label: LiveMapCache, emoji: "🗂️", value: "riverside: expired", row: 1, col: 2 }
  - { id: svc, label: Station-status service, emoji: "🖥️", value: "idle", row: 1, col: 4 }
steps:
  - say: Ana misses first. She puts a new "loading Riverside" ticket in the cache and calls the service.
    send: [{ from: ana, to: cache, label: "riverside?" }, { from: cache, to: svc, label: "load riverside" }]
    set: { cache: "riverside: loading", svc: "1 load" }
  - say: Ben misses too. He finds Ana's ticket already there, so he waits for it instead of loading.
    send: { from: ben, to: cache, label: "riverside?" }
    focus: [ben]
  - say: Cai finds the same ticket and waits as well. The service still has only one load to do.
    send: { from: cai, to: cache, label: "riverside?" }
    focus: [cai]
  - say: The load returns 7 bikes. The cache stores it, completes the ticket and removes it.
    send: { from: svc, to: cache, label: "7 bikes", back: true }
    set: { cache: "riverside: fresh", svc: "idle" }
  - say: All three riders get the same answer from one load.
    send: [{ from: cache, to: ana, label: "7 bikes", back: true }, { from: cache, to: ben, label: "7 bikes", back: true }, { from: cache, to: cai, label: "7 bikes", back: true }]
```

### Syntax: one load per station

```java nocompile
CompletableFuture<StationStatus> mine = new CompletableFuture<>();
CompletableFuture<StationStatus> running = loading.putIfAbsent(stationId, mine);
if (running != null) {
    return running.join();          // someone else is loading: wait for their result
}
// you are the one loader: load, then mine.complete(...) or mine.completeExceptionally(...)
// and in a finally block: loading.remove(stationId, mine);
```

`putIfAbsent` checks and puts in one atomic step. It returns `null` only to the first thread. `remove(key, value)` removes the entry only if it still holds your future.

A `CompletableFuture` that failed throws a `CompletionException` from `join()`, with the real exception as its cause. Gold needs the original `StatusUnavailableException`, so catch the `CompletionException` and rethrow `getCause()`.

### Stale if error

When the service is down, a map with numbers from 30 seconds ago is still useful. A map full of errors is not. So on a failed load, show the last numbers you have, marked stale, so the app can grey them out.

```callout jargon
**Stale if error** means: when a refresh fails, keep serving the old cached value for a limited time, marked as old.
```

There are two limits. After 60 seconds past expiry the numbers are too old to trust, so throw `StatusUnavailableException`. And while the service is failing, ask it at most once per second. Every extra call to a failing service keeps it down longer.

### The fake clock and the fake source

The checks never sleep. They give the cache a fake `Clock` that starts at 8:00:00 and only moves when a check moves it. The fake source counts its calls, can be made to fail, and can hold a load until the check lets it finish. That is how the silver checks get 20 riders waiting on one load, every time.

## The tiers

1. **Bronze** resolves the incident. The cache is keyed by station id, so riders share entries. An entry is fresh while `now` is before `loadedAt + 5 s`, and expired at exactly 5 seconds.
2. **Silver**: 20 riders arriving at once on a missing or expired entry cost one load, and all get its result. A failed load reaches every waiting rider and is not cached. A slow station does not hold up the others.
3. **Gold**: a failing source gives the last numbers marked stale, for up to 60 seconds after expiry. The source is tried at most once per second while it fails. With nothing to show, `StatusUnavailableException` names the station and keeps the source's exception as its cause.

Change only `LiveMapCache`. The exact rules, with every boundary, are in the README.

## Recap

- A cache key holds exactly what the answer depends on. An extra field, such as a request id, drops the hit rate to 0%.
- A TTL check with `isBefore` is strict: an entry loaded at 8:00:00 with a 5-second TTL has expired at exactly 8:00:05.
- Single flight stops a stampede: one load per key, and everyone else waits for that result. Remove the in-flight load when it finishes, even when it fails.
- Stale if error keeps the map useful while a service is down, with a limit on how old, a stale mark, and a slow retry pace.

## Common mistakes

**1. Locking the whole cache.** A `synchronized get` stops the stampede, but now one slow Riverside load blocks Harbor, Uptown and every other station. The silver check for Harbor fails with a timeout. Wait per station, not for the whole cache.

**2. Loading inside `computeIfAbsent`.**

```java nocompile
return entries.computeIfAbsent(stationId, id -> source.load(id));
```

The slow load runs while `ConcurrentHashMap` holds a lock on part of the map, so other stations can wait too. And `computeIfAbsent` only runs when the key is missing, so the entry never expires. Use a separate map of loads in progress.

**3. Forgetting to remove a finished load.** If the future stays in the `loading` map, every later request joins it. After one failure, every request for that station gets the same old exception forever. Remove it in a `finally` block.

**4. Not unwrapping the waiter's exception.** A waiter that calls `join()` on a failed future sees a wrapper, not the source's exception:

```text
java.util.concurrent.CompletionException: java.lang.IllegalStateException: station-status service is down
```

The silver checks accept any exception. The gold checks need `StatusUnavailableException` itself, so a wrapped one fails them. So catch `CompletionException` around every `join()`, the loading request's included, and rethrow `e.getCause()`. Or let the loading request throw its own exception directly, and unwrap only in the waiters.

**5. Refreshing `loadedAt` when you serve a stale copy.** Then the copy never gets older, and the map could show 3-hour-old numbers as if they were fine. Keep the original `loadedAt`. Only a real load changes it.

**6. Calling the failing service on every request.** That is how the service fell over. Remember when each station's last load failed, and wait `RETRY_INTERVAL` before trying again.
