## Hook

Picture the staff room at a school. There is a filing cabinet, with every record ever kept, safe and complete. And there is a whiteboard by the door. Anyone can glance at it, add a tally mark or wipe a line, in a second. Nobody keeps the school's history on the whiteboard. But for "who has the minibus key right now?", it is perfect.

Spoke's filing cabinet is Postgres. Its whiteboard is **Redis**. Kakashi's ticket has three problems that belong on the whiteboard: a leaderboard, a rate-limit counter shared by 30 servers, and a "who runs billing tonight?" note. In this ticket you learn what Redis really is, its data types, and the patterns that solve all three.

## Concept

### What Redis is

You met Redis as a shared cache in [caching](/t/module-09-architecture/topic-05-caching), and as a key-value store in [NoSQL data modelling](/t/module-09b-data/topic-04-nosql-modeling). Now look inside.

```callout jargon
**Redis** is a server that keeps keys and their values **in memory** (RAM) and answers commands over the network. A value is not just text: it can be a list, a set, a sorted set and more. Each of these is a **data type** with its own commands.
```

Memory is the point. Reading RAM takes about 100 nanoseconds. Even with the network round trip, a Redis command usually answers in well under a millisecond inside one data centre. A busy Redis server handles around 100,000 commands a second.

The price is also memory. RAM costs far more than disk, and it is wiped when the power goes. So Redis holds **hot, small, short-lived** data: counters, sessions, caches, leaderboards, locks. Rides, payments and riders stay in Postgres, the source of truth.

### One thread runs every command

Redis runs every command on **one main thread**, one after another. It never runs two commands at the same moment.

That sounds slow, but each command is tiny and in memory, so one thread keeps up with a huge load. Since Redis 6, extra **I/O threads** can read and write network bytes, but commands still run one at a time.

```callout jargon
An operation is **atomic** when nobody can see it half done or squeeze in between its steps. In Redis, **every single command is atomic**, because nothing else runs while it runs.
```

This one fact powers every pattern in this ticket. `INCR` reads, adds and writes with nothing in between. `SET ... NX` checks and sets with nothing in between.

The flip side: one **slow** command blocks everyone. `KEYS *` walks every key in the database. With ten million keys, the whole server freezes for seconds, and every Spoke API server waits. Use `SCAN`, which walks keys a small batch at a time.

### Persistence and replicas

If RAM is wiped on a restart, how does Redis keep anything? It has two ways to save to disk.

```callout jargon
An **RDB snapshot** is a point-in-time copy of all data, written to a file every few minutes. The **AOF** (append-only file) is a log of every write command, which Redis replays on start-up to rebuild memory.
```

- **RDB** files are compact and quick to load. But a crash loses every write since the last snapshot: minutes of data.
- **AOF** with the default `appendfsync everysec` flushes the log to disk once a second. A crash loses about **one second** of writes. `appendfsync always` flushes on every write: safer, much slower.
- Many teams turn on both.

For uptime, Redis has **replicas**: copies that receive every write from the main server, the **primary**. Replication is **asynchronous**: the primary answers "OK" before the replica has the write. **Redis Sentinel**, or Redis Cluster, promotes a replica if the primary dies. Writes it had not copied yet are lost. [The next ticket](/t/module-09b-data/topic-06-replication-sharding) goes deep on replication.

The lesson: Redis can lose a second of data, or a little more on failover. Fine for a leaderboard. Never the only copy of a payment.

### Keys and naming

Every value lives under a **key**, a string. Redis has no tables or folders, so teams build structure into key names with colons:

```text
rider:7:profile            one rider's profile
rate:rider-7:29386011      rider-7's request count in one minute
leaderboard:2026-W47       the leaderboard for one week
lock:billing-2026-11-18    who runs tonight's billing job
```

Good keys are **predictable** (code can build them), **scoped** (they include every id the value depends on) and **short-ish**. A key and a string value can each be up to 512 MB, but anything near that is a bad idea, as the big keys card explains.

Put a prefix per app or feature, such as `spoke:`, when several apps share one Redis. Then nobody's `rider:7` overwrites anyone else's.

### Strings, hashes and lists

Here are the first three data types, as a **redis-cli** session. redis-cli is Redis's command-line client. This session is typed out by hand, in redis-cli's real reply format:

```text
127.0.0.1:6379> SET rider:7:home harbor
OK
127.0.0.1:6379> GET rider:7:home
"harbor"
127.0.0.1:6379> GET rider:8:home
(nil)
127.0.0.1:6379> HSET rider:7 name Ada home harbor plan monthly
(integer) 3
127.0.0.1:6379> HGET rider:7 plan
"monthly"
127.0.0.1:6379> RPUSH trucks:jobs move-5-bikes-to-uptown fix-dock-harbor-3
(integer) 2
127.0.0.1:6379> LPOP trucks:jobs
"move-5-bikes-to-uptown"
```

