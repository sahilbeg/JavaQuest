## Hook

Picture a small car park with three spaces. A guard at the gate holds three tokens. Each driver takes a token to get in and hands it back on the way out. When the tokens run out, the next driver waits at the gate.

Now picture a race. Every runner crouches at the line, and nobody moves until the starter fires the gun. Then they all go at once.

Tsunade's ticket needs both ideas. The payment provider allows only three calls from Spoke at a time. And the server must wait for all five station checks before it lets riders in. Locks alone do not do this. This page shows the tools Java built for threads that wait for each other.

## Concept

### Threads that wait for each other

So far, locks have kept threads *apart*: one thread at a time inside. Often you need threads to *wait* for each other instead:

- "Wait until these five jobs are done."
- "Let at most three in at once."
- "Everyone wait here until the whole group arrives."
- "Many may read, but a writer goes alone."

```callout jargon
A **synchronizer** is an object whose job is to make threads wait for each other. `java.util.concurrent` has several ready-made ones: `CountDownLatch`, `Semaphore`, `CyclicBarrier` and `ReadWriteLock`. A `BlockingQueue` works as one too.
```

Each tool below is safe to share between threads. Calls on it also create happens-before links, from the memory model topic, so data handed over through it is seen correctly.

### Syntax: CountDownLatch

```java nocompile
CountDownLatch done = new CountDownLatch(5);        // count starts at 5
done.countDown();                                   // a job finished: count goes down by 1
boolean inTime = done.await(2, TimeUnit.SECONDS);   // wait until the count is 0, at most 2 seconds
```

```callout jargon
A **latch** is a gate that starts closed and opens once, for good. A `CountDownLatch` opens when its count reaches 0. `await` waits for that moment.
```

- `countDown()` never waits. It just lowers the count by 1.
- `await(time, unit)` waits until the count is 0, or until the time runs out. It returns `true` if the count reached 0, and `false` on a timeout.
- `getCount()` tells you the count right now. Extra `countDown()` calls do nothing once it is 0.
- A latch cannot be reset. Once it is open, it stays open. Make a new one for the next job.

```java run
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        List<String> stations = List.of("Riverside", "Old Mill", "Harbor", "Uptown", "Depot");
        Set<String> checked = ConcurrentHashMap.newKeySet();
        CountDownLatch ready = new CountDownLatch(stations.size());

        for (String station : stations) {
            new Thread(() -> {
                try {
                    checked.add(station);
                } finally {
                    ready.countDown();
                }
            }).start();
        }

        boolean allReady = ready.await(2, TimeUnit.SECONDS);
        System.out.println("All stations ready: " + allReady);
        System.out.println("Checked: " + new TreeSet<>(checked));
    }
}
```

This prints `All stations ready: true`, then `Checked: [Depot, Harbor, Old Mill, Riverside, Uptown]`. The checks finish in any order, but `await` waits for all five.

```animate
title: A latch waits for three stations
kind: scene
nodes:
  - { id: server, label: Server start, emoji: "🖥️", value: starting, row: 0, col: 1 }
  - { id: latch, label: CountDownLatch, emoji: "🚧", value: "count 3", row: 1, col: 1 }
  - { id: riverside, label: Riverside check, emoji: "🚲", row: 2, col: 0 }
  - { id: harbor, label: Harbor check, emoji: "🚲", row: 2, col: 1 }
  - { id: uptown, label: Uptown check, emoji: "🚲", row: 2, col: 2 }
steps:
  - say: The server makes a latch with count 3, one for each station check. Then it starts three check threads.
    focus: [latch]
  - say: The server calls await. It sleeps until the count reaches 0, or until 2 seconds pass.
    send: { from: server, to: latch, label: "await(2 s)" }
    set: { server: waiting }
  - say: Harbor finishes first and counts down. The order does not matter.
    send: { from: harbor, to: latch, label: countDown }
    set: { latch: "count 2" }
  - say: Uptown counts down next.
    send: { from: uptown, to: latch, label: countDown }
    set: { latch: "count 1" }
  - say: Riverside is last. The count hits 0 and the latch opens for good.
    send: { from: riverside, to: latch, label: countDown }
    set: { latch: "count 0: open" }
  - say: await returns true, and the server opens its doors to riders.
    send: { from: latch, to: server, label: "true", back: true }
    set: { server: open }
```

