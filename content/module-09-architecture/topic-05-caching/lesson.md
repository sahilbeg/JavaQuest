## Hook

A school library keeps most books in a basement archive. Fetching one takes the librarian ten minutes. So the ten books everyone asks for sit on the front desk instead. Most students get their book in seconds, and the librarian only walks downstairs for the rare ones.

Spoke has the same problem. Every app open asks the slow rider-profile service for the same profile. The pricing service works out the same EBIKE quote thousands of times a minute. In this ticket you learn to keep answers close by, and the harder part: knowing when an answer has gone bad.

## Concept

### What a cache is

In [how computers work](/t/module-00-foundations/topic-01-how-computers-work) you saw that each kind of storage is far slower than the one above it. That lesson promised that keeping a copy of data close by, called **caching**, would come back. Here it is.

```callout jargon
A **cache** is a small, fast store that keeps copies of answers you already fetched. The **source of truth** is where the real data lives, such as the profile database. A **cache key** is the name you look a copy up by, such as `rider-7`.
```

Why bother? Two reasons. A copy close by is faster to read. And every answer served from the cache is one request the slow service never sees.

The price is that a copy can go out of date. Almost every caching problem in this ticket is about that price.

### Hits, misses and hit ratio

When the answer is in the cache, that is a **hit**. When it is not, that is a **miss**, and you must ask the source of truth.

```callout jargon
The **hit ratio** is hits divided by all lookups. 90 hits out of 100 lookups is a hit ratio of 0.9, or 90%.
```

Say a shared cache answers in 1 ms, and the profile service in 50 ms. A miss costs both: 1 ms to check the cache, then 50 ms. So the average time is `ratio × 1 + (1 − ratio) × 51`.

```java run
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        double hitMs = 1;               // the answer is in the cache
        double missMs = 1 + 50;         // check the cache, then ask the profile service
        int requestsPerMinute = 10_000;

        for (double hitRatio : new double[] {0.0, 0.5, 0.9, 0.99}) {
            double averageMs = hitRatio * hitMs + (1 - hitRatio) * missMs;
            long reachStore = Math.round((1 - hitRatio) * requestsPerMinute);
            System.out.println(String.format(Locale.ROOT,
                    "hit ratio %4.0f%%: average %5.2f ms, %5d calls a minute reach the store",
                    hitRatio * 100, averageMs, reachStore));
        }
    }
}
```

It prints:

```text
hit ratio    0%: average 51.00 ms, 10000 calls a minute reach the store
hit ratio   50%: average 26.00 ms,  5000 calls a minute reach the store
hit ratio   90%: average  6.00 ms,  1000 calls a minute reach the store
hit ratio   99%: average  1.50 ms,   100 calls a minute reach the store
```

Two lessons hide in those numbers. Going from 90% to 99% cuts the store's load by ten, not by 10%. And a cache with no hits is **slower** than no cache, because every request pays for the useless check. Always measure the hit ratio in production.

### Where caches live

Caches sit at every level, from the chip to the rider's phone.

| Where | How fast | Shared by | Spoke example |
|---|---|---|---|
| CPU cache | about 1 ns | one CPU core | the JVM's hot loop over fares |
| In-process (a map inside your JVM) | under 1 µs | one server | the fare table, held in memory by each API server |
| Distributed cache (Redis, Memcached) | about 1 ms over the network | every server | rider profiles, shared by all API servers |
| CDN | a few ms from a nearby city | every rider | the app's bike pictures and help pages |
| Browser or app HTTP cache | 0 ms: no request at all | one phone | the rider's own profile, kept for a minute |

```callout jargon
**Redis** and **Memcached** are servers that keep keys and values in memory and answer over the network. A **CDN** (content delivery network) is a set of servers around the world that keep copies of files near the people who download them.
```

An in-process cache is the fastest, but each server has its own copy. A distributed cache is one copy for everyone, but each lookup is a network round trip. You meet that trade-off again below.

### Cache-aside

The most common pattern is **cache-aside**, also called lazy loading. Your code talks to the cache and to the store itself.

1. Look in the cache. A hit? Return it.
2. A miss: load from the store.
3. Put the answer in the cache, then return it.

