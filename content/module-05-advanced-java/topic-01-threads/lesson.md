## Hook

Picture a café with one barista. Each customer orders, then waits while the milk steams. Nobody else gets served during that wait. The queue grows out the door.

Now picture three baristas. While one waits for milk, the others take orders. The milk takes just as long, but the queue moves.

Kakashi's ticket is the first café. The Harbor kiosk checks 12 dock sensors one by one. Each check spends most of its time waiting on the network. In this topic you learn to start all 12 checks at once.

## Concept

### What a thread is

Every program you have written so far did one thing at a time. Line 1 ran, then line 2, then line 3.

```callout jargon
A **thread** is one path of work running through your program. A program can have several threads in progress at once. All threads in one program share the same memory, so they can see the same objects.
```

When you run `main`, Java starts one thread for you. Its name is `main`, and it is called the **main thread**. You can ask Java which thread is running a line of code:

```java run
public class Main {
    public static void main(String[] args) {
        String name = Thread.currentThread().getName();
        System.out.println("This line runs on: " + name);
    }
}
```

`Thread.currentThread()` gives you the thread running that line. `getName()` gives you its name.

### Processes, threads and cores

Three words come up in every talk about threads. Here is how they fit together.

```callout jargon
A **process** is one running program, such as your Java app or your browser. The operating system gives each process its own private memory. One process cannot read another's memory.
```

```callout jargon
A **core** is one part of the processor (CPU) that runs instructions. A core runs one thread at any instant. A laptop usually has 4 to 16 cores.
```

So a process holds one or more threads, and the threads take turns on the cores. Your Java program is one process. Its threads share that process's memory.

```callout jargon
**Concurrency** means several jobs are in progress at the same time, even if they take turns on one core. **Parallelism** means several jobs run at the very same instant, on different cores.
```

When there are more threads than cores, the operating system swaps them. It pauses one thread, saves where it was, and loads another. That swap is a **context switch**.

```callout jargon
A **context switch** is the operating system pausing one thread on a core and resuming another. It takes a few microseconds. The new thread's data is often not in the core's fast caches yet, so it starts slowly.
```

One switch is cheap. Thousands of switches every second, between thousands of threads, waste real time. That is why "start more threads" stops helping after a point. The next tickets come back to this.

```animate
title: Two cores, three threads
kind: scene
nodes:
  - { id: t1, label: dock-a, emoji: "🧵", value: waiting, row: 0, col: 0 }
  - { id: t2, label: dock-b, emoji: "🧵", value: waiting, row: 0, col: 1 }
  - { id: t3, label: dock-c, emoji: "🧵", value: waiting, row: 0, col: 2 }
  - { id: c1, label: Core 1, emoji: "⚙️", value: idle, row: 1, col: 0 }
  - { id: c2, label: Core 2, emoji: "⚙️", value: idle, row: 1, col: 2 }
steps:
  - say: Your program has three threads, but the laptop has only two cores.
    focus: [t1, t2, t3]
  - say: The operating system puts dock-a on core 1 and dock-b on core 2. They run in parallel.
    send: [{ from: t1, to: c1, label: run }, { from: t2, to: c2, label: run }]
    set: { t1: running, t2: running, c1: dock-a, c2: dock-b }
  - say: dock-c waits its turn. It is in progress too, so all three are concurrent.
    focus: [t3]
  - say: dock-a starts waiting for the network. The operating system saves its place. That is a context switch.
    set: { t1: waiting, c1: switching }
  - say: dock-c gets core 1. The core never sat idle while dock-a waited.
    send: { from: t3, to: c1, label: run }
    set: { t3: running, c1: dock-c }
```

### Syntax: Runnable

A thread needs a job to do. Java describes a job with the interface `Runnable`.

```java nocompile
public interface Runnable {
    void run();
}
```

`Runnable` has exactly one abstract method, so it is a functional interface. You can write the job as a lambda:

```java
Runnable job = () -> System.out.println("checking Harbor-3");
```

The lambda takes no inputs and returns nothing. It is just a job waiting to be done.

### Syntax: new Thread and start

```java nocompile
Thread worker = new Thread(job);
worker.start();
```

- `new Thread(job)` makes a thread object that holds the job. Nothing runs yet.
- `start()` asks Java to begin a new thread. That new thread calls `job.run()`.
- `start()` returns at once. The line after it runs while the new thread works.

You can also pass the lambda straight in:

```java nocompile
Thread worker = new Thread(() -> System.out.println("checking Harbor-3"));
```

### Syntax: naming a thread

```java nocompile
Thread worker = new Thread(job, "sensor-worker");
```

The second argument is the thread's name. Names help when you read logs. Without a name, Java picks one such as `Thread-0`.

### Syntax: join

```java nocompile
worker.join();
```

`join()` makes the current thread wait until `worker` has finished. It can throw `InterruptedException`, a checked exception. That exception means another thread asked this one to stop waiting. In a small `main`, add `throws InterruptedException` to the method. The card "Interrupting a thread" below shows what to do in real code.

### Order between threads is not fixed

Two threads run side by side. Java and your operating system decide who goes when. You do not control it.

```java run
public class Main {
    public static void main(String[] args) throws InterruptedException {
        Thread a = new Thread(() -> System.out.println("Riverside checked"), "dock-a");
        Thread b = new Thread(() -> System.out.println("Uptown checked"), "dock-b");
        a.start();
        b.start();
        a.join();
        b.join();
        System.out.println("All done");
    }
}
```

Run it a few times. Sometimes Riverside prints first, and sometimes Uptown does. `All done` always prints last, because `main` waits with `join()` before printing it.

```callout warning
Never write code that needs one thread to finish before another unless you wait for it. Use `join()` to wait. A lucky order on your laptop can flip on a busy server.
```

