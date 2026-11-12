## Hook

Picture a café with one barista. Each customer orders, then waits while the milk steams. Nobody else gets served during that wait. The queue grows out the door.

Now picture three baristas. While one waits for milk, the others take orders. The milk takes just as long, but the queue moves.

Theo's ticket is the first café. The Harbor kiosk checks 12 dock sensors one by one. Each check spends most of its time waiting on the network. In this topic you learn to start all 12 checks at once.

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

`join()` makes the current thread wait until `worker` has finished. It can throw `InterruptedException`, a checked exception. That exception means another thread asked this one to stop waiting. For now, add `throws InterruptedException` to your method.

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

- A thread is one path of work. Threads in one program share memory.
- Your program starts on the `main` thread.
- Write the job as a `Runnable` lambda, wrap it in `new Thread(job, "name")` and call `start()`.
- `run()` runs the job on the current thread. Only `start()` creates a new one.
- `join()` waits for a thread to finish. The order of output between threads is not fixed.

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
