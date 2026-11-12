## Hook

Picture a shoe rack at the Spoke office. It has 12 slots. You can put shoes on it, or helmets, or water bottles. The rack does not care what goes on it. It only cares that it has 12 slots.

The dispatch code has three racks written as three classes: `BikeShelf`, `RiderShelf` and `StationShelf`. They are the same code with a different word in the middle. When Maya fixed a bug in one, the other two kept the bug. This page shows you how to write the rack once and use it for any type.

## Concept

You have already used this idea. `List<String>` and `List<Integer>` are the same `List` class holding different types. The word in the angle brackets `< >` tells the list what it holds. Now you will write classes like that yourself.

```callout jargon
**Generics** let you write a class or method once with a placeholder for a type. A **type parameter** is that placeholder, usually a single capital letter like `T`. The person who uses the class picks the real type.
```

### Syntax: a generic class

```java
class Box<T> {
    private T item;

    void put(T item) { this.item = item; }
    T take() { return item; }
}
```

- `<T>` after the class name declares the type parameter.
- Inside the class, `T` works like any type name. You can use it for fields, parameters and return types.
- `T` stands for "type". Other common letters are `E` (element), `K` (key) and `V` (value).

### Syntax: using a generic class

```java nocompile
Box<String> name = new Box<>();
Box<Integer> count = new Box<>();
```

You write the real type in the angle brackets. The empty `<>` on the right is called the **diamond**. Java copies the type from the left side, so you do not repeat it.

```java run
public class Main {
    static class Box<T> {
        private T item;
        void put(T item) { this.item = item; }
        T take() { return item; }
    }

    public static void main(String[] args) {
        Box<String> station = new Box<>();
        station.put("Riverside");
        System.out.println(station.take());

        Box<Integer> minutes = new Box<>();
        minutes.put(25);
        System.out.println(minutes.take() + 5);
    }
}
```

Notice `minutes.take() + 5`. Java knows `take()` returns an `Integer`, so the maths works with no cast. A **cast** is when you force a value into another type, like `(String) value`. Generics remove the need for most casts.

```callout tip
Byte says: the compiler checks the type for you. `station.put(42)` will not compile, because `station` holds only Strings. A mistake caught at compile time never reaches a rider.
```

### Syntax: two type parameters

```java
class Pair<A, B> {
    private final A first;
    private final B second;

    Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    A first() { return first; }
    B second() { return second; }
}
```

Separate the letters with a comma. A `Pair<String, Integer>` could hold a bike id and its minutes.

### Syntax: a generic method

```java
static <T> T firstOf(java.util.List<T> items) {
    return items.get(0);
}
```

A **generic method** declares its own type parameter. The `<T>` goes before the return type. Java works out `T` from the argument you pass. You do not write it yourself.

```java nocompile
String s = firstOf(List.of("Harbor", "Uptown"));   // T is String
Integer n = firstOf(List.of(12, 30));              // T is Integer
```

### A list of Object is not a parent of a list of String

A `String` is an `Object`. So you might expect a `List<String>` to be a `List<Object>`. It is not, and Java refuses this line:

```java nocompile
List<String> names = new ArrayList<>();
List<Object> things = names;   // does not compile
```

Here is why. If that line worked, you could call `things.add(42)`. Now a number sits inside a list that promised to hold only Strings. Java blocks the first line so that can never happen.

```callout jargon
This rule has a name: generics are **invariant**. `List<String>` and `List<Object>` are two unrelated types, even though `String` and `Object` are related.
```

### Syntax: bounded types

Sometimes `T` must be able to do something. To add numbers, `T` must be a number. A **bounded type** limits which types can replace `T`.

```java nocompile
static <T extends Number> double total(List<T> values) { ... }
```

`T extends Number` means "T is `Number` or one of its subclasses". `Integer`, `Double` and `Long` all extend `Number`. Every `Number` has a `doubleValue()` method, so you can call it on `T`.