### start versus run

`run()` is a normal method. If you call `worker.run()` yourself, the job runs on the thread that called it. No new thread starts. Only `start()` begins a new thread.

```java run
public class Main {
    public static void main(String[] args) throws InterruptedException {
        Runnable job = () -> System.out.println("Running on " + Thread.currentThread().getName());
        Thread worker = new Thread(job, "sensor-worker");

        worker.run();
        worker.start();
        worker.join();
    }
}
```

This prints `Running on main` and then `Running on sensor-worker`.

### Interrupting a thread

Sometimes you want a thread to stop early. Maybe the kiosk is shutting down, or a check is taking too long. Java has no safe "kill" button. Instead, you ask politely.

```callout jargon
To **interrupt** a thread is to set a yes/no mark on it, called the **interrupt flag**. It means "please stop what you are doing". The thread decides how to react.
```

`worker.interrupt()` sets the flag on `worker`. If `worker` is asleep in `Thread.sleep`, or waiting in `join()`, it wakes up at once. The waiting method throws `InterruptedException` and clears the flag.

```java run
public class Main {
    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(10_000);
                System.out.println("Harbor-3: ok");
            } catch (InterruptedException e) {
                System.out.println("Woken early. Flag is now " + Thread.currentThread().isInterrupted());
                Thread.currentThread().interrupt();
                System.out.println("Flag restored: " + Thread.currentThread().isInterrupted());
            }
        }, "sensor-worker");
        worker.start();
        worker.interrupt();
        worker.join();
        System.out.println("main is done");
    }
}
```

The worker would sleep for 10 seconds, but `interrupt()` wakes it straight away. `isInterrupted()` reads the flag without changing it. A thread that is busy computing, not waiting, does not wake up. It must check `Thread.currentThread().isInterrupted()` itself, for example once per loop.

### Restoring the interrupt flag

Look at the `catch` block above again. When `InterruptedException` is thrown, the flag is already cleared. The stop request is now known only to your `catch` block.

If you just print the error and carry on, the request is lost. Code higher up in the same thread, such as the loop that called your method, checks the flag to decide whether to stop. It sees `false` and keeps working.

So when you catch `InterruptedException` and cannot throw it on, put the flag back:

```java nocompile
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();   // keep the stop request for the code above
    return;                               // and finish early
}
```

You will meet this exact line again in the next tickets. Now you know why it is there.

```callout warning
Never write an empty `catch (InterruptedException e) {}`. It swallows the stop request, and a shutdown can hang forever waiting for your thread.
```

## Worked example

Here is a fake sensor check. `Thread.sleep(1000)` pauses the current thread for 1000 milliseconds. Use it only in demos like this one, to pretend the network is slow.

```java run
import java.util.List;

public class Main {
    static String readSensor(String dockId) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            return dockId + ": interrupted";
        }
        return dockId + ": ok";
    }

    public static void main(String[] args) throws InterruptedException {
        List<String> docks = List.of("Harbor-1", "Harbor-2", "Harbor-3");
        String[] results = new String[docks.size()];
        Thread[] threads = new Thread[docks.size()];

        for (int i = 0; i < docks.size(); i++) {
            int slot = i;
            String id = docks.get(i);
            threads[i] = new Thread(() -> results[slot] = readSensor(id), "check-" + id);
            threads[i].start();
        }
        for (Thread t : threads) {
            t.join();
        }
        for (String r : results) {
            System.out.println(r);
        }
    }
}
```

Line by line:

1. `results` has one slot per dock. Each thread writes only to its own slot, so two threads never touch the same slot.
2. `int slot = i;` copies the loop counter. A lambda can only use local variables that never change, and `i` changes every loop.
3. The first loop starts all three threads. It does not wait for any of them.
4. The second loop waits for each thread with `join()`.
5. The last loop prints the results in dock order.

All three checks sleep at the same time, so the program takes about 1 second instead of 3. Your timing may differ a little.

## Try it

Add `"Harbor-4"` and `"Harbor-5"` to the list in the worked example and run it again. It still takes about one second. Then delete the `join()` loop and run it. Some results print as `null`, because `main` printed before the threads finished.

## Recap

- A thread is one path of work inside a process, and threads take turns on the cores. Your program starts on the `main` thread.
- Write the job as a `Runnable` lambda, wrap it in `new Thread(job, "name")` and call `start()`.
- `run()` runs the job on the current thread. Only `start()` creates a new one.
- `join()` waits for a thread to finish. The order of output between threads is not fixed.
- `interrupt()` asks a thread to stop. When you catch `InterruptedException`, restore the flag with `Thread.currentThread().interrupt()`.

## Common mistakes

### Calling run instead of start

```java
public class Main {
    public static void main(String[] args) {
        Thread worker = new Thread(() -> System.out.println(Thread.currentThread().getName()), "sensor-worker");
        worker.run();
    }
}
```

Output:

```text
main
```

The job ran on `main`, one after another like before. Fix: call `worker.start()`, then `worker.join()` if you need to wait.

### Forgetting to join

```java
public class Main {
    public static void main(String[] args) {
        String[] result = new String[1];
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
            result[0] = "Harbor-3: ok";
        });
        worker.start();
        System.out.println(result[0]);
    }
}
```

Output:

```text
null
```

`main` printed before the worker wrote its result. Fix: call `worker.join();` before you read `result[0]`, and add `throws InterruptedException` to `main`.

### Starting the same thread twice

```java run crash
public class Main {
    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> System.out.println("check"));
        worker.start();
        worker.join();
        worker.start();
    }
}
```

Output:

```text
check
Exception in thread "main" java.lang.IllegalThreadStateException
```

A thread object can start only once. Fix: create a new `Thread` for each run. The same `Runnable` can be reused.