### A latch as a start gun

A latch with count 1 works like a starter's gun. Every worker thread calls `start.await()` first. When the main thread calls `start.countDown()`, they all go at once.

```java nocompile
CountDownLatch start = new CountDownLatch(1);
// each worker thread:
if (!start.await(2, TimeUnit.SECONDS)) {   // crouch at the line
    return;                                // no gun within 2 s: give up
}
doTheWork();
// main thread, once every worker is started:
start.countDown();                  // bang: everyone runs
```

Tests use this trick to make many threads hit the same code at the same moment.

### countDown in finally, and always a timeout

If a job throws an exception before it calls `countDown()`, the count never reaches 0. A plain `await()` would then wait forever, and the server would never open.

Two habits prevent that:

- Call `countDown()` in a `finally` block, so a crashed job still counts.
- Use `await(time, unit)`, never plain `await()`. Then a stuck job means "false after 2 seconds", not a frozen server.

### Syntax: Semaphore

```java nocompile
Semaphore permits = new Semaphore(3);   // 3 permits to hand out
permits.acquire();                      // take one; wait if none is free
try {
    callPaymentProvider();
} finally {
    permits.release();                  // give it back, even after an exception
}
```

```callout jargon
A **semaphore** is a counter of **permits**. `acquire()` takes one permit, and waits while none are free. `release()` gives one back. With 3 permits, at most 3 threads can be between acquire and release at once.
```

```callout analogy
The car park guard with three tokens is a semaphore. Unlike a lock, a semaphore does not care which thread returns a token. It only counts them.
```

A `Semaphore(1)` lets one thread in at a time, like a lock. A bigger number limits how many share a resource: payment calls, database connections, or bikes on one truck.

```animate
title: Three payment slots
kind: scene
nodes:
  - { id: r1, label: Rider 1, emoji: "🙋", value: tapped, row: 0, col: 0 }
  - { id: r2, label: Rider 2, emoji: "🙋", value: tapped, row: 0, col: 1 }
  - { id: r3, label: Rider 3, emoji: "🙋", value: tapped, row: 0, col: 2 }
  - { id: r4, label: Rider 4, emoji: "🙋", value: tapped, row: 0, col: 3 }
  - { id: gate, label: Semaphore, emoji: "🎟️", value: "3 free", row: 1, col: 1 }
  - { id: provider, label: Payment provider, emoji: "💳", row: 2, col: 1 }
steps:
  - say: The gate starts with 3 permits. Four riders tap Pay at the same moment.
    focus: [r1, r2, r3, r4]
  - say: Riders 1, 2 and 3 each acquire a permit. Now none are free.
    send:
      - { from: r1, to: gate, label: acquire }
      - { from: r2, to: gate, label: acquire }
      - { from: r3, to: gate, label: acquire }
    set: { gate: "0 free", r1: paying, r2: paying, r3: paying }
  - say: Their three calls reach the provider together. That is the most the provider allows.
    send: { from: gate, to: provider, label: "3 calls" }
  - say: Rider 4 asks for a permit too. None is free, so acquire waits.
    send: { from: r4, to: gate, label: acquire }
    set: { r4: waiting }
    focus: [r4]
  - say: Rider 1's call finishes. The finally block releases the permit.
    send: { from: r1, to: gate, label: release }
    set: { gate: "1 free", r1: done }
  - say: Rider 4 takes that permit straight away, and its call goes through.
    send: { from: gate, to: provider, label: "1 call" }
    set: { gate: "0 free", r4: paying }
```

### Syntax: tryAcquire with a timeout

```java nocompile
if (permits.tryAcquire(200, TimeUnit.MILLISECONDS)) {
    try {
        callPaymentProvider();
    } finally {
        permits.release();
    }
} else {
    showMessage("Payments are busy, please try again");
}
```

`tryAcquire(time, unit)` waits at most that long. It returns `true` with a permit, or `false` without one. Only release a permit you really got: the `try` and `finally` sit *inside* the `if`.

### Fair or not

`new Semaphore(3)` is **non-fair**. When a permit comes back, any thread may grab it, even one that just arrived. A thread that has waited a long time can lose again and again.

