## Hook

Before a city builds a new bridge, engineers count the cars. How many cross at 8 a.m.? How heavy is a loaded lorry? What happens if one lane closes? Only then do they draw the bridge. Pouring concrete first and counting later is how bridges end up too narrow.

Spoke is about to do the same with software. Ten more cities, each about the size of Riverton, launch next year. Tsunade wants a sketch with real numbers before anyone writes code. In this ticket you learn how engineers sketch a system, and how they defend every box on the whiteboard.

## Concept

### What a system design interview is

In a **system design interview**, you get a vague task, such as "design a bike-sharing app", and 45 to 60 minutes. There is no single right answer. The interviewer watches how you think: what you ask, what you estimate, and how you weigh choices.

```callout jargon
**System design** is deciding the parts of a software system, how they talk to each other, and where data lives, so it meets its goals. A **trade-off** is a choice where getting more of one good thing means giving up some of another.
```

It matters most for SDE 2 and above. A junior engineer is asked "can you build this function?". A mid-level engineer is asked "can you build this service, and will it survive real traffic?".

The same skill is used at work. A design document, reviewed before coding, is how big companies avoid building the wrong thing.

### A framework in eight steps

A clear order keeps you from drawing boxes too early. Spend roughly these minutes in a 45-minute interview:

1. **Clarify requirements** (5 min): what must it do, and how well?
2. **Estimate** (5 min): requests a second, data a year, bandwidth.
3. **Define the API** (5 min): the main endpoints and what they return.
4. **Sketch the data model** (5 min): the main tables or records, and their keys.
5. **Draw the high-level design** (10 min): clients, load balancer, services, stores, queues.
6. **Deep dive** (10 min): the two or three hardest parts, in detail.
7. **Discuss trade-offs**: what you chose, what you gave up, and why.
8. **Find bottlenecks and failures**: what breaks first as traffic grows, and what happens when a part dies.

Say each step out loud as you start it. The interviewer can then steer you, and you never lose your place.

### Step one: requirements

```callout jargon
A **functional requirement** is something the system must do: "a rider can unlock a bike". A **non-functional requirement** is how well it must do it: fast, always up, never losing a payment.
```

For Spoke, the functional list is short. Riders see bikes near them, unlock one, end the ride and pay. Bikes report where they are. Ops staff see the whole fleet.

The non-functional list shapes the design more:

- **Latency**: the map loads in under 200 ms for 99% of requests. This is called the **p99** latency.
- **Availability**: unlocking works 99.95% of the time.
- **Consistency**: a bike is never rented to two riders, and a payment is never lost. The map may be a few seconds behind.
- **Scale**: eleven cities, growing.

```callout jargon
**p99** (the 99th percentile) is the time that 99% of requests beat. If p99 is 200 ms, only 1 request in 100 takes longer. Averages hide slow requests; percentiles show them.
```

Asking "which data must never be wrong, and which can be a little stale?" is the most useful single question. It decides much of what follows.

### Estimation: powers of ten

A **back-of-the-envelope estimate** is a quick sum with rounded numbers. It tells you whether you need one server or a hundred. Being within a factor of two is plenty.

| Power | Name | Bytes |
|---|---|---|
| 10^3 | thousand | 1 KB (kilobyte) |
| 10^6 | million | 1 MB (megabyte) |
| 10^9 | billion | 1 GB (gigabyte) |
| 10^12 | trillion | 1 TB (terabyte) |

One number you use constantly: a day has 86,400 seconds, about **10^5**. So a million events a day is about 10 a second.

```callout jargon
**QPS** (queries per second) is how many requests arrive each second. **Throughput** is how many a system can handle each second.
```

Round early, and write each assumption down where everyone can see it. "Assume an average ride lasts 20 minutes" is fine, as long as you said it.

### Average and peak traffic

Traffic is never flat. Spoke's morning rush is far busier than 3 a.m. Servers must survive the **peak**, so estimate it separately from the average.

The story's numbers for Riverton: 100,000 riders, 2,000 bikes, 300 stations. 5,000 rides start in the morning rush hour. Each moving bike sends a GPS ping every 10 seconds. The map in the app refreshes every 5 seconds.