- A **string** holds text, a number or bytes (such as JSON). `(nil)` means "no such key".
- A **hash** holds named fields under one key, like a small object. `HSET` answers how many fields were new. `HINCRBY` adds to one field.
- A **list** keeps items in order. `RPUSH` adds to the right, `LPOP` takes from the left, so it works as a queue. `BLPOP` waits until an item arrives.

Reading or writing one item is O(1) for all three.

### Sets, sorted sets and streams

Three more types, each with a classic job:

| Type | What it holds | Key commands | Spoke use |
|---|---|---|---|
| set | unique members, no order | `SADD`, `SISMEMBER`, `SCARD` | riders who used a promo code |
| sorted set | unique members, each with a number called a **score**, kept in score order | `ZADD`, `ZINCRBY`, `ZREVRANGE`, `ZREVRANK` | the weekly leaderboard |
| stream | an append-only log of entries, each with an id | `XADD`, `XREADGROUP`, `XACK` | ride events for several workers |

```callout jargon
A **sorted set** keeps its members ordered by score. Inside, Redis uses a **skip list**, a linked list with "express lanes" that make search O(log N), plus a hash table for O(1) score lookups.
```

Two more you should know by name. A **HyperLogLog** counts unique items, such as daily unique riders, in 12 KB with under 1% error. **Bitmaps** store one bit per id, such as "did rider N ride today?".

### Expiry: EXPIRE and TTL

Any key can have a **time to live**. After it, the key is gone, as if it had been deleted.

```text
127.0.0.1:6379> SET quote:EBIKE:20 700 EX 600
OK
127.0.0.1:6379> TTL quote:EBIKE:20
(integer) 600
127.0.0.1:6379> TTL rider:7:home
(integer) -1
127.0.0.1:6379> TTL rider:999:home
(integer) -2
```

(Typed out by hand, in the real reply format.)

- `EX 600` (seconds) or `PX 600000` (milliseconds) on `SET` sets the TTL in the same command. `EXPIRE key 600` sets it on a key that already exists.
- `TTL` answers the seconds left. **-1** means the key exists but never expires. **-2** means the key does not exist. `PTTL` answers in milliseconds.
- A plain `SET` **throws away** the old TTL. `INCR`, `HSET` and `ZINCRBY` change the value but **keep** it.

How does a key vanish? Two ways. **Lazily**: when a command touches an expired key, Redis deletes it first. And **actively**: about ten times a second, Redis samples some keys that have a TTL and deletes the expired ones. So an expired key may sit in memory a little while, but no command ever sees it.

### Eviction: maxmemory and its policies

Expiry removes keys on a schedule. **Eviction** removes keys because memory is full. You set the limit with `maxmemory`, such as `maxmemory 2gb`, and choose what happens with `maxmemory-policy`:

| Policy | When memory is full |
|---|---|
| `noeviction` (the default) | refuse writes with an out-of-memory error; reads still work |
| `allkeys-lru` | evict the least recently used key, any key |
| `allkeys-lfu` | evict the least frequently used key |
| `volatile-lru` / `volatile-lfu` | the same, but only among keys that have a TTL |
| `volatile-ttl` | evict the key closest to expiring |
| `allkeys-random` / `volatile-random` | evict a random key |

Pick by what the Redis holds. A **pure cache** wants `allkeys-lru` or `allkeys-lfu`: any key can be loaded again. A Redis that also holds **locks and counters** must not lose them, so use a `volatile-` policy and give only the cache keys a TTL. Or better, use separate Redis servers for cache and for state.

Redis's LRU is **approximate**. It samples a few keys (`maxmemory-samples`, 5 by default) and evicts the oldest of those. It is close to true LRU, and needs no extra memory per key.

### Atomic counters with INCR

The rides counter on Spoke's dashboard holds 41. Two API servers end a ride at the same moment. If each does `GET`, adds 1 in Java, then `SET`, one ride is lost:

```animate
title: GET, add, SET loses a ride; INCR does not
kind: code
code: |
  import java.util.HashMap;
  import java.util.Map;

  public class Main {
      public static void main(String[] args) {
          Map<String, Long> redis = new HashMap<>();
          redis.put("rides:today", 41L);
          long seenByA = redis.get("rides:today");
          long seenByB = redis.get("rides:today");
          redis.put("rides:today", seenByA + 1);
          redis.put("rides:today", seenByB + 1);
          System.out.println("GET then SET: " + redis.get("rides:today"));
          redis.put("rides:today", 41L);
          redis.merge("rides:today", 1L, Long::sum);
          redis.merge("rides:today", 1L, Long::sum);
          System.out.println("INCR twice: " + redis.get("rides:today"));
      }
  }
steps:
  - line: 7
    vars: { redis: "{rides:today=41}" }
    say: The map plays Redis. The counter holds 41 rides.
  - line: 8
    vars: { seenByA: 41 }
    say: Server A ends a ride. It GETs the counter and sees 41.
  - line: 9
    vars: { seenByB: 41 }
    say: Before A writes, server B ends another ride. It GETs the counter too, and also sees 41.
  - line: 10
    vars: { redis: "{rides:today=42}" }
    say: A adds 1 in Java and SETs 42.
  - line: 11
    vars: { redis: "{rides:today=42}" }
    say: B adds 1 to the 41 it saw, and SETs 42 as well. A's ride has vanished.
  - line: 12
    out: "GET then SET: 42"
    say: Two rides ended, but the counter only went up by one. This is a lost update.
  - line: 14
    vars: { redis: "{rides:today=42}" }
    say: Start again from 41. INCR reads, adds and writes as one step, so nothing can squeeze in between.
  - line: 15
    vars: { redis: "{rides:today=43}" }
    say: The second INCR sees 42, not 41.
  - line: 16
    out: "INCR twice: 43"
    say: Two rides, two counts. Here merge stands in for INCR; in Redis, the single thread is what makes it safe.
```

`INCR key` adds 1 and answers the new value. `INCRBY key 5` adds 5; a negative number subtracts. A missing key counts as 0. A value that is not a whole number gives an error:

```text
127.0.0.1:6379> INCR rider:7:home
(error) ERR value is not an integer or out of range
```

(Typed out by hand, in the real reply format.) Each counter is O(1) time and a few dozen bytes. This is the same lost update you fixed with a database lock in [concurrent writes](/t/module-08-backend/topic-15-concurrent-writes). Redis fixes it with a single command.

### Cache-aside in Redis

[Caching](/t/module-09-architecture/topic-05-caching) taught cache-aside: look in the cache, load on a miss, store with a TTL, and delete on a write. In Spring, with Redis, it looks like this:

```java nocompile
String key = "spoke:rider:" + riderId + ":profile";
String cached = redis.opsForValue().get(key);                       // GET
if (cached != null) {
    return json.readValue(cached, RiderProfile.class);
}
RiderProfile profile = profiles.load(riderId);                      // the slow source of truth
redis.opsForValue().set(key, json.writeValueAsString(profile),
        Duration.ofMinutes(10).plusSeconds(random.nextInt(60)));    // SET key value EX 600..659: TTL with jitter
return profile;
```

`redis` here is Spring's `StringRedisTemplate`. Every rule from the caching ticket still holds: a TTL on every key, jitter, write then delete. What Redis adds: **one shared copy** for all 30 API servers, so one `DEL` on a write fixes every server at once. The cost is a network round trip, about half a millisecond, on every lookup.

### A rate limiter shared by every server

The rate limiter from [resilience](/t/module-09-architecture/topic-09-resilience) keeps its counters in each server's memory. Since the move to Kubernetes, Spoke runs 30 copies of the API, so a rider gets 30 allowances. A limit of 100 a minute is really 3,000.

The fix is a **fixed window** counter in Redis, one key per rider per minute:

```text
key = "rate:rider-7:" + (nowMillis / 60_000)     the minute number, such as 29386011
count = INCR key
first request of the minute?  set a TTL so the key cleans itself up
count <= 100 ?  allow  :  answer 429 Too Many Requests
```

Every server builds the **same key** for the same rider and minute, so they share one counter. The minute number in the key means a new minute starts a new counter at 0. The TTL only cleans up old keys.

Each check is one or two O(1) commands. Memory is one small key per active rider per minute: at Spoke's 100,000 riders, a few megabytes. The Worked example runs it.

### The INCR-then-EXPIRE race, and three fixes

The obvious code sends two commands:

```java nocompile
long count = redis.opsForValue().increment(key);           // INCR
if (count == 1) {
    redis.expire(key, Duration.ofSeconds(60));             // EXPIRE: a second, separate command
}
```

If the server crashes, or the network drops, **between** the two, the key exists with **no TTL**. With a window number in the key, the cost is a little leaked memory. But many limiters use a fixed key like `rate:rider-7`. Then the counter never resets, and once it passes 100, rider-7 is blocked **forever**.

