## Hook

You post a birthday card instead of phoning. You do not need your friend to be home. The card waits in their letterbox until they look. But a letter can get lost, or be delivered twice by mistake. A careful friend checks the date on each card, so they do not celebrate twice.

Since the microservices ticket, Spoke took its first strangler-fig step. Monthly rider bills moved out of the Payments module into their own **Billing** service, with its own database. Rentals and Billing now talk like the card. Rentals posts "a ride ended", and Billing reads it when it can. In this ticket you learn how that works. You also find out why Kakashi saw rides go missing and rides billed by mistake, and how the outbox pattern fixes both.

## Concept

### Calling versus telling: sync and async

Rentals could tell Billing about each ended ride with an HTTP call, like the sync calls in [Microservices](/t/module-09-architecture/topic-03-microservices). That is a **synchronous** call: Rentals waits for the answer before it can finish.

So when Billing is slow, ending a ride is slow. When Billing is down, ending a ride fails, and a rider stands at the dock with a bike that will not lock. Two services that must both be up at the same moment are **temporally coupled**.

The other way is **asynchronous**: Rentals writes down "rental-17 ended" in a shared place and moves on. Billing reads it a moment later, or an hour later after a restart. Rentals does not even need to know Billing exists.

```callout jargon
**Synchronous** means the caller waits for the answer. **Asynchronous** means the caller hands the work over and carries on, and the work is done later. **Temporal coupling** means two parts only work when both are running at the same time.
```

The price of async: the answer is not ready straight away, and new failure modes appear. The rest of this ticket is about those.

### Message, producer, consumer, broker

Four words name every part of async messaging.

```callout jargon
A **message** is one packet of data sent from one program to another, such as "rental-17 ended, 700 cents". The **producer** sends it. The **consumer** receives it. The **broker** is the server in the middle that stores messages until consumers take them.
```

Popular brokers are Apache Kafka, RabbitMQ, and cloud services such as Amazon SQS. The producer and consumer only know the broker's address and a name, never each other.

The broker writes messages to disk. So a message survives a consumer crash, and often a broker restart too. That storage is what lets Billing be down for an hour and still catch up.

You met the in-memory version of this idea in [Coordinating threads](/t/module-05-advanced-java/topic-15-coordinating-threads): a `BlockingQueue` between threads. A broker is that queue, moved out into its own server, shared across machines, and written to disk.

### Queues: competing consumers

A **queue** hands each message to **one** consumer. Several consumers can read the same queue. Each message goes to whichever is free, so they share the work.

Spoke's repair jobs fit a queue. "Fix the brakes on B-1042" should go to one mechanic's phone, not to all five. Add a sixth mechanic and the queue simply spreads work over six.

This shape is called **competing consumers**. It scales a slow job by adding more workers. The consumer tells the broker "done" with an **acknowledgement** (an **ack**). Only then does the broker delete the message. If a worker dies before acking, the broker gives the message to another worker.

```callout jargon
An **acknowledgement** (**ack**) is the consumer telling the broker "I handled this message, you can forget it".
```

### Topics: publish and subscribe

A **topic** hands every message to **every** subscriber. This is **publish/subscribe**, or **pub/sub**.

When a ride ends, three teams care. Billing charges the rider. Rewards adds points. Analytics counts rides per station. Rentals publishes one `RideEnded` to the `ride-events` topic, and each of the three gets its own copy.

Next month the safety team wants ride events too. They subscribe, and Rentals changes nothing. That is the big win of pub/sub: new consumers cost the producer no work.

| | Queue | Topic (pub/sub) |
|---|---|---|
| Who gets a message | one consumer | every subscriber |
| Used for | sharing work | spreading news |
| Spoke example | repair jobs | ride events |

### Commands, events and plain messages

Messages come in three kinds, and the name shows which.

- A **command** asks for something to happen: `EndRide`, `ChargeRider`. It has one owner who handles it, and that owner may say no. Name it as an order.
- An **event** says something already happened: `RideEnded`, `PaymentFailed`. It is a fact, so nobody can refuse it. Any number of services may listen. Name it in the **past tense**.
- A **document** message just carries data, such as a nightly copy of the station list.

The tense matters. `RideEnded` tells the reader "do what you like with this fact". `EndRide` tells one reader "do this for me", which couples the sender to that reader. Most messages on a topic should be events.

### Log-based brokers: Kafka

