## Hook

A teacher writes the homework on the board, and three students copy it into their notebooks. One student writes slowly. A friend who missed class asks the slow student what the homework is, and hears only half of it. Nobody lied. The copy just was not finished yet.

Spoke has exactly that bug. Ada starts a ride, her app reloads, and the rental is gone: the app asked a copy of the database that had not caught up. Meanwhile the rides table has outgrown one machine. In this ticket you learn how copies stay in step, what "in step" can mean, and how to split data over many machines without losing your way.

## Concept

### Where system design left off

[System design](/t/module-09-architecture/topic-06-system-design) sketched the big ideas: read replicas and lag, failover, sharding, consistent hashing, and CAP. [NoSQL data modelling](/t/module-09b-data/topic-04-nosql-modeling) showed partition keys and quorum reads. Now you open the machinery.

```callout jargon
A **replica** is a full copy of a database on another machine. **Replication** keeps the copies in step. A **shard** is one piece of a database that has been split by key; **sharding** is doing the splitting.
```

The two ideas solve different problems, and big systems use both:

- **Replication** copies the **same** data. It buys more read capacity, and survival when a machine dies.
- **Sharding** splits **different** data across machines. It buys more storage and more write capacity.

A typical setup has, say, 8 shards, and each shard has a leader and two followers: 24 machines in all.

### Leader and followers: shipping the log

In **leader-follower** replication, every write goes to one machine, the **leader**. The **followers** copy it. But copy what, exactly?

The answer is the log. In [inside transactions](/t/module-09b-data/topic-03-transaction-internals) you met the **write-ahead log** (WAL): every change is written to a log before the data pages. Postgres streams that same log to each follower, which replays it, change by change. This is **streaming replication**.

```callout jargon
A **log position** says how far into the log something is. Postgres calls it the **LSN** (log sequence number) and prints it like `0/3000060`. A higher LSN is later in the log.
```

Every write gets a log position on the leader. Each follower knows the position it has replayed up to. Comparing the two tells you exactly how far behind a follower is. Your practice builds this model: a leader's log with positions 1, 2, 3, and followers that have applied up to some position.

In Postgres, `pg_current_wal_lsn()` gives the leader's position, and `pg_last_wal_replay_lsn()` gives a follower's. MySQL has its own log, the binlog, and identifies each transaction in it with a GTID.

### Synchronous or asynchronous

When the leader commits, must it wait for a follower?

```callout jargon
With **synchronous** replication, the leader waits until a follower confirms it has the change before telling the client "committed". With **asynchronous** replication, the leader answers at once and the followers catch up afterwards.
```

| | Synchronous | Asynchronous |
|---|---|---|
| Commit speed | adds a network round trip | as fast as the leader alone |
| Leader dies | nothing confirmed is lost | the last few writes may be lost |
| A follower is slow or down | every write waits, or stops | nothing waits |

All-synchronous means one dead follower stops every write. So most teams choose **semi-synchronous**: one follower is synchronous and the rest are asynchronous. If the synchronous one fails, another is promoted to that role.

In Postgres, `synchronous_standby_names = 'ANY 1 (replica_a, replica_b)'` waits for any one of two followers. `synchronous_commit` chooses how far: `on` waits until the follower has the change safely on disk, `remote_apply` until it has also **replayed** it, so reads there see it.

### Replication lag

With asynchronous followers, there is always a gap between the leader's position and each follower's. That gap, in time, is **replication lag**.

On a healthy network it is a few milliseconds. But it grows, sometimes to minutes, when:

- a burst of writes arrives faster than a follower can replay them, such as a big nightly import;
- a long query on the follower holds back replay, because Postgres will not remove rows that query still needs;
- the network between data centres is slow or congested.

Lag is fine for data that may be a little old, like yesterday's ride history. It is a bug for anything a rider just did. Watch it: Postgres's `pg_stat_replication` view shows each follower's `replay_lag`. Alert when it passes a few seconds.