- **Ride starts at peak:** 5,000 an hour ÷ 3,600 seconds ≈ 1.4 a second. Tiny.
- **Bikes moving at once:** use **Little's law**. With 5,000 starts an hour and 20 minute rides, about 5,000 × 1/3 ≈ 1,700 bikes ride at once.
- **GPS pings at peak:** 1,700 bikes ÷ 10 seconds ≈ 170 pings a second.
- **Map reads at peak:** assume 1 rider in 20 has the app open at the busiest moment. That is 5,000 apps, each asking every 5 seconds: 1,000 reads a second.

```callout jargon
**Little's law**: the number of things inside a system equals the rate they arrive times how long each one stays. 5,000 rides an hour, each lasting a third of an hour, means about 1,700 rides in progress.
```

Notice the shape: map reads outnumber ride starts by about 700 to 1. Reads drive this design.

### Storage and bandwidth

Next, how much data piles up. Assume 20,000 rides a day in Riverton, each 20 minutes long.

- **Pings a day:** 20,000 rides × 20 minutes × 6 pings a minute = 2.4 million.
- **Average pings a second:** 2.4 million ÷ 86,400 ≈ 28. With the 10^5 shortcut: 24. Close enough.
- **Ping storage:** at 100 bytes a ping, 2.4 million × 100 B = 240 MB a day. Times 365, about 88 GB a year.
- **Ride storage:** 20,000 rides × 500 B = 10 MB a day, under 4 GB a year. Small.
- **Map bandwidth:** 1,000 answers a second × 2 KB = 2 MB a second, or 16 Mbit/s.

The peak of 170 pings a second is six times the average of 28. Size the servers for the peak, and the storage for the total.

Remember that production keeps several copies of data. Three copies of 88 GB is about 264 GB a year.

### Latency numbers worth knowing

[How computers work](/t/module-00-foundations/topic-01-how-computers-work) showed that each kind of storage is far slower than the last. System design adds the network. These are rough orders, not exact figures.

| Action | Rough time |
|---|---|
| Read from CPU cache | 1 ns |
| Read from RAM | 100 ns |
| Read 1 MB in a row from RAM | 10 µs |
| Random read from an SSD | 100 µs |
| Round trip inside one data centre | 0.5 ms |
| Read 1 MB in a row from an SSD | under 1 ms |
| Disk seek on a spinning hard drive | 10 ms |
| Round trip within one country | 10 to 30 ms |
| Round trip across an ocean | 100 to 150 ms |

Three rules follow. Keep hot data in memory. Avoid many sequential network calls in one request. Never cross an ocean inside a request a rider is waiting for: put servers near riders instead.

### Scaling up or out

When one server is not enough, you have two choices.

```callout jargon
**Vertical scaling** (scaling up) is buying a bigger machine: more CPU cores, more RAM. **Horizontal scaling** (scaling out) is adding more machines and spreading the work across them.
```

Vertical scaling needs no code changes, so try it first. But machines have a size limit, big ones cost far more per core, and one machine is still one machine. When it dies, everything stops.

Horizontal scaling has no ceiling and survives the loss of a machine. But the work must be splittable, and the machines must share state somehow. Most of this ticket is about that "somehow".

### Stateless servers behind a load balancer

The easiest part to scale out is a **stateless** server.

```callout jargon
A **stateless** server keeps nothing about a rider between requests. Everything it needs comes in the request, such as a login token, or from a shared store. Any copy of the server can answer any request.
```

Put many identical API servers behind the load balancer from [networking](/t/module-08-backend/topic-13-networking). It spreads requests with a rule such as **round robin** (each server in turn) or **least connections** (the least busy server). It sends health checks, and stops sending traffic to a server that fails them.

