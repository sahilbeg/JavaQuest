# A hash ring, a lag-aware router and a quorum read

Riders see a rental vanish right after starting it, because the app read from a replica that had not caught up. And the rides table no longer fits on one machine. Build three small pieces that real databases are made of: a consistent-hash ring to split rides across shards, a router that gives each rider read-your-writes and monotonic reads, and a quorum reader that finds the newest copy and the replicas that need repair.

The three parts are independent, so you can do them in any order.

Given to you, finished: `ConsistentHashRing.hash`, the `ReplicatedLog` constructor and `followers()`, the records `ReplicaAnswer` and `ReadResult`, and `QuorumNotReachedException`.

## 1. `ConsistentHashRing(int virtualNodes)`

The ring holds points numbered from 0 to 4,294,967,295. `hash(text)` (given: 32-bit FNV-1a with MurmurHash3's final mix) turns any text into one of those numbers, the same on every machine.

- **TODO 1:** throw `IllegalArgumentException` when `virtualNodes < 1`.
- **TODO 2, `addNode(node)`:** a node already on the ring throws `IllegalArgumentException`. Otherwise the node gets `virtualNodes` points: `hash(node + "#0")`, `hash(node + "#1")`, ... up to `hash(node + "#" + (virtualNodes - 1))`. Keep them in the `TreeMap<Long, String> ring` (point to node). If a point is already taken, the node that has it keeps it (`putIfAbsent`).
- **TODO 3, `removeNode(node)`:** `false` if the node is not on the ring. Otherwise take off the points **it owns** (`ring.remove(point, node)`) and answer `true`.
- **TODO 4, `nodeFor(key)`:** the node owning the first point **at or after** `hash(key)`. If no point is at or after it, wrap round to the **lowest** point. `ring.ceilingEntry(...)` and `ring.firstEntry()` do this in O(log P) for P points. An empty ring throws `IllegalStateException`.
- **TODO 5, `pointCount()`:** how many points are on the ring.
- **TODO 6, `nodes()`:** the nodes on the ring, in name order.
- **TODO 7, `assign(keys)`:** a map from each key to `nodeFor(key)`, in the keys' own order.
- **TODO 8, `countMoved(before, after)`:** how many keys of `before` have a different node in `after`.

The checks use exact keys and nodes, so follow the point names exactly. Adding a fifth node to four, with 100 virtual nodes each, moves about one key in five, and **every moved key moves to the new node**. Removing a node moves only that node's keys.

## 2. `ReplicatedLog(followers)` and `SessionRouter(log)`

The leader keeps a list of entries. **Positions count from 1.** Each follower has applied the log up to some position, starting at 0. A replica at position `p` shows exactly the first `p` entries. `ReplicatedLog.LEADER` (`"leader"`) names the leader.

- **TODO 9, `append(entry)`:** add the entry and answer its position: 1, then 2, 3...
- **TODO 10, `lastPosition()`:** the newest position, or 0 for an empty log.
- **TODO 11, `replicateTo(follower, position)`:** the follower is now at `position`. Throw `IllegalArgumentException` for an unknown follower (the leader is not a follower), for a position past `lastPosition()`, and for a position below the follower's current one: a follower never goes backwards. The same position again is fine.
- **TODO 12, `appliedPosition(replica)`:** the follower's position; the leader is always at `lastPosition()`. An unknown name throws `IllegalArgumentException`.
- **TODO 13, `read(replica)`:** the first `appliedPosition(replica)` entries, oldest first.

The router remembers, per session, the **highest position the session has seen**: the positions it wrote, and the positions of the replicas it read from.

- **TODO 14, `write(session, entry)`:** append through the log, raise the session's seen position to the new position, and answer the position.
- **TODO 15, `replicaFor(session)`:** the **first follower in `log.followers()` order** whose applied position is at least the session's seen position. If none has caught up, `ReplicatedLog.LEADER`. A session that has never written or read has seen position 0, so the first follower always qualifies.
- **TODO 16, `read(session)`:** read from `replicaFor(session)`, then raise the session's seen position to that replica's applied position, and return the entries.

Part 14 gives **read-your-writes** (you see what you wrote). Raising the seen position on reads gives **monotonic reads** (a refresh never shows less than before).

## 3. `QuorumReader(n, r)`

N replicas hold each value; a read waits for answers from R of them. Each answer is a `ReplicaAnswer(replica, value, version)`; a replica with no copy answers `value = null, version = 0`.

- **TODO 17:** throw `IllegalArgumentException` unless `1 <= r <= n`.
- **TODO 18, `overlaps(n, w, r)`:** `true` when `w + r > n`. Throw `IllegalArgumentException` unless `1 <= w <= n` and `1 <= r <= n`.
- **TODO 19, `read(answers)`:** fewer than `r` answers throws `QuorumNotReachedException` with the message `needed 2 answers, got 1` (with the real numbers).
- **TODO 20:** the answer with the **highest version** wins. Return `ReadResult(value, version, needRepair)`, where `needRepair` lists the replicas with a **lower** version, in the order they answered. If another answer has the **same** highest version but a **different** value (compare with `Objects.equals`, since values may be null), throw `IllegalStateException`: that is a conflict a version number cannot settle.

Time: the ring's `nodeFor` is O(log(N × V)); adding a node is O(V log(N × V)). The log is O(1) per append, the router O(F) per read for F followers, the quorum read O(R). Space: O(N × V) points, O(entries + sessions).

Press **Submit** in Cadence, or just save the file in your IDE. The checks run on their own.