### Read-your-writes: the vanishing rental

Here is Kakashi's bug, step by step.

```animate
title: Ada's rental vanishes
kind: scene
nodes:
  - { id: app, label: Ada's app, emoji: "📱" }
  - { id: api, label: Spoke API, emoji: "🖥️" }
  - { id: leader, label: Leader, emoji: "🗄️", value: "position 41", col: 3 }
  - { id: replica, label: Follower, emoji: "📚", value: "position 41", row: 1, col: 3 }
steps:
  - say: Ada taps Unlock on B-1042. The API writes rental-17 to the leader, which gives it log position 42.
    send: [{ from: app, to: api, label: "POST /rentals" }, { from: api, to: leader, label: "INSERT rental-17" }]
    set: { leader: "position 42" }
  - say: The leader answers at once. Asynchronous replication means it did not wait for the follower.
    send: [{ from: leader, to: api, label: "committed", back: true }, { from: api, to: app, label: "201 Created", back: true }]
  - say: The app reloads Ada's rides. All reads go to the follower, to spare the leader.
    send: { from: app, to: api, label: "GET my rentals" }
  - say: The follower is still at position 41. Rental-17 is not there yet.
    send: [{ from: api, to: replica, label: "SELECT" }, { from: replica, to: api, label: "no active rental", back: true }]
    focus: [replica]
  - say: Ada sees no rental, though she is sitting on the bike. She taps Unlock again.
    send: { from: api, to: app, label: "[]", back: true }
  - say: A moment later the follower replays position 42. The rental "comes back", too late.
    set: { replica: "position 42" }
```

```callout jargon
**Read-your-writes** consistency means that after you write something, your own reads always see it. Other people may still see the old data for a moment.
```

Every step worked as designed. The bug is the **combination**: asynchronous replication plus reading your own fresh data from a follower.

### Three fixes for read-your-writes

**1. Read from the leader after a write.** For data the user can change, such as their own rentals, read from the leader. Or read from the leader for a short time after each write, say 10 seconds. Simple, but it can pile load onto the leader.

**2. Track the log position.** When Ada writes, remember the position, 42, for her session. For her next read, use a follower that has replayed 42 or later. If none has, read from the leader, or wait briefly for a follower to catch up. This is exact and keeps most reads on followers. Your practice's `SessionRouter` does this.

**3. Use the answer you already have.** The `POST` returned the new rental. The app can show it straight away, without asking again. Often the cheapest fix of all.

For fix 2, where does the position live? In the rider's session, or in a cookie the app sends back. In Postgres, the write returns `pg_current_wal_lsn()`; the read checks `pg_last_wal_replay_lsn()` on a follower. Across several devices it gets harder: a write from Ada's phone should also be seen by her laptop, so the position must live on the server.

### Monotonic reads and sticky sessions

A second anomaly. Ada refreshes twice. The first read lands on a follower that is up to date and shows rental-17. The second lands on a follower that is behind, and rental-17 disappears again. Time seems to run backwards.

```callout jargon
**Monotonic reads** means that once you have seen some data, later reads never show you an older state. You may not see the newest data, but you never go back.
```

Two fixes:

- **Sticky sessions**: always send one rider's reads to the same follower, for example by hashing the rider id to pick it. A single follower never goes backwards. If it dies, move the rider to another and accept one possible jump.
- **Track the highest position seen**, not only the highest written. After each read, remember the position of the follower that answered. Only use followers at or beyond it. This is the same idea as fix 2, and your practice does both together.

### Failover, step by step

When the leader dies, a follower must take over. This is **failover**. It has three steps, and each can go wrong.

1. **Notice the leader is dead.** Usually it misses heartbeats for some timeout, such as 30 seconds. Too short, and a brief network hiccup triggers a needless failover. Too long, and writes are down the whole time.
2. **Choose the new leader.** Pick the follower with the highest replayed log position, so the fewest writes are lost. With semi-synchronous replication, pick the synchronous follower: it has everything.
3. **Point everyone at it.** The other followers now copy the new leader. The apps write to it, through a changed DNS name, a proxy, or a connection string with several hosts.