```animate
title: Cache-aside for a rider profile
kind: scene
nodes:
  - { id: app, label: Rider app, emoji: "📱" }
  - { id: api, label: Spoke API, emoji: "🖥️" }
  - { id: cache, label: Cache, emoji: "⚡", value: empty }
  - { id: store, label: Profile service, emoji: "🐢" }
steps:
  - say: Ada opens the app. It asks the API for rider-7's profile.
    send: { from: app, to: api, label: "GET profile rider-7" }
  - say: The API looks in the cache first. Nothing there yet. That is a miss.
    send: [{ from: api, to: cache, label: "get rider-7" }, { from: cache, to: api, label: "miss", back: true }]
  - say: Only now does the API ask the slow profile service. This takes 50 ms.
    send: [{ from: api, to: store, label: "load rider-7" }, { from: store, to: api, label: "Ada, harbor", back: true }]
  - say: The API keeps a copy in the cache, then answers.
    send: [{ from: api, to: cache, label: "put rider-7" }, { from: api, to: app, label: "200 OK", back: true }]
    set: { cache: "rider-7" }
  - say: Ada opens the app again. This time the cache has it. A hit, in 1 ms, and the profile service never hears about it.
    send: [{ from: app, to: api, label: "GET profile rider-7" }, { from: api, to: cache, label: "get rider-7" }, { from: cache, to: api, label: "hit", back: true }]
    focus: [cache]
```

Cache-aside is simple, and the cache only holds data someone asked for. A cache failure is not fatal: the app falls back to the store. The cost: the first read of each key is always slow, and your code must keep the cache and the store in step.

### Designing the cache key

The key decides which requests share an answer. It has two halves, and both matter.

**Put in everything the answer depends on.** A key like `profile:me` caches the first rider's profile and shows it to every rider. That is a data leak. A profile key needs the rider id. A fare quote key needs the bike type and the minutes. If the answer changes with the language or the plan, those go in too.

**Put in nothing else.** A request id, a timestamp or a trace id is different on every request. A trace id is a tag that follows one request through every service. Add one to the key, and no two requests ever share an entry.

```java run
import java.util.HashMap;
import java.util.Map;

public class Main {
    static int pricingCalls = 0;

    static long slowPricing(String bikeType, int minutes) {
        pricingCalls++;
        return 100 + 30L * minutes;
    }

    static void run(String label, boolean keyHasRequestId) {
        Map<String, Long> cache = new HashMap<>();
        pricingCalls = 0;
        int hits = 0;
        for (int requestId = 1; requestId <= 1_000; requestId++) {
            String key = keyHasRequestId
                    ? "quote:EBIKE:20:req-" + requestId     // WRONG: the request id changes every time
                    : "quote:EBIKE:20";                     // only what the price depends on
            if (cache.containsKey(key)) {
                hits++;
            } else {
                cache.put(key, slowPricing("EBIKE", 20));
            }
        }
        System.out.println(label + ": " + hits / 10 + "% hits, " + pricingCalls + " pricing calls, "
                + cache.size() + " entries kept");
    }

    public static void main(String[] args) {
        run("key with request id", true);
        run("key without it     ", false);
    }
}
```

It prints:

```text
key with request id: 0% hits, 1000 pricing calls, 1000 entries kept
key without it     : 99% hits, 1 pricing calls, 1 entries kept
```

With the request id in the key, the cache never hits. The pricing service gets exactly the load it had before, and memory fills with entries nobody reads again. Nothing errors, so the only clue is the hit ratio. That is why you watch it.

A good test for a key: requests that must get the same answer build the same key. Requests that may get different answers do not.

### Read-through and write-through

In **read-through**, the cache loads missing keys itself. Your code only ever asks the cache. You hand the cache a loader function once, and it calls the store on a miss. Libraries such as Caffeine work this way.

In **write-through**, every write goes to the cache, and the cache writes it to the store before saying "done". The cache always holds the latest value, but each write waits for both.

```callout jargon
A **loader** is the function a cache calls to fetch a missing key from the source of truth.
```

Read-through keeps your code shorter. Write-through keeps reads fresh after writes. Both put the cache in the middle of every request, so the cache must be very reliable.

### Write-behind and write-around