```animate
title: One API server dies at rush hour
kind: scene
nodes:
  - { id: app, label: Rider apps, emoji: "📱", row: 1, col: 0 }
  - { id: lb, label: Load balancer, emoji: "⚖️", row: 1, col: 2 }
  - { id: s1, label: API server 1, emoji: "🖥️", value: healthy, row: 0, col: 4 }
  - { id: s2, label: API server 2, emoji: "🖥️", value: healthy, row: 1, col: 4 }
  - { id: s3, label: API server 3, emoji: "🖥️", value: healthy, row: 2, col: 4 }
steps:
  - say: Rider apps send map requests. The load balancer hands them out in turn.
    send: [{ from: app, to: lb, label: "GET map" }, { from: lb, to: s1, label: "1" }, { from: lb, to: s2, label: "2" }, { from: lb, to: s3, label: "3" }]
  - say: Server 2 runs out of memory and crashes.
    set: { s2: down }
    focus: [s2]
  - say: Every few seconds the load balancer asks each server "are you healthy?". Server 2 does not answer.
    send: [{ from: lb, to: s1, label: "health?" }, { from: lb, to: s2, label: "health?" }, { from: lb, to: s3, label: "health?" }]
  - say: The load balancer takes server 2 out of the rotation. Requests go to servers 1 and 3 only.
    send: [{ from: lb, to: s1, label: "4" }, { from: lb, to: s3, label: "5" }]
    set: { s1: busier, s3: busier }
  - say: Riders notice nothing, because the servers are stateless. Any server can answer any rider.
    focus: [app]
```

Plan spare room. If three servers each run at 80% and one dies, the other two need 120% each, and they fall over too. Running at about 50 to 60% leaves room to lose one.

The load balancer itself must not be the one thing that can stop everything. Real setups run two, or use a managed one built from many machines.

### Scaling the database: indexes and caches first

Servers are easy to copy. The database holds the state, so it is the hard part. Scale it step by step, and stop at the first step that is enough.

1. **Indexes.** A missing index turns a 1 ms lookup into a full table scan, as in [database design](/t/module-08-backend/topic-14-database-design). Fix slow queries first. It is cheap, and it is often all you need.
2. **Caching.** Keep hot, rarely changing answers, such as rider profiles and fare quotes, in a cache, as in [caching](/t/module-09-architecture/topic-05-caching). A 99% hit ratio removes 99% of those reads.
3. **Read replicas**, for read-heavy load. Next card.
4. **Sharding**, when writes or data outgrow one machine. After that.

Each step adds moving parts that can break. Do not shard a database that an index would fix.

### Read replicas and replication lag

```callout jargon
**Replication** is keeping copies of the same data on several machines. In **leader-follower** replication, one machine, the **leader**, takes every write. The others, **followers** or **read replicas**, copy the leader's changes and serve reads.
```

Reads can now spread over many followers. But copying takes time, usually milliseconds, sometimes seconds. That delay is **replication lag**.

Lag causes a classic bug. Ada changes her home station to uptown. The write goes to the leader. Her app reloads the profile from a follower that has not caught up, and shows harbor. To Ada, her change was lost.

```callout jargon
**Read-your-writes** consistency means a user always sees their own changes. A lagging follower breaks it.
```

Fixes: read a user's own data from the leader for a short time after they write, such as one minute. Or remember the moment of their last write, and only read from a follower that has caught up past it. Data that must be exact, such as whether a bike is free, is always read from the leader.

### Failover

What happens when the leader dies? A follower must take over. This is **failover**.

```callout jargon
**Failover** is switching to a standby copy when the main one fails. **Synchronous** replication waits for a follower to confirm each write before answering. **Asynchronous** replication answers first and copies after.
```

1. Detect the failure, usually when the leader misses several heartbeats in a row.
2. Promote the most up-to-date follower to leader.
3. Point every writer at the new leader.

With asynchronous replication, writes the old leader confirmed but had not yet copied are **lost**. With synchronous replication, nothing is lost, but every write waits for a follower. Many teams use one synchronous follower and the rest asynchronous.

The other danger is **split brain**: the old leader was only cut off, not dead, and comes back still thinking it leads. Two leaders accept different writes. Systems guard against it by giving each leader a numbered term, and refusing writes from an old term.

### Sharding: hash, range and directory

When writes or data outgrow one leader, split the data across several databases. Each piece is a **shard**.

```callout jargon
**Sharding** (or partitioning) splits rows across several databases by a **shard key**. Each shard holds only its own rows and takes only their writes.
```

There are three common ways to choose the shard:

- **Hash**: shard = hash(key) mod N. Keys spread evenly. But a range query, such as "rides from 08:00 to 09:00", must ask every shard.
- **Range**: each shard holds a range of keys, such as dates. Range queries are easy. But today's rides all land on one shard, which then takes every new write.
- **Directory**: a lookup table says which shard holds each key. Very flexible, and you can move one key at a time. But the directory is one more service that must never be down.

For Spoke, `city_id` is a natural shard key. A ride never crosses cities, so unlock, end-ride and payment for one ride all touch one shard.