With asynchronous replication, writes the old leader confirmed but never shipped are **lost**. Worse, the old leader may come back with those extra writes, and they get thrown away. Any system that saw them, such as a cache or a payment provider, now disagrees with the database.

Tools such as **Patroni** run this for Postgres, using a consensus store like etcd to agree on who leads. Doing it by hand at 3 a.m. is how mistakes happen.

### Split brain and fencing

```callout jargon
**Split brain** is when two machines both believe they are the leader and both accept writes. The data then splits into two histories that cannot simply be merged.
```

It happens when the old leader was not dead, only cut off. The cluster promotes a new leader. The network heals, and the old one still thinks it leads.

The guard is a number that only goes up:

- Each leader gets a **term** (also called an epoch or generation): 7, then 8 after a failover.
- Every write and every message carries the term.
- Followers, and the storage itself, refuse anything from an older term.

This is the **fencing token** from [Redis patterns](/t/module-09b-data/topic-05-redis-patterns), at the scale of a whole database. The blunt version is called STONITH, "shoot the other node in the head": cut the old leader's power or network before promoting the new one.

### Leaderless replication

Some databases have no leader at all. Amazon's Dynamo paper made the idea famous; **Cassandra**, **ScyllaDB** and **Riak** work this way.

- Each key is stored on **N** replicas, often 3.
- A client, or a coordinator node acting for it, sends every write to **all** N replicas, and counts it a success when **W** of them confirm.
- A read asks the replicas, and waits for **R** answers.

There is no failover, because there is no leader to lose. If one replica of three is down, writes and reads carry on with the other two. The price is that replicas can disagree, and every read has to sort that out.

### Quorums: N, W and R

The rule that makes leaderless reads safe is short:

```text
W + R > N   →   every read group shares at least one replica with every write group
```

With N = 3, W = 2 and R = 2: a write reached at least 2 replicas, and a read asks 2. Two groups of 2 out of 3 must share one. So at least one answer holds the latest successful write.

```callout jargon
A **quorum** is the minimum number of replicas that must answer for an operation to count. N is how many copies exist, W how many must confirm a write, and R how many must answer a read.
```

```animate
title: A quorum read finds the newest copy
kind: scene
nodes:
  - { id: client, label: Coordinator, emoji: "🧭" }
  - { id: a, label: Replica A, emoji: "🗄️", value: "v4 ACTIVE", col: 2 }
  - { id: b, label: Replica B, emoji: "🗄️", value: "v4 ACTIVE", row: 1, col: 2 }
  - { id: c, label: Replica C, emoji: "🗄️", value: "v4 ACTIVE", row: 2, col: 2 }
steps:
  - say: N = 3, W = 2, R = 2. Rental-17 ends. The write goes to all three replicas as version 5.
    send: [{ from: client, to: a, label: "v5 ENDED" }, { from: client, to: b, label: "v5 ENDED" }, { from: client, to: c, label: "v5 ENDED" }]
  - say: A and B confirm. Two is W, so the write succeeds. C was restarting and missed it.
    set: { a: "v5 ENDED", b: "v5 ENDED" }
    send: [{ from: a, to: client, label: "ok", back: true }, { from: b, to: client, label: "ok", back: true }]
  - say: Later, a read asks for 2 answers. B and C reply first.
    send: [{ from: b, to: client, label: "v5 ENDED", back: true }, { from: c, to: client, label: "v4 ACTIVE", back: true }]
  - say: The versions disagree. The highest version, 5, wins. Because 2 + 2 > 3, one answer had to be from the write group.
    focus: [b]
  - say: The coordinator also sends v5 to C. That is read repair. Now all three agree.
    send: { from: client, to: c, label: "repair: v5 ENDED" }
    set: { c: "v5 ENDED" }
```

