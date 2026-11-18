## Hook

A city's water pipes have valves. When one pipe bursts, workers close the valves around it. One street gets wet, and the rest of the city keeps its water.

Spoke had no valves this morning. The SMS provider, the company that sends Spoke's text messages, stopped answering. Every rental waited for it. Payments got stuck in the queue behind those waits, and rentals failed across Riverton. A greedy partner app made it worse.

In this ticket you add the valves: timeouts, safe retries, rate limits, circuit breakers and bulkheads.

## Concept

### Failure is normal

Spoke now depends on other companies' servers. The **payment provider** charges cards. The **SMS provider** sends "Ride started" texts. Each one fails sometimes.

```callout jargon
A **dependency** is another service your code calls and cannot work without, such as a payment provider. **Resilience** is a system's ability to keep working, maybe a little worse, while parts of it fail.
```

Say each provider is up 99.9% of the time. That sounds perfect. But a 30-day month has 43,200 minutes, and 0.1% of that is 43.2 minutes of failure. With four such providers, at least one is down almost 3 hours a month, since 1 - 0.999⁴ is about 0.4%.

So the question is never "will it fail?" Ask instead: "when it fails, how much of Spoke fails with it?" The goal: one broken provider breaks one feature, never the whole app.

### How one slow provider stopped every rental

A slow provider is worse than a dead one. A dead one fails fast. A slow one holds your threads hostage while you wait.

You saw this shape in [Observability](/t/module-09-architecture/topic-08-observability). From 21:40 to 22:20, a slow payment provider filled the connection pool, and riders could not end rides. Its postmortem asked for timeouts and a circuit breaker. This ticket delivers them.

Spoke's servers share **100 threads** for calls to outside companies, for both SMS and payments. Here is what happened in this morning's rush hour:

- 5,000 rides start in the rush hour, about 1.4 a second. About as many end. Each start and end sends an SMS, so 2.8 SMS calls a second.
- Each call waited the full 30 s, and the client tried 3 times: 90 s per call.

```callout jargon
**Little's law**: the number of things in a system equals how fast they arrive times how long each one stays. For threads: busy threads = calls per second × seconds per call.
```

Busy threads = 2.8 × 90 = **252**. The pool has 100. Within a minute every thread was stuck on SMS. Payment calls, which every rental start needs, queued behind them. Rentals failed everywhere, though payments were fine.

Even with no retries, 2.8 × 30 = 84 threads would have been stuck. One slow, unimportant feature ate a shared resource. The rest of this page is the set of tools that stops that.

### Timeouts everywhere

Every call across a network needs a **timeout**, the longest you will wait. You met two kinds in [Networking](/t/module-08-backend/topic-13-networking):

- The **connect timeout** limits opening the connection. Keep it short, like 1 to 2 s, because a healthy server accepts within milliseconds.
- The **read timeout** (or request timeout) limits waiting for the answer after connecting. Base it on how slow the provider normally is.

The trap: many Java clients **wait forever by default**. This run asks four of them, without sending anything:

```java run
import java.net.URI;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public class Main {
    public static void main(String[] args) throws Exception {
        URI sms = URI.create("https://sms.example/send");
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(sms).build();
        System.out.println("HttpClient connect timeout: " + client.connectTimeout());
        System.out.println("HttpRequest timeout:        " + request.timeout());

        URLConnection old = sms.toURL().openConnection();   // makes the object only; nothing is sent yet
        System.out.println("URLConnection connect timeout: " + old.getConnectTimeout());
        System.out.println("URLConnection read timeout:    " + old.getReadTimeout());
    }
}
```

`Optional.empty` means "no limit", and `0` on `URLConnection` means "wait forever" too. JDBC query timeouts also default to none. A provider that accepts your connection and then never answers keeps your thread for good.

How long should a timeout be? Look at the provider's latency on your dashboards from [Observability](/t/module-09-architecture/topic-08-observability). If its p99, the time 99% of calls beat, is 400 ms, a 1 s read timeout fails only truly stuck calls.

### Syntax: timeouts on HttpClient

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;

class SmsHttp {
    static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(1))          // opening the connection
            .build();

    static HttpRequest send(String body) {
        return HttpRequest.newBuilder(URI.create("https://sms.example/send"))
                .timeout(Duration.ofSeconds(2))             // waiting for the answer
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }
}
```

`connectTimeout` lives on the client, and `timeout` on each request. Set both. When the request timeout passes, `send` throws `HttpTimeoutException`.

### Deadlines: one time budget for the whole chain

The rider app waits 3 s for "Ride started". Inside that time, Spoke reads the database, charges the card and sends the SMS. If each step has its own 2 s timeout, the steps together can take 6 s. The app gave up long ago, yet the server keeps working for nobody.

A **deadline** fixes this. The first server sets "finish by 08:30:03", and passes it down. Each call waits at most the smaller of its own timeout and the time left.

```java run
import java.time.Duration;
import java.time.Instant;