```java run
import java.util.List;

public class Main {
    static <T extends Number> double total(List<T> values) {
        double sum = 0;
        for (T v : values) {
            sum += v.doubleValue();
        }
        return sum;
    }

    public static void main(String[] args) {
        System.out.println(total(List.of(10, 25, 5)));
        System.out.println(total(List.of(1.5, 2.5)));
    }
}
```

`total(List.of("a", "b"))` would not compile. `String` does not extend `Number`.

### Syntax: a bound on Comparable

```java nocompile
static <T extends Comparable<T>> T biggest(T a, T b) { ... }
```

`Comparable<T>` is an interface for types that can compare themselves. It has one method: `a.compareTo(b)`. It returns a negative number when `a` comes first, zero when they are equal, and a positive number when `a` comes after `b`. `String` and `Integer` already implement it. The next topic shows how to add it to your own classes.

## Worked example

Here is the generic method that finds the biggest item in a list.

```java run
import java.util.List;

public class Main {
    static <T extends Comparable<T>> T maxOf(List<T> items) {
        if (items.isEmpty()) {
            return null;
        }
        T best = items.get(0);
        for (T item : items) {
            if (item.compareTo(best) > 0) {
                best = item;
            }
        }
        return best;
    }

    public static void main(String[] args) {
        System.out.println(maxOf(List.of(12, 45, 30)));
        System.out.println(maxOf(List.of("Harbor", "Uptown", "Old Mill")));
    }
}
```

Line by line:

1. `<T extends Comparable<T>>` says T can be any type that can compare itself with another T.
2. An empty list has no biggest item, so the method returns `null`.
3. `best` starts as the first item.
4. The loop checks every item. When `item.compareTo(best) > 0`, the item comes after `best`, so it becomes the new `best`.
5. With numbers, the biggest is 45. With Strings, the order is alphabetical, so `"Uptown"` wins.

One method now works for numbers, words and any other comparable type.

## Try it

Change the program below. Add a `Box<Boolean>` that holds `true` and print it. Then try `station.put(7);` and read the compiler error.

```java run
public class Main {
    static class Box<T> {
        private T item;
        void put(T item) { this.item = item; }
        T take() { return item; }
    }

    public static void main(String[] args) {
        Box<String> station = new Box<>();
        station.put("Old Mill");
        System.out.println("Box holds " + station.take());
    }
}
```

## Recap

- A generic class declares a type parameter like `<T>` and uses `T` as a type inside.
- The user picks the real type: `Box<String>`, `Box<Integer>`. The diamond `<>` repeats it for you.
- A generic method puts `<T>` before its return type, and Java works out `T` from the arguments.
- `List<String>` is not a `List<Object>`. Generics are invariant.
- Bounds like `<T extends Number>` and `<T extends Comparable<T>>` let you call `doubleValue()` or `compareTo` on `T`.

## Common mistakes

**1. Using a primitive as the type argument.**

```java nocompile
Box<int> count = new Box<>();
```

The compiler says: `error: unexpected type ... required: reference found: int`. Type arguments must be classes. Use the wrapper class instead: `Box<Integer> count = new Box<>();`.

**2. Calling a method that plain T does not have.**

```java nocompile
static <T> double total(List<T> values) {
    double sum = 0;
    for (T v : values) {
        sum += v.doubleValue();
    }
    return sum;
}
```

The compiler says: `error: cannot find symbol ... symbol: method doubleValue()`. A plain `T` could be anything, so Java only allows `Object` methods. Add a bound: `static <T extends Number> double total(List<T> values)`.

**3. Putting the method's type parameter in the wrong place.**

```java nocompile
static T <T> lastOf(List<T> items) {
    return items.get(items.size() - 1);
}
```

The compiler says: `error: cannot find symbol ... symbol: class T`, among other errors. The `<T>` must come before the return type: `static <T> T lastOf(List<T> items)`.