Three problems come with sharding:

- **Hot partitions.** One shard gets far more traffic than the others. A new city might grow to twice Riverton's size. Split big cities further, by hashing bike ids inside the city.
- **Cross-shard queries.** "All of Ada's rides, in every city she visited" must ask several shards and merge the answers, which is slow. Choose the shard key so the most common queries hit one shard.
- **Resharding.** Adding a shard means moving data while the system runs. The next card makes that cheaper.

### Consistent hashing

With hash mod N, going from 4 shards to 5 changes N, so most keys get a new home. Consistent hashing moves only about one key in five.

Picture a ring of numbers. Each server is placed at many points on it, by hashing its name. A key is hashed onto the ring too, then walks clockwise to the first server point it meets.

```text
the ring, read clockwise:  s0 ... B-17 ... s2 ... s1 ... B-1042 ... s3 ... (back to s0)

B-17   walks clockwise, meets s2 first   -> stored on server-2
B-1042 walks clockwise, meets s3 first   -> stored on server-3
add a point for s4 between B-1042 and s3 -> only B-1042 moves, to server-4
```

Adding server-4 adds new points. Only keys that now meet a server-4 point first move. Everything else stays put.

```java run
import java.util.Map;
import java.util.TreeMap;

public class Main {
    static int hash(String text) {                 // a fixed, well-mixed hash, the same on every machine
        int h = 0x811C9DC5;                        // FNV-1a, then MurmurHash3's final mix
        for (char c : text.toCharArray()) {
            h ^= c;
            h *= 0x01000193;
        }
        h ^= h >>> 16;                             // final mixing, so similar names land far apart
        h *= 0x85EBCA6B;
        h ^= h >>> 13;
        h *= 0xC2B2AE35;
        return h ^ (h >>> 16);
    }

    static TreeMap<Integer, String> ring(int servers) {
        TreeMap<Integer, String> ring = new TreeMap<>();
        for (int s = 0; s < servers; s++) {
            for (int v = 0; v < 100; v++) {        // 100 points on the ring per server
                ring.put(hash("server-" + s + "#" + v), "server-" + s);
            }
        }
        return ring;
    }

    static String onRing(TreeMap<Integer, String> ring, String key) {
        Map.Entry<Integer, String> next = ring.ceilingEntry(hash(key));   // walk clockwise
        return next != null ? next.getValue() : ring.firstEntry().getValue();
    }

    public static void main(String[] args) {
        int keys = 10_000, movedMod = 0, movedRing = 0;
        TreeMap<Integer, String> four = ring(4), five = ring(5);
        for (int i = 0; i < keys; i++) {
            String bike = "B-" + i;
            if (Math.floorMod(hash(bike), 4) != Math.floorMod(hash(bike), 5)) movedMod++;
            if (!onRing(four, bike).equals(onRing(five, bike))) movedRing++;
        }
        System.out.println("4 -> 5 servers, hash % N:       " + movedMod * 100 / keys + "% of bikes move");
        System.out.println("4 -> 5 servers, hash ring:      " + movedRing * 100 / keys + "% of bikes move");
    }
}
```

It prints 80% for hash mod N and 19% for the ring. Finding a key's server is a `TreeMap` lookup, O(log P) for P points on the ring.

Why 100 points per server, called **virtual nodes**? With one point each, servers get very uneven slices of the ring. Many points even them out. Redis Cluster, Cassandra and DynamoDB all use ideas like this.

### CAP and PACELC in plain words

Networks fail. A cable is cut, and two data centres cannot talk. That is a **network partition**. During one, a system that keeps copies in both places must choose.

```callout jargon
The **CAP theorem**: during a network **P**artition, a system can stay **C**onsistent (every read sees the latest write) or stay **A**vailable (every request gets an answer), not both.
```

- **Choose consistency** for unlocking and payments. If the shard leader is out of reach, refuse to unlock. A rider waiting is better than two riders on one bike.
- **Choose availability** for the map. Show bike positions that are a few seconds old rather than an error.

Partitions are rare, so CAP only covers the bad days. **PACELC** adds the normal days: if there is a **P**artition, choose **A** or **C**; **E**lse, choose **L**atency or **C**onsistency. Waiting for a follower to confirm every write is consistent but slower. Answering at once is fast but can lose the write.