Each command is atomic. **Two** commands are not. Three ways to fix it:

1. **Create the key with its TTL first.** `SET key 0 EX 60 NX` makes the key, TTL included, only if it is missing. Then `INCR` adds 1 and keeps the TTL. A crash between them leaves a key that still expires.
2. **A Lua script.** Redis runs a whole script as one atomic step, as [resilience](/t/module-09-architecture/topic-09-resilience) showed.
3. **A transaction with `EXPIRE ... NX`.** `MULTI`, `INCR key`, `EXPIRE key 60 NX`, `EXEC`. `EXPIRE` with `NX` (Redis 7.0 and later) only sets a TTL if there is none.

```callout jargon
A Redis **transaction** is `MULTI`, then commands, then `EXEC`. Redis queues the commands and runs them back to back, with no other client's command in between. If the client dies before `EXEC`, none of them run. There is **no rollback**: if one command fails, the others still run.
```

Watch the trap in fix 3. Without `NX`, every request runs `EXPIRE key 60` and pushes the deadline forward. A rider who never pauses for 60 seconds is never reset.

`WATCH key` before `MULTI` adds a check: if anyone changes the key before `EXEC`, the transaction does nothing and answers nil, so you retry. That is optimistic locking, as in [concurrent writes](/t/module-08-backend/topic-15-concurrent-writes).

### Syntax: sorted-set commands

```text
ZADD key score member               add a member, or change its score; answers 1 if it is new, else 0
ZINCRBY key increment member        add to the score (a missing member starts at 0); answers the new score
ZSCORE key member                   the score, or (nil)
ZREVRANGE key start stop WITHSCORES places start to stop, both included, highest score first; -1 = the last place
ZREVRANK key member                 the 0-based place from the top, or (nil)
```

`ZADD`, `ZINCRBY` and `ZREVRANK` are O(log N). `ZREVRANGE` is O(log N + M) for M members returned. Since Redis 6.2, `ZRANGE key 0 9 REV WITHSCORES` does what `ZREVRANGE` does; both work.

**Ties.** Members with equal scores are ordered by the member's bytes, like dictionary order. `ZRANGE` (lowest first) puts them in **A to Z** order. `ZREVRANGE` reverses everything, so ties come in **Z to A** order. Text order compares character by character: `"rider-9"` comes after `"rider-10"`, because `'9'` is after `'1'`.

In Spring: `opsForZSet().incrementScore(key, member, 1)`, `reverseRangeWithScores(key, 0, 9)` and `reverseRank(key, member)`.

### A weekly leaderboard

Today, every app open runs a Postgres query that counts a week of rides per rider, sorts 100,000 riders and keeps ten. That is heavy work, repeated thousands of times an hour for the same answer.

With a sorted set, the work moves to the moment a ride ends:

```text
on every ride end:   ZINCRBY leaderboard:2026-W47 1 rider-7       O(log N)
top ten:             ZREVRANGE leaderboard:2026-W47 0 9 WITHSCORES   O(log N + 10)
"you are #1,234":    ZREVRANK leaderboard:2026-W47 rider-7       O(log N)
clean up:            EXPIRE leaderboard:2026-W47 5184000         60 days, then it is gone
```

A new week name starts a new board at zero. 100,000 members take roughly 10 MB.

Three production details:

- **Ties.** Two riders on 30 rides are ordered by their ids, which is not fair. To rank the one who got there first higher, build the score as `rides × 1,000,000 + (999,999 − seconds into the week)`. A score is a 64-bit double, exact for whole numbers up to about 9 × 10^15.
- **Counting twice.** Ride events arrive **at least once**, as in [messaging](/t/module-09-architecture/topic-04-messaging). A redelivered event would add a second point. Record each ride id with `SADD` in the same Lua script, and only add a point when `SADD` answers 1.
- **The truth stays in Postgres.** If Redis loses the board, rebuild it from the rides table. Redis is the fast view, not the record.

### A distributed lock: SET NX PX, and a safe release

Spoke's nightly billing job is scheduled in every one of the 30 API copies. Exactly one should do the work. Last month two did, and some riders were charged twice.

```callout jargon
A **distributed lock** is a lock that processes on different machines agree on through a shared service, such as Redis. A **lease** is a lock that expires on its own after a set time, in case its holder dies.
```

Taking the lock is one atomic command:

```text
SET lock:billing-2026-11-18 7f3a9c... NX PX 600000
```

- `NX`: only if nobody holds it. The winner gets `OK`; everyone else gets `(nil)` and skips the job.
- `PX 600000`: a 10 minute lease. If the holder crashes, the lock frees itself.
- `7f3a9c...`: a **random token** only this holder knows, such as a UUID.

