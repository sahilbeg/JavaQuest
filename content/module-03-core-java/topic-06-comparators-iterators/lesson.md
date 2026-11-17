## Hook

Think of a pile of ride receipts dumped on a desk. Itachi needs the longest ride from Harbor. With a messy pile, Itachi has to read every single receipt. With a stack sorted by minutes, the answer sits on top.

Java can sort a list for you. It only needs one thing: a rule that says which of two items comes first. This page shows you how to write that rule, and how to walk through a list and remove items safely.

## Concept

Sorting is built on one small question, asked many times: "Does `a` come before `b`?" Java answers it with a number.

```callout jargon
A **comparison** returns an `int`. A negative number means `a` comes first. Zero means they are equal for sorting. A positive number means `b` comes first.
```

There are two ways to give Java this rule. The class can carry its own rule, or you can write the rule in a separate class.

### Syntax: Comparable and compareTo

```java
record Ride(String bikeId, int minutes) implements Comparable<Ride> {
    public int compareTo(Ride other) {
        return Integer.compare(this.minutes, other.minutes);
    }
}
```

- `Comparable<Ride>` is an interface with one method, `compareTo`.
- The order it defines is called the **natural order**. It is the order a type uses when you give no other rule.
- `Integer.compare(a, b)` returns a negative, zero or positive int for you. Use it for whole numbers.

`String` and `Integer` already implement `Comparable`. Strings sort alphabetically and numbers sort from small to big.

```java
int a = Integer.compare(5, 20);          // negative
int b = "Harbor".compareTo("Uptown");    // negative, H before U
int c = Integer.compare(30, 30);         // 0
```

### Syntax: sorting a list

```java nocompile
Collections.sort(rides);     // uses natural order
rides.sort(null);            // same thing, null means natural order
```

Both change the list in place. The list must be changeable, so wrap `List.of(...)` in `new ArrayList<>(...)` first.

```java run
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    record Ride(String bikeId, int minutes) implements Comparable<Ride> {
        public int compareTo(Ride other) {
            return Integer.compare(this.minutes, other.minutes);
        }
    }

    public static void main(String[] args) {
        List<Ride> rides = new ArrayList<>(List.of(
                new Ride("B-1042", 25),
                new Ride("B-2001", 8),
                new Ride("B-3300", 95)));
        Collections.sort(rides);
        for (Ride r : rides) {
            System.out.println(r.bikeId() + " " + r.minutes());
        }
    }
}
```

### Syntax: a Comparator class

A class has only one natural order. To sort another way, write a **comparator**. A comparator is an object whose job is to compare two other objects.

```java nocompile
class ByStation implements Comparator<Ride> {
    public int compare(Ride a, Ride b) {
        return a.station().compareTo(b.station());
    }
}
```

- `Comparator<Ride>` comes from `java.util`. Its method is `compare(a, b)`, with two parameters.
- You pass an object of the class to `sort`: `rides.sort(new ByStation());`.

```callout analogy
Comparable is the rule printed on the receipt itself. A Comparator is a separate card that Itachi holds up and says "sort by this today".
```

### Syntax: sorting by two keys

Sometimes two rides tie on the first key. Then you check a second key.

```java nocompile
public int compare(Ride a, Ride b) {
    int byStation = a.station().compareTo(b.station());
    if (byStation != 0) {
        return byStation;
    }
    return Integer.compare(a.minutes(), b.minutes());
}
```

Compare the first key. If it is not zero, you have your answer. Only on a tie do you compare the second key.

### Syntax: reversed()

```java nocompile
class ByMinutes implements Comparator<Ride> {
    public int compare(Ride a, Ride b) {
        return Integer.compare(a.minutes(), b.minutes());
    }
}

Comparator<Ride> longestFirst = new ByMinutes().reversed();
rides.sort(longestFirst);
```

Every comparator object has a `reversed()` method. It returns a new comparator with the opposite order. You never need to flip the signs by hand.

### Syntax: Iterator

An **iterator** is an object that walks through a collection one item at a time. You get one by calling `iterator()` on a list.