"We chose CA" is not a real answer for a system that spans machines. Partitions happen whether you choose them or not.

### Consistency models

"Consistent" comes in strengths. From strongest to weakest:

- **Strong consistency**: every read sees the latest write, as if there were one copy. Needed for "is B-1042 free?".
- **Read-your-writes**: you always see your own changes, though others may see them later. Needed for Ada's profile.
- **Monotonic reads**: you never see time go backwards. Without it, a refresh from a fresher follower then a staler one shows a bike jump back.
- **Eventual consistency**: if writes stop, all copies agree in the end. Fine for ride history and the fleet dashboard.

Stronger models cost more waiting and coordination. Pick the weakest model each feature can live with. Sticking each user to one follower is a cheap way to get monotonic reads.

### Choosing storage

There is no single best database. Choose by the shape of the data and how it is read.

| Kind | Good at | Spoke data |
|---|---|---|
| Relational (SQL), e.g. PostgreSQL | transactions, constraints, joins | rentals, payments, bikes, stations, riders |
| Key-value, e.g. Redis, DynamoDB | very fast get and put by key | latest position of each bike, login sessions |
| Document, e.g. MongoDB | flexible nested records | rider app settings |
| Wide-column, e.g. Cassandra | huge write volume spread over many machines | a global ping store at very large scale |
| Time-series, e.g. TimescaleDB, InfluxDB | values over time, range queries by time | GPS pings, battery levels |
| Object storage, e.g. Amazon S3 | cheap, huge files | old pings archived after 90 days, damage photos |
| CDN | files served near the reader | bike pictures, app downloads |

Money stays in SQL, because transactions and constraints protect it, as in [concurrent writes](/t/module-08-backend/topic-15-concurrent-writes). GPS pings are written constantly, never updated, and read by time range. That is exactly what a time-series store is built for.

### Queues absorb spikes

At 08:00, pings jump from 30 a second to 170 in Riverton, and every city peaks at once. The time-series store need not take each ping the instant it arrives.

Put a queue or log in between, as in [messaging](/t/module-09-architecture/topic-04-messaging). Ingest servers append each ping and answer `202 Accepted` at once. Consumers read the log at a steady pace and write pings in batches.

- A spike makes the log longer for a while, not the database slower.
- If the database is down for five minutes, pings wait in the log instead of being lost.
- You can add consumers when the backlog grows, up to the number of partitions.

The cost is delay: a ping is stored a moment later. That is fine for history. It is not fine for unlocking a bike, which stays a direct, synchronous call.

### Availability: counting nines

Availability is the share of time a system works, written as "nines".

```java run
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        double minutesPerYear = 365 * 24 * 60;
        for (double nines : new double[] {99.0, 99.9, 99.95, 99.99, 99.999}) {
            double downMinutes = minutesPerYear * (100 - nines) / 100;
            System.out.println(String.format(Locale.ROOT, "%7.3f%% up: %8.1f minutes down a year (%5.1f hours)",
                    nines, downMinutes, downMinutes / 60));
        }
        System.out.println(String.format(Locale.ROOT, "two 99.9%% parts in a row: %.4f%%", 99.9 * 99.9 / 100));
        System.out.println(String.format(Locale.ROOT, "two 99%% copies side by side: %.4f%%", (1 - 0.01 * 0.01) * 100));
    }
}
```

It prints:

```text
 99.000% up:   5256.0 minutes down a year ( 87.6 hours)
 99.900% up:    525.6 minutes down a year (  8.8 hours)
 99.950% up:    262.8 minutes down a year (  4.4 hours)
 99.990% up:     52.6 minutes down a year (  0.9 hours)
 99.999% up:      5.3 minutes down a year (  0.1 hours)
two 99.9% parts in a row: 99.8001%
two 99% copies side by side: 99.9900%
```

Two rules hide in the last lines. Parts **in a row**, where each request needs both, multiply: the chain is weaker than either part. Copies **side by side**, where either one will do, fail only when both fail at once. Two 99% copies give 99.99%, if their failures are independent.

```callout jargon
A **single point of failure** (SPOF) is one part whose failure stops the whole system. **Redundancy** means having spare copies so no single part is a SPOF.
```

Hunt for SPOFs on your diagram: one load balancer, one database leader with no failover, one Redis, one data centre. Each needs a spare, or a plan for when it dies.

