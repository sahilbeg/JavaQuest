## Hook

Think of the evening queue at the Harbor dock. Riders join at the back. A rider with a broken lock gets waved to the front. Someone leaves the line. Someone asks, "Is bike B-1042 in this line?"

A real queue changes all the time. Theo's evening ride list does too. In this topic you learn every tool a `List` gives you for that. You also learn why Theo's code crashes when it removes rides inside a loop.

## Concept

A **List** is an ordered collection. Each item has a position, called an **index**, starting at 0. You met `add`, `get` and `size` in the last topic. A list can do much more.

### Syntax: add at an index

```java nocompile
list.add(index, item);
```

The item goes in at that position. Every item after it shifts one place to the right.

```java nocompile
List<String> ids = new ArrayList<>();
ids.add("B-2001");
ids.add("B-3300");
ids.add(0, "B-1042");        // [B-1042, B-2001, B-3300]
```

### Syntax: set

```java nocompile
list.set(index, item);
```

`set` replaces the item at that position. The size stays the same.

```java nocompile
ids.set(1, "B-9999");        // [B-1042, B-9999, B-3300]
```

### Syntax: remove by index and by value

```java nocompile
list.remove(index);
list.remove(item);
```

- `remove(1)` takes out the item at position 1. Items after it shift left.
- `remove("B-3300")` takes out the first item equal to `"B-3300"`. It returns `true` if it found one.

```java nocompile
ids.remove(0);               // [B-9999, B-3300]
ids.remove("B-3300");        // [B-9999]
```

```callout warning
With a `List<Integer>`, `remove(2)` means "remove position 2". To remove the number 2, write `remove(Integer.valueOf(2))`.
```

### Syntax: searching

```java nocompile
boolean found = list.contains(item);
int where = list.indexOf(item);
```

- `contains` answers yes or no.
- `indexOf` gives the position of the first match, or `-1` if the item is missing.

Both use `equals` to compare, which you met in module 2. So a record works out of the box.

### Syntax: isEmpty and clear

```java nocompile
boolean none = list.isEmpty();
list.clear();
```

- `isEmpty()` is `true` when the size is 0.
- `clear()` removes every item.

```java run
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> ids = new ArrayList<>();
        ids.add("B-2001");
        ids.add("B-3300");
        ids.add(0, "B-1042");
        System.out.println(ids);
        ids.set(1, "B-9999");
        ids.remove("B-3300");
        System.out.println(ids);
        System.out.println(ids.contains("B-1042"));
        System.out.println(ids.indexOf("B-3300"));
        ids.clear();
        System.out.println(ids.isEmpty());
    }
}
```

### Looping over a list

You can loop with an index, like an array:

```java nocompile
for (int i = 0; i < ids.size(); i++) {
    System.out.println(ids.get(i));
}
```

Or with a **for-each** loop, which reads each item in turn:

```java nocompile
for (String id : ids) {
    System.out.println(id);
}
```

Use for-each when you only read. Use the index loop when you need the position.

### Removing inside a loop

Here is the bug from Theo's ticket:

```java nocompile
for (String id : ids) {
    if (id.startsWith("T")) {
        ids.remove(id);       // usually crashes
    }
}
```

A for-each loop keeps track of where it is in the list. When you remove an item, the list changes under the loop's feet. The next step of the loop usually notices and throws a **ConcurrentModificationException**. If you remove the second-to-last item, the loop ends early instead and skips the last item. That is worse, because nothing tells you.

```callout jargon
**Concurrent modification** means "changing something while someone else is walking through it". Java usually stops the program rather than give you wrong results.
```

There are two safe ways.

**Safe way 1: loop backwards with an index.** Start at the last item and step down to 0. Removing item `i` only shifts the items after it. You already visited those, so nothing is skipped.

```java nocompile
for (int i = ids.size() - 1; i >= 0; i--) {
    if (ids.get(i).startsWith("T")) {
        ids.remove(i);
    }
}
```

**Safe way 2: build a new list.** Read the old list with for-each, and add the items you want to keep to a fresh list.

```java nocompile
List<String> kept = new ArrayList<>();
for (String id : ids) {
    if (!id.startsWith("T")) {
        kept.add(id);
    }
}
```

### ArrayList and LinkedList

`List` is an interface. Two classes implement it:

- **ArrayList** stores items in a hidden array. Reading `get(i)` is instant. Inserting near the front is slow, because every later item shifts.
- **LinkedList** stores each item in a little box that points to the next box. Adding at the front is quick. Reading `get(500)` is slow, because it walks 500 boxes.

```callout analogy
An ArrayList is a row of numbered lockers: jump straight to locker 500. A LinkedList is a chain of people holding hands: to reach person 500, you walk down the chain.
```

Both have the same `List` methods. Pick `ArrayList` unless you have measured a problem. Declare the variable as `List`, so you can swap the class with one word:

```java nocompile
List<String> ids = new LinkedList<>();   // import java.util.LinkedList;
```

### Syntax: List.of

```java nocompile
List<String> stations = List.of("Riverside", "Old Mill", "Harbor", "Uptown");
```

`List.of` makes a list in one line. That list is **unmodifiable**: it can never change. Calling `add`, `remove` or `set` on it throws an `UnsupportedOperationException`. Use it for fixed data, like the four stations. To get a list you can change, copy it: `new ArrayList<>(List.of(...))`.

## Worked example

This program tidies an evening list of rides. It uses the `Ride` record shape from Spoke.

```java run
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Ride> rides = new ArrayList<>();
        rides.add(new Ride("B-1042", "Harbor", "ebike", 25));
        rides.add(new Ride("B-2001", "Harbor", "classic", 1));
        rides.add(new Ride("B-3300", "Uptown", "cargo", 40));
        rides.add(0, new Ride("B-7777", "Harbor", "classic", 12));

        for (int i = rides.size() - 1; i >= 0; i--) {
            if (rides.get(i).minutes() < 2) {
                rides.remove(i);
            }
        }

        int total = 0;
        for (Ride ride : rides) {
            total = total + ride.minutes();
        }
        System.out.println("Rides left: " + rides.size());
        System.out.println("First: " + rides.get(0).bikeId());
        System.out.println("Total minutes: " + total);
    }
}

record Ride(String bikeId, String station, String type, int minutes) {}
```

1. The list starts with three rides added at the end.
2. `add(0, ...)` puts the late fix `B-7777` at the front.
3. The backwards loop removes the 1-minute test ride safely.
4. The for-each loop only reads, so it is safe. It adds up the minutes: 12 + 25 + 40.
5. The output is `Rides left: 3`, `First: B-7777` and `Total minutes: 77`.

## Try it

Run this program. It crashes with `ConcurrentModificationException`. Change the loop to the backwards index loop, then run it again.

```java run crash
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> ids = new ArrayList<>(List.of("B-1042", "T-0001", "B-2001", "T-0002"));
        for (String id : ids) {
            if (id.startsWith("T")) {
                ids.remove(id);
            }
        }
        System.out.println(ids);
    }
}
```

When it works, it prints `[B-1042, B-2001]`.

## Recap

- `add(i, x)` inserts, `set(i, x)` replaces, and `remove` works by index or by value.
- `contains` and `indexOf` search with `equals`. `indexOf` gives `-1` when the item is missing.
- Removing inside a for-each loop usually throws `ConcurrentModificationException`. Removing the second-to-last item silently skips the last one. Loop backwards with an index, or build a new list.
- `ArrayList` is fast at `get(i)`. `LinkedList` is fast at adding to the front. Start with `ArrayList`.
- `List.of(...)` makes a list that can never change.

## Common mistakes

**Removing inside a for-each loop.**

```java nocompile
List<String> ids = new ArrayList<>(List.of("B-1", "T-1", "B-2", "B-3"));
for (String id : ids) {
    if (id.startsWith("T")) {
        ids.remove(id);
    }
}
```

Java says: `Exception in thread "main" java.util.ConcurrentModificationException`. Loop backwards with an index and call `ids.remove(i)`.

**Changing a list made with List.of.**

```java nocompile
List<String> stations = List.of("Riverside", "Harbor");
stations.add("Uptown");
```

Java says: `Exception in thread "main" java.lang.UnsupportedOperationException`. Copy it into a list you can change: `new ArrayList<>(List.of("Riverside", "Harbor"))`.

**Reading one past the end.**

```java nocompile
List<String> ids = new ArrayList<>(List.of("B-1042", "B-2001"));
System.out.println(ids.get(ids.size()));
```

Java says: `java.lang.IndexOutOfBoundsException: Index 2 out of bounds for length 2`. The last item is at `ids.size() - 1`.