**Write-behind** (or write-back) writes to the cache and answers at once. The cache writes to the store later, often in batches. Writes are very fast. But if the cache crashes before it writes, those changes are lost. Never use it for payments.

**Write-around** writes only to the store and skips the cache. The cache fills on the next read. It suits data that is written often but rarely read back, such as ride logs.

| Pattern | Reads | Writes | Main risk |
|---|---|---|---|
| Cache-aside | app checks the cache, loads on a miss | app writes the store, deletes the key | stale copies if the delete is missed |
| Read-through | the cache loads on a miss | (paired with a write pattern) | the cache is in every request's path |
| Write-through | always fresh | cache and store, both, before answering | slower writes |
| Write-behind | always fresh | cache now, store later | lost writes if the cache dies |
| Write-around | first read after a write misses | store only | a burst of misses after writes |

### TTL and staleness

A copy is **stale** when the source of truth has changed but the cache still holds the old answer.

```callout jargon
A **TTL** (time to live) is how long a cached copy may be used. After it, the copy **expires** and the next read loads a fresh one.
```

You choose a TTL by asking: how wrong may this answer be, and for how long? A rider's name and home station can be ten minutes old. The fare table can be a minute old, if new fares start on the minute. A bike's lock state cannot be cached at all.

A TTL is also a safety net. Even if you forget to delete a key on some rare write path, the wrong answer dies when the TTL runs out. That is why almost every cached key should have one.

In the practice, an entry is expired when `now >= putTime + ttl`. Pick one rule like that, write it down, and test the exact boundary.

### Jitter: do not expire everything at once

Imagine Spoke restarts its servers at 08:00, and every profile is cached in the next minute with a 10 minute TTL. At 08:10, all of them expire together, and the profile service gets the whole morning's load at once.

So add **jitter**: a small random change to each TTL. Each key then expires at a slightly different moment.

```java run
import java.time.Duration;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Duration base = Duration.ofMinutes(10);
        Random random = new Random(42);     // fixed seed: the same "random" numbers on every run

        for (int i = 1; i <= 5; i++) {
            long jitterSeconds = random.nextInt(121) - 60;     // somewhere from -60 to +60
            Duration ttl = base.plusSeconds(jitterSeconds);
            System.out.println("rider-" + i + " expires after " + ttl.toMinutesPart() + " min " + ttl.toSecondsPart() + " s");
        }
    }
}
```

It prints `rider-1 expires after 10 min 57 s`, then 9 min 15 s, 10 min 36 s, and so on. A real server uses an unseeded `Random`. The fixed seed only makes this lesson's output the same every time.

### Eviction: LRU, LFU and FIFO

Memory is limited, so a cache has a **capacity**. When it is full and a new key arrives, one key must go.

```callout jargon
**Eviction** is removing an entry to make room for a new one. An **eviction policy** decides which entry goes.
```

- **LRU** (least recently used): evict the key nobody has read for the longest time. It is the usual default, because what was used recently is likely to be used again soon.
- **LFU** (least frequently used): evict the key with the fewest reads. It keeps steady favourites, like the EBIKE 20 minute quote, even after a quiet spell. But an old favourite can hog space long after it stops being popular.
- **FIFO** (first in, first out): evict the oldest key, however often it is read. It is the simplest, but it throws out popular keys just for being old.

Redis lets you choose with its `maxmemory-policy` setting, for example `allkeys-lru`.

### Syntax: an LRU cache with LinkedHashMap

```java nocompile
new LinkedHashMap<K, V>(16, 0.75f, true)   // true = access order: every get or put moves the key to the end
protected boolean removeEldestEntry(Map.Entry<K, V> eldest)   // return true to drop the first (oldest) key
```

A `LinkedHashMap` remembers an order. With the third argument `true`, the order is **access order**. The least recently used key comes first, and every `get` or `put` moves a key to the end. After each new key, the map asks `removeEldestEntry`. Return `true`, and it drops the first key.