```java nocompile
Iterator<Ride> it = rides.iterator();
while (it.hasNext()) {
    Ride r = it.next();
    if (r.minutes() > 120) {
        it.remove();
    }
}
```

- `hasNext()` returns `true` while items are left.
- `next()` returns the next item and moves forward.
- `remove()` deletes the item that `next()` just returned.

```callout warning
Never call `rides.remove(...)` inside a for-each loop over `rides`. The loop crashes with a `ConcurrentModificationException`. Use the iterator's own `remove()` instead.
```

## Worked example

Kakashi wants the report sorted by station, then by minutes inside each station.

```java run
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    record Ride(String bikeId, String station, int minutes) { }

    static class ByStationThenMinutes implements Comparator<Ride> {
        public int compare(Ride a, Ride b) {
            int byStation = a.station().compareTo(b.station());
            if (byStation != 0) {
                return byStation;
            }
            return Integer.compare(a.minutes(), b.minutes());
        }
    }

    public static void main(String[] args) {
        List<Ride> rides = new ArrayList<>(List.of(
                new Ride("B-1042", "Uptown", 25),
                new Ride("B-2001", "Harbor", 95),
                new Ride("B-3300", "Harbor", 12)));
        rides.sort(new ByStationThenMinutes());
        for (Ride r : rides) {
            System.out.println(r.station() + " " + r.minutes());
        }
        rides.sort(new ByStationThenMinutes().reversed());
        System.out.println("First after reversed: " + rides.get(0).bikeId());
    }
}
```

Line by line:

1. `compare` first checks the stations. "Harbor" comes before "Uptown", so both Harbor rides go first.
2. The two Harbor rides tie on station, so `byStation` is 0. Then minutes decide: 12 before 95.
3. `rides.sort(new ByStationThenMinutes())` uses the comparator object.
4. `reversed()` flips the whole order, so the Uptown ride comes first.

The output is:

```
Harbor 12
Harbor 95
Uptown 25
First after reversed: B-1042
```

## Try it

The program removes rides longer than 60 minutes. Change the limit to 20 and run it again. Then count the removed rides with an `int` counter.

```java run
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Integer> minutes = new ArrayList<>(List.of(25, 95, 8, 140, 30));
        Iterator<Integer> it = minutes.iterator();
        while (it.hasNext()) {
            int m = it.next();
            if (m > 60) {
                it.remove();
            }
        }
        System.out.println(minutes);
    }
}
```

## Recap

- `Comparable` gives a class its natural order through `compareTo(other)`.
- `Collections.sort(list)` and `list.sort(comparator)` sort a list in place.
- A `Comparator` class has `compare(a, b)` and gives you any other order. `Integer.compare` handles numbers.
- For two keys, return the first comparison unless it is zero, then compare the second key.
- `reversed()` flips a comparator. An `Iterator` with `hasNext`, `next` and `remove` deletes items safely.

## Common mistakes

**1. Removing from the list inside a for-each loop.**

```java
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Integer> minutes = new ArrayList<>(List.of(95, 8, 140, 30));
        for (Integer m : minutes) {
            if (m > 60) {
                minutes.remove(m);
            }
        }
    }
}
```

The program crashes with `Exception in thread "main" java.util.ConcurrentModificationException`. The list changed under the loop. Use an `Iterator` and call `it.remove()`.

**2. Subtracting instead of comparing.**

```java nocompile
public int compareTo(Ride other) {
    return this.minutes - other.minutes;
}
```

This works for small numbers but breaks for huge ones. A very large minus a very negative number wraps around and gives the wrong sign. Use `return Integer.compare(this.minutes, other.minutes);`.

**3. Calling remove() before next().**

```java nocompile
Iterator<Ride> it = rides.iterator();
while (it.hasNext()) {
    it.remove();
    it.next();
}
```

The program crashes with `java.lang.IllegalStateException`. `remove()` deletes the item that `next()` just returned, so `next()` must come first. Call `Ride r = it.next();` and then `it.remove();`.