Releasing needs the token. A plain `DEL` might delete **someone else's** lock, if yours already expired and another server took it. So release with this Lua script, which checks and deletes in one atomic step:

```text
if redis.call("GET", KEYS[1]) == ARGV[1] then
    return redis.call("DEL", KEYS[1])
else
    return 0
end
```

A `GET` from Java followed by a `DEL` from Java is **not** safe: the lease can run out between them.

### When a lease runs out: pauses and fencing tokens

A lease protects you from a dead holder. It does **not** protect you from a holder that is only **paused**.

```animate
title: A paused server wakes up after its lease ran out
kind: scene
nodes:
  - { id: a, label: Server A, emoji: "🖥️", value: idle }
  - { id: redis, label: Redis, emoji: "🧠", value: "lock free", col: 2 }
  - { id: b, label: Server B, emoji: "🖥️", value: idle, col: 4 }
  - { id: ledger, label: Billing ledger, emoji: "📒", value: "highest token 32", row: 1, col: 2 }
steps:
  - say: At 02:00, server A takes the lock with a 30 s lease. INCR on a counter gives it fencing token 33.
    send: [{ from: a, to: redis, label: "SET lock NX PX 30000" }, { from: redis, to: a, label: "OK, token 33", back: true }]
    set: { a: "holds lock, 33", redis: "held by A" }
  - say: A's JVM stops for a 40 second garbage-collection pause. A's code cannot notice; it is frozen mid-line.
    set: { a: "paused" }
    focus: [a]
  - say: After 30 s the lease runs out. Redis deletes the key, as it should.
    set: { redis: "lock free" }
  - say: Server B sees a free lock, takes it, and gets fencing token 34.
    send: [{ from: b, to: redis, label: "SET lock NX PX 30000" }, { from: redis, to: b, label: "OK, token 34", back: true }]
    set: { b: "holds lock, 34", redis: "held by B" }
  - say: B charges rental-17 and sends its token. The ledger remembers 34 as the highest token so far.
    send: { from: b, to: ledger, label: "charge rental-17, token 34" }
    set: { ledger: "highest token 34" }
  - say: A wakes up, still sure it holds the lock, and tries to charge rental-17 again with token 33.
    set: { a: "awake, thinks it holds lock" }
    send: { from: a, to: ledger, label: "charge rental-17, token 33" }
  - say: 33 is below 34, so the ledger refuses. No double charge. A's release script also finds B's token, and deletes nothing.
    send: { from: ledger, to: a, label: "refused: old token", back: true }
    focus: [ledger]
```

```callout jargon
A **fencing token** is a number that goes up by one every time anyone gets the lock. The resource being protected, here the ledger, refuses any request with a token lower than one it has already seen.
```

Take the token in the same Lua script as the `SET NX PX`, so nobody can grab the lock between the two. Otherwise a holder that pauses in between could get a higher token than the next holder.

GC pauses, a slow network or a laptop lid closing can all freeze a holder past its lease. Only the protected resource can catch it, because only the resource sees both writes. That is why the check lives in the ledger, not in Redis.

**Redlock** is an algorithm by Redis's creator for locking across five separate Redis servers, taking a majority. The researcher Martin Kleppmann argued in 2016 that it is still unsafe without fencing, because it trusts clocks and pause lengths. Redis's creator replied, and the debate is famous. The practical lesson: a Redis lock is great for **efficiency**, such as not running a job twice for nothing. When a double run would be **wrong**, add fencing tokens, or use a consensus system such as etcd or ZooKeeper.

### Pub/sub and streams

Redis has two ways to pass messages, and they behave very differently.

**Pub/sub** (publish/subscribe): `PUBLISH channel message` sends to everyone **listening right now**. It stores nothing. A subscriber that is restarting, or slow, simply misses the message. It suits "refresh your local cache" hints, where a missed message only costs a TTL.

**Streams** are a stored, append-only log, like a small Kafka topic from [messaging](/t/module-09-architecture/topic-04-messaging):

```text
XADD ride-events * type ended rental rental-17         append; * = let Redis make the id
XGROUP CREATE ride-events billing $                    a consumer group called billing
XREADGROUP GROUP billing worker-1 COUNT 10 STREAMS ride-events >
XACK ride-events billing 1794967200000-0               "I have handled this entry"
```

```callout jargon
A **consumer group** shares a stream's entries among several workers: each entry goes to one worker in the group. Entries a worker has read but not acknowledged sit in a **pending entries list**, so another worker can claim them if it dies.
```