public class Main {
    /** The time a call may take: its own limit, or what is left of the whole budget, whichever is smaller. */
    static Duration timeoutFor(Duration ownLimit, Instant deadline, Instant now) {
        Duration left = Duration.between(now, deadline);
        if (left.isNegative() || left.isZero()) {
            throw new IllegalStateException("deadline passed: give up, the rider has stopped waiting");
        }
        return ownLimit.compareTo(left) < 0 ? ownLimit : left;
    }

    public static void main(String[] args) {
        Instant tap = Instant.parse("2026-11-16T08:30:00Z");
        Instant deadline = tap.plusSeconds(3);                 // the rider app waits 3 s in all

        Instant afterDatabase = tap.plusMillis(1_300);         // finding the bike and rider took 1.3 s
        System.out.println("payment call gets " + timeoutFor(Duration.ofSeconds(2), deadline, afterDatabase).toMillis() + " ms");

        Instant afterPayment = afterDatabase.plusMillis(1_500);  // the payment took 1.5 s
        System.out.println("SMS call gets " + timeoutFor(Duration.ofSeconds(2), deadline, afterPayment).toMillis() + " ms");

        Instant late = tap.plusMillis(3_100);
        try {
            timeoutFor(Duration.ofSeconds(2), deadline, late);
        } catch (IllegalStateException e) {
            System.out.println("at 3.1 s: " + e.getMessage());
        }
    }
}
```

Between services, the deadline travels in a header. gRPC, a popular way for services to call each other, does this for you: it calls it **deadline propagation**. The trap is the opposite: inner timeouts longer than outer ones, so work continues after the caller has left.

### Which failures to retry

Some failures pass if you try again. Others fail the same way every time. [REST design](/t/module-08-backend/topic-02-rest-design) gave the rule; here it is as a table.

| Failure | Retry? | Why |
|---|---|---|
| Timeout, connection refused or reset | yes | the network or server hiccuped |
| `503 Service Unavailable`, `502`, `504`, other 5xx | yes | the server is overloaded or restarting |
| `429 Too Many Requests` | yes, after `Retry-After` | you were too fast; wait as told |
| `400 Bad Request`, `422` | no | your request is wrong; it stays wrong |
| `401 Unauthorized`, `403 Forbidden` | no | fix the credentials first |
| `404 Not Found`, `409 Conflict` | no | the data says no, every time |

Retrying a 409 for "payment pay-301 already captured" just asks the same question again. You get the same answer, three times slower.

### Idempotency before retries

A timeout is the hardest case. You asked the payment provider to charge 460 cents, and heard nothing. Did it charge? You cannot know. The request may have arrived, and only the answer got lost.

Retry it blindly, and rider-7 may pay twice. So **make the call idempotent first**, then retry. Send the same `Idempotency-Key` with every try of one charge, as in [REST design](/t/module-08-backend/topic-02-rest-design). The provider sees the repeat and returns the first answer instead of charging again. Real payment providers, such as Stripe, accept exactly this header.

```http
POST /v1/charges HTTP/1.1
Host: payments.example
Idempotency-Key: rental-17-unlock
Content-Type: application/json

{"amountCents": 460, "rider": "rider-7"}
```

A key built from the rental, like `rental-17-unlock`, stays the same across retries and even across a server restart. The rule: no idempotency, no retry. GET, PUT and DELETE are idempotent already; a POST needs a key.

### Exponential backoff, a cap and full jitter

Retrying at once rarely helps: whatever broke a moment ago is probably still broken. So wait, and wait longer each time. That is **exponential backoff**: the limit doubles after every failed try.

```text
ceiling(attempt) = min(cap, base × 2^(attempt - 1))
wait             = a random number from 0 to ceiling     (full jitter)
```

With base 200 ms and cap 5 s, the ceilings are 200, 400, 800, 1,600, 3,200, then 5,000 ms forever. The **cap** stops the waits from growing to minutes.

**Full jitter** picks the actual wait at random, anywhere from 0 to the ceiling. Why random? At 08:30 the payment provider hiccups, and 1,000 phones fail at the same instant. Without jitter, all 1,000 retry at exactly 200 ms, then exactly 400 ms. Each wave hits the provider like a hammer and knocks it down again. Jitter spreads the same 1,000 retries evenly over the 400 ms. The Try it section lets you see both.

A well-known AWS study compared ways to add jitter. Full jitter was simple and among the best at finishing the work with the fewest calls.

### Max attempts and retry budgets

Retries must stop. Cap them, often at 3 attempts in all, the first try included. After that, fail and tell the rider, or fall back.

Max attempts still fails at scale. When the provider is fully down, every request becomes 3 requests. Your retries triple the load on a server that is already drowning. A **retry budget** caps retries as a share of normal traffic, such as 10%.

```callout jargon
A **retry budget** is a limit on retries across a whole client: for example, "retries may add at most 10% to our requests". When the budget is spent, failures are returned at once instead of retried.
```

Picture a big company's payment client making 1,000 calls a second. A 10% budget allows it at most 100 retries a second. When a few calls fail, all of them are retried. When everything fails, retries stop adding fuel. The Envoy proxy calls this a retry budget; gRPC's version is called retry throttling.

### Retry amplification: 3 × 3 × 3

The worst retry bug hides across layers. The rider app retries 3 times. The Spoke API retries 3 times. The billing service it calls retries 3 times too. Each layer's loop wraps the next one's.

```java run
import java.util.function.BooleanSupplier;

