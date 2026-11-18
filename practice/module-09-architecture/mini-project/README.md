# FEAT-09: Event-driven billing

Billing should stop calling the rentals service. Instead, the rentals service appends a `RideEnded` event to the `ride-events` log when a ride ends, and billing reads the log at its own pace. Build the log, the offsets and billing's consumer, in package `jq.m09.feature`. Everything runs in memory, on one thread.

Ready to use: `BikeType`, `RideEnded`, `LogRecord`, `PaymentGateway`, `PaymentFailedException`, `Charge`, `DeadLetter`, `Statement`, and most of `BillingStore`. Fill in the numbered TODOs.

## 1. `Fares.expectedCents(type, minutes)` (TODO 1)

100 cents to unlock, plus the per-minute price times the minutes: CLASSIC 15, EBIKE 30, CARGO 25. A 0-minute ride costs 100.

## 2. `EventLog` (TODOs 2 to 6)

- `new EventLog(partitionCount)`: throw `IllegalArgumentException` when `partitionCount` is below 1. `partitions()` returns the count.
- `partitionFor(key)`: `Math.floorMod(key.hashCode(), partitions())`. The same key always gets the same partition, and the result is never negative.
- `append(key, event)`: adds a `LogRecord(partition, offset, key, event)` to the end of the key's partition and returns its offset. Offsets start at 0 and count up **in each partition on its own**. The rentals service uses the rider id as the key.
- `read(partition, fromOffset, max)`: the records from `fromOffset`, in offset order, at most `max` of them. Returns an empty list when `fromOffset` is at or past the end.
- `endOffset(partition)`: the offset the next record will get, which is the number of records in that partition.
- `read` and `endOffset` throw `IllegalArgumentException` for a partition below 0 or at or above `partitions()`.

## 3. `OffsetStore` (TODO 7)

- `committed(group, partition)`: the next offset that consumer group should read in that partition. It is 0 if the group never committed there.
- `commit(group, partition, nextOffset)`: saves it, replacing the old value. Each group and each partition has its own offset.

## 4. `BillingConsumer` (TODOs 8 to 11)

Billing's consumer group is `BillingConsumer.GROUP`, which is `"billing"`.

- **`poll(maxPerPartition)`** (TODO 8): for each partition from 0 upwards, read up to `maxPerPartition` records from the group's committed offset. For each record, call `handle(record.event())` **first**, then commit `record.offset() + 1`. Return how many records were handled. If `handle` or the commit throws, let the exception out. A crash before a commit means the record is read again later: delivery is **at least once**.
- **`handle(ride)`**, in this order:
  1. (TODO 9) If `store.isProcessed(ride.eventId())`, do nothing. That event was already charged or dead-lettered, so this is a redelivery or a duplicate. A rider is never charged twice for one event, even by a new `BillingConsumer` after a crash, because the store survives.
  2. (TODO 10) If `ride.fareCents()` is not `Fares.expectedCents(ride.type(), ride.minutes())`, call `store.deadLetter(ride, reason)` with a reason that starts with `fare mismatch`, for example `fare mismatch: expected 400 cents, event says 350`. Do not call the gateway.
  3. (TODO 11) Call `gateway.charge(ride.riderId(), ride.fareCents(), ride.eventId())`. The event id is the idempotency key. On success, call `store.recordCharge(ride, paymentId)`. On a `PaymentFailedException`, try again at once, up to `MAX_ATTEMPTS` (3) attempts in all. If all 3 fail, call `store.deadLetter(ride, reason)` with a reason that starts with `payment failed`, for example `payment failed after 3 attempts: bank timed out`. Then move on: the next record in the partition is still handled.
- `lag()` is ready: how many records in the log the group has not committed yet.

`recordCharge` and `deadLetter` both mark the event as processed.

## 5. `BillingStore.statement(riderId)` (TODO 12)

Return `new Statement(riderId, totalCents, rides)`: the sum of that rider's charges and how many there are. Dead-lettered rides do not count. A rider with no charges gets `Statement(riderId, 0, 0)`.

## How the checks work

The checks use a fake payment gateway that records every attempt, and fails a given event's next attempts on command. To fake a crash, one check uses an `OffsetStore` whose first `commit` throws, then starts a new `BillingConsumer` with the same log, offsets and store. The fares in the checks: evt-1001 rider-7 EBIKE 12 min 460 cents; evt-1002 rider-8 CLASSIC 20 min 400; evt-1003 rider-7 CARGO 10 min 350; evt-1004 rider-7 EBIKE 10 min says 350, but the rule gives 400; evt-1005 rider-7 CLASSIC 4 min 160.
