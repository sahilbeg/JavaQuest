## Hook

Itachi opens the log and reads this line: `Bike@1b6d3586`. Which bike is that? Nobody can tell.

The lost-bike report has a second problem. It lists B-1042 twice and counts two lost bikes. The rider and the station scanner each made a `Bike` object for the same bike. Java sees two objects, so it thinks they are two bikes.

Both bugs have the same cure. You teach `Bike` how to describe itself and how to decide when two bikes are the same bike.

## Concept

### Object: every class's parent

Every class you write already has a parent, even without `extends`. That parent is `Object`. Java adds `extends Object` for you.

```callout jargon
**Object** is the class at the top of every family tree in Java. Every object inherits a few methods from it, including `toString()`, `equals(Object)` and `hashCode()`.
```

The inherited versions are very plain. You **override** them to make them useful. Override means "replace the parent's method with your own", as in the inheritance lesson.

### Syntax: toString

```java nocompile
@Override
public String toString() {
    return "Bike[" + id + ", " + type + "]";
}
```

`toString()` returns the text that describes an object. Java calls it for you whenever it needs text, for example in `println` or with `+`.

```java nocompile
Bike bike = new Bike("B-1042", "ebike");
System.out.println(bike);              // Bike[B-1042, ebike]
System.out.println("Lost: " + bike);   // Lost: Bike[B-1042, ebike]
```

Without the override, you get the `Object` version: the class name, an `@`, and a number in hexadecimal (a base-16 number). That is the `Bike@1b6d3586` in Itachi's log.

`@Override` asks the compiler to check that a parent really has this method. If you type `tostring` by mistake, the compiler stops you.

### == compares identity

```java nocompile
Bike a = new Bike("B-1042", "ebike");
Bike b = new Bike("B-1042", "ebike");
System.out.println(a == b);   // false
System.out.println(a == a);   // true
```

**Identity** means "the very same object in memory". `==` on objects checks identity. It asks whether two references point to one object. Watch the arrows:

```animate
title: Two bikes, or one bike twice?
kind: code
code: |
  public class Main {
      public static void main(String[] args) {
          Bike a = new Bike("B-1042");
          Bike b = new Bike("B-1042");
          Bike c = a;
          System.out.println(a == b);
          System.out.println(a == c);
      }
  }
  class Bike {
      String id;
      Bike(String id) { this.id = id; }
  }
steps:
  - line: 3
    vars: { a: "@b1" }
    objects: { b1: { type: Bike, fields: { id: '"B-1042"' } } }
    say: The rider's app builds a Bike for B-1042. a holds an arrow to it.
  - line: 4
    vars: { b: "@b2" }
    objects: { b2: { type: Bike, fields: { id: '"B-1042"' } } }
    say: The scanner builds its own Bike with the same id. A second new means a second object.
  - line: 5
    vars: { c: "@b1" }
    say: No new here. c copies a's arrow, so c and a point at the very same object.
  - line: 6
    out: "false"
    say: a == b asks one question. Do both arrows end at one object? They end at two objects, so false, even though the ids match.
  - line: 7
    out: "true"
    say: a and c end at the same object, so a == c is true.
```

### equals compares meaning

```callout analogy
Two printed copies of the same ride receipt are two pieces of paper. They are not the same paper (identity), yet they say the same thing (equality). `equals` is how you decide what "say the same thing" means for your class.
```

The `Object` version of `equals` just uses `==`. So you override it. For Spoke, two bikes are equal when their ids are equal. The type does not matter, because the id alone names a bike.

### Syntax: instanceof with a pattern

```java nocompile
if (other instanceof Bike that) {
    // here, that is other viewed as a Bike
}
```

`equals` receives an `Object`, because anything can be compared with anything. `instanceof` checks whether `other` really is a `Bike`. If it is, Java also gives you a new variable `that` of type `Bike`. You can then read `that.id`. For `null`, `instanceof` is always false.

### Syntax: overriding equals

```java nocompile
@Override
public boolean equals(Object other) {
    if (this == other) {
        return true;
    }
    if (!(other instanceof Bike that)) {
        return false;
    }
    return Objects.equals(id, that.id);
}
```

Read it top to bottom:

1. Same object? Then it is certainly equal.
2. Not a `Bike`, or `null`? Then it is not equal.
3. Otherwise compare the ids. `Objects.equals` (from `java.util.Objects`) is like `id.equals(that.id)`, but it is also safe when an id is `null`.

The parameter type must be `Object`. Writing `equals(Bike other)` creates a new method that nobody calls. `@Override` catches that mistake.

### Syntax: hashCode

```java nocompile
@Override
public int hashCode() {
    return Objects.hash(id);
}
```

```callout jargon
A **hash code** is an `int` that summarises an object. Fast lookup tools sort objects into buckets by this number, and only compare with `equals` inside one bucket.
```

`Objects.hash(...)` lives in `java.util.Objects`, so add `import java.util.Objects;` at the top of the file. `return id.hashCode();` works too.

### Why equal bikes need equal hash codes