Common choices for N = 3:

- W = 2, R = 2: the usual balance.
- W = 3, R = 1: fast reads, but writes fail when any replica is down.
- W = 1, R = 1: fastest, with **no** overlap guarantee.

The Try it card checks every combination.

Two warnings. A **sloppy quorum** lets a write land on stand-in replicas when the home replicas are unreachable, and **hinted handoff** passes it home later. That keeps writes flowing, but the overlap guarantee is gone until the hand-off finishes. And even a strict quorum does not give full consistency when two writes happen at the same moment, which the next card handles.

### Versions, last write wins and vector clocks

To pick "the newest" answer, each value carries a **version**. The simplest version is a number the database raises on every write, as in the animation and your practice.

But two riders' apps, or two servers, can write the same key at the same moment, through different replicas. Which one is newer? Two common answers:

- **Last write wins (LWW)**: each write carries a timestamp, and the highest wins. Cassandra does this. It is simple, but the loser is **silently dropped**. And a server whose clock runs a little fast always "wins", even when its write was really older.
- **Vector clocks** (or version vectors): each replica keeps its own counter, and a version is the list of all counters. Comparing two lists tells you whether one write came after the other, or whether they were **concurrent**. Concurrent writes are both kept, and the application merges them. Riak and the Dynamo paper did this.

Your practice's `QuorumReader` refuses to guess: two different values with the same version is a conflict, and it throws. For a rental's status, a conflict like that is a bug you want to hear about, not hide.

### Read repair and anti-entropy

Replicas that missed a write must catch up somehow. There are two ways.

```callout jargon
**Read repair** fixes stale replicas during a read: the coordinator sends the newest value to every replica that answered with an older one. **Anti-entropy** is a background process that compares replicas and copies over whatever differs.
```

Read repair only fixes keys that someone reads. A ride record nobody looks at could stay stale on one replica forever. So databases also run anti-entropy. Cassandra compares replicas with **Merkle trees**: trees of hashes, where comparing two roots tells you at once whether anything differs, and walking down finds the exact range. Its `nodetool repair` command runs this, and teams schedule it regularly.

Your `QuorumReader` returns the list of replicas that need read repair, in the order they answered.

### Sharding: when one machine is not enough

Kakashi's second problem: Spoke's rides and their GPS points add about 5 GB a day across eleven cities. One Postgres machine is running out of disk, and its write rate is near the limit.

Replicas do not help here. Every follower holds **all** the data and replays **every** write. So replication multiplies read capacity, but not storage or write capacity. For those, you split the data.

```callout jargon
**Sharding** (also called **partitioning**) splits a table's rows across several databases by a **shard key**, a column whose value decides each row's shard. Each shard holds and writes only its own rows.
```

Before sharding, try the cheaper steps. Add better indexes, archive old rides to cheap storage, or buy a bigger machine. Postgres can also partition a table on one machine, which a later ticket covers. Sharding is powerful, but every query and every transaction now has to know where its rows live. Do it when you must, not before.

### By range or by hash

There are two main ways to map a shard key to a shard.

**By range.** Each shard holds a range of keys: rider ids 1 to 25,000 on shard 1, and so on, or rides by month. Range scans are fast: "all rides in March" is one shard. Ranges can split when they grow, which HBase, Bigtable and CockroachDB do automatically. The danger is a hot range. If the key is the ride's start time, **every** new ride goes to the newest range, and one shard takes all the writes.

**By hash.** Hash the key, and the hash picks the shard. Neighbouring keys scatter evenly, so writes spread well. But a range query, such as "rides from 08:00 to 09:00", must ask every shard. Cassandra and MongoDB's hashed sharding work this way.

| | Range | Hash |
|---|---|---|
| Spread of writes | uneven if keys arrive in order | even |
| Range queries on the key | one or a few shards | every shard |
| Adding a shard | split one range | move some keys everywhere |

