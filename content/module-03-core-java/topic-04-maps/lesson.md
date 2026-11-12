## Hook

Priya wants total minutes for each station. The current code walks the full ride list for Riverside. Then it walks it again for Old Mill, again for Harbor, and again for Uptown.

Picture a school librarian who counts books by rereading the whole shelf once per author. A smarter librarian keeps one sheet with a line per author. She walks the shelf once and adds a tally to the right line. Java's version of that sheet is a map.

## Concept

### What a map is

```callout jargon
A **map** stores pairs. Each pair has a **key** (the thing you look up) and a **value** (what you find). Every key appears at most once. Values may repeat.
```

For Priya, the key is a station name and the value is its total minutes. `"Harbor"` maps to `45`.

```callout analogy
A map works like a phone's contact list. You look up a name (the key) and get a number (the value). Two contacts cannot share a name, but two names may share a number.
```

`Map` is an interface in `java.util`. A map has two type parameters: first the key type, then the value type.

### Syntax: creating a map

```java nocompile
Map<String, Integer> minutes = new HashMap<>();
```

The value type is `Integer`, the object version of `int`. Collections hold objects, as you saw with lists.

### Syntax: put and get

```java nocompile
minutes.put("Harbor", 30);   // add a pair
minutes.put("Harbor", 45);   // same key: replaces 30 with 45
int h = minutes.get("Harbor"); // 45
```

`put` with a key that already exists replaces the old value. The map still has one `Harbor` entry.

`get` returns `null` when the key is missing. It does not throw an error.

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Map<String, Integer> minutes = new HashMap<>();
        minutes.put("Harbor", 30);
        minutes.put("Harbor", 45);
        System.out.println(minutes.get("Harbor")); // 45
        System.out.println(minutes.get("Uptown")); // null
        System.out.println(minutes.size());        // 1
    }
}
```

### Syntax: getOrDefault, containsKey, remove

```java nocompile
minutes.getOrDefault("Uptown", 0);  // the value, or 0 if the key is missing
minutes.containsKey("Harbor");      // true if the key is there
minutes.remove("Harbor");           // removes the pair
```

`getOrDefault` saves you from `null`. You choose the fallback value yourself.

### Syntax: the counting pattern

```java nocompile
int old = totals.getOrDefault(station, 0);
totals.put(station, old + ride.minutes());
```

This is the most useful map pattern of all. Read the old total, or 0 for a new key. Then put back the new total. One pass over the rides fills the whole map.

To count rides instead of minutes, add 1 each time: `counts.put(type, counts.getOrDefault(type, 0) + 1);`.

### Syntax: looping over a map

```java nocompile
for (String station : totals.keySet()) { ... }        // every key
for (int m : totals.values()) { ... }                 // every value
for (Map.Entry<String, Integer> e : totals.entrySet()) {
    System.out.println(e.getKey() + ": " + e.getValue());
}
```

`keySet()` gives a set of the keys. `values()` gives the values. `entrySet()` gives the pairs. Each pair is a `Map.Entry` with `getKey()` and `getValue()`. Use `entrySet()` when you need both.

### HashMap or TreeMap

| Class | Order of keys when you loop or print |
|---|---|
| `HashMap` | No promised order. |
| `LinkedHashMap` | The order you first put the keys. |
| `TreeMap` | Sorted by key. Station names come out alphabetically. |

This matches the three sets from the last topic. In fact, `keySet()` of a `TreeMap` is sorted too.

```java run
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Map<String, Integer> totals = new TreeMap<>();
        totals.put("Uptown", 12);
        totals.put("Harbor", 45);
        totals.put("Riverside", 30);
        System.out.println(totals);
        System.out.println(totals.keySet());
    }
}
```

A printed map shows `{key=value, ...}`. The `TreeMap` sorted the stations for you.

```callout tip
Keys follow the same rules as set values. A `HashMap` uses `equals` and `hashCode` on its keys. `String` keys are always safe.
```

## Worked example

Total the minutes per station in one pass, then print them.

```java run
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<Ride> rides = List.of(
                new Ride("B-1042", "Harbor", "ebike", 25),
                new Ride("B-2001", "Uptown", "classic", 12),
                new Ride("B-1042", "Harbor", "ebike", 20),
                new Ride("B-3300", "Riverside", "cargo", 30));

        Map<String, Integer> totals = new TreeMap<>();
        for (Ride ride : rides) {
            int old = totals.getOrDefault(ride.station(), 0);
            totals.put(ride.station(), old + ride.minutes());
        }

        for (Map.Entry<String, Integer> e : totals.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue() + " min");
        }
    }
}

record Ride(String bikeId, String station, String type, int minutes) {}
```

Line by line:

- The first Harbor ride finds no key, so `getOrDefault` gives 0. Harbor becomes 25.
- The second Harbor ride finds 25 and puts back 45.
- Each ride is visited once. Four stations would need four passes with the old code.
- The `TreeMap` loops in alphabetical order: Harbor, Riverside, Uptown.

## Try it

Count how many rides used each bike type. Add a `"classic"` ride and run it again.

```java run
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<String> types = List.of("ebike", "classic", "ebike", "cargo", "ebike");
        Map<String, Integer> counts = new TreeMap<>();
        for (String type : types) {
            counts.put(type, counts.getOrDefault(type, 0) + 1);
        }
        System.out.println(counts);
    }
}
```

## Recap

- A `Map` links each key to one value. A key appears at most once.
- `put` adds or replaces. `get` returns `null` for a missing key. `getOrDefault` returns your fallback.
- The counting pattern: `map.put(key, map.getOrDefault(key, 0) + amount);`.
- Loop with `keySet()`, `values()` or `entrySet()`. Use `entrySet()` when you need key and value.
- `HashMap` has no order. `TreeMap` keeps its keys sorted.

## Common mistakes

### 1. Unboxing a missing value

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Map<String, Integer> totals = new HashMap<>();
        int uptown = totals.get("Uptown");
        System.out.println(uptown);
    }
}
```

The program crashes with:

```text
Exception in thread "main" java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "java.util.Map.get(Object)" is null
```

`get` returned `null`, and Java cannot turn `null` into an `int`. Use `totals.getOrDefault("Uptown", 0)`.

### 2. Putting the ride instead of the running total

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Map<String, Integer> totals = new TreeMap<>();
        totals.put("Harbor", 25);
        totals.put("Harbor", 20);
        System.out.println(totals);
    }
}
```

It prints `{Harbor=20}`. The second `put` replaced 25. Add to the old total: `totals.put("Harbor", totals.getOrDefault("Harbor", 0) + 20);`.

### 3. Expecting a HashMap to be sorted

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Map<String, Integer> totals = new HashMap<>();
        totals.put("Uptown", 12);
        totals.put("Harbor", 45);
        totals.put("Riverside", 30);
        totals.put("Old Mill", 8);
        System.out.println(totals);
    }
}
```

It prints `{Uptown=12, Harbor=45, Old Mill=8, Riverside=30}`. A `HashMap` promises no order. Use `new TreeMap<>()` when you need the keys sorted.
