## Hook

Picture a pizza shop that hires a new cook for every single order. Each cook needs an apron, a locker and a quick training. Then the cook makes one pizza and goes home. On a busy Friday the shop fills with cooks and runs out of lockers.

A smart shop keeps four cooks. Orders wait on a rail, and each free cook grabs the next one. That is exactly what Shizune's ticket needs. Your sensor check starts a new thread for every dock, and with 60 docks the kiosk runs out of memory.

## Concept

### Why one thread per task is costly

A thread is not free. Each one gets its own **stack**, a block of memory that holds the method calls it is running. Starting a thread also asks the operating system for work. Ending it throws all of that away.

For 12 docks this cost hardly matters. For 60 docks, every few seconds, at rush hour, it adds up fast.

```callout jargon
A **thread pool** is a fixed group of threads that stay alive and run task after task. A **task** is one piece of work, such as "read the sensor on dock Harbor-3".
```

### ExecutorService

Java gives you thread pools through the interface `ExecutorService` in the package `java.util.concurrent`. You hand it tasks. It decides which pool thread runs each one.

```mermaid
flowchart LR
  T1[task 1] --> Q[waiting line]
  T2[task 2] --> Q
  T3[task 3] --> Q
  Q --> W1[pool thread 1]
  Q --> W2[pool thread 2]
```

Tasks wait in a line, called a **queue**. Each free pool thread takes the next task from the front.

### Syntax: Executors.newFixedThreadPool

```java nocompile
ExecutorService pool = Executors.newFixedThreadPool(4);
```

- `Executors` is a helper class with ready-made pools.
- `newFixedThreadPool(4)` makes a pool with exactly 4 threads.
- 60 tasks still work. They just wait their turn.

### Syntax: submit with a Runnable

```java nocompile
pool.submit(() -> System.out.println("checking Harbor-1"));
```

A `Runnable` from the threads topic takes nothing and gives nothing back. `submit` puts it in the queue and returns straight away.

### Syntax: shutdown and awaitTermination

```java nocompile
pool.shutdown();
pool.awaitTermination(5, TimeUnit.SECONDS);
```

- `shutdown()` says "take no new tasks". Tasks already in the queue still run.
- `awaitTermination(5, TimeUnit.SECONDS)` waits until every task has finished, or until 5 seconds pass.

Pool threads stay alive and wait for more work. If you never shut the pool down, your program never ends.

### Syntax: try-with-resources on ExecutorService

```java nocompile
try (ExecutorService pool = Executors.newFixedThreadPool(4)) {
    pool.submit(() -> System.out.println("checking Harbor-1"));
}
```

You met try-with-resources with files in module 3. Since Java 19, so in your Java 21, an `ExecutorService` works the same way. At the closing brace Java calls `close()`. That method shuts the pool down and **waits** for every task to finish.

```java run
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            for (int dock = 1; dock <= 6; dock++) {
                String id = "Harbor-" + dock;
                pool.submit(() -> System.out.println(id + " on " + Thread.currentThread().getName()));
            }
        }
        System.out.println("all docks checked");
    }
}
```

Six tasks run on only two threads, named like `pool-1-thread-1` and `pool-1-thread-2`. The order of the lines changes between runs. The last line always comes last, because `close()` waited.

### Syntax: Callable and Future

```java nocompile
Callable<String> task = () -> "Harbor-1: ok";
Future<String> future = pool.submit(task);
String result = future.get();
```

- A `Callable<String>` is like a `Runnable` that **returns** a value, here a `String`. Unlike `run()`, `call()` may throw a checked exception.
- `submit` gives back a `Future<String>`. A **future** is a receipt for a result that may not exist yet.
- `get()` waits until the task is done, then hands you the result.

```callout analogy
A `Future` is the buzzer a café gives you. You can sit down and do other things. When you call `get()`, you walk to the counter and wait there until your coffee is ready.
```

`get()` can throw two **checked exceptions**: `InterruptedException` and `ExecutionException`. So the method that calls it must catch them or declare them with `throws`.

### Syntax: invokeAll