The rule is simple: **if two objects are equal, their hash codes must be equal**. Otherwise a lookup tool searches the wrong bucket and misses a bike that is really there. So build `hashCode` from the same fields that `equals` uses. Here that is `id` only.

```animate
title: Finding the right bucket
kind: scene
nodes:
  - { id: rider, label: "Rider's B-1042", emoji: "🚲", row: 0, col: 0 }
  - { id: scanner, label: "Scanner's B-1042", emoji: "🚲", row: 0, col: 2 }
  - { id: hash, label: hashCode(), emoji: "🔢", row: 1, col: 1 }
  - { id: b0, label: Bucket 0, emoji: "🪣", value: empty, row: 2, col: 0 }
  - { id: b1, label: Bucket 1, emoji: "🪣", value: empty, row: 2, col: 1 }
  - { id: b2, label: Bucket 2, emoji: "🪣", value: empty, row: 2, col: 2 }
steps:
  - say: The lost-bike report adds the rider's B-1042 to a lookup tool. First the tool asks the bike for its hash code.
    send: { from: rider, to: hash, label: "B-1042" }
  - say: The id gives a number, and the number picks bucket 2. The bike is stored there.
    send: { from: hash, to: b2, label: "bucket 2" }
    set: { b2: "B-1042" }
  - say: Now the scanner's B-1042 arrives. It is a different object, but hashCode uses only the id, so it gives the same number.
    send: { from: scanner, to: hash, label: "B-1042" }
  - say: Same number, same bucket. Only inside bucket 2 does the tool call equals. It says true, so this is a duplicate and it is not added again. One lost bike.
    send: { from: hash, to: b2, label: "bucket 2" }
    focus: [b2]
  - say: Now imagine Bike forgot hashCode. Object's version usually gives each object its own number, so the scanner's copy could land in bucket 0.
    send: { from: hash, to: b0, label: "bucket 0" }
    set: { b0: "B-1042" }
  - say: Nobody looks in bucket 2, so equals is never asked. B-1042 is stored twice, and the report counts two lost bikes.
    focus: [b0, b2]
```

### The rules every equals must keep

Lookup tools, sorting and tests all trust your `equals`. So every `equals` must keep five promises. Together they are called the **equals contract**.

```callout jargon
A **contract** is a set of promises a method makes. Other code relies on them without reading your code. Break one, and that code quietly gives wrong answers.
```

| Rule | In plain words |
|---|---|
| Reflexive | `a.equals(a)` is always `true`. A bike equals itself. |
| Symmetric | If `a.equals(b)` is `true`, then `b.equals(a)` is `true` too. |
| Transitive | If `a` equals `b` and `b` equals `c`, then `a` equals `c`. |
| Consistent | Ask again and you get the same answer, unless a compared field changed. |
| Not null | `a.equals(null)` is `false`. It never crashes. |

Comparing one `final` id keeps all five. The `instanceof` check gives you the last one for free, because `null instanceof Bike` is `false`.

### The subclass trap: instanceof or getClass

Trouble starts when a subclass adds a field and compares it too. Here `EBike` adds `battery`:

```java run
public class Main {
    public static void main(String[] args) {
        Bike plain = new Bike("B-1042");
        EBike charged = new EBike("B-1042", 80);
        System.out.println(plain.equals(charged));
        System.out.println(charged.equals(plain));
    }
}

class Bike {
    final String id;
    Bike(String id) { this.id = id; }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Bike that)) {
            return false;
        }
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

class EBike extends Bike {
    final int battery;
    EBike(String id, int battery) {
        super(id);
        this.battery = battery;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof EBike that)) {
            return false;
        }
        return id.equals(that.id) && battery == that.battery;
    }
}
```

It prints `true`, then `false`. An `EBike` is a `Bike`, but a plain `Bike` is not an `EBike`. Symmetry is broken, so a lookup tool may find the bike or miss it, depending on the order it compares.

The fix depends on what "same" means:

- For Spoke, the id alone names a bike. So `EBike` should not override `equals` at all.
- If a subclass truly needs its own fields in `equals`, compare classes instead of using `instanceof`. Then a `Bike` never equals an `EBike`:

```java nocompile
if (other == null || getClass() != other.getClass()) {
    return false;
}
```

### A key that changes gets lost

```callout jargon
A **HashSet** is Java's ready-made lookup tool. It keeps each item once. It uses `hashCode` to pick a bucket and `equals` inside that bucket. Module 3 covers it fully.
```

A set stores an object in the bucket for its hash code **at the moment it was added**. Change a field that `hashCode` uses, and the set looks in the wrong bucket. In the code, `Set<Bike>` means a set that holds `Bike` objects. `<>` repeats that type.

```java run
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Bike bike = new Bike("B-1042");
        Set<Bike> lost = new HashSet<>();
        lost.add(bike);
        System.out.println(lost.contains(bike));

        bike.setId("B-1043");   // the crew re-labels the bike
        System.out.println(lost.contains(bike));
        System.out.println(lost.size());
    }
}

class Bike {
    private String id;
    Bike(String id) { this.id = id; }
    void setId(String id) { this.id = id; }

    @Override
    public boolean equals(Object other) {
        return other instanceof Bike that && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
```