`new Semaphore(3, true)` is **fair**: waiting threads get permits in the order they asked. Fairness is a little slower, so use it when no rider may be skipped for long.

One catch: the plain `tryAcquire()`, with no time, jumps the queue even on a fair semaphore. The timed `tryAcquire(time, unit)` respects fairness.

### Semaphores and virtual threads

Virtual threads, from the virtual threads topic, are cheap. You can start thousands of them. The payment provider is not cheap: it still takes only three calls at once.

So do not limit virtual threads with a small pool. Give every rider a virtual thread, and guard the scarce thing with a `Semaphore`:

```java nocompile
Semaphore providerSlots = new Semaphore(3);
for (String rentalId : rentalIds) {
    Thread.ofVirtual().start(() -> payWithSlot(providerSlots, rentalId));
}
```

Thousands of riders can wait cheaply at the semaphore. Only three at a time reach the provider. The Try it section lets you watch this happen.

### Syntax: CyclicBarrier

```java nocompile
CyclicBarrier allLoaded = new CyclicBarrier(3, () -> System.out.println("all loaded"));
allLoaded.await(2, TimeUnit.SECONDS);   // wait here until all 3 threads arrive
```

```callout jargon
A **barrier** is a meeting point. Each thread calls `await` and stops there. When the last of the group arrives, they all go on together. **Cyclic** means it resets by itself, ready for the next round.
```

The optional action runs once per round, when the last thread arrives. Three rebalancing trucks use one to leave Depot together, round after round:

```java run
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        AtomicInteger round = new AtomicInteger();
        CyclicBarrier allLoaded = new CyclicBarrier(3,
                () -> System.out.println("Round " + round.incrementAndGet() + ": all 3 trucks loaded, leave Depot"));
        Thread[] trucks = new Thread[3];
        for (int t = 0; t < trucks.length; t++) {
            trucks[t] = new Thread(() -> {
                try {
                    for (int r = 0; r < 2; r++) {
                        // load bikes for this round here
                        allLoaded.await(2, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
                    System.out.println("A truck gave up: " + e);
                }
            });
            trucks[t].start();
        }
        for (Thread truck : trucks) {
            truck.join();
        }
    }
}
```

It prints `Round 1: all 3 trucks loaded, leave Depot`, then the same for round 2. A latch counts *events* and opens once. A barrier waits for *threads*, and it can be used again.

`await` also returns your **arrival index**: `parties - 1` for the first thread to arrive, and 0 for the last one. You can use it to pick one thread for a job.

### When a barrier breaks

A barrier is all or nothing. If one waiting thread times out or is interrupted, the barrier **breaks** for everyone.

```callout jargon
A **broken barrier** is one that gave up on the current round. The thread that timed out gets a `TimeoutException`. Every other waiting thread gets a `BrokenBarrierException`. Any later `await` fails straight away with `BrokenBarrierException`.
```

A broken barrier stays broken. Nobody can retry until some thread calls `reset()`, which starts a fresh round.

```java run
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CyclicBarrier allLoaded = new CyclicBarrier(3);
        AtomicReference<String> truck1 = new AtomicReference<>();
        Thread first = new Thread(() -> truck1.set(meet(allLoaded, 5_000)));
        first.start();
        String truck2 = meet(allLoaded, 300);
        first.join();

        System.out.println("Truck 1: " + truck1.get());
        System.out.println("Truck 2: " + truck2);
        System.out.println("Truck 3, arriving late: " + meet(allLoaded, 5_000));
        allLoaded.reset();
        System.out.println("Broken after reset: " + allLoaded.isBroken());
    }

    static String meet(CyclicBarrier barrier, long millis) {
        try {
            int index = barrier.await(millis, TimeUnit.MILLISECONDS);
            return "went on, arrival index " + index;
        } catch (TimeoutException e) {
            return "timed out";
        } catch (BrokenBarrierException e) {
            return "barrier broken";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "interrupted";
        }
    }
}
```

It prints:

```
Truck 1: barrier broken
Truck 2: timed out
Truck 3, arriving late: barrier broken
Broken after reset: false
```

