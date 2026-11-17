## Hook

Picture the Spoke coat room. It has 8 hooks, numbered 0 to 7. When Kakashi hangs his jacket, a simple rule turns his name into a hook number. When he comes back, the same rule gives the same number. He walks straight to that hook. He never checks the other seven.

That rule is the whole secret of `HashMap`. It is also why a bad rule made the BikeId lookups slow. If the rule sends every jacket to hook 1, hook 1 holds a giant pile, and you dig through it every time.

## Concept

### A hash table in one sentence

```callout jargon
A **hash table** stores key and value pairs in an array of slots. It turns each key into a slot number, so it can jump straight to the right slot. `HashMap` is Java's hash table.
```

Each slot is called a **bucket**. The table you build in this topic starts with 8 buckets.

### Step 1: hashCode gives a number

Every Java object has a `hashCode()` method. It returns an `int` worked out from the object. Equal strings always give the same number.

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Riverside".hashCode());
        System.out.println("Harbor".hashCode());
        System.out.println("Riverside".hashCode());
    }
}
```

The number can be huge, and it can be negative. `"Harbor"` gives `-2140592980`.

### Step 2: squeeze the number into a bucket index

```callout jargon
**Hashing** means turning a key into a bucket index. The index must be between 0 and the bucket count minus 1.
```

### Syntax: Math.floorMod

```java nocompile
int index = Math.floorMod(key.hashCode(), buckets);
```

`Math.floorMod(a, b)` gives the remainder of `a / b`, and it is never negative when `b` is positive. The `%` operator can give a negative remainder, which is not a valid array index.

```java run
public class Main {
    public static void main(String[] args) {
        String[] stations = {"Riverside", "Old Mill", "Harbor", "Uptown"};
        for (String station : stations) {
            int hash = station.hashCode();
            System.out.println(station + " -> % gives " + (hash % 8)
                    + ", floorMod gives " + Math.floorMod(hash, 8));
        }
    }
}
```

Look at Harbor and Uptown. `%` gives `-4` and `-3`. `floorMod` gives `4` and `5`, which are safe indexes.

### Step 3: collisions and chaining

Old Mill and Uptown both land in bucket 5. Two keys in the same bucket is called a **collision**. Collisions are normal, because many keys share a few buckets.

```callout jargon
**Chaining** handles collisions by keeping a small linked list in each bucket. Each node in the list, called an **entry**, holds a key, a value and a link to the next entry.
```

```mermaid
flowchart LR
  B3["bucket 3"] --> R["Riverside = 12"]
  B4["bucket 4"] --> H["Harbor = 9"]
  B5["bucket 5"] --> U["Uptown = 7"] --> O["Old Mill = 10"]
  B0["bucket 0"] --> N0["null"]
```

To find Old Mill, go to bucket 5. Walk the chain and compare each key with `equals`. Stop when one matches.

### Syntax: an entry and a chain walk

```java nocompile
private static class Entry {
    String key;
    Integer value;
    Entry next;
}

Entry entry = buckets[index];
while (entry != null) {
    if (entry.key.equals(key)) {
        return entry.value;
    }
    entry = entry.next;
}
return null;
```

Two different strings can even share the exact same `hashCode`. `"Aa"` and `"BB"` both give `2112`. Chaining still works, because `equals` tells them apart.

### Syntax: unlinking from a chain

To remove a key, walk the chain and keep the entry before the current one in `prev`. When you find the key, link `prev` past it. If the match is the first entry, there is no `prev`, so set `buckets[i] = e.next`.

```java nocompile
Entry prev = null;
Entry e = buckets[i];
while (e != null) {
    if (e.key.equals(key)) {
        if (prev == null) {
            buckets[i] = e.next;
        } else {
            prev.next = e.next;
        }
        size--;
        return e.value;
    }
    prev = e;
    e = e.next;
}
return null;
```

### Step 4: load factor and resizing

```callout jargon
The **load factor** is the number of entries divided by the number of buckets. With 6 entries in 8 buckets, the load factor is 0.75.
```

As the table fills, chains get longer and lookups slow down. So the table grows. In this topic, it doubles its buckets when `size > 0.75 * buckets`. `HashMap` uses the same 0.75 limit.

```callout warning
When the bucket count changes, every key's index changes too. Harbor sits in bucket 4 out of 8, but in bucket 12 out of 16. So resizing must put every entry into the new array again, at its new index. This step is called **rehashing**.
```

### Step 5: equals and hashCode must agree

You met this rule in [equals, hashCode and toString](/t/module-02-oop/topic-10-equals-hashcode-tostring). Now you can see why it matters.

- `hashCode` picks the bucket.
- `equals` finds the key inside that bucket.

If two equal keys give different hash codes, they go to different buckets. `get` looks in the wrong bucket and returns `null`. If every key gives the same hash code, as the old BikeId did, every entry lands in one bucket. That one chain holds everything, and each lookup walks all of it.

### How fast is it?

| Operation | Average | Worst case | In plain words |
|---|---|---|---|
| put, get, remove | O(1) | O(n) | Usually one jump and a short chain. With a broken hashCode, one long chain |
| resize | O(n) | O(n) | Touches every entry, but doubling makes it rare |
| indexFor | O(1) | O(1) | One hashCode and one remainder |

O(1) means the cost stays the same as the table grows. O(n) means doubling the entries doubles the time.

## Worked example

This program builds a tiny table by hand, with only 4 buckets and no resizing. Read it, then run it.

```java run
public class Main {
    static class Entry {
        String key;
        Integer value;
        Entry next;