### In an interview

Spot a design question when you hear "design X", "how would you scale", "millions of users", or "what happens when Y fails". Lead with requirements and numbers, name each trade-off, and finish with bottlenecks.

Classic problems, and the idea each one tests:

- **URL shortener**: estimation, generating unique short ids, a read-heavy cache, hash sharding.
- **Chat app**: long-lived connections, message ordering, delivery receipts, users who are offline.
- **News feed**: building feeds on write or on read, and the celebrity problem, which is a hot key.
- **Rate limiter**: counting requests per user across many servers. You built a token bucket in [object-oriented design](/t/module-06-craft/topic-13-object-oriented-design); a later ticket spreads it across servers.
- **Ride-hailing or bike sharing**: location pings, "near me" queries, and never booking one vehicle twice. That is this ticket's worked example.

## Worked example

Here is Tsunade's sketch: Spoke's bike tracking and rider app, in Riverton plus ten new cities, following the eight steps.

### Requirements and numbers

Functional: riders find nearby bikes on a map, unlock, ride, end the ride and pay. Bikes send GPS pings. Ops see the fleet per city. Non-functional: unlock and payment are strongly consistent and 99.95% available. The map has a p99 under 200 ms and may be a few seconds stale. Every ping is kept for a year.

Assumptions, said out loud: every new city is Riverton-sized, so 11 cities in all. Rides average 20 minutes, with 20,000 a day per city. At the busiest moment, 1 rider in 20 has the app open. A ping is 100 bytes, a ride row 500 bytes, a map answer 2 KB. All cities share one time zone, so their rush hours line up: the worst case.

```java run
public class Main {
    public static void main(String[] args) {
        // The story's numbers for Riverton
        long riders = 100_000, bikes = 2_000, rushHourStarts = 5_000;
        long pingEverySeconds = 10, mapRefreshSeconds = 5;
        // Our assumptions, said out loud
        long cities = 1 + 10;            // Riverton plus ten new cities of the same size
        long rideMinutes = 20;           // an average ride
        long ridesPerDay = 20_000;       // per city
        long appsOpenPercent = 5;        // share of riders with the app open at the busiest moment
        long pingBytes = 100, rideBytes = 500, mapAnswerBytes = 2_000;

        long ridingAtPeak = rushHourStarts * rideMinutes / 60;               // Little's law, per city
        long peakPings = cities * ridingAtPeak / pingEverySeconds;
        long pingsPerDay = cities * ridesPerDay * rideMinutes * (60 / pingEverySeconds);
        long averagePings = pingsPerDay / 86_400;
        long peakMapReads = cities * riders * appsOpenPercent / 100 / mapRefreshSeconds;
        long peakRideStarts = cities * rushHourStarts / 3_600;

        System.out.println("bikes riding at peak, per city: " + ridingAtPeak + " of " + bikes);
        System.out.println("GPS pings per second, peak:     " + peakPings);
        System.out.println("GPS pings per day:              " + pingsPerDay);
        System.out.println("GPS pings per second, average:  " + averagePings);
        System.out.println("map reads per second, peak:     " + peakMapReads);
        System.out.println("ride starts per second, peak:   " + peakRideStarts);
        System.out.println("ping storage per day:           " + pingsPerDay * pingBytes / 1_000_000 + " MB");
        System.out.println("ping storage per year:          " + pingsPerDay * pingBytes * 365 / 1_000_000_000 + " GB");
        System.out.println("ride storage per year:          " + cities * ridesPerDay * rideBytes * 365 / 1_000_000_000 + " GB");
        System.out.println("map bandwidth out, peak:        " + peakMapReads * mapAnswerBytes * 8 / 1_000_000 + " Mbit/s");
    }
}
```

It prints:

```text
bikes riding at peak, per city: 1666 of 2000
GPS pings per second, peak:     1832
GPS pings per day:              26400000
GPS pings per second, average:  305
map reads per second, peak:     11000
ride starts per second, peak:   15
ping storage per day:           2640 MB
ping storage per year:          963 GB
ride storage per year:          40 GB
map bandwidth out, peak:        176 Mbit/s
```