Streams keep entries until you trim them, for example with `XADD ride-events MAXLEN ~ 1000000 * ...`. So a worker that was down catches up. Choose pub/sub for "nice to know now", and streams for "must be handled".

### Pipelining

Each command is a network **round trip**: send, wait, read the reply. Inside a data centre that might be 0.5 ms. Loading 10,000 leaderboard points one by one costs 10,000 × 0.5 ms = **5 seconds**, almost all of it waiting.

```callout jargon
**Pipelining** means sending many commands without waiting for each reply, then reading all the replies together. The commands still run one by one, in order.
```

In batches of 1,000, the same job takes about 10 round trips: around 5 ms plus Redis's own work. Spring offers `executePipelined`, and most clients have a pipeline object.

A pipeline is **not atomic**. Other clients' commands can run between yours. When you need "all or nothing in one step", use `MULTI`/`EXEC` or Lua. When you only need speed, pipeline.

### Big keys and hot keys

Two shapes of key cause most Redis outages.

A **big key** is one key with a huge value: a hash with ten million fields, or a list of every ride ever. Reading all of it ships megabytes over the network. Deleting it with `DEL` frees memory on the main thread, and the server stalls. Fixes: split it into many keys, such as one per week. Delete with `UNLINK`, which frees memory in the background. And never call `HGETALL` or `LRANGE 0 -1` on something unbounded.

A **hot key** is one key that gets a big share of all traffic. Every command for it hits the same server, however many servers you have. In [caching](/t/module-09-architecture/topic-05-caching) you met the fix: a small local cache in each API server, or several copies of the key read at random.

`redis-cli --bigkeys` and `--hotkeys` help find both. Watch memory per key, not only the total.

### Redis Cluster: slots and hash tags

One Redis server holds what one machine's RAM can hold. **Redis Cluster** spreads keys over several primaries, each with replicas.

```callout jargon
Redis Cluster splits all keys into **16,384 hash slots**. A key's slot is CRC16(key) mod 16,384, where CRC16 is a fixed checksum. Each primary owns a range of slots. A **hash tag** is a part of the key in braces, `{...}`: when present, only that part is hashed.
```

Why hash tags? A command or script that touches **several keys** works only if they share a slot. Otherwise Redis answers with a `CROSSSLOT` error. Put the same tag in related keys, and they land together:

```java run
import java.nio.charset.StandardCharsets;

public class Main {
    // CRC16 as Redis Cluster uses it (the XMODEM variant: polynomial 0x1021, starting at 0)
    static int crc16(String key) {
        int crc = 0;
        for (byte b : key.getBytes(StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                crc = (crc & 0x8000) != 0 ? (crc << 1) ^ 0x1021 : crc << 1;
            }
        }
        return crc & 0xFFFF;
    }

    // Only the part between the first { and the next } is hashed, if that part is not empty
    static int slot(String key) {
        int open = key.indexOf('{');
        if (open >= 0) {
            int close = key.indexOf('}', open + 1);
            if (close > open + 1) {
                key = key.substring(open + 1, close);
            }
        }
        return crc16(key) % 16_384;
    }

    public static void main(String[] args) {
        for (String key : new String[] {"rider:7:profile", "rider:7:wallet",
                "rider:{rider-7}:profile", "rider:{rider-7}:wallet"}) {
            System.out.println(key + " -> slot " + slot(key));
        }
    }
}
```

It prints:

```text
rider:7:profile -> slot 4735
rider:7:wallet -> slot 7089
rider:{rider-7}:profile -> slot 1619
rider:{rider-7}:wallet -> slot 1619
```

This CRC16 gives slot 11,058 for `somekey`, the same number Redis's own documentation shows. Use tags with care: put one tag on **everything**, and all keys pile onto one slot, a hot spot. Slots are a "fixed number of partitions" design; [the next ticket](/t/module-09b-data/topic-06-replication-sharding) compares it with consistent hashing.

### In an interview

Redis shows up in system design whenever you hear "real time", "top K", "count per user per minute", "only one worker may", or "under a millisecond".

- **Design a leaderboard**: a sorted set per period, `ZINCRBY`, `ZREVRANGE`, `ZREVRANK`; ties; rebuild from the database.
- **Design a rate limiter**: a counter per key per window with `INCR` and a TTL; the two-command race; why it must be shared across servers.
- **Make a cron job run once across many servers**: `SET NX PX` with a token, a safe release, and fencing tokens.
- **Design a cache for X**: cache-aside, TTL with jitter, an eviction policy, hot keys.
- **Count unique visitors**: a set when exact and small; a HyperLogLog when huge and approximate.

