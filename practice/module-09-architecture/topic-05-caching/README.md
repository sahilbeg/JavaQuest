# Caching rider profiles and fare quotes

Every app open asks the slow rider-profile service for the same profile. Fare quotes are recomputed by a slow pricing service thousands of times a minute. Build three pieces: a cache with a size limit and a TTL, a cache-aside wrapper for rider profiles, and a single-flight loader for fare quotes.

Do the parts in order. Part 2 uses your cache from part 1, so its checks only pass once part 1 works.

Time never comes from the real clock. `TtlLruCache` gets a `java.time.Clock`, and the checks move a fake clock by hand.

## 1. `TtlLruCache<K, V>(int capacity, Duration ttl, Clock clock)`

The methods are `synchronized` already, so one cache can be shared by many threads.

- **TODO 1:** throw `IllegalArgumentException` when `capacity` is below 0, or when `ttl` is `null`, zero or negative.
  - **Capacity 0 is allowed and means "caching is off":** every `put` is thrown away at once, and every `get` is a miss.
- **TODO 2:** make `entries` a `LinkedHashMap` in **access order** (`new LinkedHashMap<>(16, 0.75f, true)`), as an anonymous subclass that overrides `removeEldestEntry` to return `size() > capacity`. Inside the subclass, `size()` is the map's size; write `TtlLruCache.this.capacity` for the cache's capacity.
- **TODO 3, `isExpired(entry)`:** each entry stores `expiresAt` = the clock's instant at `put` plus `ttl`. **An entry is expired when now >= expiresAt.** So with a 10 minute TTL, it is fresh at 9:59.999999999 and expired at exactly 10:00.
- **TODO 4, `get(key)`:** returns `Optional<V>`.
  - Key not there: count a **miss**, return `Optional.empty()`.
  - Key there but expired: remove it, count a **miss**, return `Optional.empty()`.
  - Otherwise count a **hit** and return the value. With access order, the `get` also makes the key the most recently used.
- **TODO 5, `put(key, value)`:** store the value with a fresh `expiresAt`. Putting a key that is already there replaces its value, restarts its TTL and makes it the most recently used, without evicting anything. A **new** key that makes the cache hold more than `capacity` entries evicts the **least recently used** entry, whether it has expired or not. `null` keys and values throw `NullPointerException` (given).
- **TODO 6, `invalidate(key)`:** remove the key. An unknown key does nothing.
- **TODO 7, `size()`:** remove every expired entry, then return how many entries are left.
- **TODO 8, `hitRatio()`:** `hits / (hits + misses)` as a `double`, or `0.0` before the first `get`. `hits()` and `misses()` are given. Only `get` changes them.

Time: `get`, `put` and `invalidate` are O(1). `size()` is O(n), because it checks every entry. Space: O(capacity).

## 2. `RiderProfileCache(RiderProfileStore store, Clock clock)`

The constructor is given. It makes two caches: `profiles` (TTL `PROFILE_TTL`, 10 minutes) and `unknownRiders` (TTL `UNKNOWN_TTL`, 1 minute), each holding at most `CAPACITY` entries.

- **TODO 9, `find(riderId)`:** cache-aside, with negative caching.
  1. A profile in `profiles`: return it. The store is not asked.
  2. `riderId` in `unknownRiders`: return `Optional.empty()`. The store is not asked.
  3. Otherwise call `store.load(riderId)` once. Found: put it in `profiles`. Not found: put `riderId -> Boolean.TRUE` in `unknownRiders`. Return what the store said.
- **TODO 10, `update(profile)`:** call `store.save(profile)` **first**. **Then delete** the rider's entry from `profiles` **and** from `unknownRiders`.
  - Do not delete before saving. A read that arrives during the save would load the old profile and cache it again.
  - Do not put the new profile into the cache. If two updates race, the slower one would leave its older profile cached. Deleting is safe in any order; the next `find` loads the newest profile.
  - Deleting from `unknownRiders` matters for a rider who was unknown and has just signed up.

## 3. `FareQuoteLoader(PricingService pricing)`

`quote(request)` returns the fare in cents for a `QuoteRequest(bikeType, minutes)`. The pricing service is slow. When eight riders ask for the same quote at the same moment, it must be called **once**, and the other seven wait for that answer. This is **single flight**.

- **TODO 11, `quote(request)`:** make a new `CompletableFuture<Long> mine` and call `quotes.putIfAbsent(request, mine)`.
  - It returned someone else's future: return `waitFor(thatFuture)`. That quote is either loaded already or loading right now.
  - It returned `null`: you are the loader. Call `pricing.quoteCents(request)`, then `mine.complete(cents)`, and return `cents`.
  - A successful quote stays in `quotes`, so later asks reuse it without calling the pricing service.
  - Quotes have no TTL here. That is safe only because fares change by a deploy, which calls `invalidateAll()`. A real cache would still add a TTL as a safety net.
- **TODO 12:** when the pricing service throws a `RuntimeException`, call `quotes.remove(request, mine)`, so the failure is **not** remembered and the next ask tries again. Then `mine.completeExceptionally(e)`, so the waiting riders wake up, and throw `e`.
- **TODO 13, `waitFor(future)`:** return `future.join()`. A failed future makes `join()` throw a `CompletionException` that wraps the real error. Catch it and throw its cause when the cause is a `RuntimeException`. So every waiting rider gets the same `PricingException` as the loader.
- **TODO 14 and 15:** `invalidate(request)` forgets one quote; `invalidateAll()` forgets every quote. The next ask computes it again.
- **Never call the pricing service while holding a lock or inside `computeIfAbsent`.** A slow load for one quote must not block riders asking for a different quote.

Time: O(1) map work per ask, plus one pricing call per quote. Space: one entry per quote remembered.

## How the checks work

The checks use a fake clock, a fake profile store that counts `load` calls, and a fake pricing service that counts calls. In the single-flight checks, eight threads ask for one quote while the fake pricing service holds the first call at a gate. The gate opens only once every thread has asked. Then the checks count the calls. A check gives up after a few seconds if a thread never wakes up.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