Check the sums by hand. 26.4 million pings ÷ 10^5 ≈ 264 a second, close to the exact 305. 1,666 riding bikes is below the fleet of 2,000, so the assumption is possible. What the numbers say: map reads, at 11,000 a second, are the biggest load. Pings, at about 1,800 a second, come next. Unlocks, at 15 a second, are small but must be exactly right. Pings grow by about 1 TB a year, or 3 TB with three copies.

### API and data model

The API keeps module 08's endpoints and adds two:

```text
POST /rentals                        body {"bikeId":"B-1042"}  -> 201 Created, Location: /rentals/rental-17
POST /rentals/{id}/end                                         -> 200 OK with the fare
GET  /stations/{id}/availability                               -> 200 OK
GET  /cities/{cityId}/bikes?lat=..&lon=..&radius=500           -> 200 OK, nearby bikes  (new)
POST /bikes/{bikeId}/pings           body: a batch of pings    -> 202 Accepted          (new)
```

`202 Accepted` means "received, will be processed later". A bike can send a few pings in one batch to save radio power.

The data, by store:

- **SQL, sharded by `city_id`**: `rider`, `bike (id, city_id, type, status)`, `station`, `rental (id, rider_id, bike_id, started_at, ended_at, fare_cents)`, `payment`.
- **Time-series store**: `ping (bike_id, at, lat, lon, battery)`, split by day.
- **Key-value store, per city**: the latest position of each bike, with a geo index for "near me" searches.
- **Object storage**: pings older than 90 days, compressed.

### The high-level design

```text
 bikes ──pings──▶ ingest servers ──append──▶ ping log (partitioned by bike id)
                                                   │
                                     consumers ◀───┘
                                       │      └──▶ time-series store ──90 days──▶ object storage
                                       ▼
                         latest positions (key-value, per city)
                                       ▲
 rider apps ──HTTPS──▶ load balancers ──▶ API servers (stateless, 3+ per region)
                                             │
                                             └──▶ SQL shards by city: leader + followers
```

```animate
title: A GPS ping's journey
kind: scene
nodes:
  - { id: bike, label: B-1042, emoji: "🚲", row: 0, col: 0 }
  - { id: ingest, label: Ingest server, emoji: "📥", row: 0, col: 2 }
  - { id: log, label: Ping log, emoji: "📜", row: 0, col: 4 }
  - { id: consumer, label: Consumer, emoji: "⚙️", row: 1, col: 4 }
  - { id: tsdb, label: Time-series store, emoji: "📈", row: 2, col: 5 }
  - { id: latest, label: Latest positions, emoji: "📍", row: 2, col: 3 }
  - { id: api, label: API server, emoji: "🖥️", row: 2, col: 1 }
steps:
  - say: B-1042 sends a batch of pings. The ingest server checks it and appends it to the log.
    send: [{ from: bike, to: ingest, label: "POST pings" }, { from: ingest, to: log, label: "append" }]
  - say: The ingest server answers 202 at once. It never waits for a database.
    send: { from: ingest, to: bike, label: "202 Accepted", back: true }
  - say: A consumer reads the log at its own pace, in batches.
    send: { from: log, to: consumer, label: "read batch" }
  - say: It writes the pings to the time-series store, for history and ops reports.
    send: { from: consumer, to: tsdb, label: "insert batch" }
  - say: It also overwrites B-1042's latest position in the key-value store.
    send: { from: consumer, to: latest, label: "set B-1042" }
    set: { latest: "B-1042 updated" }
  - say: A rider's "bikes near me" request reads only the latest positions. It never touches the busy history store.
    send: { from: api, to: latest, label: "nearby?" }
    focus: [api, latest]
```

The write path and the read path are separate. Each can scale on its own.

### Deep dives, trade-offs and bottlenecks

**Unlocking a bike.** It goes to the leader of the city's SQL shard, as one conditional update, as in [concurrent writes](/t/module-08-backend/topic-15-concurrent-writes). Strong consistency, at only 15 a second. If the leader is unreachable, unlocking fails with a clear error: consistency over availability.

**Pings.** About 1,800 a second at peak is easy for a partitioned log. Partitioning by bike id keeps each bike's pings in order. A consumer may see a ping twice after a crash, so the store treats `(bike_id, at)` as the key, and a repeat overwrites itself.

**Nearby bikes.** 11,000 reads a second, all from memory, spread over a few API servers per region. Each city's positions are small: 2,000 bikes × 100 bytes is 200 KB. Answers may be a few seconds old: availability over consistency.

