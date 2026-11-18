# A mini Redis, a leaderboard and a lock

Spoke's weekly leaderboard hammers Postgres, the rate limiter counts per server, and the nightly billing job once ran twice. All three fixes need Redis. The checks cannot start a real Redis server, so you build `MiniRedis`: a small, in-memory Redis with a few commands and **Redis's real rules**. Then you build a lock on top of it.

Do the parts in order. Parts 3 and 4 use your `MiniRedis`, so their checks only pass once parts 1 and 2 work.

Time never comes from the real clock. `MiniRedis` gets a `java.time.Clock`, and the checks move a fake clock by hand. Read the time with the given `now()` (epoch milliseconds).

Given to you, finished: `RedisException` (with the three error texts as constants), `SetOptions`, `ScoredMember`, `Lease`, and in `MiniRedis` the fields, `atomically`, `set(key, value)` and `incr(key)`.

## How MiniRedis stores things

- `data` maps each key to its value: a `String` for a string key, or a `Map<String, Double>` (member to score) for a sorted set.
- `expiresAt` maps a key to its **deadline** in epoch milliseconds. A key with no entry there never expires.
- **A key is gone once `now() >= deadline`.** An expired key is invisible to **every** command: `GET` gives `null`, `TTL` gives `-2`, `DEL` counts it as missing, `SET NX` may write it, `INCR` and `ZINCRBY` start a fresh key. Real Redis also deletes expired keys lazily, when a command touches them; `removeIfExpired(key)` does that here.

## 1. Strings and expiry

- **TODO 1, `removeIfExpired(key)`:** if the key has a deadline and `now() >= deadline`, remove it from `data` and `expiresAt`.
- **TODO 2, `set(key, value, options)`:** returns `"OK"`. With `options.onlyIfAbsent()` (NX) and the key already there, return `null` and change nothing. Otherwise store the value, whatever type the key held before. **A SET throws away the key's old TTL.** If `options.pxMillis() > 0` (PX), the new deadline is `now() + pxMillis`.
- **TODO 3, `stringAt(key)`:** the string at the key, or `null` if it is missing. If the key holds a sorted set, throw `new RedisException(RedisException.WRONG_TYPE)`.
- **TODO 4, `get(key)`:** the value or `null`. WRONGTYPE for a sorted set.
- **TODO 5, `del(keys...)`:** remove each key and its TTL. Answer how many of them existed. Missing and expired keys do not count.
- **TODO 6, `incrBy(key, increment)`:** a missing key counts as `"0"`. The value must be text that `Long.parseLong` accepts, otherwise throw `RedisException` with `NOT_AN_INTEGER` (`ERR value is not an integer or out of range`) and change nothing. Add with `Math.addExact`; if that overflows, throw `RedisException` with `OVERFLOW`. Store the result as text, such as `"7"`. **INCR keeps the key's TTL**; a key made by INCR has none. WRONGTYPE for a sorted set.
- **TODO 7, `expire(key, seconds)`:** `0` if the key is missing. If `seconds <= 0`, delete the key and answer `1`. Otherwise set the deadline to `now() + seconds × 1000` and answer `1`.
- **TODO 8, `pttl(key)`:** `-2` if the key is missing, `-1` if it has no deadline, otherwise the milliseconds left.
- **TODO 9, `ttl(key)`:** like `pttl`, but in seconds, **rounded to the nearest second** the way Redis does: `(millis + 500) / 1000`. `-1` and `-2` stay as they are. So 2500 ms left is `3`, and 1400 ms left is `1`.

## 2. Sorted sets

A sorted set holds members, each with a `double` score.

- **TODO 10, `sortedSetAt(key, create)`:** the `Map<String, Double>` at the key. Missing: a new empty map stored under the key if `create` is true, otherwise `null`. If the key holds a string, throw WRONGTYPE. Changing a member's score keeps the key's TTL.
- **TODO 11, `zadd(key, score, member)`:** answer `1` if the member is new, `0` if it was there (its score is replaced).
- **TODO 12, `zincrby(key, increment, member)`:** add to the member's score (a missing member starts at 0) and answer the new score.
- **TODO 13, `zscore(key, member)`:** the score, or `null` if the key or the member is missing.
- **TODO 14, `highestFirst(key)`:** every member as a `ScoredMember`, **highest score first**. **Equal scores are ordered by member, in reverse text order** (`String.compareTo`, descending). This is what Redis's `ZREVRANGE` does. Text order compares character by character, so `"rider-9"` comes before `"rider-3"`, which comes before `"rider-10"`. A missing key gives an empty list.
- **TODO 15, `zrevrange(key, start, stop)`:** places `start` to `stop` of `highestFirst`, **both included**, counting from 0. A negative index counts from the end: `-1` is the last place, `-2` the one before. After that, a `start` below 0 becomes 0, and a `stop` past the end becomes the last place. If `start > stop`, or `start` is past the end, answer an empty list. So `0, -1` is everything.
- **TODO 16, `zrevrank(key, member)`:** the member's 0-based place in `highestFirst` (the top is `0`), or `null` if the key or the member is missing.

## 3. `RedisLock(redis, tokens)`

`tokens` is a `Supplier<String>` of random values; the checks pass one that gives `token-1`, `token-2`, ...

- **TODO 17, `tryAcquire(name, leaseMillis)`:** `leaseMillis < 1` throws `IllegalArgumentException`. Take **one** token from `tokens.get()` on every call. Then, inside **one** `redis.atomically(r -> ...)`, do `SET "lock:" + name token NX PX leaseMillis` (`SetOptions.nxPx(leaseMillis)`). A `null` reply means someone else holds the lock: return `Optional.empty()`. Only after a successful SET, `INCR "fence:" + name`; that number is the **fencing token**. The SET and the INCR must be one atomic step: two separate commands would let a holder pause between them, lose its lease, and get a **higher** token than the server that took the lock after it. Return `Optional.of(new Lease(name, token, fencingToken))`. So the first holder ever gets fencing token 1, the next one 2, and refused attempts never use one up.
- **TODO 18, `release(lease)`:** inside `redis.atomically(r -> ...)`, `GET` the lock key. Only if it equals `lease.token()`, `DEL` it and answer `true`. Otherwise (someone else's token, or no key) answer `false` and delete nothing. This is the Lua script from the lesson.

## 4. `FencedStore`

- **TODO 19, `write(fencingToken, entry)`:** refuse (answer `false`, keep nothing) when `fencingToken` is **lower** than the highest token accepted so far. Otherwise remember the token, add the entry and answer `true`. The same token may write many times. `entries()` is given.

Time: every string command is O(1). `highestFirst` sorts, so `zrevrange` and `zrevrank` are O(N log N) here; real Redis keeps a skip list and does them in O(log N + M) and O(log N). Space: O(keys + members).

Press **Submit** in JavaQuest, or just save the file in your IDE. The checks run on their own.