A classic queue deletes a message once it is acked. **Apache Kafka** works differently: a topic is a **log**.

```callout jargon
A **log** here is a list you can only add to at the end, like a diary. Nothing is changed or removed when it is read. Each entry gets a number in order, called its **offset**: 0, 1, 2, and so on.
```

Each consumer keeps its own bookmark: the offset of the next entry it will read. Billing can be at offset 9,000 while Analytics, which is slower, is at offset 7,500. Neither gets in the other's way.

Kafka keeps entries for a set time, the **retention**: seven days unless you configure it. Because nothing is deleted on read, you can **replay**. Analytics found a bug in its station counts? Fix it, move its bookmark back to the start of the week, and recount every ride.

Some topics use **log compaction** instead of a time limit: Kafka keeps only the newest entry for each key. That suits "the latest status of each bike", where old values do not matter.

### Partitions: how a topic scales

One log on one machine has a limit. So Kafka splits a topic into **partitions**: several separate logs. Each partition can live on a different machine, and each has its own offsets.

When the producer sends a message, it gives a **key**. The key's hash picks the partition, so the same key always lands in the same partition. Here is the idea with three partitions. Kafka uses a different hash function, murmur2, but the rule is the same.

```java run
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int partitions = 3;
        List<String> events = List.of("rider-7 started", "rider-8 started", "rider-7 ended",
                "rider-9 started", "rider-8 ended", "rider-9 ended");
        for (String event : events) {
            String key = event.split(" ")[0];                       // the rider id is the key
            int partition = Math.floorMod(key.hashCode(), partitions);
            System.out.println("partition " + partition + " <- " + event);
        }
    }
}
```

It prints:

```text
partition 1 <- rider-7 started
partition 2 <- rider-8 started
partition 1 <- rider-7 ended
partition 0 <- rider-9 started
partition 2 <- rider-8 ended
partition 0 <- rider-9 ended
```

`Math.floorMod` keeps the result between 0 and 2, even when `hashCode()` is negative. Choosing a partition costs O(1) time and O(1) space per message: one hash and one division.

### Consumer groups and offsets

Billing runs two copies of its consumer for speed. Both join one **consumer group** named `billing`. Kafka gives each partition to exactly one member of the group. Watch.

```animate
title: Two billing consumers share three partitions
kind: scene
nodes:
  - { id: rentals, label: Rentals, emoji: "🚲", row: 1, col: 0 }
  - { id: p0, label: partition 0, emoji: "📜", value: "empty", row: 0, col: 2 }
  - { id: p1, label: partition 1, emoji: "📜", value: "empty", row: 1, col: 2 }
  - { id: p2, label: partition 2, emoji: "📜", value: "empty", row: 2, col: 2 }
  - { id: c1, label: billing-1, emoji: "🧾", value: "owns p0, p1", row: 0, col: 4 }
  - { id: c2, label: billing-2, emoji: "🧾", value: "owns p2", row: 2, col: 4 }
steps:
  - say: rider-7's ride starts. The key rider-7 hashes to partition 1, so the event goes there at offset 0.
    send: { from: rentals, to: p1, label: "rider-7 started" }
    set: { p1: "offsets 0" }
  - say: rider-8's start hashes to partition 2. Then rider-7's ride ends, and it joins partition 1 at offset 1, after the start.
    send: [{ from: rentals, to: p2, label: "rider-8 started" }, { from: rentals, to: p1, label: "rider-7 ended" }]
    set: { p2: "offsets 0", p1: "offsets 0, 1" }
  - say: In the billing group, each partition has exactly one owner. billing-1 owns partitions 0 and 1. billing-2 owns partition 2.
    focus: [c1, c2]
  - say: billing-1 reads offsets 0 and 1 of partition 1, in order. Then it commits offset 2, meaning "next, I read offset 2".
    send: { from: p1, to: c1, label: "offsets 0, 1" }
    set: { c1: "p1 committed 2" }
  - say: billing-2 reads rider-8's start, but crashes before it commits. Its committed offset for partition 2 is still 0.
    send: { from: p2, to: c2, label: "offset 0" }
    set: { c2: crashed }
  - say: The group rebalances. billing-1 takes over partition 2 and starts at the committed offset, 0. So rider-8's start is read a second time.
    send: { from: p2, to: c1, label: "offset 0 again" }
    set: { c1: "owns p0, p1, p2" }
```