A **compound** key mixes both: hash on the first part, then sort by the second inside each shard. Cassandra's partition key plus clustering columns, from [NoSQL data modelling](/t/module-09b-data/topic-04-nosql-modeling), is exactly that.

### Choosing a shard key

The shard key is the hardest decision to undo. Choose by the **queries**, not the data.

Spoke's busiest queries are "this rider's active rental" and "this rider's ride history". Both name one rider. So the shard key is `rider_id`, hashed. Each rider's rides live together on one shard, and those queries touch one machine.

[System design](/t/module-09-architecture/topic-06-system-design) first suggested `city_id`. At eleven cities that is too few values: a big city becomes one huge, hot shard, and you can never spread one city over several machines. A good shard key has **many values, used evenly**.

Even then, one value can be hot. Spoke's rebalancing crew logs every truck move under one staff account, `rider-1`, with thousands of rows a day. One key always maps to one shard. Fixes:

- **Split the hot key**: add a suffix, such as `rider-1#0` to `rider-1#7`, and read all eight.
- **Give it its own table or shard**, since it is not really a rider.

Watch per-shard load, not only the total. An average hides the one shard on fire.

### Consistent hashing with virtual nodes

With `hash(key) mod N`, adding a shard changes N, and most keys change shard. Consistent hashing puts the shards on a ring instead, so adding one moves only the keys it takes over. System design showed the ring; this run shows why **virtual nodes**, many points per shard, matter:

```java run
import java.util.Map;
import java.util.TreeMap;

public class Main {
    static long hash(String text) {                // 32-bit FNV-1a, then MurmurHash3's final mix
        int h = 0x811C9DC5;
        for (char c : text.toCharArray()) {
            h ^= c;
            h *= 0x01000193;
        }
        h ^= h >>> 16;
        h *= 0x85EBCA6B;
        h ^= h >>> 13;
        h *= 0xC2B2AE35;
        h ^= h >>> 16;
        return Integer.toUnsignedLong(h);          // a point from 0 to 4,294,967,295
    }

    static TreeMap<Long, String> ring(int shards, int virtualNodes) {
        TreeMap<Long, String> ring = new TreeMap<>();
        for (int s = 0; s < shards; s++) {
            for (int v = 0; v < virtualNodes; v++) {
                ring.putIfAbsent(hash("shard-" + s + "#" + v), "shard-" + s);
            }
        }
        return ring;
    }

    static String owner(TreeMap<Long, String> ring, String key) {
        Map.Entry<Long, String> next = ring.ceilingEntry(hash(key));   // walk clockwise
        return next != null ? next.getValue() : ring.firstEntry().getValue();
    }

    public static void main(String[] args) {
        int riders = 100_000;
        for (int virtualNodes : new int[] {1, 10, 100}) {
            TreeMap<Long, String> four = ring(4, virtualNodes);
            TreeMap<Long, String> five = ring(5, virtualNodes);
            int[] perShard = new int[4];
            int moved = 0;
            for (int i = 0; i < riders; i++) {
                String rider = "rider-" + i;
                String before = owner(four, rider);
                perShard[before.charAt(6) - '0']++;
                if (!before.equals(owner(five, rider))) moved++;
            }
            int smallest = Math.min(Math.min(perShard[0], perShard[1]), Math.min(perShard[2], perShard[3]));
            int biggest = Math.max(Math.max(perShard[0], perShard[1]), Math.max(perShard[2], perShard[3]));
            System.out.printf("V=%3d: shards hold %5.1f%% to %5.1f%% of riders; adding shard-4 moves %4.1f%%%n",
                    virtualNodes, smallest * 100.0 / riders, biggest * 100.0 / riders, moved * 100.0 / riders);
        }
        int movedMod = 0;
        for (int i = 0; i < riders; i++) {
            long h = hash("rider-" + i);
            if (h % 4 != h % 5) movedMod++;
        }
        System.out.printf("hash %% N instead: going from 4 to 5 shards moves %4.1f%%%n", movedMod * 100.0 / riders);
    }
}
```