```java nocompile
List<Callable<String>> tasks = List.of(() -> "Old Mill-1: ok", () -> "Old Mill-2: ok");
List<Future<String>> futures = pool.invokeAll(tasks);
```

`invokeAll` submits a whole list of callables and waits until all are done. The futures come back in the same order as the tasks.

### Syntax: an exception inside a task

```java nocompile
try {
    String line = future.get();
} catch (ExecutionException e) {
    Throwable real = e.getCause();
}
```

When a task throws, the pool thread does not crash. The pool stores the exception inside the future. Later, `get()` throws an `ExecutionException` that wraps it. `getCause()` gives you the original exception back.

```java run
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<String> broken = pool.submit(() -> {
                if (true) {
                    throw new IllegalStateException("sensor offline");
                }
                return "Harbor-X: ok";
            });
            try {
                System.out.println(broken.get());
            } catch (ExecutionException e) {
                System.out.println("wrapper: " + e.getClass().getSimpleName());
                System.out.println("cause: " + e.getCause().getMessage());
            }
        }
    }
}
```

## Worked example

Here is a small version of the sensor check. It uses a pool of 3 threads for 5 Uptown docks and keeps the results in order.

```java run
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    static String readSensor(String dockId) {
        return dockId + ": ok";
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        List<String> docks = List.of("Uptown-1", "Uptown-2", "Uptown-3", "Uptown-4", "Uptown-5");
        List<Future<String>> futures = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(3)) {
            for (String id : docks) {
                futures.add(pool.submit(() -> readSensor(id)));
            }
            for (Future<String> f : futures) {
                System.out.println(f.get());
            }
        }
    }
}
```

Line by line:

1. `readSensor` stands in for the slow network call.
2. The `try` line makes a pool of 3 threads and promises to close it.
3. The first loop submits one callable per dock. Each `submit` returns at once, so all 5 tasks are queued quickly.
4. The `Future` for each dock goes into `futures`, in the same order as `docks`.
5. The second loop calls `get()` on each future in that order. The tasks may finish in any order, but you read them in list order.
6. At the closing brace, `close()` waits for the pool and shuts it down.

The output is always `Uptown-1: ok` to `Uptown-5: ok`, top to bottom.

## Try it

Run the worked example. Then change the pool size from 3 to 1 and run it again. The output stays the same, because one thread can still do all 5 tasks one by one. Next, add `Thread.currentThread().getName()` to the text `readSensor` returns and look at the thread names.

```callout tip
For CPU work, use about one thread per core. Slow network work can use more, and topic 05 shows a better tool.
```

## Recap

- Creating a thread for every task costs memory and time. A pool reuses a few threads.
- `Executors.newFixedThreadPool(n)` makes an `ExecutorService` with `n` threads.
- `submit` takes a `Runnable` or a `Callable` and returns a `Future`. `get()` waits for the result.
- `invokeAll` runs a list of callables and returns their futures in order.
- Always shut a pool down. Try-with-resources does it for you, and its `close()` waits.
- An exception inside a task comes out of `get()` as an `ExecutionException`. `getCause()` holds the original.

## Common mistakes

**Forgetting to shut the pool down.**

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.submit(() -> System.out.println("Riverside-1: ok"));
    }
}
```

The program prints `Riverside-1: ok` and then never ends. The pool threads keep waiting for more work. Fix it with `try (ExecutorService pool = ...) { ... }`, or call `pool.shutdown()` when you are done.

**Calling get() without handling its exceptions.**

```java nocompile
public static void main(String[] args) {
    try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
        Future<String> f = pool.submit(() -> "Riverside-1: ok");
        System.out.println(f.get());
    }
}
```

Java says: `unreported exception InterruptedException; must be caught or declared to be thrown`. Add `throws InterruptedException, ExecutionException` to the method, or wrap the call in `try` and `catch`.

**Catching the task's own exception around get().**

```java nocompile
try {
    System.out.println(broken.get());
} catch (IllegalStateException e) {
    System.out.println("failed");
}
```

The catch never runs. The program crashes with `Exception in thread "main" java.util.concurrent.ExecutionException: java.lang.IllegalStateException: sensor offline`. `get()` throws the wrapper. Catch `ExecutionException` and read `e.getCause()`.
