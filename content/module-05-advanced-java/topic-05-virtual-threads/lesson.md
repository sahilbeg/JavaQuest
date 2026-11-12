## Hook

Picture a restaurant with 8 waiters. Each waiter takes an order, walks to the kitchen and stands there until the food is ready. With 8 waiters, only 8 tables get served at a time. Everyone else waits at the door.

A smarter waiter drops the order at the kitchen and serves another table while the food cooks. The same 8 waiters can now look after hundreds of tables.

Theo's ticket has the first problem. On marathon day, 10,000 riders pay at once. Each payment waits on the slow payment service. The fixed pool from SPK-0503 has a few threads, and each one stands still while it waits. Virtual threads are the smarter waiters.

## Concept

### Platform threads

Every thread you made so far is a **platform thread**. Java asks the operating system for one real thread for each `Thread` object.

```callout jargon
The **operating system** (OS) is the program that runs your computer, such as macOS, Windows or Linux. An **OS thread** is a thread the OS creates and schedules. A **platform thread** is a Java thread that holds one OS thread for its whole life.
```

OS threads are expensive. Each one reserves memory for its stack, often about a megabyte. OS threads cost much more memory, so you can usually run far fewer of them.

That is why the executors topic used a fixed pool. A small pool shares a few platform threads among many tasks. The catch is waiting. When a pooled thread calls the payment service, it sits idle until the answer comes back. The other riders queue behind it.

### Virtual threads

A **virtual thread** is a cheap thread that the JVM manages itself. It is still a `Thread` object, with `start`, `join` and a name. Under the hood it works differently.

- The JVM keeps a few platform threads, about one per CPU core. These are called **carrier threads**.
- A virtual thread runs by riding on a carrier thread.
- When the virtual thread waits, for example in `Thread.sleep` or a network call, the JVM **parks** it. Parking means it gets lifted off its carrier and set aside.
- The carrier is now free to run another virtual thread.
- When the wait ends, the JVM puts the virtual thread back on any free carrier.

```callout analogy
Carrier threads are the few waiters. Virtual threads are the table orders. An order that waits for the kitchen does not keep a waiter standing still.
```

A virtual thread uses a small amount of memory that grows only when needed. You can start far more virtual threads than platform threads.

```mermaid
flowchart LR
    V1[virtual thread 1] -->|runs on| C1[carrier 1]
    V2[virtual thread 2] -.->|parked while waiting| P[(parked)]
    V3[virtual thread 3] -->|runs on| C2[carrier 2]
    P -.->|wait is over| C1
```

### Syntax: Thread.ofVirtual().start

```java nocompile
Thread t = Thread.ofVirtual().start(() -> System.out.println("hi"));
t.join();
```

- `Thread.ofVirtual()` gives you a **builder**, an object that sets up a thread step by step.
- `.start(task)` creates the virtual thread and starts it right away.
- `join()` waits for it, exactly as with a platform thread.

You can name it on the builder too:

```java nocompile
Thread t = Thread.ofVirtual().name("payer-7").start(() -> pay(7));
```

### Syntax: Thread.startVirtualThread

```java nocompile
Thread t = Thread.startVirtualThread(() -> System.out.println("hi"));
```

This is a shortcut for `Thread.ofVirtual().start(...)`. Use it when you need no name.

### Syntax: isVirtual

```java nocompile
boolean v = Thread.currentThread().isVirtual();
```

`isVirtual()` returns `true` on a virtual thread and `false` on a platform thread. The `main` thread is a platform thread.

```java run
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("main is virtual: " + Thread.currentThread().isVirtual());

        Thread t = Thread.ofVirtual().name("payer-1").start(() -> {
            Thread me = Thread.currentThread();
            System.out.println(me.getName() + " is virtual: " + me.isVirtual());
        });
        t.join();

        Thread quick = Thread.startVirtualThread(() -> System.out.println("shortcut works"));
        quick.join();
    }
}
```

### Syntax: newVirtualThreadPerTaskExecutor

Most of the time you do not create threads by hand. You use an executor, as in the executors topic.

```java nocompile
try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
    pool.submit(() -> pay(1));
    pool.submit(() -> pay(2));
}
```

- This executor starts a brand new virtual thread for every task you submit.
- It has no fixed size, so tasks never queue for a free thread.
- The `try (...)` is **try-with-resources** from the exceptions topic. When the block ends, Java calls `close()` on the executor.
- `close()` waits until every submitted task has finished, then shuts the executor down.

```callout tip
Do not pool virtual threads. They are cheap, so you make a fresh one per task and throw it away after. A pool exists to save expensive threads.
```

`submit` still returns a `Future`, so `get()` works the same as before:

```java nocompile
Future<String> receipt = pool.submit(() -> "paid r1");
String line = receipt.get();   // "paid r1"
```

### Counting from many threads

When 10,000 tasks each add 1 to a plain `int`, updates get lost. You saw that race in the synchronization topic. An `AtomicInteger` from `java.util.concurrent.atomic` is a counter whose `incrementAndGet()` is safe from many threads at once. The next topic covers it fully.

### Syntax: AtomicInteger

