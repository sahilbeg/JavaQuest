## Hook

Bike B-1042 did three rides from Harbor this morning. The dashboard says "3 bikes used today". Itachi knows that is wrong. One bike did three rides.

A list keeps every entry, even repeats. Itachi needs a box that keeps each bike only once. Java has exactly that box. It is called a set.

## Concept

### What a set is

```callout jargon
A **set** is a collection that never holds the same value twice. If you add a value that is already inside, the set stays the same.
```

`Set` is an interface in `java.util`, just like `List`. You pick a class that implements it. The three you will use most are `HashSet`, `LinkedHashSet` and `TreeSet`.

```callout analogy
Think of a stamp card at a fair. Each stall stamps one box. Visit the same stall again, and you get no new stamp. The card shows which stalls you visited. It does not count repeat visits.
```

### Syntax: creating a set

```java nocompile
Set<String> bikes = new HashSet<>();
```

Import `java.util.Set` and `java.util.HashSet`, or write `import java.util.*;` to get them all.

### Syntax: add returns true or false

```java nocompile
boolean added = bikes.add("B-1042");
```

`add` returns `true` when the value was new. It returns `false` when the set already had it. The set does not change in that case.

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Set<String> bikes = new HashSet<>();
        System.out.println(bikes.add("B-1042")); // true
        System.out.println(bikes.add("B-1042")); // false
        System.out.println(bikes.size());        // 1
    }
}
```

### Syntax: contains, remove, size

```java nocompile
bikes.contains("B-1042");  // true if it is inside
bikes.remove("B-1042");    // true if it was there and is now gone
bikes.size();              // how many different values
```

`contains` on a `HashSet` is very fast. It does not walk through every value the way a list does.

### Syntax: a set from a list

```java nocompile
Set<String> unique = new HashSet<>(rideIds);
```

Pass a list to the constructor. The set copies every value and drops the repeats. This is the shortest way to count different values.

### Three kinds of set

| Class | Order when you loop or print |
|---|---|
| `HashSet` | No promised order. It may look random. |
| `LinkedHashSet` | The order you added the values. |
| `TreeSet` | Sorted. Strings go in alphabetical order, numbers go from small to big. |

```java run
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<String> rides = List.of("Uptown", "Harbor", "Riverside", "Harbor", "Old Mill");
        Set<String> inOrder = new LinkedHashSet<>(rides);
        Set<String> sorted = new TreeSet<>(rides);
        System.out.println(inOrder);
        System.out.println(sorted);
    }
}
```

Both sets drop the second `Harbor`. The `LinkedHashSet` keeps the arrival order. The `TreeSet` sorts the names.

A set has no index. There is no `get(0)`. You loop over it with a for-each loop instead.

```java nocompile
for (String id : sorted) {
    System.out.println(id);
}
```

### Syntax: set operations

```java nocompile
a.addAll(b);     // union: a now holds everything from a and b
a.retainAll(b);  // intersection: a keeps only values also in b
a.removeAll(b);  // difference: a drops every value that is in b
```

All three change `a`. If you still need the original, copy it first: `Set<String> both = new TreeSet<>(a);`.

```callout warning
`retainAll` and `removeAll` change the set you call them on. Make a copy first if you need the original later.
```

### Sets use equals and hashCode

How does a `HashSet` know two values are "the same"? It asks `hashCode()` to find the right bucket. Then it asks `equals()` to compare. You met both in module 2, topic 10.

`String`, `Integer` and records already have good versions. Your own class needs both overrides. Without them, two `Bike` objects with the same id count as two bikes.

## Worked example

Count the different bikes and find the bikes seen at two stations.

```java run
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<String> harbor = List.of("B-1042", "B-2001", "B-1042", "B-1042", "B-3300");
        List<String> uptown = List.of("B-3300", "B-7777", "B-1042");

        Set<String> harborBikes = new TreeSet<>(harbor);
        System.out.println("Rides at Harbor: " + harbor.size());
        System.out.println("Bikes at Harbor: " + harborBikes.size());

        Set<String> both = new TreeSet<>(harborBikes);
        both.retainAll(new HashSet<>(uptown));
        System.out.println("At both: " + both);
    }
}
```

Line by line:

- `harbor` is a list with five rides. B-1042 appears three times.
- `new TreeSet<>(harbor)` keeps one copy of each id, sorted. It holds three ids.
- `both` starts as a copy of `harborBikes`, so `harborBikes` stays untouched.
- `retainAll` keeps only ids that Uptown also has: B-1042 and B-3300.

## Try it

This loop finds the first bike that shows up twice. Change the list and run it again.

```java run
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<String> rides = List.of("B-2001", "B-1042", "B-3300", "B-1042", "B-2001");
        Set<String> seen = new HashSet<>();
        String repeat = null;
        for (String id : rides) {
            if (!seen.add(id)) {
                repeat = id;
                break;
            }
        }
        System.out.println("First repeat: " + repeat);
    }
}
```

`seen.add(id)` returns `false` for an id already in `seen`. That is the first repeat. Try a list with no repeats and see `null`.

## Recap

- A `Set` never holds the same value twice. `add` returns `false` for a duplicate.
- `HashSet` has no order, `LinkedHashSet` keeps insertion order, `TreeSet` keeps values sorted.
- `contains`, `remove` and `size` work like they do on a list. There is no `get`.
- `addAll`, `retainAll` and `removeAll` give union, intersection and difference. They change the set you call them on.
- Sets compare values with `equals` and `hashCode`, so your own classes need both.

## Common mistakes

### 1. Asking a set for an index

```java nocompile
Set<String> bikes = new TreeSet<>(List.of("B-2001", "B-1042"));
System.out.println(bikes.get(0));
```

The compiler says:

```text
error: cannot find symbol
  symbol:   method get(int)
```

A set has no positions. Loop with for-each, or copy it into a list first: `new ArrayList<>(bikes).get(0)`.

### 2. A class without equals and hashCode

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Set<Bike> bikes = new HashSet<>();
        bikes.add(new Bike("B-1042"));
        bikes.add(new Bike("B-1042"));
        System.out.println(bikes.size());
    }
}

class Bike {
    private final String id;
    Bike(String id) { this.id = id; }
}
```

It prints `2`. The set sees two different objects. Override `equals` and `hashCode` using `id`, or make `Bike` a record. Then it prints `1`.

### 3. Losing the original set with retainAll

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Set<String> harbor = new TreeSet<>(List.of("B-1042", "B-2001", "B-3300"));
        Set<String> uptown = new TreeSet<>(List.of("B-1042"));
        harbor.retainAll(uptown);
        System.out.println("Bikes at Harbor: " + harbor.size());
    }
}
```

It prints `Bikes at Harbor: 1`, but Harbor had three bikes. `retainAll` changed `harbor`. Copy first: `Set<String> both = new TreeSet<>(harbor); both.retainAll(uptown);`.