Three rules come out of this:

- A partition is read by **one** member of a group at a time. So a group can use at most as many consumers as there are partitions. A fourth billing consumer with three partitions would sit idle.
- Another group, such as `rewards`, has its **own** offsets and reads every event too. Groups are how one topic serves many subscribers.
- A crash between "handled it" and "committed the offset" means the message is **read again**. Remember this one.

```callout jargon
A **consumer group** is a set of consumers that share a topic's work. A **rebalance** is the group reshuffling which member owns which partition, when a member joins, leaves or crashes.
```

### Ordering and the partition key

Kafka keeps order **inside one partition only**. Every rider-7 event is in partition 1, so a consumer always sees rider-7's start before rider-7's end. But there is no order between partitions: rider-9's start may be read before rider-7's.

So pick the key so that every message whose order matters shares it. Spoke keys `ride-events` by `riderId`. Why that, and not something else?

- **Order where it matters.** A rider's start, end and payment must be read in order. Keying by rider puts them in one partition.
- **Spread.** 100,000 riders spread evenly over the partitions. Keying by station would put every Riverside ride in one partition: a **hot partition** that one consumer must handle alone.
- **Stability.** A key that never changes keeps a rider's history together.

```callout warning
Never use a random key, or no key, when order matters. Kafka then spreads messages over all partitions, and "ride ended" can be read before "ride started".
```

Two more traps. First, adding partitions later changes `hash % partitions`, so a rider's new events can land in a different partition from the old ones. Pick a generous number at the start. Second, a key that is too coarse, like `"all"`, gives one partition and no scaling at all.

### Delivery guarantees

What happens when something crashes depends on **when the consumer commits its offset**.

| Guarantee | How | After a crash | Spoke example |
|---|---|---|---|
| **At-most-once** | commit first, then handle | the message may be **lost** | fine for a live map dot that is replaced in 10 seconds anyway |
| **At-least-once** | handle first, then commit | the message may come **twice** | the usual choice, and Billing's |
| **Exactly-once** | needs transactions across every part | each message counts once | Kafka can do it for Kafka-to-Kafka work only |

Kafka's "exactly-once" covers reading from Kafka and writing back to Kafka in one Kafka transaction. It cannot reach into your database or a payment provider. Once a side effect leaves Kafka, a crash can still repeat it.

So real systems aim for **effectively once**: at-least-once delivery, plus a consumer that gives the same result when a message comes twice. Nothing is lost, and repeats do no harm.

### Idempotent consumers

```callout jargon
An operation is **idempotent** when doing it twice has the same effect as doing it once. You met this for HTTP in [REST design](/t/module-08-backend/topic-02-rest-design) with the `Idempotency-Key` header.
```

There are three ways to make a consumer idempotent.

1. **Natural idempotency.** Some effects repeat safely. "Set rental-17's status to ENDED" twice leaves it ENDED. "Add 700 cents to the bill" twice adds 1,400, so it is not naturally idempotent.
2. **Upsert by a unique id.** Store one row per event, keyed by its id, with an **upsert**. An upsert inserts the row, or overwrites it if the key exists (`INSERT ... ON CONFLICT` in PostgreSQL, `MERGE` in H2). A repeat writes the same values again.
3. **A processed-event table.** Record each event id you handled, with a primary key. A repeat breaks the key, and you skip it.

The third way needs one rule above all: record the id **in the same transaction** as the effect.

### Syntax: a processed-event table

```sql
CREATE TABLE processed_event (event_id VARCHAR(40) PRIMARY KEY);

-- in ONE transaction:
INSERT INTO processed_event (event_id) VALUES ('evt-1001');  -- a repeat fails here with SQLState 23505
UPDATE rider_bill SET total_cents = total_cents + 700 WHERE rider_id = 'rider-7';
COMMIT;
```

If the insert breaks the primary key, roll back and skip the message. The bill is untouched. If the bill update fails, roll back: the event id is not recorded either, so a retry is still allowed.

Record the id in its own transaction, and a crash in between leaves "evt-1001 handled" with no bill change. Every retry is then skipped, and the ride is never billed.

The table grows by one row per event. Keep ids for longer than any redelivery can happen, say 30 days, then delete older rows.

### Retries, poison messages and dead-letter queues

Sometimes handling fails. If Billing's database blinked, trying again in a moment will work. That is a **transient** failure: retry it, a few times, with a growing wait, as in [REST design](/t/module-08-backend/topic-02-rest-design)'s backoff.