public class Main {
    static int providerCalls = 0;

    /** The payment provider is down: every call fails. */
    static boolean chargeCard() {
        providerCalls++;
        return false;
    }

    /** Each layer's "helpful" retry loop: up to 3 tries. */
    static boolean withRetries(int attempts, BooleanSupplier call) {
        for (int i = 1; i <= attempts; i++) {
            if (call.getAsBoolean()) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        // rider app -> Spoke API -> billing service -> payment provider
        withRetries(3, () -> withRetries(3, () -> withRetries(3, Main::chargeCard)));
        System.out.println("one tap on Pay: " + providerCalls + " calls to the provider");
        System.out.println("1000 riders at rush hour: " + providerCalls * 1_000 + " calls");
    }
}
```

3 × 3 × 3 = 27 calls for one tap. With 5 layers it is 3⁵ = 243. This is called **retry amplification**, and it turns a small outage into a long one.

The fix: retry in **one** layer only, usually the one closest to the flaky dependency. Every other layer fails fast and passes the error up.

### Rate limiting: why, and per what

```callout jargon
A **rate limiter** caps how many requests one caller may send in a stretch of time, such as 20 a second. Requests over the cap get `429 Too Many Requests`.
```

Spoke needs one for three reasons:

- **Protection.** One caller cannot flood the servers and starve everyone else. This morning a partner app sent 4,000 requests a minute.
- **Fairness.** Each caller gets its own share, so a heavy user cannot take a quiet one's turn.
- **Cost.** Each SMS costs Spoke money. A bug that loops on "send SMS" must hit a wall, not the monthly bill.

Limits are counted **per key**. The key decides who shares a counter:

| Key | When | Trap |
|---|---|---|
| rider id | logged-in rider requests | none before login |
| API key | partner apps, like the one that flooded us | a leaked key can be shared by many |
| IP address | before login, such as the sign-in page | a whole campus can share one IP |

[REST design](/t/module-08-backend/topic-02-rest-design) promised each app an allowance, say 20 requests a second. The next five cards are the ways to count it.

### Fixed window counter

Keep one counter per key per calendar minute. Each request adds 1. Over the limit, answer 429. At 08:01:00 a new counter starts at 0.

It is cheap: one number per key, and **O(1)** time and memory. But it has a famous hole at the edge of a window. Say the limit is 100 a minute:

| Time | Requests | Counted in | Allowed? |
|---|---|---|---|
| 08:00:59.5 | 100 | the 08:00 minute | yes, 100 of 100 |
| 08:01:00.5 | 100 | the 08:01 minute | yes, a fresh 100 |

That is **200 requests in one second**, twice the limit, and all of them allowed. This is the **boundary burst**.

### Sliding window log

Keep the time of every allowed request for each key. On a new request, throw away times older than 60 s. Count what is left. Below the limit? Allow it and add its time.

It is **exact**: at any moment, it counts precisely the last 60 seconds, so no boundary burst. The price is memory. Each key stores up to `limit` timestamps, so memory is **O(limit)** per key.

The math for Spoke: 100,000 riders × 100 timestamps × 8 bytes is 80,000,000 bytes, about 80 MB of raw numbers. Each stored entry also carries extra bookkeeping, so the real total is several times that.

### Sliding window counter

This one keeps two counters per key: last minute's and this minute's. It estimates the last 60 seconds by assuming last minute's requests were spread evenly.

```text
estimate = previous × (time left in this minute ÷ 60 s) + current
```

Worked example. The limit is 100. Last minute had 80 requests. This minute has 30 so far, and it is 15 s in. The last 60 seconds still overlap 45 s of last minute, so its weight is 45 ÷ 60 = 0.75.

estimate = 80 × 0.75 + 30 = 60 + 30 = **90**. That is below 100, so the request is allowed.

Memory is **O(1)** per key, like the fixed window, and the boundary burst mostly disappears. It is an estimate, though: the Worked example shows a burst where it lets a few too many through. On real traffic, which is spread out, the error is tiny. Big CDNs, the networks that serve websites worldwide, use it for exactly that reason.

### Token bucket

Picture a bucket of tokens for each key. Each request takes one token. No token, no entry. Tokens drip back in at a fixed rate, but the bucket never holds more than its size.

- **Capacity** is the biggest burst allowed at once.
- **Refill rate** is the long-run requests per second.

```text
tokens = min(capacity, tokens + seconds since last refill × refill rate)
```

The partner app's allowance: capacity 40, refill 20 a second. It can burst 40 requests at once, then keep 20 a second forever. Its 66 a second this morning would get about 20 through each second; the rest get 429.

```animate
title: The partner app's bucket refills
kind: code
code: |
  long capacity = 5;
  long ratePerSecond = 2;
  long tokens = 5;
  tokens = tokens - 5;
  System.out.println("after the burst: " + tokens);
  long elapsedMs = 1_500;
  tokens = Math.min(capacity, tokens + elapsedMs * ratePerSecond / 1_000);
  System.out.println("after 1.5 s: " + tokens);
  tokens = tokens - 1;
  elapsedMs = 60_000;
  tokens = Math.min(capacity, tokens + elapsedMs * ratePerSecond / 1_000);
  System.out.println("after a quiet minute: " + tokens);
steps:
  - line: 1
    vars: { capacity: 5 }
    say: A small bucket to watch. It holds at most 5 tokens, so the biggest burst is 5 requests.
  - line: 2
    vars: { ratePerSecond: 2 }
    say: It refills 2 tokens a second. That is the steady rate the caller may keep up.
  - line: 3
    vars: { tokens: 5 }
    say: A new bucket starts full.
  - line: 4
    vars: { tokens: 0 }
    say: The app fires 5 requests at once. Each takes a token, and the bucket is empty.
  - line: 5
    out: "after the burst: 0"
    say: A 6th request now would get 429 Too Many Requests.
  - line: 6
    vars: { elapsedMs: 1500 }
    say: 1.5 seconds pass. Nobody adds tokens on a timer; the bucket works them out from the time that passed.
  - line: 7
    vars: { tokens: 3 }
    say: 1,500 ms × 2 per second = 3 tokens. 3 is below the capacity of 5, so all 3 count.
  - line: 8
    out: "after 1.5 s: 3"
    say: Three more requests may go now.
  - line: 9
    vars: { tokens: 2 }
    say: One request spends a token.
  - line: 10
    vars: { elapsedMs: 60000 }
    say: Then a quiet minute.
  - line: 11
    vars: { tokens: 5 }
    say: A minute earns 120 tokens, but min caps it at 5. A quiet hour never buys a huge burst later.
  - line: 12
    out: "after a quiet minute: 5"
    say: Full again, and never more than full.
```

Each check is a little arithmetic, so it is **O(1)** time, and two numbers per key make it **O(1)** memory. Token buckets are the most common choice, because they allow short bursts while holding the long-run rate. You sketched one in [object-oriented design](/t/module-06-craft/topic-13-object-oriented-design); the practice builds it for real.

### Leaky bucket, and choosing one

A **leaky bucket** is a queue that empties at a fixed rate, like water dripping from a hole. Requests join the queue; a worker takes them out at a steady pace. When the queue is full, new requests are dropped.

It **smooths** traffic: the output rate is constant, however bursty the input. That suits Spoke's own outgoing SMS. The SMS provider allows 50 messages a second, so Spoke queues texts and sends them at 50 a second. A token bucket instead lets a burst through at once, up to its capacity.

| Algorithm | Bursts | Memory per key | Accuracy |
|---|---|---|---|
| Fixed window | up to 2× the limit at a boundary | O(1) | rough |
| Sliding window log | none beyond the limit | O(limit) | exact |
| Sliding window counter | small | O(1) | close estimate |
| Token bucket | up to the capacity, by design | O(1) | exact for its rule |
| Leaky bucket | none: output is smoothed | O(queue size) | exact; adds waiting |

### Where to limit, and counting across many servers

Limits live in layers:

- **At the edge**, in an **API gateway** (a server in front of all your APIs that checks every request first) or the load balancer. It rejects floods before they cost you anything. It knows IPs and API keys.
- **In the service**. It knows the rider and the business rules, such as "at most 3 unlock attempts a minute".
- **On the way out**, before calling a provider with its own limit, like the SMS provider's 50 a second.

Spoke runs six copies of the API. If each copy keeps its own counters in memory, a rider can use up each copy's limit. The real limit becomes 6 × 100 = 600. Shared limits need a shared counter, usually in **Redis**, a fast in-memory data store that all servers can reach.

```java nocompile
// key "rate:rider-7": one counter, shared by every server
Long count = redis.opsForValue().increment(key);       // Redis INCR: add 1 and return the new value, atomically
if (count == 1) {
    redis.expire(key, Duration.ofSeconds(60));          // Redis EXPIRE: delete the counter after 60 s
}
return count <= 100;
```

`INCR` is **atomic**: two servers can never read the same old value. A "GET, add 1 in Java, SET" version would lose counts, the lost update from [Concurrent writes](/t/module-08-backend/topic-15-concurrent-writes).

There is still a race. `INCR` and `EXPIRE` are two separate commands. If a server crashes between them, the counter never expires. Rider-7 hits 100 and is blocked forever. The fix is to make the pair atomic: run both inside one small Lua script, which Redis runs as a single step.

```text
local count = redis.call('INCR', KEYS[1])
if count == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end
return count
```

One more decision: what if Redis itself is down? Most teams **fail open**: they let requests through, maybe with a looser in-memory limit, rather than refuse every rider.

### Syntax: 429 with Retry-After

```http
HTTP/1.1 429 Too Many Requests
Retry-After: 2
Content-Type: application/problem+json

{"type": "https://spoke.example/problems/rate-limited", "title": "Too many requests", "status": 429,
 "detail": "This API key may send 20 requests a second. Try again in 2 seconds."}
```

`Retry-After` holds whole seconds to wait, or a date. Round **up**: if 300 ms remain, say 1, never 0. `Retry-After: 0` invites the client to hammer you at once.

Many APIs, GitHub's among them, also tell clients where they stand on every answer, with `X-RateLimit-Limit`, `X-RateLimit-Remaining` and `X-RateLimit-Reset`. An IETF draft standard names these fields `RateLimit` and `RateLimit-Policy`. A good client reads them and slows down before it ever sees a 429.

### Circuit breakers: closed, open, half-open

Retries help with short blips. When a provider is truly down, retrying just wastes threads. A circuit breaker notices and stops calling it for a while, like a fuse in a house.

```callout jargon
A **circuit breaker** sits between your code and a dependency. After too many failures it "opens" and rejects calls at once, for a while. A **fallback** is what you do instead when a call fails or is rejected.
```

```animate
title: The SMS breaker trips, and heals
kind: scene
nodes:
  - { id: api, label: Spoke API, emoji: "🖥️" }
  - { id: breaker, label: Circuit breaker, emoji: "🔌", value: "CLOSED, 0 failures", col: 2 }
  - { id: sms, label: SMS provider, emoji: "📨", value: up, col: 4 }
  - { id: queue, label: SMS queue, emoji: "📥", value: empty, row: 1, col: 2 }
steps:
  - say: Closed is normal. Each "Ride started" text passes through the breaker to the provider.
    send: [{ from: api, to: breaker, label: send SMS }, { from: breaker, to: sms, label: send SMS }]
  - say: At 08:10 this morning, the provider stops answering. The call times out after 2 s, and the breaker counts one failure.
    set: { sms: down, breaker: "CLOSED, 1 failure" }
    send: { from: breaker, to: sms, label: "send SMS: timeout" }
  - say: Two more timeouts. That makes 3 failures in a row, the threshold. The breaker opens.
    set: { breaker: "OPEN for 30 s" }
    focus: [breaker]
  - say: While open, the breaker answers at once with CallNotPermittedException. No thread waits on the provider.
    send: [{ from: api, to: breaker, label: send SMS }, { from: breaker, to: api, label: not permitted, back: true }]
  - say: That is the fallback's cue. The text goes into a queue to send later. The rental still starts.
    show: [queue]
    send: { from: api, to: queue, label: "queue for rider-7" }
    set: { queue: "1 waiting" }
  - say: 30 s later the breaker is half-open. It lets exactly one trial call through to test the water.
    set: { breaker: "HALF_OPEN", sms: up again }
    send: { from: breaker, to: sms, label: trial SMS }
  - say: The trial works, so the breaker closes and the count starts at 0. The queued texts go out.
    send: { from: sms, to: breaker, label: "200 OK", back: true }
    set: { breaker: "CLOSED, 0 failures", queue: empty }
  - say: Had the trial failed, the breaker would open again for another 30 s.
    focus: [breaker]
```

The three states:

- **Closed**: calls go through. The breaker counts failures.
- **Open**: calls fail at once, without touching the provider. It stays open for the **open duration**, say 30 s.
- **Half-open**: a few **trial calls** go through. Success closes the breaker; a failure opens it again for a fresh open duration.

When does it open? Two common rules:

- **Consecutive failures**: open after 5 failures in a row. Simple, and good when calls are few.
- **Failure rate**: open when 50% of the last 20 calls failed, with a **minimum number of calls**, say 10. Without that minimum, 1 failure out of 1 call is "100%". Libraries can also count **slow calls** as failures: a call that takes 5 s is nearly as bad as an error.

The breaker usually sits inside the retry. When it is open, the retry must not retry `CallNotPermittedException`: the answer is already "no".

### Fail fast, fall back, shed load

An open breaker answers in microseconds. That speed is the point: no thread waits, and the provider gets room to recover. What you do with the failure is the **fallback**, a product decision per feature:

| Feature | When its dependency fails |
|---|---|
| "Ride started" SMS | put it in a queue and send it later; the app already shows the ride |
| ride receipt by email | skip it now, retry overnight |
| card payment at unlock | cannot skip; show "Payments are slow, please try again in a minute" |

```callout jargon
**Graceful degradation** means a system drops or delays less important features so the important ones keep working. **Load shedding** means refusing some requests on purpose when overloaded, so the rest are served well.
```

This is graceful degradation: Spoke gets a little worse instead of falling over. Riders barely notice a late text. They definitely notice "rentals are down".

Load shedding is the same idea for your own server. When it is overloaded, it refuses some work early and quickly, with `503` and `Retry-After`. Slow refusals are useless: they burn the very threads you are short of. Shed the least important requests first: ride history before rental starts. A bounded queue with a rejection policy, from [executors](/t/module-05-advanced-java/topic-03-executors), is a simple load shedder.

### Bulkheads

A ship's hull is split into watertight rooms called **bulkheads**. One hole floods one room, and the ship stays afloat.

```callout jargon
A **bulkhead** is a separate, limited share of threads or connections for one dependency. When that dependency hangs, only its share is used up.
```

In software, a bulkhead gives each dependency its own limited share of threads. This morning SMS and payments shared 100 threads, so stuck SMS calls sank payments. With bulkheads, SMS may use at most 20. When those 20 are stuck, the 21st SMS is queued for later, and payments keep their own threads.

The simplest bulkhead is a `Semaphore`, from [coordinating threads](/t/module-05-advanced-java/topic-15-coordinating-threads), with `tryAcquire()` so nobody waits:

```java run
import java.util.concurrent.Semaphore;

public class Main {
    // The SMS provider may use at most 2 request threads at once. Spoke's real setting would be bigger, like 20.
    static final Semaphore smsSlots = new Semaphore(2);

