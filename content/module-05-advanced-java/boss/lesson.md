## What happened

`UnlockService` runs on many threads at once: one per rider request. It has three bugs, and each one only shows up under load.

1. Two riders can unlock the same bike.
2. The payment counter loses counts.
3. Moving bikes between two stations can freeze forever.

```callout analogy
Two people reach for the last bike at the same time. Both saw it free a moment ago, and both grab it.
```

## What you need

This builds on module 5 topics 02, 05 and 06, plus compareTo from module 3.

### Syntax: check and act in one step

```java nocompile
String earlier = reservations.putIfAbsent(bikeId, riderId);
boolean won = earlier == null;
```

`putIfAbsent` returns the old value, or `null` if the key was free. On a `ConcurrentHashMap` the whole call is atomic: no other thread can slip in between the check and the put.

### Syntax: a counter that never loses a step

```java nocompile
private final AtomicInteger payments = new AtomicInteger();
payments.incrementAndGet();
```

### Syntax: a fixed lock order

```java nocompile
Station first = from;
Station second = to;
if (to.name().compareTo(from.name()) < 0) {
    first = to;
    second = from;
}
synchronized (first) {
    synchronized (second) {
        // move the bike
    }
}
```

When every thread takes the locks in the same order, no two threads can wait for each other in a circle.

## The tiers

1. **Bronze** resolves the incident: when 50 riders try to unlock the same bike at once, exactly one succeeds.
2. **Silver**: 8 threads that each record 10,000 payments give a count of exactly 80,000.
3. **Gold**: two threads moving bikes in opposite directions between Riverside and Harbor always finish, and no bike is lost.

## Recap

- A check followed by an act is a race unless both happen in one atomic step.
- `count++` is three steps. Use an atomic class for shared counters.
- Deadlock needs locks taken in different orders. Pick one order and keep it.

## Common mistakes

**Making every method `synchronized`.** It hides all three bugs. But now only one rider in the whole city can unlock or move a bike at a time.

**Testing once.** A race may pass a single run. The tests repeat the work many times so the bug shows up.