Some messages can never succeed. An event with a bike type Billing has never heard of fails on every try. That is a **poison message**. Retried forever, it blocks its whole partition, because everything behind it waits.

```callout jargon
A **dead-letter queue** (**DLQ**) is a separate queue or topic where a consumer parks messages it gave up on. The main flow moves on, and a person looks at the parked ones later.
```

The usual recipe:

1. Retry a few times with backoff.
2. Still failing? Send the message to the DLQ, such as `ride-events.dlq`, with the error attached. Then commit and move on.
3. Alert someone when the DLQ is not empty.
4. After the fix, replay the DLQ messages into the main topic.

A DLQ nobody watches is a slow way to lose data. Treat a growing DLQ like an error alarm.

### Consumer lag and back-pressure

**Consumer lag** is how far a consumer is behind: the newest offset in a partition minus the consumer's committed offset. It is the first number to watch on any consumer.

Here is the arithmetic for bike GPS pings. At most 2,000 bikes are riding at once, and each pings every 10 seconds. That is up to 2,000 / 10 = 200 pings a second. Suppose one consumer handles 50 a second. Lag then grows by 150 every second: 9,000 behind after one minute.

The fix is 200 / 50 = 4 consumers. A group can only use as many consumers as partitions, so the topic needs at least 4 partitions. Plan partitions for the peak, with room to grow.

```callout jargon
**Back-pressure** is a slow part of a system telling the faster part before it to slow down, instead of drowning.
```

Kafka consumers **pull** messages at their own pace, so a slow consumer never drowns. The log simply holds the backlog, and lag grows. Brokers that **push** messages, such as RabbitMQ, use a **prefetch** limit: at most, say, 20 unacked messages per consumer at a time. Inside one JVM, a bounded `BlockingQueue` does the same: a full queue makes the producer wait.

### Designing an event

An event outlives the code that wrote it. Consumers you never met will read it for years. Give every event:

- **`eventId`**: unique, such as `evt-1001`, so consumers can spot repeats.
- **`eventType`** and **`version`**: `RideEnded`, version 1, so readers know the shape.
- **`occurredAt`**: when it happened, in ISO-8601 UTC, such as `2026-11-18T08:30:00Z`. That is not when it was sent.
- The **facts**: ids, amounts in whole cents, the key you partition by.

```json
{"eventId": "evt-1001", "eventType": "RideEnded", "version": 1, "occurredAt": "2026-11-18T08:30:00Z",
 "rentalId": "rental-17", "riderId": "rider-7", "bikeId": "B-1042", "bikeType": "EBIKE",
 "minutes": 20, "fareCents": 700}
```

The `RideEnded` in your fix is a smaller version. It names the bike-type field `type`, and leaves out `eventType`, `version` and `occurredAt`, so the code stays short.

**Schema evolution** follows the safe-change rules from [API docs, versioning and error design](/t/module-08-backend/topic-12-api-docs-errors). Add new optional fields. Never rename, remove, or change a field's type or meaning. Readers ignore fields they do not know: a **tolerant reader**. For a truly breaking change, publish a new version beside the old one until every consumer has moved. Teams often use Avro or Protobuf with a **schema registry**, a service that refuses a producer's schema change if it would break readers.

**Fat or thin?** A **fat** event carries everything consumers need, like the fare above. A **thin** event carries only ids: "rental-17 ended, ask Rentals for details". Thin events are small, but every consumer then calls Rentals back. That brings back the coupling and load you wanted to remove, and the details may have changed by then. Spoke prefers fat events for facts that do not change after they happen.

### The dual-write problem

Now Kakashi's two bugs. Ending a ride does two writes to two systems: update the rental row in the database, and publish `RideEnded` to the broker. There is no transaction that covers both.

**Commit, then publish.** The commit works. Then the broker hiccups, or the server crashes, before the publish. The ride is ENDED, but no event exists. Billing never hears of it. That is Tuesday's bug.

**Publish, then commit.** The event goes out. Then the commit fails and the update rolls back. Billing charges a ride that, as far as Rentals knows, never ended. That is Thursday's bug.

```callout jargon
The **dual-write problem**: writing to two systems, such as a database and a broker, with no shared transaction. Either write can fail after the other succeeded, so the two drift apart.
```