Only 2 of 3 trucks came. Truck 2 gave up after 300 ms and broke the barrier, so truck 1 was woken with `BrokenBarrierException`. Truck 3 arrived later and failed at once. Only `reset()` made the barrier usable again.

### Syntax: ReadWriteLock

```java nocompile
ReadWriteLock lock = new ReentrantReadWriteLock();

lock.readLock().lock();      // many readers may hold this at once
try {
    readPrices();
} finally {
    lock.readLock().unlock();
}

lock.writeLock().lock();     // a writer holds this alone
try {
    changePrices();
} finally {
    lock.writeLock().unlock();
}
```

A `ReadWriteLock` is a pair of locks. Many threads may hold the read lock at the same time. The write lock is only given when nobody holds either lock. While a writer holds it, every reader waits.

That fits a price board: thousands of riders read prices, and the rush-hour job changes them twice a day. With one plain lock, readers would queue behind each other for no reason.

When does it help? Only when reads far outnumber writes **and** each read takes some time, such as copying a map. For tiny reads, a plain lock or a `ConcurrentHashMap` is simpler and often faster.

```callout warning
A thread holding the read lock can never get the write lock. Java waits for every reader to leave, including that thread itself, so it waits forever. Unlock the read lock first, then take the write lock.
```

The other way round is allowed. While you hold the write lock, you may take the read lock, then unlock the write lock. This is called **downgrading**. You keep reading the data you just wrote, and other readers can come in again.

### wait and notify: know them when you see them

Older code coordinates threads with methods that every object has: `wait()`, `notify()` and `notifyAll()`.

```java nocompile
synchronized (this) {
    while (queue.size() == capacity) {
        wait();            // let go of the lock and sleep until notified
    }
    queue.add(item);
    notifyAll();           // wake the threads waiting on this object
}
```

- They only work inside `synchronized` on the same object, or Java throws `IllegalMonitorStateException`.
- Always check the condition in a `while` loop, never an `if`. A thread can wake up when nothing has changed.

`Condition` is the same idea for a `ReentrantLock`: `lock.newCondition()`, then `await()` and `signal()`.

You should recognise these, and interviewers may ask you to build a bounded buffer with them. In real code, prefer `BlockingQueue` and the tools on this page. They are tested by millions of programs, and they are much harder to get wrong.

### BlockingQueue: producer and consumer, again

A `BlockingQueue`, from the concurrent collections topic, is a synchronizer too. `ArrayBlockingQueue(capacity)` is a **bounded buffer**: a queue with a size limit.

- `put` waits while the queue is full. `take` waits while it is empty.
- `offer(item, time, unit)` waits at most that long for space, and returns `false` if the queue is still full. `poll(time, unit)` waits at most that long for an item, and returns `null` on a timeout.
- `add(item)` does not wait at all. On a full `ArrayBlockingQueue` it throws `IllegalStateException: Queue full`.

The kiosk below sends a last item, `"DONE"`, to say it has finished. That trick is called a **poison pill**: a special item that tells the consumer to stop.

```java run
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<String> rides = new ArrayBlockingQueue<>(2);

        Thread kiosk = new Thread(() -> {
            try {
                for (int i = 1; i <= 4; i++) {
                    rides.put("rental-" + i);
                }
                rides.put("DONE");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        kiosk.start();

        while (true) {
            String ride = rides.poll(2, TimeUnit.SECONDS);
            if (ride == null) {
                System.out.println("No ride for 2 seconds, billing worker gives up");
                break;
            }
            if (ride.equals("DONE")) {
                System.out.println("Kiosk closed, billing worker stops");
                break;
            }
            System.out.println("Billing " + ride);
        }
        kiosk.interrupt();
        kiosk.join();
    }
}
```

The kiosk can only get two rides ahead, because the queue holds two. It prints `Billing rental-1` to `Billing rental-4`, then `Kiosk closed, billing worker stops`. If the kiosk ever went quiet for 2 seconds, `poll` would return `null` and the worker would give up. Then `kiosk.interrupt()` frees the kiosk if it is stuck in `put`, so `join` can never hang.

### Always use a timeout

Every waiting method has a version with a time limit. Use it:

| Plain (can wait forever) | With a timeout |
|---|---|
| `latch.await()` | `latch.await(2, TimeUnit.SECONDS)` |
| `semaphore.acquire()` | `semaphore.tryAcquire(200, TimeUnit.MILLISECONDS)` |
| `queue.take()` | `queue.poll(500, TimeUnit.MILLISECONDS)` |
| `barrier.await()` | `barrier.await(2, TimeUnit.SECONDS)` |
| `future.get()` | `future.get(2, TimeUnit.SECONDS)` |

Without a limit, one stuck thread can freeze a whole server at rush hour. With one, you get `false`, `null` or a `TimeoutException`, and your code decides what to do: retry, skip, or tell the rider.

A barrier is the exception to "just retry". Its timeout breaks the barrier for the whole group, so every thread must handle `BrokenBarrierException`, and someone must call `reset()` before the next round.

### What it costs

- Each call (`countDown`, `acquire`, `release`, `put`, `take`) is O(1) time.
- A latch or semaphore uses O(1) memory, plus a small entry for each thread that is waiting.
- An `ArrayBlockingQueue` uses O(capacity) memory, fixed when you create it. That limit is the point: a fast producer cannot fill all your memory.
- A waiting platform thread keeps its memory while it waits. A waiting virtual thread is cheap, which is why "many virtual threads plus one semaphore" scales well.

### In an interview

Listen for these words, and reach for the matching tool:

- **"wait until N things finish"** or **"start all at once"**: `CountDownLatch`.
- **"at most K at a time"**, **"rate limit"**, **"connection pool"**: `Semaphore`.
- **"all threads meet each round"**: `CyclicBarrier`.
- **"many readers, few writers"**: `ReadWriteLock`.
- **"producer and consumer"**, **"bounded buffer"**: `BlockingQueue`, or `wait`/`notifyAll` if asked to build it yourself.

Classic problems:

- **Print in order:** three threads must print first, second, third. Use two latches, as in the worked example below.
- **Print odd and even numbers with two threads, taking turns:** two semaphores that hand a single permit back and forth.
- **Build a bounded blocking queue:** `synchronized` with `wait` and `notifyAll` in `while` loops, or a lock with two `Condition`s.
- **A rate-limited worker pool:** many workers, one `Semaphore` around the scarce call, `tryAcquire` with a timeout.
- **Dining philosophers:** five threads, five forks. Avoid deadlock with a fixed lock order, or a semaphore that lets only four sit down.

## Worked example

Rental rental-17 has three steps, each run by its own thread: take payment, unlock bike B-1042, send the receipt. They must happen in that order, even though the threads start in the *opposite* order.