```java nocompile
AtomicInteger done = new AtomicInteger();   // starts at 0
done.incrementAndGet();                     // adds 1 safely, returns the new value
int total = done.get();                     // reads the current value
```

`incrementAndGet()` adds 1 in one safe step, and `get()` reads the value.

```java run
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        AtomicInteger done = new AtomicInteger();
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10_000; i++) {
                pool.submit(() -> {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        return;
                    }
                    done.incrementAndGet();
                });
            }
        }
        System.out.println("Completed tasks: " + done.get());
    }
}
```

Ten thousand threads each sleep 10 milliseconds. Because sleeping virtual threads are parked, they all wait at the same time. The program prints `Completed tasks: 10000` and ends quickly.

### When virtual threads help

Virtual threads help when tasks spend most of their time **waiting**. Waiting is called **blocking** or **I/O** (input and output): network calls, database queries, reading files, `Thread.sleep`.

They do not help with **CPU-heavy work**, such as counting primes or sorting a huge list. That work keeps a carrier busy the whole time. You still have only one carrier per core, so it runs no faster than a fixed pool.

| Task | Virtual threads help? |
|---|---|
| Waiting on the payment service | Yes |
| Reading telemetry from 5,000 dock sensors over the network | Yes |
| Computing a fare report with heavy maths | No |

### Pinning, in one line

```callout warning
A virtual thread that waits inside a `synchronized` block can get **pinned** on Java 21. Pinned means it stays stuck to its carrier and blocks it. Keep slow waits outside `synchronized` code.
```

## Worked example

Here is a small version of the ticket. Each rider pays through a slow service, and every rider gets their own virtual thread.

```java run
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    static String pay(String rider) throws InterruptedException {
        Thread.sleep(5);
        return "paid " + rider;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        List<String> riders = List.of("r1", "r2", "r3");
        List<Future<String>> receipts = new ArrayList<>();

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (String rider : riders) {
                receipts.add(pool.submit(() -> pay(rider)));
            }
        }

        for (Future<String> receipt : receipts) {
            System.out.println(receipt.get());
        }
    }
}
```

Line by line:

1. `pay` stands in for the slow payment service. It sleeps 5 ms, then returns a receipt line.
2. `receipts` will hold one `Future` per rider, in the same order as `riders`.
3. The `try (...)` opens a virtual-thread executor.
4. The loop submits one task per rider. Each task runs on its own new virtual thread.
5. The lambda throws `InterruptedException`, so Java treats it as a `Callable`. That is fine, because `submit` accepts a `Callable`.
6. When the `try` block ends, `close()` waits for all three tasks.
7. The second loop reads the futures in list order. The output is always `paid r1`, `paid r2`, `paid r3`, whichever thread finished first.

## Try it

Change the count to 100,000, then swap in `newFixedThreadPool(8)` with the count back at 10,000 (about 12 seconds). The virtual version finishes in about the same time. The fixed pool takes much longer, because only 8 riders wait at once.

```java run
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        int riders = 10_000;
        AtomicInteger paid = new AtomicInteger();
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < riders; i++) {
                pool.submit(() -> {
                    try {
                        Thread.sleep(10);
                        paid.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
        System.out.println("Riders paid: " + paid.get());
    }
}
```

## Recap

- A platform thread holds one expensive OS thread. A virtual thread is a cheap thread that the JVM parks while it waits.
- Start one with `Thread.ofVirtual().start(task)` or `Thread.startVirtualThread(task)`. Check with `isVirtual()`.
- `Executors.newVirtualThreadPerTaskExecutor()` gives each task a new virtual thread. Use it in try-with-resources so `close()` waits for all tasks.
- Virtual threads help with waiting (I/O). They do not speed up CPU-heavy work.
- Keep slow waits out of `synchronized` blocks to avoid pinning.

## Common mistakes

### 1. Forgetting to wait for the tasks

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        AtomicInteger paid = new AtomicInteger();
        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < 1000; i++) {
            pool.submit(() -> {
                try { Thread.sleep(5); } catch (InterruptedException e) { return; }
                paid.incrementAndGet();
            });
        }
        System.out.println("Paid: " + paid.get());
    }
}
```

You see something like `Paid: 0`, because `main` prints before the tasks finish. Virtual threads are also **daemon threads**, which means the JVM does not wait for them before it exits. Fix it by opening the executor in `try (ExecutorService pool = ...) { ... }` and printing after the block.

### 2. Expecting virtual threads to speed up maths

```java nocompile
try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 10_000; i++) {
        pool.submit(() -> countPrimesBelow(5_000_000));
    }
}
```

This runs no faster than a fixed pool of one thread per core. The tasks never wait, so nothing gets parked. Use `Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())` for CPU-heavy work.

### 3. Calling isVirtual on the wrong thread

```java
public class Main {
    public static void main(String[] args) throws InterruptedException {
        Thread t = Thread.startVirtualThread(() -> System.out.println("working"));
        t.join();
        System.out.println(Thread.currentThread().isVirtual());
    }
}
```

This prints `working` then `false`. `Thread.currentThread()` means the thread running that line, and that line runs on `main`. Call `isVirtual()` inside the lambda, or call `t.isVirtual()` on the thread object, which prints `true`.