**Ten cities.** Each region runs its own API servers and SQL shards, near its riders. A city is a natural shard: a ride never crosses cities. A city that grows far beyond Riverton is split further.

**Failures.** Two load balancers per region. A synchronous follower for every SQL leader, so failover loses no payments. Three copies of the ping log. If the time-series store is down, pings wait in the log.

**Trade-offs said out loud.** The map is seconds behind, to stay fast and up. Pings are stored a moment late, to survive spikes. Rider history across cities needs several shards, because sharding by city makes the common case simple.

**What breaks first.** Map reads, if usage grows faster than planned: add API servers. Then the ping log's partitions: add partitions before peak. Then the biggest city's shard: split it by bike id.

## Try it

Estimate the ping store for yourself. Run it, then change the assumptions.

```java run
public class Main {
    public static void main(String[] args) {
        long cities = 11;
        long ridesPerDayPerCity = 20_000;
        long rideMinutes = 20;
        long pingEverySeconds = 10;
        long pingBytes = 100;
        long copies = 3;

        long pingsPerDay = cities * ridesPerDayPerCity * rideMinutes * 60 / pingEverySeconds;
        long bytesPerYear = pingsPerDay * pingBytes * 365;

        System.out.println("pings per day:        " + pingsPerDay);
        System.out.println("average per second:   " + pingsPerDay / 86_400);
        System.out.println("GB per year:          " + bytesPerYear / 1_000_000_000);
        System.out.println("GB per year, copies:  " + copies * bytesPerYear / 1_000_000_000);
    }
}
```

1. Product wants smoother maps: a ping every 5 seconds. What happens to storage? Predict it before you run.
2. Spoke adds pings with speed and battery data, 400 bytes each. How many TB a year, with copies?
3. Twenty more cities launch. Which number would make you add a queue, if you did not have one already?

## Recap

- Follow the steps: requirements, estimates, API, data model, high-level design, deep dives, trade-offs, bottlenecks. Ask what must never be wrong and what may be stale.
- Estimate with powers of ten and 86,400 ≈ 10^5 seconds a day. Size servers for the peak, and storage for the total, times the number of copies.
- Scale stateless servers out behind a load balancer. Scale the database in order: indexes, caching, read replicas, then sharding with a good shard key and consistent hashing.
- Replication brings lag and failover choices. During a partition, choose consistency or availability per feature, and pick the weakest consistency model each feature can live with.
- Match each kind of data to a store, absorb spikes with queues, and remove single points of failure. Nines multiply in a row and improve side by side.

## Common mistakes

**1. Drawing boxes before asking questions.**

```text
"Okay, so we'll have Kafka, Redis, Cassandra and twelve microservices..."   (minute 2)
```

Symptom: the design solves a problem nobody had, and misses the one that matters, such as never renting one bike twice. Fix: spend the first five minutes on functional and non-functional requirements, and write them on the board.

**2. Sizing for the average.**

```text
pings: 26,400,000 a day / 86,400 s = 305 a second  ->  "two small servers are plenty"
at 08:00 every city peaks together: 1,832 a second
```

Symptom: everything works at 3 a.m. and falls over at rush hour. Fix: estimate the peak separately, with Little's law or a peak-to-average ratio, and keep spare room on top.

**3. Reading your own write from a replica.**

```text
08:01:00.000  Ada saves home station = uptown     -> leader
08:01:00.050  app reloads the profile             -> follower, 2 s behind -> harbor
```

Symptom: riders say "my change didn't save", but the database has it. Fix: read a user's own recent writes from the leader, or from a follower known to have caught up.

**4. Sharding by a key that piles up.**

```text
shard by ping date: 2026-11-17 -> shard 3, 2026-11-18 -> shard 4
today, every new ping goes to shard 4; shards 1, 2 and 3 sit idle
```

Symptom: one shard runs at 100% while the others idle, so adding shards helps nothing. Fix: shard by a key that spreads new writes, such as a hash of the bike id, inside each city.

**5. Forgetting a single point of failure.**

```text
rider apps -> load balancer -> API servers x 6 -> one Redis -> SQL leader (no follower)
```

Symptom: six API servers, yet one Redis restart or one database crash stops every city. Fix: walk the diagram box by box and ask "what if this dies?". Give each box a spare or a failover plan.