```java run
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch paid = new CountDownLatch(1);
        CountDownLatch unlocked = new CountDownLatch(1);

        Thread receipt = new Thread(() -> {
            waitFor(unlocked);
            System.out.println("3. Receipt sent for rental-17");
        });
        Thread unlock = new Thread(() -> {
            waitFor(paid);
            System.out.println("2. Bike B-1042 unlocked");
            unlocked.countDown();
        });
        Thread payment = new Thread(() -> {
            System.out.println("1. Payment of 100 cents taken");
            paid.countDown();
        });

        receipt.start();
        unlock.start();
        payment.start();
        receipt.join();
    }

    static void waitFor(CountDownLatch latch) {
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Gave up waiting");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

Line by line:

1. Two latches, each with count 1: `paid` opens when payment is done, `unlocked` opens when the bike is free.
2. The receipt thread starts first, but it waits on `unlocked`.
3. The unlock thread waits on `paid`. When it opens, it unlocks the bike and opens `unlocked`.
4. The payment thread waits for nothing. It takes the payment and opens `paid`.
5. `waitFor` uses a 2-second timeout. If a step never comes, the thread gives up with an exception instead of hanging.
6. `main` joins the receipt thread, the last step, so the program ends only after all three lines.

It always prints:

```
1. Payment of 100 cents taken
2. Bike B-1042 unlocked
3. Receipt sent for rental-17
```

The latches also carry the memory model's happens-before promise. Whatever the payment thread wrote before `countDown()` is visible to the unlock thread after `await()`.

## Try it

Three hundred riders each get a virtual thread, and all of them pay through one `Semaphore(3)`. `accumulateAndGet(value, Math::max)` keeps the biggest number seen, safely. On a real run it printed `Payment calls: 300` and `Most at once: 3`. It can never print more than 3.

```java run
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Semaphore permits = new Semaphore(3);
        AtomicInteger inside = new AtomicInteger();
        AtomicInteger most = new AtomicInteger();
        Thread[] riders = new Thread[300];
        for (int i = 0; i < riders.length; i++) {
            riders[i] = Thread.ofVirtual().start(() -> {
                try {
                    permits.acquire();
                    try {
                        most.accumulateAndGet(inside.incrementAndGet(), Math::max);
                        Thread.sleep(5);
                        inside.decrementAndGet();
                    } finally {
                        permits.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        for (Thread t : riders) {
            t.join();
        }
        System.out.println("Payment calls: " + riders.length);
        System.out.println("Most at once: " + most.get());
    }
}
```

Now change `new Semaphore(3)` to `new Semaphore(1)`. `Most at once` drops to 1, and the run takes longer. Try `new Semaphore(10)` next. Then remove the `permits.acquire()` and `permits.release()` lines, and watch the number jump far past 3.

## Recap

- `CountDownLatch` waits until a count of events reaches 0. Use it to wait for N jobs, or as a start gun. Count down in `finally`.
- `Semaphore` limits how many threads use something at once. Release in `finally`, and only release a permit you got.
- `CyclicBarrier` makes a group of threads meet, round after round. `ReadWriteLock` lets many readers in at once, or one writer alone.
- `wait`/`notify` and `Condition` are the old low-level tools. Prefer `BlockingQueue` and the synchronizers.
- Always wait with a timeout, so one stuck thread cannot freeze the server.

## Common mistakes

**1. Releasing the permit outside finally.** Permits leak away until every call waits.

```java
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Main {
    static final Semaphore permits = new Semaphore(3);

    static void pay(String rentalId) throws InterruptedException {
        permits.acquire();
        if (rentalId.isEmpty()) {
            throw new IllegalArgumentException("no rental id");
        }
        permits.release();
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            try {
                pay("");
            } catch (IllegalArgumentException e) {
                System.out.println("Payment failed: " + e.getMessage());
            }
        }
        System.out.println("Free permits: " + permits.availablePermits());
        System.out.println("Got a permit: " + permits.tryAcquire(100, TimeUnit.MILLISECONDS));
    }
}
```

It prints `Payment failed: no rental id` three times, then `Free permits: 0` and `Got a permit: false`. Three failures used up every permit. With plain `acquire()`, the next rider would wait forever. Fix: put the work in `try` and `permits.release()` in `finally`.

**2. Waiting on a latch with no timeout.** One crashed job freezes startup.

```java nocompile
new Thread(() -> {
    checkStation();        // throws, so the next line never runs
    ready.countDown();
}).start();
ready.await();             // waits forever
```

There is no error message. The program just stops making progress. Fix: call `countDown()` in `finally`, and wait with `ready.await(2, TimeUnit.SECONDS)`, which returns `false` when time runs out.

**3. Releasing a permit you never got.** The semaphore grows extra permits.

```java
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Semaphore permits = new Semaphore(1);
        permits.acquire();               // another call is busy
        boolean got = false;
        try {
            got = permits.tryAcquire(50, TimeUnit.MILLISECONDS);
        } finally {
            permits.release();           // runs even though got is false
        }
        System.out.println("Got a permit: " + got);
        permits.release();               // the busy call finishes
        System.out.println("Permits now: " + permits.availablePermits());
    }
}
```

It prints `Got a permit: false`, then `Permits now: 2`. A gate built for 1 call now lets 2 through, and nothing warns you. Fix: release only inside `if (permits.tryAcquire(...)) { try { ... } finally { permits.release(); } }`.

**4. Taking the write lock while holding the read lock.** The thread waits for itself.

```java
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        lock.readLock().lock();
        try {
            boolean upgraded = lock.writeLock().tryLock(100, TimeUnit.MILLISECONDS);
            System.out.println("Got the write lock while reading: " + upgraded);
        } finally {
            lock.readLock().unlock();
        }
    }
}
```

It prints `Got the write lock while reading: false`. With plain `writeLock().lock()`, the thread would wait forever. Fix: unlock the read lock first, take the write lock, then check the data again, because it may have changed in between.