```animate
title: An LRU cache with three places
kind: code
code: |
  import java.util.LinkedHashMap;
  import java.util.Map;

  public class Main {
      public static void main(String[] args) {
          int capacity = 3;
          Map<String, String> lru = new LinkedHashMap<>(16, 0.75f, true) {
              @Override
              protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                  return size() > capacity;
              }
          };
          lru.put("rider-1", "Ada");
          lru.put("rider-2", "Ben");
          lru.put("rider-3", "Cy");
          lru.get("rider-1");
          lru.put("rider-4", "Dee");
          System.out.println(lru.keySet());
      }
  }
steps:
  - line: 7
    vars: { capacity: 3, lru: "[]" }
    say: An empty map in access order. The key on the left is the least recently used.
  - line: 13
    vars: { lru: "[rider-1]" }
    say: Ada's profile goes in.
  - line: 15
    vars: { lru: "[rider-1, rider-2, rider-3]" }
    say: Ben and Cy follow. The cache is now full, and rider-1 is the oldest.
  - line: 16
    vars: { lru: "[rider-2, rider-3, rider-1]" }
    say: Ada opens the app again. The get moves rider-1 to the end. Now rider-2 is the least recently used.
  - line: 17
    vars: { lru: "[rider-2, rider-3, rider-1, rider-4]" }
    say: Dee's profile arrives. For a moment the map holds four keys, one more than the capacity.
  - line: 10
    vars: { lru: "[rider-3, rider-1, rider-4]" }
    say: removeEldestEntry sees size 4 > 3 and returns true. The first key, rider-2, is evicted, even though it went in after rider-1.
  - line: 18
    out: "[rider-3, rider-1, rider-4]"
    say: The keys print from least to most recently used.
```

Both `get` and `put` are O(1) on average, because a hash map finds the key and a linked list keeps the order. Space is O(capacity). This exact structure, a hash map plus a doubly linked list, is the classic interview answer for "design an LRU cache".

Built with `false`, or the two-argument constructor, the map keeps **insertion order** instead, and the same code becomes a FIFO cache. `LinkedHashMap` is not thread-safe, so a shared cache wraps it in `synchronized` methods.

### Memory limits

A cache with no size limit is a memory leak with a nice name. Every new key stays forever, until the JVM dies with `OutOfMemoryError`.

Do the arithmetic before you choose a capacity. Spoke has 100,000 riders. If a profile takes about 1 KB in memory, all of them take about 100 MB. That fits in one Redis server. It may not fit in every API server's heap next to everything else.

Set limits in both places: a capacity on each in-process cache, and `maxmemory` with an eviction policy on Redis. Then watch the eviction count. Many evictions of keys that are soon read again mean the cache is too small.

### Invalidation: delete on write

When a rider changes their home station, the cached profile is wrong. Removing it is called **invalidation**.

```callout jargon
To **invalidate** a cached entry is to mark it as no longer usable, usually by deleting it, so the next read loads a fresh copy.
```

The safe recipe for cache-aside is:

1. Write the new profile to the source of truth.
2. **Then** delete the cached key.
3. The next read misses and loads the new profile.

The order matters. Delete first, and a read can sneak in before the write: it misses, loads the **old** profile, and caches it again. That stale copy then lives for a whole TTL.

There is a famous joke, credited to Phil Karlton: the two hard things in computer science are cache invalidation and naming things. The next card shows why.

### The race between a writer and a cache fill

Even "write, then delete" has one rare hole. A slow reader can load the old value **before** the write, and put it in the cache **after** the delete.

```animate
title: A slow reader puts an old profile back
kind: scene
nodes:
  - { id: reader, label: Reader, emoji: "👀" }
  - { id: cache, label: Cache, emoji: "⚡", value: empty, col: 2 }
  - { id: db, label: Profile database, emoji: "🗄️", value: harbor, col: 4 }
  - { id: writer, label: Writer, emoji: "✍️", row: 1, col: 3 }
steps:
  - say: rider-7's profile is not cached. A reader misses.
    send: { from: reader, to: cache, label: "get rider-7: miss" }
  - say: The reader loads the profile. Home station is harbor. Then its thread pauses for a moment.
    send: { from: reader, to: db, label: "load" }
    focus: [reader]
  - say: Meanwhile, a writer saves the new home station, uptown.
    send: { from: writer, to: db, label: "save uptown" }
    set: { db: uptown }
  - say: The writer deletes the cached key. There is nothing to delete yet.
    send: { from: writer, to: cache, label: "delete rider-7" }
  - say: The reader wakes up and caches what it loaded earlier, harbor.
    send: { from: reader, to: cache, label: "put harbor" }
    set: { cache: harbor }
  - say: The cache now says harbor while the database says uptown, until the TTL runs out.
    focus: [cache, db]
```

