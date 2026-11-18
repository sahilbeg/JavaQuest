# Resilience: rate limits, retries and circuit breakers

This morning the SMS provider went down, and every rental waited for it until the server ran out of threads. The same morning a partner app flooded the API. Build the four tools that stop both: a token bucket rate limiter, a circuit breaker, a backoff calculator and a retry helper.

No class reads the real clock or sleeps. Time comes from a `LongSupplier nowMillis` (milliseconds), randomness from a `RandomGenerator`, and waiting goes through a `Sleeper`. The checks pass fakes, so they control time exactly and never wait.

Given to you, finished: `CallNotPermittedException`, `ProviderException` (an HTTP error from a provider, with `status()`), `ProviderTimeoutException` and the `Sleeper` interface.

## 1. `TokenBucket(capacity, refillPerSecond, nowMillis)`

- **TODO 1:** throw `IllegalArgumentException` when `capacity < 1` or `refillPerSecond < 1`. A new bucket starts **full**, with `capacity` tokens. Read `nowMillis` once here as the starting time.
- **TODO 2, refilling:** before every answer, add the tokens earned since the last refill. A bucket gains `refillPerSecond` tokens a second, counted to the millisecond: after `t` ms it has gained `t × refillPerSecond / 1000` tokens, **fractions included**. Never go above `capacity`, even after a gap of a whole day.
- **The rounding rule:** keep the part of a token that has built up. At 2 tokens a second, 300 ms earns 0.6 of a token. Another 200 ms completes it. Code that rounds 0.6 down to 0 and then forgets those 300 ms fails the checks. The easy way: store thousandths of a token in a `long` (`MILLI = 1000` is one whole token).
- **TODO 3, `tryAcquire()`:** if at least one whole token is there, spend it and return `true`. Otherwise return `false`.
- **TODO 4, `availableTokens()`:** the whole tokens there now, rounded down.
- **TODO 5, `millisUntilNextToken()`:** `0` when a whole token is there. Otherwise the milliseconds until one is, **rounded up**. An empty bucket at 3 a second needs 333.3 ms, so answer `334`.

## 2. `RateLimiter(capacity, refillPerSecond, nowMillis)`

One `TokenBucket` per key: a rider id, an API key or an IP address.

- **TODO 6:** throw `IllegalArgumentException` when `capacity < 1` or `refillPerSecond < 1`.
- **TODO 7, `tryAcquire(key)`:** use that key's bucket, making a new full one the first time a key appears (`computeIfAbsent`). Keys never share tokens.
- **TODO 8, `retryAfterSeconds(key)`:** the value for a `Retry-After` header: `millisUntilNextToken()` turned into whole seconds, **rounded up**. 1 ms left is 1 second. A key with a token now, or a key never seen, gives `0`.

## 3. `CircuitBreaker(failureThreshold, openDuration, nowMillis)`

States: `CLOSED`, `OPEN` and `HALF_OPEN`. A new breaker is `CLOSED`.

- **TODO 9:** throw `IllegalArgumentException` when `failureThreshold < 1`, or when `openDuration` is zero or negative.
- **TODO 10, `state()`:** an `OPEN` breaker becomes `HALF_OPEN` once **at least** `openDuration` has passed since it opened. At exactly 30 s of a 30 s open time, it is `HALF_OPEN`.
- **TODO 11 and 12, `call(action)`:**
  - `CLOSED`: call `action.get()` and return its answer. A `RuntimeException` from the action counts as a failure, and is thrown on **unchanged**.
  - `OPEN`: throw `CallNotPermittedException` at once. Never call the action.
  - `HALF_OPEN`: let exactly **one** trial call through. Any other call that arrives while the trial is still running gets `CallNotPermittedException`.
- **TODO 13, success:** a successful trial closes the breaker. Any success resets the count of failures in a row to 0.
- **TODO 14, failure:** when `CLOSED`, count the failure. At `failureThreshold` failures **in a row**, open the breaker, starting now. A failed trial opens it again at once, for a whole new `openDuration` from the moment the trial failed.

## 4. `Backoff(baseMillis, capMillis, random)`

- **TODO 15:** throw `IllegalArgumentException` unless `baseMillis >= 1` and `capMillis >= baseMillis`.
- **TODO 16, `ceilingMillis(attempt)`:** attempts count from 1. Return `min(capMillis, baseMillis × 2^(attempt - 1))`: with base 200 and cap 5000 that is 200, 400, 800, 1600, 3200, 5000, 5000... Throw `IllegalArgumentException` for `attempt < 1`. It must return the cap for any huge attempt, up to `Integer.MAX_VALUE`, and do so quickly. Careful: `1L << 64` is `1` in Java, not a huge number.
- **TODO 17, `delayMillis(attempt)`:** full jitter. Return `random.nextLong(ceilingMillis(attempt) + 1)`, called exactly once: a random wait from 0 to the ceiling, both included.

## 5. `Retry(maxAttempts, backoff, sleeper)`

- **TODO 18:** throw `IllegalArgumentException` when `maxAttempts < 1`.
- **TODO 19, `isRetryable(e)`:** `true` for a `ProviderTimeoutException`, and for a `ProviderException` with status 429 or any status of 500 and above. `false` for every other status (400, 401, 403, 404, 409, 422...) and every other exception.
- **TODO 20, `call(action)`:** call `action.get()` at most `maxAttempts` times **in all**, the first try included. Return the first answer.
  - A failure that is not retryable is thrown at once, unchanged.
  - A retryable failure on the last attempt is thrown unchanged.
  - Otherwise call `sleeper.sleep(backoff.delayMillis(attempt))`, where `attempt` is the number of the try that just failed (1 for the first), then try again. Never sleep after the last try.

Press **Submit** in JavaQuest, or just save the file in your IDE. The checks run on their own.