It prints:

```text
V=  1: shards hold  10.5% to  39.8% of riders; adding shard-4 moves 15.8%
V= 10: shards hold  15.8% to  33.5% of riders; adding shard-4 moves 19.4%
V=100: shards hold  24.5% to  25.9% of riders; adding shard-4 moves 19.9%
hash % N instead: going from 4 to 5 shards moves 80.0%
```

With one point each, one shard holds four times as many riders as another. With 100 points each, every shard holds close to a quarter. Adding a fifth shard moves about one rider in five, the fair share the new shard should take, and every moved rider moves **to** the new shard. With mod N, 80% move.

Finding a key's shard is a `TreeMap` lookup: O(log P) for P points. Space is O(P). Virtual nodes also help when shards differ in size: give a bigger machine more points.

### Resharding: moving data while it runs

Adding a shard means moving rows while riders keep riding. There are two common designs.

- **A fixed number of partitions.** Split the key space into many more partitions than machines from day one, say 1,024 for 8 machines. A new machine takes whole partitions from the others. Keys never change partition; only partitions change machine. Redis Cluster's 16,384 slots work this way, and so do Elasticsearch shards.
- **Consistent hashing**, as above: the new node's points take over small slices of the ring.

Either way, the move itself follows the same recipe:

1. **Copy** the moving rows to the new shard in the background (backfill).
2. **Stream** changes made during the copy, from the log, so the copy catches up.
3. **Switch**: briefly pause writes for those keys, confirm the copy is current, then send reads and writes to the new shard.
4. **Clean up** the old copies, after a safety period.

Tools such as **Vitess** for MySQL and **Citus** for Postgres automate this. A routing layer, which knows which shard holds which key, makes the switch invisible to the app.

### Cross-shard queries: scatter-gather

Sharding by `rider_id` makes rider queries fast. Now Kakashi asks: "how many rides started at Harbor today?" Harbor's rides belong to riders on **every** shard.

```callout jargon
A **scatter-gather** query is sent to every shard at once (scatter); a coordinator then merges the answers (gather), such as adding counts or merge-sorting lists.
```

It works, but it costs:

- **Every shard does work** for every such query. Ten shards, ten times the load.
- **The slowest shard sets the speed.** If each shard is slow 1% of the time, a query that needs all ten is slow almost 10% of the time. This is called **tail latency**.
- **Sorting and paging get hard.** "The newest 20 rides at Harbor" needs the newest 20 from **each** shard, then a merge.

So keep scatter-gather for rare or background queries. Make the frequent ones hit one shard, or give them their own copy of the data, as the next card shows.

### Global secondary indexes

An index on `station_id` inside each shard is a **local** index. It speeds up each shard's part of a scatter-gather, but the query still visits every shard.

```callout jargon
A **global secondary index** is an index on another column, sharded by **that** column. All of Harbor's index entries live on one index shard, so "rides at Harbor" asks one place.
```

The catch: a ride's row lives on its rider's shard, and its index entry lives on Harbor's index shard. A single write now touches two shards. Keeping both in one transaction across machines is slow, so most systems update global indexes **asynchronously**. DynamoDB's GSIs work this way, as [NoSQL data modelling](/t/module-09b-data/topic-04-nosql-modeling) showed: a GSI read can miss a write from a moment ago.

| | Local index | Global index |
|---|---|---|
| A write updates | one shard | the row's shard plus an index shard |
| A read by that column asks | every shard | one index shard |
| Fresh? | yes, same transaction | usually a little behind |

A third option is a separate copy built for the question, such as a daily per-station summary table filled by a pipeline. A later ticket on data pipelines builds those.

### In an interview

Replication and sharding come up in almost every system design round. Listen for "millions of users", "it must survive a data centre failure", "the data is too big for one machine", or "users must see their own changes".