This needs a read and a write to the same key within milliseconds, and an unlucky pause. It is rare, but at Spoke's scale rare things happen daily. The defences:

- **A TTL** caps how long the wrong copy can live. This is the main reason never to cache without one.
- **Versions**: store a version number with each value, and refuse to put an older version over a newer one.
- **Delete twice**: delete again a moment after the write, to catch a slow reader. It narrows the gap but cannot close it.

### Why delete beats update

Why not just put the new profile into the cache on a write, instead of deleting? Because two writers can race.

Writer A saves uptown and writer B saves riverside, in that order. But A's thread is slower, so B updates the cache first and A updates it second. The database says riverside; the cache says uptown, forever, until the TTL.

Deleting has no such order problem. Two deletes in any order leave the same result: nothing cached. The next read loads whatever the database holds now. Deleting is also cheaper when the new value is expensive to build and nobody reads it soon.

### Cache stampede and single flight

A popular key expires. In the next 300 ms, 500 riders ask for the EBIKE 20 minute quote. Each one misses, and each one calls the pricing service. 500 identical slow calls hit a service that was sized for a handful.

```callout jargon
A **cache stampede**, also called the **thundering herd**, is many requests missing the same key at once and all hitting the source of truth together.
```

The fix is **single flight**: per key, only one request loads. The others wait for that one answer.

```java run
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static final AtomicInteger pricingCalls = new AtomicInteger();
    static final ConcurrentHashMap<String, CompletableFuture<Long>> quotes = new ConcurrentHashMap<>();

    static long slowPricing(String key) {
        pricingCalls.incrementAndGet();
        try {
            Thread.sleep(200);                   // the pricing service is slow
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return 700;
    }

    static long quote(String key) {
        CompletableFuture<Long> mine = new CompletableFuture<>();
        CompletableFuture<Long> existing = quotes.putIfAbsent(key, mine);
        if (existing != null) {
            return existing.join();              // someone is already loading it: wait for their answer
        }
        long cents = slowPricing(key);           // we are the one loader
        mine.complete(cents);                    // wake up everyone who waited
        return cents;
    }

    public static void main(String[] args) throws Exception {
        try (ExecutorService riders = Executors.newFixedThreadPool(5)) {
            List<Future<Long>> answers = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                answers.add(riders.submit(() -> quote("EBIKE:20")));
            }
            for (Future<Long> answer : answers) {
                System.out.println("rider got " + answer.get() + " cents");
            }
        }
        System.out.println("pricing service calls: " + pricingCalls.get());
    }
}
```

Five riders get 700 cents, and the pricing service is called once. `putIfAbsent` is atomic, so exactly one thread's future goes in. That thread loads; the rest `join()` its future. The cost is O(1) map work per request.

Notice what is missing: this version never handles a failure. You add that in the practice. It also keeps each quote forever. That is safe only because fares change by a deploy that clears every quote. A real cache would still add a TTL as a safety net. Other stampede defences work alongside it. Add jitter to TTLs. Refresh a hot key a little **before** it expires. Or let one request refresh while the others keep reading the old copy.

### Serving stale when the source is down

The profile service crashes at 08:05. Ada's cached profile expired at 08:04. A plain cache-aside lookup now misses, calls the dead service, and shows Ada an error. Yet the cache held a perfectly good copy a minute ago.

So keep the **last good copy** a while after it expires, and use it when the source fails:

1. On a miss or an expired entry, try the source. If it answers, cache the fresh copy as usual.
2. If the source fails, return the last good copy, **marked as stale**, for example with a `stale: true` field. The app can then say "showing saved details".
3. **Cap the staleness.** Allow, say, one hour past expiry. Older than that, fail clearly, such as `503 Service Unavailable`, rather than show something badly out of date.
4. **Do not hammer a failing source.** If every request retries a dead service, it can never recover. Remember when the last try failed, and try again at most once per interval, such as every 5 seconds. Every other request in between gets the stale copy at once.