Retrying does not fix it, because the crash can hit between the two writes. Moving the publish inside a `@TransactionalEventListener`, as in [Transactions](/t/module-08-backend/topic-10-transactions), only narrows the gap. As that ticket warned, a crash right after the commit still loses the event.

### The transactional outbox

The fix: do not write to two systems. Write the event into the **same database**, in the **same transaction** as the rental change. A separate process publishes it later.

```animate
title: A ride event travels through the outbox
kind: scene
nodes:
  - { id: rentals, label: Rentals, emoji: "🚲", value: idle }
  - { id: db, label: Rentals DB, emoji: "🗄️", value: "rental-17 ACTIVE" }
  - { id: relay, label: Outbox relay, emoji: "🔁", row: 1, col: 1 }
  - { id: broker, label: Broker, emoji: "📮", value: up, row: 1, col: 2 }
  - { id: billing, label: Billing, emoji: "🧾", value: "rider-7: 0", row: 1, col: 3 }
steps:
  - say: A rider ends rental-17. Rentals opens one database transaction.
    focus: [rentals]
    set: { rentals: "ending rental-17" }
  - say: In that one transaction it updates the rental and inserts the event as a row in the outbox table. Then it commits both together.
    send: [{ from: rentals, to: db, label: "UPDATE rental" }, { from: rentals, to: db, label: "INSERT outbox evt-1001" }]
    set: { db: "ENDED + evt-1001 unsent", rentals: idle }
  - say: Either both rows exist or neither does. A crash now loses nothing, because the event waits safely in the table.
    focus: [db]
  - say: The relay wakes up and reads unsent outbox rows, oldest first.
    send: [{ from: relay, to: db, label: "SELECT unsent ORDER BY id" }, { from: db, to: relay, label: "evt-1001", back: true }]
  - say: The broker hiccups, and the publish fails. The relay leaves the row unsent and stops. Next run, it tries again.
    send: { from: relay, to: broker, label: "publish evt-1001" }
    set: { broker: down }
  - say: The broker is back. The publish works, and the broker confirms. Only now does the relay mark the row sent.
    send: [{ from: relay, to: broker, label: "publish evt-1001" }, { from: broker, to: relay, label: "ack", back: true }, { from: relay, to: db, label: "sent = TRUE" }]
    set: { broker: up, db: "ENDED + evt-1001 sent" }
  - say: Billing reads evt-1001. In one transaction it records the event id and adds 700 cents. A repeat of evt-1001 would be skipped.
    send: { from: broker, to: billing, label: "evt-1001" }
    set: { billing: "rider-7: 700" }
```

```callout jargon
A **transactional outbox** is a table in a service's own database that holds events waiting to be published. Events are inserted in the same transaction as the business change. A **relay** (or poller) publishes them and marks them sent.
```

### Syntax: the outbox table

```sql
CREATE TABLE outbox (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,  -- the order rows were written
    event_id    VARCHAR(40) NOT NULL UNIQUE,
    topic       VARCHAR(40) NOT NULL,           -- 'ride-events'
    message_key VARCHAR(40) NOT NULL,           -- the partition key: the rider id
    payload     VARCHAR(1000) NOT NULL,         -- the event as JSON
    sent        BOOLEAN DEFAULT FALSE NOT NULL
);
```

`GENERATED ... AS IDENTITY` makes the database number rows 1, 2, 3 as they are inserted. The relay publishes in that order. Many teams add a `created_at` column for cleanup and dashboards.

### The relay: order, marking and cleanup

The relay is a simple loop, run every half second or so:

1. Select unsent rows, `ORDER BY id`.
2. Publish the first one. Wait for the broker's ack.
3. Only then, mark that row `sent = TRUE`.
4. If a publish fails, **stop**. The rest wait for the next run.

Why stop? If row 3 fails and row 4 goes out, rider-7's "ended" may reach the broker before its "started". Stopping keeps the order.

Why mark **after** the ack? Marking first, then crashing before the publish, loses the event for good. Marking after means a crash between ack and mark publishes the row **again** next run. So the outbox gives **at-least-once** delivery, and consumers must be idempotent. That is the price of never losing an event.

Run **one** relay at a time. Two relays reading the same rows would publish each one twice and mix up the order. If you run spare copies for safety, let only one work, for example by locking one row with `FOR UPDATE`, as in [Concurrent writes](/t/module-08-backend/topic-15-concurrent-writes).