    static String notifyRider(String riderId) {
        if (!smsSlots.tryAcquire()) {
            return riderId + ": SMS queued for later, rental still starts";
        }
        // A real call would send the SMS here, then smsSlots.release() in a finally block.
        // We keep the slot taken, to act as a slow call that is still hanging.
        return riderId + ": SMS call started, free slots: " + smsSlots.availablePermits();
    }

    public static void main(String[] args) {
        for (String rider : new String[] {"rider-7", "rider-8", "rider-9", "rider-10"}) {
            System.out.println(notifyRider(rider));
        }
    }
}
```

The other way is a separate thread pool per dependency. It costs more threads, but it also lets the caller stop waiting with a timeout, as in [CompletableFuture](/t/module-05-advanced-java/topic-04-completable-future)'s `orTimeout`. Size each bulkhead with Little's law. 2.8 SMS a second × a 2 s timeout ≈ 6 threads, even if every call hits the 2 s timeout. So 20 leaves plenty of room.

### Resilience4j

You will rarely hand-write these tools at work. **Resilience4j** is the common Java library for them, and Spring Boot works with it through annotations.

```java nocompile
@Service
class RiderNotifier {
    private final SmsClient sms;
    private final SmsQueue queue;

    @Bulkhead(name = "sms")
    @CircuitBreaker(name = "sms", fallbackMethod = "sendLater")
    public void rideStarted(String riderId, String rentalId) {
        sms.send(riderId, "Ride " + rentalId + " started. Have a good ride!");
    }