```text
08:04:00  ada's profile expires (TTL 10 min), copy kept as "last good"
08:05:00  profile service down: try once, fails -> serve stale copy, next try after 08:05:05
08:05:02  serve stale copy without calling the service
08:05:05  try again, fails -> serve stale copy
09:04:00  copy is now 1 hour past expiry -> 503, do not serve it
```

HTTP has the same idea built in, as two `Cache-Control` directives:

```text
Cache-Control: max-age=60, stale-while-revalidate=30, stale-if-error=3600
```

- `stale-while-revalidate=30`: for 30 seconds after expiry, reuse the old copy at once while fetching a fresh one in the background.
- `stale-if-error=3600`: for up to an hour after expiry, reuse the old copy if the server answers with an error or does not answer.

Stale-on-error suits data that is fine slightly old, such as a name or a home station. It never suits money: a fare at the end of a ride, or a wallet balance, must come fresh or not at all.

### Cache penetration and negative caching

A broken script asks for `rider-404`, `rider-405`, `rider-406`, thousands a second. None of them exist. So none are ever cached, and every request goes straight through to the profile service.

```callout jargon
**Cache penetration** is requests for keys that do not exist, passing through the cache to the store every time. **Negative caching** stores the answer "not found" too, so repeats stop at the cache.
```

Give "not found" a **shorter** TTL than real data, such as one minute. Otherwise a rider who signs up right after a failed lookup looks missing for ten minutes. And delete the negative entry when that rider is created.

For huge key spaces, teams add a **Bloom filter**: a compact structure that answers "definitely not here" or "maybe here". Requests for ids that are definitely not here are refused before they reach the cache.

### Hot keys

Some keys get far more traffic than others. At rush hour, the EBIKE 20 minute quote might be a third of all quote lookups.

```callout jargon
A **hot key** is a single cache key that receives a large share of all requests.
```

In a distributed cache, each key lives on one cache server. A hot key makes that one server busy while the others idle. Its network card can even fill up.

Fixes: keep hot keys in a small in-process cache on each API server, in front of Redis. Or store copies under several keys, such as `quote:EBIKE:20#1` to `#4`, and read a random one.

### Many servers, many local caches

Spoke runs six API servers. Each has its own in-process profile cache. Ada changes her home station, and the request lands on server A. Server A deletes its copy. Servers B to F still serve harbor until their TTL runs out. Ada refreshes the app and sees her old station come back.

You have three options:

- **A shared cache** like Redis: one copy, so one delete fixes it for everyone. It costs a network round trip per lookup.
- **Broadcast the invalidation**: publish a "rider-7 changed" event, as in [messaging](/t/module-09-architecture/topic-04-messaging), and each server deletes its copy.
- **Two levels**: a small local cache with a very short TTL, in front of a shared cache with a longer one.

### What not to cache

Some data must never come from a cache, or must never share a key.

- **Per-user data under a shared key.** A key like `profile:me` shows the first rider's profile to every rider, as in the cache key card. If you cannot build a key that includes the rider, do not cache it.
- **Money-critical reads.** The final fare of a ride and a wallet balance before a charge must come from the source of truth. So must whether a bike is free to unlock.
- **Data that changes faster than it is read.** If every read finds a changed value, the cache only adds work.
- **Cheap answers.** If the database answers in 1 ms from an index, a cache adds risk for little gain.

### HTTP caching: Cache-Control and ETag

HTTP has caching built in, so the rider's phone can skip requests entirely. The server tells the client how long an answer stays fresh:

```text
GET /riders/rider-7/profile

HTTP/1.1 200 OK
Cache-Control: private, max-age=60
ETag: "v12"
```

`max-age=60` means "reuse this for 60 seconds without asking". `private` means only the rider's own phone may keep it, never a shared CDN. `no-store` means never keep it at all, which suits payment pages. `public` lets a CDN keep it, which suits bike pictures.

After 60 seconds, the app revalidates with the ETag from [REST design](/t/module-08-backend/topic-02-rest-design):

```text
GET /riders/rider-7/profile
If-None-Match: "v12"

HTTP/1.1 304 Not Modified
```