Finally, **cleanup**. Sent rows pile up: 5,000 rides in a rush hour is 5,000 rows. A nightly job deletes sent rows older than a few days, in batches. Never delete unsent rows: they are events nobody has published yet.

### Change data capture

There is another way to feed the broker: read the database's own change log.

```callout jargon
**Change data capture** (**CDC**) reads the log a database writes for every committed change (PostgreSQL's write-ahead log, MySQL's binlog) and turns each change into a message.
```

The best-known tool is **Debezium**. It watches the database's log and publishes each committed insert, update and delete to Kafka. Only committed changes appear in the log, so a rolled-back ride never becomes an event.

| | Polling relay | Change data capture |
|---|---|---|
| Delay | up to one polling interval | usually well under a second |
| Load on the database | a query every interval | reads the log, no queries |
| Moving parts | a small loop in your service | a CDC platform to run and watch |
| Event shape | you design it | raw table rows, unless you capture only the outbox table |

The two combine well. Debezium can watch just the outbox table and publish each new row as your designed event. That keeps clean events and drops the polling.

### Eventual consistency for readers

With events, Billing learns about a ride a moment after Rentals. Between the two, they disagree. This is **eventual consistency**: if no new changes come in, every copy ends up agreeing, just not at once.

```callout jargon
**Eventual consistency** means copies of data may differ for a short time after a change, but all of them catch up if you wait.
```

Riders notice this. Ada ends her ride and opens "My bill" at once. The relay polls every half second, and Billing has a small lag. Her ride is not there yet, and she thinks she rode for free, or that Spoke lost her ride.

Ways to handle it:

- Return the fare in the end-ride response itself, so the app shows it straight away.
- Show "Updating..." for rides that ended in the last minute.
- Read the rider's own recent changes from the service that wrote them (**read-your-writes**).

Never design a screen that must read another service's copy straight after a write.

### In an interview

Messaging comes up in almost every system design round. Spot it when you hear "notify other services", "decouple", "spikes in traffic", "audit trail", "process later", or "exactly once".

- **Design an order or payment pipeline**: events between services, an outbox on the writing side, idempotent consumers keyed by event id.
- **How do you keep a database and a message in sync?** Name the dual-write problem, then the transactional outbox or CDC with Debezium.
- **Kafka basics**: topics, partitions, offsets, consumer groups. Ordering only inside a partition, so choose the key carefully. Lag as the health signal.
- **Can you get exactly-once delivery?** Not end to end with side effects. At-least-once plus idempotent consumers gives effectively once.
- **A message keeps failing**: bounded retries with backoff, then a dead-letter queue with an alert and a replay tool.

## Worked example

Here are both of Kakashi's fixes on one in-memory database. The broker is a plain list, and a flag makes it hiccup. First the old way, then the outbox.