Always say what happens when Redis is down or loses a second of writes. Interviewers listen for that.

## Worked example

Kakashi's second problem: the limiter counts per server, and Spoke runs 30 of them. Here is a fixed-window limiter, first counted in each server's memory, then in one shared Redis. `TinyRedis` is just enough Redis for the job: `SET ... EX ... NX` and `INCR`, with expiry.

```java run
import java.util.HashMap;
import java.util.Map;

public class Main {
    /** Just enough Redis for a rate limiter: SET ... EX ... NX and INCR, with expiry. */
    static class TinyRedis {
        final Map<String, Long> values = new HashMap<>();
        final Map<String, Long> deadlines = new HashMap<>();
        long nowMs = 0;                                    // a clock we move by hand

        void dropIfExpired(String key) {
            Long deadline = deadlines.get(key);
            if (deadline != null && nowMs >= deadline) {
                values.remove(key);
                deadlines.remove(key);
            }
        }

        void setExNx(String key, long value, long seconds) { // SET key value EX seconds NX
            dropIfExpired(key);
            if (!values.containsKey(key)) {
                values.put(key, value);
                deadlines.put(key, nowMs + seconds * 1000);
            }
        }

        long incr(String key) {                            // INCR keeps the deadline
            dropIfExpired(key);
            return values.merge(key, 1L, Long::sum);
        }
    }

    static final int LIMIT = 100;                          // requests per rider per minute

    public static void main(String[] args) {
        TinyRedis shared = new TinyRedis();
        Map<Integer, Map<String, Long>> perServer = new HashMap<>();
        int allowedShared = 0;
        int allowedPerServer = 0;

        for (int i = 0; i < 3_000; i++) {                  // a script sends 3,000 requests in one minute
            shared.nowMs = i * 20L;
            int server = i % 30;                           // the load balancer spreads them over 30 servers

            // Before: each server counts in its own memory
            long local = perServer.computeIfAbsent(server, s -> new HashMap<>())
                    .merge("rider-7", 1L, Long::sum);
            if (local <= LIMIT) allowedPerServer++;

            // After: one counter in Redis, for this rider and this minute
            String key = "rate:rider-7:" + (shared.nowMs / 60_000);
            shared.setExNx(key, 0, 60);                    // creates the key WITH its TTL, or does nothing
            if (shared.incr(key) <= LIMIT) allowedShared++;
        }
        System.out.println("counted per server: " + allowedPerServer + " of 3000 allowed");
        System.out.println("counted in Redis:   " + allowedShared + " of 3000 allowed");

        shared.nowMs = 60_000;                             // the next minute starts
        String next = "rate:rider-7:" + (shared.nowMs / 60_000);
        shared.setExNx(next, 0, 60);
        System.out.println("next minute, first request: count " + shared.incr(next));
        System.out.println("old window still in memory? " + shared.values.containsKey("rate:rider-7:0"));
        shared.dropIfExpired("rate:rider-7:0");            // what any command touching the key would do
        System.out.println("after a command touches it? " + shared.values.containsKey("rate:rider-7:0"));
    }
}
```

It prints:

```text
counted per server: 3000 of 3000 allowed
counted in Redis:   100 of 3000 allowed
next minute, first request: count 1
old window still in memory? true
after a command touches it? false
```

Line by line:

1. Thirty servers each count rider-7 in their own map. Each sees only 100 of the 3,000 requests, so each allows all of them. The limit of 100 became 3,000.
2. With one shared counter, every server builds the same key, `rate:rider-7:0`. Exactly 100 requests pass.
3. `setExNx` creates the key **with** its TTL in one step, so the counter can never be left without one. This is fix 1 from the race card.
4. At 60 s the key name becomes `rate:rider-7:1`, a fresh counter that starts at 1.
5. The old key is still in memory until something touches it. That is lazy expiry; Redis's background sweep would also remove it soon.

Your practice builds a real `MiniRedis` with these rules and more, then a leaderboard and a lock on top of it.

## Try it

Here is the leaderboard from the sorted-set cards, with a Java comparator playing `ZREVRANGE`. Run it, then change it.