`304` has no body. The app keeps its copy, and only a few bytes crossed the network.

Two more directives, `stale-while-revalidate` and `stale-if-error`, let a client reuse an expired copy while it refreshes, or while the server is failing. The card on serving stale explains them.

### Syntax: @Cacheable in Spring

```java nocompile
@Configuration
@EnableCaching
class CacheConfig {}

@Service
class RiderProfiles {
    @Cacheable("riderProfiles")                         // key = the riderId argument
    public RiderProfile find(String riderId) { return slowStore.load(riderId); }

    @CacheEvict(value = "riderProfiles", key = "#profile.riderId()")
    public void update(RiderProfile profile) { slowStore.save(profile); }
}
```

`@EnableCaching` turns caching on. `@Cacheable("riderProfiles")` makes Spring check the `riderProfiles` cache before running the method, and store the result after. `@CacheEvict` deletes the key after the method runs. By default it evicts after the method returns, which matches "write, then delete".

Two traps. Spring adds caching through a proxy, so a call from **inside** the same class skips the cache. And the default cache is a plain map with no TTL and no size limit. In production you plug in Caffeine or Redis, which have both.

### In an interview

Caching shows up in almost every system design interview. Spot it when you hear "read-heavy", "slow downstream service" or "reduce database load". Also "latency must be under 100 ms", or "the same data is requested again and again".

- **Design an LRU cache** with O(1) `get` and `put`: a hash map plus a doubly linked list. Then extend it with a TTL.
- **Design an LFU cache**: a map of counts, plus a list of keys per count.
- **Keep a cache and a database consistent**: write, then delete; explain the race; add a TTL as a safety net.
- **A popular key expires and the database falls over**: single flight, jitter, early refresh.
- **Design a distributed cache**: spread keys across servers with consistent hashing (the next ticket), choose an eviction policy, handle hot keys.

## Worked example

Kakashi's ticket asks for profiles that are fast but never wrong after a change. Here is cache-aside with negative caching and delete-on-write, using plain maps so every step is visible.

```java run
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Main {
    record Profile(String riderId, String name, String homeStation) {}

    static final Map<String, Profile> database = new HashMap<>();   // the slow profile service
    static final Map<String, Profile> cache = new HashMap<>();
    static final Map<String, Boolean> unknown = new HashMap<>();    // negative cache
    static int slowCalls = 0;

    static Optional<Profile> find(String riderId) {
        Profile cached = cache.get(riderId);
        if (cached != null) {
            System.out.println("HIT   " + riderId);
            return Optional.of(cached);
        }
        if (unknown.containsKey(riderId)) {
            System.out.println("HIT   " + riderId + " (known to be missing)");
            return Optional.empty();
        }
        slowCalls++;
        Profile loaded = database.get(riderId);
        System.out.println("MISS  " + riderId + " -> asked the profile service");
        if (loaded != null) {
            cache.put(riderId, loaded);
        } else {
            unknown.put(riderId, true);
        }
        return Optional.ofNullable(loaded);
    }

    static void update(Profile profile) {
        database.put(profile.riderId(), profile);   // 1. write the source of truth
        cache.remove(profile.riderId());            // 2. then delete the cached copy
        unknown.remove(profile.riderId());
        System.out.println("SAVE  " + profile.riderId() + ", cache entry deleted");
    }

    public static void main(String[] args) {
        database.put("rider-7", new Profile("rider-7", "Ada", "harbor"));

        find("rider-7");
        find("rider-7");
        find("rider-404");
        find("rider-404");
        update(new Profile("rider-7", "Ada", "uptown"));
        System.out.println("home: " + find("rider-7").orElseThrow().homeStation());
        System.out.println("slow calls: " + slowCalls + " for 6 lookups");
    }
}
```

It prints:

```text
MISS  rider-7 -> asked the profile service
HIT   rider-7
MISS  rider-404 -> asked the profile service
HIT   rider-404 (known to be missing)
SAVE  rider-7, cache entry deleted
MISS  rider-7 -> asked the profile service
home: uptown
slow calls: 3 for 6 lookups
```

Line by line:

1. `find` checks the cache first. The first `rider-7` lookup misses, loads Ada from the profile service, and keeps a copy.
2. The second lookup is a hit. The profile service never hears about it.
3. `rider-404` does not exist. The miss is remembered in `unknown`, so the repeat stops at the cache. That is negative caching.
4. `update` writes the database **first**, then deletes both cached entries. It does not put the new profile into the cache.
5. The next `find` misses and loads uptown, the true value. Three slow calls served six lookups.

What this version still lacks is exactly your practice. It needs a size limit with LRU eviction, and a TTL on each entry, shorter for "not found". It also needs hit and miss counts, and single flight for fare quotes.

## Try it

Here is the LRU cache from the animation. Run it, then change it.

```java run
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        int capacity = 3;
        Map<String, String> lru = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > capacity;
            }
        };
        lru.put("rider-1", "Ada");
        lru.put("rider-2", "Ben");
        lru.put("rider-3", "Cy");
        lru.get("rider-1");
        lru.put("rider-4", "Dee");
        System.out.println(lru.keySet());
    }
}
```

1. Change `true` to `false`. Which rider is evicted now, and what kind of cache is it?
2. Put `true` back, and add `lru.get("rider-2");` just before the `rider-4` put. Predict who is evicted, then run it.
3. Set `capacity` to `1`. What does the map hold at the end?

## Recap

- A cache keeps copies of answers close by. The hit ratio decides everything: going from 90% to 99% cuts the store's load tenfold, and 0% is slower than no cache.
- Cache-aside (check, load on a miss, fill) is the usual pattern. Read-through, write-through, write-behind and write-around trade freshness, speed and safety differently.
- Every entry gets a TTL, with jitter, and every cache gets a size limit with an eviction policy such as LRU.
- On a write, save the source of truth first, then delete the cached key. Delete beats update, and the TTL catches the rare race.
- A key holds everything the answer depends on and nothing else. Single flight stops a stampede, negative caching stops penetration, and a capped stale copy covers a failing source.

## Common mistakes

**1. A cache with no size limit.**

```java nocompile
Map<String, Quote> cache = new HashMap<>();        // no size limit, no TTL
for (long i = 0; ; i++) {
    String key = "EBIKE:" + i;                     // every new trip length is a new key
    cache.put(key, new Quote(key, 100 + 30 * i, new byte[1024]));
}
```

```text
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
```

The map grows with every new key until the heap is full. In production this takes days, so it looks like a mystery crash. Fix: give every cache a capacity and an eviction policy, such as a `LinkedHashMap` with `removeEldestEntry`, and a TTL.

**2. Deleting the cached key before writing the database.**

```java nocompile
cache.remove(profile.riderId());   // a read here loads the OLD profile and caches it
database.save(profile);
```

Symptom: a rider changes their home station, the app says "Saved", and the old station comes back for ten minutes. It happens only under load, so it rarely shows up in testing. Fix: write the database first, then delete the key.

**3. Updating the cache instead of deleting it.**

```java nocompile
database.save(profile);
cache.put(profile.riderId(), profile);   // two racing writers can put in the wrong order
```

Symptom: after two quick edits, the database holds the second and the cache holds the first. Fix: delete the key and let the next read load it. Two deletes in any order give the same result.

**4. Remembering a failed load forever.**

```java nocompile
} catch (RuntimeException e) {
    mine.completeExceptionally(e);     // the failed future stays in the map
    throw e;
}
```

```text
1st: java.lang.IllegalStateException: pricing service timed out
2nd: java.util.concurrent.CompletionException: java.lang.IllegalStateException: pricing service timed out
pricing calls: 1
```

The pricing service had one bad second, and now every quote for that key fails until a restart. Fix: `quotes.remove(key, mine)` before completing the future exceptionally, so the next request tries again.

**5. Calling the slow loader inside a lock or computeIfAbsent.**

```java nocompile
public synchronized long quote(QuoteRequest request) {     // one lock for every key
    return quotes.computeIfAbsent(request, pricing::quoteCents);
}
```

Symptom: one slow EBIKE quote makes every CLASSIC and CARGO quote wait behind it. `ConcurrentHashMap.computeIfAbsent` can also block other keys while a slow function runs. Fix: put a future in the map first, then call the slow service outside any lock, as in single flight.