```java run
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static final List<String> broker = new ArrayList<>();   // what billing will read
    static boolean brokerUp = true;

    public static void main(String[] args) throws SQLException {
        try (Connection db = DriverManager.getConnection("jdbc:h2:mem:outbox")) {
            try (Statement sql = db.createStatement()) {
                sql.execute("""
                    CREATE TABLE rental (id VARCHAR(20) PRIMARY KEY, status VARCHAR(10) NOT NULL);
                    CREATE TABLE outbox (
                        id       BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                        event_id VARCHAR(40) NOT NULL UNIQUE,
                        payload  VARCHAR(200) NOT NULL,
                        sent     BOOLEAN DEFAULT FALSE NOT NULL);
                    INSERT INTO rental VALUES ('rental-17', 'ACTIVE'), ('rental-18', 'ACTIVE');
                    """);
            }
            brokerUp = false;                                  // the broker hiccups

            System.out.println("== Dual write: commit, then publish");
            endRideThenPublish(db, "rental-17", "evt-1001");

            System.out.println("== Outbox: one transaction, then a relay");
            endRideWithOutbox(db, "rental-18", "evt-1002");
            System.out.println("relay run 1: sent " + relay(db) + ", broker has " + broker);
            brokerUp = true;                                   // the broker is back
            System.out.println("relay run 2: sent " + relay(db) + ", broker has " + broker);
            System.out.println("relay run 3: sent " + relay(db) + ", broker has " + broker);
        }
    }

    static void endRideThenPublish(Connection db, String rentalId, String eventId) throws SQLException {
        update(db, "UPDATE rental SET status = 'ENDED' WHERE id = ?", rentalId);   // auto-commit: saved now
        System.out.println(rentalId + " ENDED and committed");
        try {
            publish(eventId);
        } catch (IllegalStateException e) {
            System.out.println("publish " + eventId + " failed: " + e.getMessage());
            System.out.println("billing will never hear that " + rentalId + " ended");
        }
    }

    static void endRideWithOutbox(Connection db, String rentalId, String eventId) throws SQLException {
        db.setAutoCommit(false);
        try {
            update(db, "UPDATE rental SET status = 'ENDED' WHERE id = ?", rentalId);
            update(db, "INSERT INTO outbox (event_id, payload) VALUES (?, '{\"rentalId\":\"" + rentalId + "\"}')", eventId);
            db.commit();                                       // both rows, or neither
            System.out.println(rentalId + " ENDED, and " + eventId + " waits in the outbox");
        } catch (SQLException e) {
            db.rollback();
            throw e;
        } finally {
            db.setAutoCommit(true);
        }
    }

    static int relay(Connection db) throws SQLException {
        List<Long> ids = new ArrayList<>();
        List<String> eventIds = new ArrayList<>();
        try (Statement sql = db.createStatement();
             ResultSet rows = sql.executeQuery("SELECT id, event_id FROM outbox WHERE sent = FALSE ORDER BY id")) {
            while (rows.next()) {
                ids.add(rows.getLong(1));
                eventIds.add(rows.getString(2));
            }
        }
        int sent = 0;
        for (int i = 0; i < ids.size(); i++) {
            try {
                publish(eventIds.get(i));
            } catch (IllegalStateException e) {
                return sent;                                   // keep the rest for the next run, in order
            }
            update(db, "UPDATE outbox SET sent = TRUE WHERE id = ?", ids.get(i));
            sent++;
        }
        return sent;
    }

    static void publish(String eventId) {
        if (!brokerUp) {
            throw new IllegalStateException("broker unavailable");
        }
        broker.add(eventId);
    }

    static void update(Connection db, String sql, Object value) throws SQLException {
        try (PreparedStatement statement = db.prepareStatement(sql)) {
            statement.setObject(1, value);
            statement.executeUpdate();
        }
    }
}
```

It prints:

```text
== Dual write: commit, then publish
rental-17 ENDED and committed
publish evt-1001 failed: broker unavailable
billing will never hear that rental-17 ended
== Outbox: one transaction, then a relay
rental-18 ENDED, and evt-1002 waits in the outbox
relay run 1: sent 0, broker has []
relay run 2: sent 1, broker has [evt-1002]
relay run 3: sent 0, broker has [evt-1002]
```

Step by step:

1. `endRideThenPublish` is the old code. Auto-commit saves the update at once. The publish then fails, and evt-1001 is gone for good. That is Tuesday's missing ride.
2. `endRideWithOutbox` turns auto-commit off, writes the rental change and the outbox row, and commits once. If the insert failed, `rollback()` would undo the update too. The ride and its event live or die together, which also fixes Thursday's bug.
3. Relay run 1 finds evt-1002, but the broker is down. It publishes nothing, marks nothing, and returns 0. The event is still safe in the table.
4. Relay run 2 publishes evt-1002, then marks it sent.
5. Relay run 3 finds no unsent rows. Nothing is published twice in the normal case.

In production the relay runs on a timer, and the outbox row also holds the topic, the key and the full event. Your fix adds those, plus Billing's idempotent consumer.

## Try it

This consumer remembers event ids in a set. Run it, then break it on purpose.

```java run
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Main {
    record RideEnded(String eventId, String riderId, long fareCents) {}

    static final Set<String> processed = new HashSet<>();
    static final Map<String, Long> bills = new HashMap<>();

    public static void main(String[] args) {
        List<RideEnded> deliveries = List.of(
                new RideEnded("evt-1001", "rider-7", 700),
                new RideEnded("evt-1002", "rider-9", 250),
                new RideEnded("evt-1001", "rider-7", 700));   // the broker delivers evt-1001 again
        for (RideEnded event : deliveries) {
            handle(event);
        }
        System.out.println("bills: " + new TreeMap<>(bills));
    }

    static void handle(RideEnded event) {
        if (!processed.add(event.eventId())) {       // add returns false when the id was already there
            System.out.println("skip " + event.eventId() + ": already billed");
            return;
        }
        bills.merge(event.riderId(), event.fareCents(), Long::sum);
        System.out.println("bill " + event.eventId() + ": " + event.fareCents() + " cents to " + event.riderId());
    }
}
```