```java run
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    record Entry(String rider, double rides) {}

    // ZREVRANGE order: highest score first, equal scores in reverse text order
    static final Comparator<Entry> ZREV = Comparator.comparingDouble(Entry::rides)
            .thenComparing(Entry::rider)
            .reversed();

    public static void main(String[] args) {
        Map<String, Double> week = new HashMap<>();      // the sorted set leaderboard:2026-W47
        String[] rideEnds = {"rider-7", "rider-3", "rider-7", "rider-12", "rider-3", "rider-9", "rider-7", "rider-9"};
        for (String rider : rideEnds) {
            week.merge(rider, 1.0, Double::sum);         // ZINCRBY leaderboard:2026-W47 1 <rider>
        }

        List<Entry> ordered = new ArrayList<>();
        week.forEach((rider, rides) -> ordered.add(new Entry(rider, rides)));
        ordered.sort(ZREV);

        System.out.println("ZREVRANGE 0 2 WITHSCORES:");
        for (Entry e : ordered.subList(0, 3)) {
            System.out.println("  " + e.rider() + " " + (long) e.rides());
        }
        for (int rank = 0; rank < ordered.size(); rank++) {
            if (ordered.get(rank).rider().equals("rider-3")) {
                System.out.println("ZREVRANK rider-3: " + rank);
            }
        }
    }
}
```

1. rider-9 and rider-3 both have 2 rides. Why is rider-9 first? Predict, then check against the Syntax card.
2. Add `"rider-10", "rider-10"` to the ride ends. Where does rider-10 land among the riders on 2, and why?
3. Remove `.reversed()` and change nothing else. What does the list show now, and which Redis command does it match?

## Recap

- Redis keeps data in memory and runs one command at a time, so every single command is atomic. It saves to disk with RDB snapshots and the AOF log, and can still lose about a second of writes.
- Pick the data type for the job: strings and counters, hashes, lists, sets, sorted sets and streams. Every key can expire (`TTL` answers -1 for no expiry and -2 for no key), and `maxmemory-policy` decides what goes when memory is full.
- `INCR` makes a counter safe across many servers. A shared fixed-window limiter needs its TTL set safely: `SET ... EX ... NX`, a Lua script, or `MULTI` with `EXPIRE ... NX`.
- A sorted set makes a leaderboard: `ZINCRBY`, `ZREVRANGE ... WITHSCORES` and `ZREVRANK`, with ties in reverse member order.
- A lock is `SET key token NX PX ms`, released only with the matching token. A paused holder can outlive its lease, so a resource that matters checks fencing tokens.

## Common mistakes

**1. Releasing a lock with a plain DEL.**

```java nocompile
if (redis.opsForValue().setIfAbsent("lock:billing", token, Duration.ofMinutes(10))) {
    runBilling();                       // takes 12 minutes tonight: the lease ran out at 10
    redis.delete("lock:billing");       // deletes the lock that server B took at minute 10
}
```

Symptom: server B took the lock when A's lease ran out. A's `DEL` then removes **B's** lock, so a third server starts too. Fix: release with the Lua script that deletes only when the key still holds your token, and pick a lease longer than the job's worst run.

**2. INCR and EXPIRE as two separate commands.**

```java nocompile
long count = redis.opsForValue().increment("rate:rider-7");
if (count == 1) {
    redis.expire("rate:rider-7", Duration.ofSeconds(60));   // never runs if the server dies right here
}
```

Symptom: after a deploy restarts servers at a busy moment, a few riders get `429 Too Many Requests` on every request, forever. `TTL rate:rider-7` answers -1. Fix: `SET key 0 EX 60 NX` before `INCR`, a Lua script, or `MULTI` with `EXPIRE ... NX`.

**3. KEYS in production code.**

```java nocompile
Set<String> keys = redis.keys("spoke:rider:*:profile");   // KEYS walks every key in the database
redis.delete(keys);
```

Symptom: it works on a laptop with 50 keys. In production, with millions, Redis's one thread is stuck for seconds, and every API server times out at once. Fix: `SCAN` with a `MATCH` pattern walks keys in small batches; better still, design keys so you never need to search for them.

**4. Expecting ties to follow the numbers in member names.**

```java nocompile
// ZREVRANGE leaderboard:2026-W47 0 -1, where rider-9 and rider-10 both have 5 rides
// expected: rider-10, rider-9
// got:      rider-9, rider-10
```

Ties order by the member's text, character by character, and `'9'` comes after `'1'`. In `ZREVRANGE` the larger text comes first, so rider-9 wins the tie. Fix: decide what a tie should mean, and put it in the score, such as earlier finish time ranking higher.

**5. Keeping the only copy of money data in Redis.**

```java nocompile
redis.opsForHash().increment("wallet:rider-7", "cents", -700);   // the wallet lives only in Redis
```

Symptom: Redis restarts after a crash with `appendfsync everysec`, and the last second of charges is gone. Or a failover promotes a replica that missed them. Fix: money lives in Postgres, inside a transaction. Redis may cache a balance for display, never decide it.