It prints `true`, `false`, `1`. The very same object is still inside the set, yet `contains` cannot find it. In production, this looks like data that vanished.

The fix: build `equals` and `hashCode` only from fields that never change. Make them `final`, as `id` is in the exercise.

### Fields that may be null: Objects.equals

`id.equals(that.id)` calls a method on `id`. If `id` is `null`, there is no object to call it on, and the program crashes:

```java run crash
public class Main {
    public static void main(String[] args) {
        Bike unlabelled = new Bike(null);
        Bike tagged = new Bike("B-1042");
        System.out.println(unlabelled.equals(tagged));
    }
}

class Bike {
    private final String id;
    Bike(String id) { this.id = id; }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Bike that)) {
            return false;
        }
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
```

```text
Exception in thread "main" java.lang.NullPointerException: Cannot invoke "String.equals(Object)" because "this.id" is null
```

`Objects.equals(id, that.id)` checks for `null` first. Two `null`s are equal, and one `null` is not equal to text. `Objects.hash(id)` is null-safe in the same way. Use them for any field that might be `null`.

### Copying a bike: a copy constructor

Sometimes you want a new object with the same values. Write a **copy constructor**: a constructor that takes another object of the same class.

```java nocompile
Bike(Bike other) {
    this(other.id, other.type);
}
```

`Object` also has a `clone()` method. It is awkward to use correctly, so most teams avoid it. A copy constructor is plain, readable Java.

## Worked example

```java run
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        Bike fromRider = new Bike("B-1042", "ebike");
        Bike fromScanner = new Bike("B-1042", "ebike");
        System.out.println(fromRider);
        System.out.println(fromRider == fromScanner);
        System.out.println(fromRider.equals(fromScanner));
        System.out.println(fromRider.hashCode() == fromScanner.hashCode());
    }
}

class Bike {
    private final String id;
    private final String type;

    Bike(String id, String type) {
        this.id = id;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Bike[" + id + ", " + type + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Bike that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

Line by line:

- `println(fromRider)` calls `toString()` for you and prints `Bike[B-1042, ebike]`.
- `fromRider == fromScanner` is `false`. Two `new` calls made two objects.
- `fromRider.equals(fromScanner)` is `true`. Your override compares ids, and both are `B-1042`.
- The hash codes match too, because both come from the same id.

## Try it

This report counts distinct bikes with nested loops. The inner loop asks: "did this bike appear earlier?"

```java run
public class Main {
    public static void main(String[] args) {
        Bike[] report = { new Bike("B-1042"), new Bike("B-2001"), new Bike("B-1042") };
        int distinct = 0;
        for (int i = 0; i < report.length; i++) {
            boolean seenBefore = false;
            for (int j = 0; j < i; j++) {
                if (report[i].equals(report[j])) {
                    seenBefore = true;
                }
            }
            if (!seenBefore) {
                distinct++;
            }
        }
        System.out.println("Different bikes: " + distinct);
    }
}

class Bike {
    private final String id;
    Bike(String id) { this.id = id; }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Bike that)) {
            return false;
        }
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
```

It prints `Different bikes: 2`. Now change `report[i].equals(report[j])` to `report[i] == report[j]` and run it again. You get 3, because `==` sees three separate objects.

## Recap

- Every class extends `Object`. Override its `toString()` so `println` shows readable text such as `Bike[B-1042, ebike]`.
- `==` on objects checks identity. `equals` checks meaning, once you override it.
- Override `equals(Object other)` with `instanceof Bike that`, then compare the fields that matter.
- Equal objects must have equal hash codes. Build `hashCode()` from the same fields as `equals`.
- Keep the equals contract. Compare only fields that never change, and use `Objects.equals` when a field may be `null`.

## Common mistakes

**1. Comparing objects with ==.**

```java nocompile
if (bikes[i] == target) {
    return true;
}
```

This compiles, but `contains` returns `false` for a bike that is in the report. The target is a different object with the same id. Use `bikes[i].equals(target)`.

**2. Writing equals with the wrong parameter type.**

```java nocompile
@Override
public boolean equals(Bike other) {
    return id.equals(other.id);
}
```

The compiler stops you:

```text
error: method does not override or implement a method from a supertype
```

`Object`'s method takes an `Object`. Change the parameter to `Object other` and use `instanceof Bike that` to get a `Bike`.

**3. Overriding equals but forgetting hashCode.**

```java nocompile
@Override
public boolean equals(Object other) {
    if (!(other instanceof Bike that)) {
        return false;
    }
    return id.equals(that.id);
}
// no hashCode method
```

This compiles, and `equals` works. But `hashCode()` still comes from `Object`, so two equal bikes almost always give different numbers. Lookup tools then treat them as different bikes. Add `hashCode()` that returns `Objects.hash(id)`.