        Entry(String key, Integer value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    static Entry[] buckets = new Entry[4];

    public static void main(String[] args) {
        put("Aa", 1);
        put("BB", 2);
        put("Harbor", 9);
        System.out.println(get("Aa") + " " + get("BB") + " " + get("Harbor"));
        System.out.println("Aa and BB share bucket " + Math.floorMod("Aa".hashCode(), 4));
        System.out.println(get("Uptown"));
    }

    static void put(String key, Integer value) {
        int i = Math.floorMod(key.hashCode(), buckets.length);
        for (Entry e = buckets[i]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                e.value = value;
                return;
            }
        }
        buckets[i] = new Entry(key, value, buckets[i]);
    }

    static Integer get(String key) {
        int i = Math.floorMod(key.hashCode(), buckets.length);
        for (Entry e = buckets[i]; e != null; e = e.next) {
            if (e.key.equals(key)) {
                return e.value;
            }
        }
        return null;
    }
}
```

Line by line:

1. `Entry` is a node in a chain: a key, a value and a link to the next entry.
2. `put` finds the bucket with `floorMod`. If the key is already in the chain, it replaces the value.
3. Otherwise it makes a new entry at the front of the chain. The old first entry becomes its `next`.
4. `get` finds the same bucket and walks the chain, comparing keys with `equals`.
5. `"Aa"` and `"BB"` share bucket 0, yet both values come back. Uptown was never stored, so `get` returns `null`.

## Try it

See where bike ids land. Run it, then change `buckets` to 16 and watch the indexes move.

```java run
public class Main {
    public static void main(String[] args) {
        int buckets = 8;
        String[] bikes = {"B-1042", "B-2210", "B-3307", "Aa", "BB"};
        for (String bike : bikes) {
            System.out.println(bike + " -> bucket " + Math.floorMod(bike.hashCode(), buckets));
        }
    }
}
```

With 8 buckets, B-2210 and B-3307 collide in bucket 2. With 16, B-2210 moves to bucket 10.

## Recap

- A hash table turns a key into a bucket index with `Math.floorMod(key.hashCode(), buckets)`.
- Keys that land in the same bucket form a chain. `equals` finds the right one.
- When `size > 0.75 * buckets`, double the buckets and rehash every entry.
- `equals` and `hashCode` must agree, or lookups miss. A constant `hashCode` puts everything in one chain.
- Lookups are O(1) on average and O(n) in the worst case.

## Common mistakes

### Using % for the index

```java run crash
public class Main {
    public static void main(String[] args) {
        String[] buckets = new String[8];
        int index = "Harbor".hashCode() % buckets.length;
        buckets[index] = "Harbor";
    }
}
```

The program stops with:

```text
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index -4 out of bounds for length 8
	at Main.main(Main.java:5)
```

`"Harbor".hashCode()` is negative, so `%` gives `-4`. Use `Math.floorMod("Harbor".hashCode(), buckets.length)`, which gives `4`.

### Comparing keys with ==

```java
public class Main {
    public static void main(String[] args) {
        String stored = "Old Mill";
        String typed = new String("Old Mill");
        System.out.println(stored == typed);
        System.out.println(stored.equals(typed));
    }
}
```

This prints `false` and then `true`. `==` checks whether both names point to the same object. A key typed by a rider is often a different object with the same letters. Walk the chain with `equals`.

### Overriding equals without hashCode

```java
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        HashMap<BikeId, String> docks = new HashMap<>();
        docks.put(new BikeId("B-1042"), "Uptown");
        System.out.println(docks.get(new BikeId("B-1042")));
    }
}

class BikeId {
    final String id;

    BikeId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BikeId b && b.id.equals(id);
    }
}
```

This usually prints `null`. The two BikeId objects are equal, but each gets a different default `hashCode`, so `get` searches the wrong bucket. Add `public int hashCode() { return id.hashCode(); }`. Never return a constant such as `1`. That works, but it puts every bike in one long chain.