- **Scale a read-heavy service**: replicas, lag, and read-your-writes by log position or leader reads.
- **Design a key-value store** (like Dynamo): consistent hashing with virtual nodes, N/W/R quorums, versions, read repair, hinted handoff.
- **Shard a table**: pick the key from the queries, hash or range, hot keys, resharding, scatter-gather.
- **What happens when the leader dies?** Failover steps, lost asynchronous writes, split brain and terms.
- **Design a distributed cache**: consistent hashing, and what moves when a node joins.

Name the trade-off every time: synchronous means safe but slower, quorums mean overlap but more waiting, global indexes mean fast reads but stale entries.

## Worked example

Kakashi's first problem in code. A leader keeps a log; a follower has replayed part of it. First, every read goes to the follower. Then each rider's last written position decides where their read may go.

```java run
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    static final List<String> leaderLog = new ArrayList<>();     // position 1 is the first entry
    static long replicaApplied = 0;                              // how far the replica has replayed the log

    static long write(String entry) {
        leaderLog.add(entry);
        return leaderLog.size();                                 // the log position of this write
    }

    static List<String> readLeader() {
        return List.copyOf(leaderLog);
    }

    static List<String> readReplica() {
        return List.copyOf(leaderLog.subList(0, (int) replicaApplied));
    }

    public static void main(String[] args) {
        write("rental-16 started by rider-3");
        replicaApplied = 1;                                      // the replica is up to date... for now

        long adaWrote = write("rental-17 started by rider-7");  // Ada taps Unlock
        System.out.println("Ada's write is at position " + adaWrote + "; replica is at " + replicaApplied);

        // Before: every read goes to a replica
        System.out.println("naive read:  " + readReplica());

        // After: remember the position each rider wrote, and only use a replica that has reached it
        Map<String, Long> lastWritten = new HashMap<>();
        lastWritten.put("rider-7", adaWrote);
        long needed = lastWritten.getOrDefault("rider-7", 0L);
        List<String> rows = replicaApplied >= needed ? readReplica() : readLeader();
        System.out.println("routed read: " + rows + (replicaApplied >= needed ? " (replica)" : " (leader)"));

        replicaApplied = 2;                                      // a few milliseconds later the replica catches up
        rows = replicaApplied >= needed ? readReplica() : readLeader();
        System.out.println("later read:  " + rows.size() + " rentals" + (replicaApplied >= needed ? " (replica)" : " (leader)"));
    }
}
```

It prints:

```text
Ada's write is at position 2; replica is at 1
naive read:  [rental-16 started by rider-3]
routed read: [rental-16 started by rider-3, rental-17 started by rider-7] (leader)
later read:  2 rentals (replica)
```

Line by line:

1. `write` appends to the leader's log and returns the position, like Postgres's LSN. Ada's rental is at position 2.
2. The replica has only replayed position 1. The naive read goes there and misses rental-17: the vanishing rental.
3. The router remembers that rider-7 wrote position 2. The replica is at 1, which is less, so the read goes to the leader and finds the rental.
4. Once the replica reaches 2, Ada's reads go back to the replica. The leader is spared again.

This version has one replica and forgets nothing. Your practice adds several followers, the "first follower that has caught up" rule, and monotonic reads. Then it adds a consistent-hash ring and a quorum reader.

## Try it

This program checks every quorum choice for N = 3 the slow, certain way: it tries every possible write group against every possible read group.

```java run
public class Main {
    // true if every group of w replicas shares at least one replica with every group of r replicas
    static boolean alwaysOverlap(int n, int w, int r) {
        for (int writeGroup = 0; writeGroup < (1 << n); writeGroup++) {
            if (Integer.bitCount(writeGroup) != w) continue;
            for (int readGroup = 0; readGroup < (1 << n); readGroup++) {
                if (Integer.bitCount(readGroup) != r) continue;
                if ((writeGroup & readGroup) == 0) return false;   // found a read that misses the write
            }
        }
        return true;
    }

    public static void main(String[] args) {
        int n = 3;
        for (int w = 1; w <= n; w++) {
            StringBuilder row = new StringBuilder("N=" + n + " W=" + w + ":");
            for (int r = 1; r <= n; r++) {
                row.append("  R=").append(r).append(alwaysOverlap(n, w, r) ? " safe" : " MISS");
            }
            System.out.println(row);
        }
    }
}
```