1. Delete the `if` block. How much does rider-7 owe now, and how much should they owe?
2. Put it back. Add a fourth delivery, `new RideEnded("evt-1003", "rider-7", 200)`. Is it billed? Why?
3. The set lives in memory. What happens to it when Billing restarts? Why does the real fix keep it in a database table, in the same transaction as the bill?

## Recap

- Async messaging decouples services in time: the producer hands a message to a broker, and consumers read it when they can. Queues share work, and topics (pub/sub) give every subscriber a copy.
- Name events in the past tense. In Kafka, a topic is split into partitions. Order holds only inside a partition, so key by the thing whose order matters, such as `riderId`. Each consumer group keeps its own offsets.
- Delivery is at-most-once, at-least-once or (inside Kafka only) exactly-once. Aim for effectively once: at-least-once plus an idempotent consumer that records event ids in the same transaction as its effect.
- Bounded retries, then a dead-letter queue, keep a poison message from blocking a partition. Watch consumer lag.
- Writing to a database and a broker separately is the dual-write problem. The transactional outbox writes the event in the same transaction, and a relay publishes it in order and marks it sent after the ack.

## Common mistakes

**1. Publishing inside the transaction, before the commit.**

```java nocompile
db.setAutoCommit(false);
updateRental(db, "rental-17", "ENDED");
broker.publish(rideEnded);              // goes out now, whatever happens next
saveFare(db, "rental-17", fareCents);   // fails a check, so everything rolls back
db.commit();
```

```text
Check constraint violation: "CONSTRAINT_8F: "; SQL statement:
broker has [evt-1001 RideEnded rental-17], but rental-17 is ACTIVE
```

A rollback cannot recall a message. Billing charges a ride that never ended. Fix: insert the event into the outbox table inside the transaction, and let the relay publish it after the commit.

**2. Publishing after the commit, with no outbox.**

```java nocompile
@Transactional
public void endRide(String rentalId) {
    rentals.markEnded(rentalId);
}
// called right after endRide returns:
broker.publish(new RideEnded(...));    // a crash or a broker hiccup here loses the event
```

```text
rental-17 ENDED and committed
publish evt-1001 failed: broker unavailable
billing will never hear that rental-17 ended
```

Nothing records that an event is owed, so no retry can ever happen. Fix: the outbox. The row is the record of the debt until the relay pays it.

**3. Marking a row sent before publishing it.**

```java nocompile
markSent(db, row.id());          // saved as sent...
publisher.publish(row.message()); // ...then the broker fails, or the relay crashes
```

Symptom: the outbox says every row was sent, but Billing is missing rides, and nothing will ever retry them. Fix: publish first, and mark the row sent only after the broker's ack. A crash then causes a repeat, which an idempotent consumer absorbs, instead of a loss.

**4. Remembering the event id in a separate transaction.**

```java nocompile
insertProcessedEvent(billingDb, eventId);   // auto-commit: saved straight away
addToBill(billingDb, riderId, fareCents);   // the database blinks here
```

Symptom: the bill update failed, so the broker redelivers. But the event id is already recorded, so every retry says "already billed" and the ride is never charged. Fix: one transaction around both writes. If either fails, both roll back.

**5. Renaming a field in a published event.**

```json
{"eventId": "evt-1002", "riderId": "rider-9", "fare": 250}
```

Billing's reader still looks for `fareCents`. With Jackson 3's default settings, reading the new event into Billing's `record RideEnded(..., long fareCents)` fails:

```text
tools.jackson.databind.exc.MismatchedInputException: Cannot map `null` into type `long` (set `DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES` to 'false' to allow)
```

Every event from the new Rentals version fails, and lands in the dead-letter queue, or worse, blocks the partition. Fix: never rename or remove a field. Add `fare` beside `fareCents` if you must, keep both until every consumer has moved, or publish version 2 as a separate event.

**6. Retrying a poison message forever.**

```java nocompile
while (true) {
    try { handle(message); break; }
    catch (Exception e) { /* try again */ }
}
```

Symptom: consumer lag on one partition climbs without end, and every rider in that partition stops being billed. Fix: a few retries with backoff, then park the message in a dead-letter queue, alert, and move on.