    // Same parameters plus the exception. Runs when the call fails or the breaker is open.
    private void sendLater(String riderId, String rentalId, Throwable why) {
        queue.add(riderId, "Ride " + rentalId + " started");
    }
}
```

The numbers live in configuration, so ops can tune them without a code change:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      sms:
        slidingWindowSize: 20
        minimumNumberOfCalls: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 1
  bulkhead:
    instances:
      sms:
        maxConcurrentCalls: 20
        maxWaitDuration: 0
  retry:
    instances:
      payment:
        maxAttempts: 3
        waitDuration: 200ms
        enableExponentialBackoff: true
        ignoreExceptions:
          - io.github.resilience4j.circuitbreaker.CallNotPermittedException
```

When you stack annotations, Resilience4j applies them in a fixed order by default, outermost first: Retry, CircuitBreaker, RateLimiter, TimeLimiter, Bulkhead. So each retry passes through the breaker, as the previous card advised. Its open breaker throws `CallNotPermittedException`, the same name the practice uses.

### Chaos testing

How do you know the fallbacks work? Your laptop's SMS provider never goes down. So break things on purpose. That is **chaos testing**.

```callout jargon
**Chaos testing** (or chaos engineering) means injecting failures on purpose, such as slow answers, dropped connections or dead servers, to prove the system copes before a real outage does it for you.
```

- **In tests**, use fakes that time out or throw, exactly like the practice checks. A tool called **Toxiproxy** sits between your app and a real dependency and adds delay or cuts connections on command.
- **In staging and production**, Netflix's **Chaos Monkey** famously shuts down random servers during working hours, so teams must build for it.
- **Game days** are planned drills: "the payment provider is down for 20 minutes; go." The team watches the dashboards and fixes what surprises them.

Start small and in a test environment. Chaos with no dashboards and no off switch is just an outage.

### In an interview

"Design a rate limiter" is one of the classic system design questions. Walk it in this order:

1. **Clarify.** Limit per what: user, API key or IP? What limits, such as 20 a second? One server or many? Hard limit, or roughly right?
2. **Pick the algorithm and say why.** Token bucket for bursts with a steady rate. Sliding window counter for cheap and close. Sliding log for exact. Give O(1) or O(limit) memory per key.
3. **Do the memory math.** 100,000 riders × two numbers × 8 bytes is 1.6 MB. Easy for one Redis.
4. **Distribute it.** A shared Redis counter, made atomic with `INCR` or a Lua script. Mention the INCR-then-EXPIRE race.
5. **The answer.** 429 with `Retry-After` and rate limit headers.
6. **Failure.** Redis down: fail open, with a local backup limit. Place it at the gateway, plus in services for business rules.

Spot the wider topic when you hear "a dependency is slow", "cascading failure", "thundering herd" or "make it reliable". Other classics: "how do you call a flaky service safely?" (timeouts, idempotent retries with backoff and jitter, a breaker, a fallback). "What is a circuit breaker?" (the three states). "Why do retries make outages worse?" (amplification and synchronized waves).

## Worked example

Kakashi's ticket has a second culprit: the partner app's flood. Here is the edge-of-window bug in action. The same three bursts go through a fixed window and a sliding window counter, with a limit of 10 a minute.

```java run
import java.util.HashMap;
import java.util.Map;

public class Main {
    static final long WINDOW_MS = 60_000;   // one minute
    static final int LIMIT = 10;            // 10 requests a minute per API key

    /** Fixed window: one counter per key per calendar minute. */
    static final Map<String, long[]> fixed = new HashMap<>();   // key -> {windowStart, count}

    static boolean fixedAllow(String key, long nowMs) {
        long windowStart = nowMs - nowMs % WINDOW_MS;
        long[] c = fixed.computeIfAbsent(key, k -> new long[] {windowStart, 0});
        if (c[0] != windowStart) {          // a new minute: start counting from 0
            c[0] = windowStart;
            c[1] = 0;
        }
        if (c[1] >= LIMIT) {
            return false;
        }
        c[1]++;
        return true;
    }

    /** Sliding window counter: this minute's count, plus last minute's count weighted by how much of it still overlaps. */
    static final class Counter {
        long windowStart;
        long previous;
        long current;
    }

    static final Map<String, Counter> sliding = new HashMap<>();

    static boolean slidingAllow(String key, long nowMs) {
        long windowStart = nowMs - nowMs % WINDOW_MS;
        Counter c = sliding.computeIfAbsent(key, k -> new Counter());
        if (c.windowStart != windowStart) {
            boolean lastMinute = windowStart - c.windowStart == WINDOW_MS;
            c.previous = lastMinute ? c.current : 0;   // older than one minute: forget it
            c.current = 0;
            c.windowStart = windowStart;
        }
        long elapsed = nowMs - windowStart;
        // estimate = previous * (WINDOW - elapsed) / WINDOW + current, kept in whole numbers by multiplying by WINDOW
        long estimateTimesWindow = c.previous * (WINDOW_MS - elapsed) + c.current * WINDOW_MS;
        if (estimateTimesWindow >= LIMIT * WINDOW_MS) {
            return false;
        }
        c.current++;
        return true;
    }

    static void burst(String clock, long atMs) {
        int fixedYes = 0;
        int slidingYes = 0;
        for (int i = 0; i < 10; i++) {
            if (fixedAllow("key-partner-app", atMs)) fixedYes++;
            if (slidingAllow("key-partner-app", atMs)) slidingYes++;
        }
        System.out.println(clock + "  fixed window: " + fixedYes + " of 10   sliding counter: " + slidingYes + " of 10");
    }

    public static void main(String[] args) {
        burst("08:00:50", 50_000);    // 10 requests, 50 s into the first minute
        burst("08:01:10", 70_000);    // 10 more, 10 s into the next minute
        burst("08:01:40", 100_000);   // 10 more, 40 s into it
    }
}
```

It prints:

```text
08:00:50  fixed window: 10 of 10   sliding counter: 10 of 10
08:01:10  fixed window: 10 of 10   sliding counter: 2 of 10
08:01:40  fixed window: 0 of 10   sliding counter: 5 of 10
```

Step by step:

1. **`windowStart = nowMs - nowMs % WINDOW_MS`** rounds the time down to the start of its minute. 70,000 ms becomes 60,000.
2. **08:00:50.** Both limiters start empty, so all 10 get through.
3. **08:01:10, fixed window.** A new minute began 10 s ago, so its counter is back at 0. All 10 pass: **20 requests in 20 seconds**, double the limit.
4. **08:01:10, sliding counter.** It is 10 s into the minute, so last minute still weighs 50 ÷ 60. Request 1 sees 10 × 0.83 = 8.33 and is allowed (now 9.33). Request 2 sees 9.33, still under 10, and is allowed (10.33). Request 3 sees 10.33 and is refused, with the rest: 2 of 10.
5. **The integer trick.** Instead of dividing by `WINDOW_MS`, the code multiplies the limit by it. Whole numbers only, so no rounding surprises.
6. **08:01:40.** The fixed window already spent this minute's 10, so it refuses everything for 20 more seconds. The sliding counter weighs last minute at 20 ÷ 60 now: 3.33 + 2 = 5.33, so it lets 5 more through.

Look at the last 60 seconds at 08:01:40: the sliding counter let 10 + 2 + 5 = 17 through. Its guess "last minute was spread evenly" was wrong here, because all 10 came in one burst. A sliding log would have said no to all of the third burst. That is the trade: two numbers per key instead of a list of times.

Each check is O(1) time. Memory is O(1) per key: two counts and a start time.

## Try it

1,000 phones failed at the same instant. This is their next retry, with a ceiling of 400 ms. Run it to see where the retries land.

```java run
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        int phones = 1_000;          // phones whose request failed at 08:30:00.000
        long ceilingMs = 400;        // their second retry: wait up to 400 ms
        boolean jitter = true;       // try false
        Random random = new Random(7);

        int[] perSlot = new int[5];  // five 80 ms slots between 0 and 400 ms
        for (int i = 0; i < phones; i++) {
            long wait = jitter ? random.nextLong(ceilingMs + 1) : ceilingMs;
            perSlot[(int) Math.min(4, wait / 80)]++;
        }
        for (int slot = 0; slot < perSlot.length; slot++) {
            String bar = "#".repeat(perSlot[slot] / 20);
            System.out.printf("%3d-%3d ms: %4d retries %s%n", slot * 80, slot * 80 + 79 + (slot == 4 ? 1 : 0), perSlot[slot], bar);
        }
    }
}
```

1. Set `jitter` to `false`. All 1,000 retries land in one slot, at exactly 400 ms. That is the wave that knocks a provider over again.
2. Put `jitter` back to `true`, and change the seed `7` to any number. The bars stay roughly level. Why does the seed hardly matter?
3. Lower `ceilingMs` to 100. Now every retry crowds into the first two slots. Why does a bigger ceiling, after more failures, spread the load more thinly?

## Recap

- Every network call needs a connect timeout and a read timeout, because many Java clients wait forever by default. Pass one deadline down a chain of calls.
- Retry only failures that may pass (timeouts, 429, 5xx), only idempotent calls, with exponential backoff, a cap and full jitter. Cap attempts, use a retry budget, and retry in one layer only: 3 × 3 × 3 is 27.
- Rate limits protect, keep things fair and save money. Fixed windows allow boundary bursts, sliding logs are exact but heavy, sliding counters estimate, and token buckets allow bursts up to their capacity. Answer 429 with `Retry-After`.
- A circuit breaker goes closed, open, half-open: it fails fast while a dependency is down, then tests it with a trial call.
- Bulkheads give each dependency its own threads, so one slow provider cannot sink the rest. Fall back, degrade gracefully and shed load instead of falling over.

## Common mistakes

**1. Trusting the default timeout.**

```java nocompile
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder(URI.create("https://sms.example/send")).build();
client.send(request, HttpResponse.BodyHandlers.ofString());
```

```text
HttpClient connect timeout: Optional.empty
HttpRequest timeout:        Optional.empty
```

There is no error at all. A provider that accepts the connection and never answers keeps this thread forever. Enough of them, and the pool is empty. Fix: `.connectTimeout(Duration.ofSeconds(1))` on the client and `.timeout(Duration.ofSeconds(2))` on every request.

**2. Retrying a payment without an idempotency key.**

```java nocompile
for (int attempt = 1; attempt <= 3; attempt++) {
    try {
        return payments.charge("rider-7", 460);   // a timeout here may still have charged the card
    } catch (TimeoutException e) {
        // try again
    }
}
```

Symptom: a rider sees two charges of 460 cents for one ride. The first charge worked; only its answer was lost. Fix: send the same `Idempotency-Key`, such as `rental-17-unlock`, with every try, so the provider returns the first result.

**3. Retrying every exception.**

```java nocompile
} catch (RuntimeException e) {
    sleeper.sleep(backoff.delayMillis(attempt));   // also retries 400, 409 and CallNotPermittedException
}
```

Symptom: a `409 Conflict` takes three tries and several seconds to reach the rider, and an open breaker gets hammered with retries. Fix: check `isRetryable(e)` first: timeouts, 429 and 5xx only. Throw everything else at once.

**4. Retries in every layer.**

```java nocompile
// rider app: 3 tries -> Spoke API: 3 tries -> billing: 3 tries -> payment provider
```

```text
one tap on Pay: 27 calls to the provider
```

The provider gets 27 times the traffic exactly when it is weakest. Fix: retry in one layer, the one next to the provider. The other layers fail fast and pass the error up.

**5. Backoff without jitter.**

```java nocompile
long wait = Math.min(capMillis, baseMillis * (1L << (attempt - 1)));   // the same wait on every phone
```

```text
320-400 ms: 1000 retries ##################################################
```

Every phone retries at the same instant, so the waves keep knocking the provider down. Fix: full jitter, `random.nextLong(ceiling + 1)`. Also note `1L << 64` is `1` in Java: huge attempt numbers need a guard, or the cap check comes first.

**6. Counting the limit in each server's memory.**

```java nocompile
private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();   // one map per server
```

Symptom: the limit is 100 a minute, but a client whose calls land on all six servers gets 600. Fix: keep shared limits in one place all servers reach, such as Redis with an atomic `INCR`. Keep in-memory buckets for per-server protection only.