Each group is a bitmask: bit i set means replica i is in the group. `writeGroup & readGroup` is 0 when they share no replica.

1. Before running, mark which cells you expect to be "safe" using W + R > N. Then run it and compare.
2. Change `n` to 5. Which is the smallest R that is safe with W = 3?
3. With N = 5, why might a team pick W = 3, R = 3 rather than W = 5, R = 1? Think about what happens when one replica is down.

## Recap

- Leader-follower replication ships the leader's log to followers. Synchronous means nothing confirmed is lost but writes wait; asynchronous is fast but leaves replication lag and can lose writes on failover.
- Lag breaks read-your-writes and monotonic reads. Fix them by reading from the leader after a write, by tracking the log position a session wrote or saw, or with sticky sessions.
- Failover must find the dead leader, promote the most up-to-date follower and repoint everyone; terms and fencing stop split brain.
- Leaderless stores write to N replicas and need W + R > N for reads to overlap writes. Versions pick the newest answer, and read repair and anti-entropy fix stale replicas.
- Sharding splits data by a shard key chosen from the queries. Consistent hashing with virtual nodes balances shards and moves few keys; scatter-gather and global indexes handle queries that cross shards.

## Common mistakes

**1. Sending every read to a replica, including a user's own fresh data.**

```java nocompile
@Transactional(readOnly = true)              // routed to a follower by the data source
public List<Rental> myRentals(String riderId) {
    return rentals.findByRiderId(riderId);
}
```

Symptom: a rider starts a ride and the app says they have none. It happens only under load, when lag grows, so it rarely shows up in testing. Fix: read data the user just changed from the leader, or from a follower that has reached the position they wrote. Or show the rental the `POST` already returned.

**2. Thinking W + R > N makes everything consistent.**

```text
N=3, W=2, R=2, but sloppy quorums are on
08:00 two home replicas are unreachable; the write lands on stand-ins
08:01 a read asks the two home replicas that are back: neither has the write
```

Symptom: a read misses a successful write, despite "2 + 2 > 3". Sloppy quorums, concurrent writes and failed writes that were not rolled back all break the guarantee. Fix: know your database's exact settings, and use real versions or a leader for data that must be exact.

**3. Choosing a shard key with few values or one busy value.**

```sql
-- Citus: spread the rides table over the shards by city
SELECT create_distributed_table('rides', 'city_id');   -- 11 cities, one of them five times bigger
```

Symptom: one shard runs hot while the others idle, and adding shards does not help, because one city can never be split. Fix: pick a key with many evenly used values that your main queries name, such as `rider_id`, and handle the few hot values on their own.

**4. Sharding with hash mod N.**

```java nocompile
int shard = Math.floorMod(riderId.hashCode(), shardCount);   // shardCount goes from 4 to 5...
```

Symptom: adding one shard means moving about 80% of all rows, as the consistent hashing card showed, in a migration that takes days. Fix: a consistent-hash ring with virtual nodes, or a fixed number of partitions much larger than the number of machines.

**5. Last write wins, with clocks that disagree.**

```text
08:00:01 server A, whose clock runs 2 s fast, writes rental-17 = ENDED, stamped 08:00:03
08:00:02 server B, whose clock is right, writes rental-17 = PAUSED, stamped 08:00:02
the database keeps ENDED, the older write, because 08:00:03 is the higher stamp
```

Symptom: a newer change silently disappears, with no error anywhere. Fix: do not rely on wall clocks to order writes to the same key. Use version numbers from one place, a leader, or vector clocks that keep concurrent writes for the application to merge.
