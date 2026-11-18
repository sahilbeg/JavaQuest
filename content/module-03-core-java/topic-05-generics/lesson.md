## Hook

Picture a shoe rack at the Spoke office. It has 12 slots. You can put shoes on it, or helmets, or water bottles. The rack does not care what goes on it. It only cares that it has 12 slots.

The dispatch code has three racks written as three classes: `BikeShelf`, `RiderShelf` and `StationShelf`. They are the same code with a different word in the middle. When Tsunade fixed a bug in one, the other two kept the bug. This page shows you how to write the rack once and use it for any type.

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
Gojo says: the compiler checks the type for you. `station.put(42)` will not compile, because `station` holds only Strings. A mistake caught at compile time never reaches a rider.
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

### Syntax: ? extends and ? super

```java nocompile
static long totalMinutes(List<? extends Number> values) { ... }   // reads Numbers out
static void addTestRides(List<? super Integer> out) { ... }        // puts Integers in
```

Invariance is safe but strict. A **wildcard**, written `?`, loosens it in a safe way.

- `List<? extends Number>` accepts a `List<Integer>`, a `List<Long>` or a `List<Number>`. You can **read** each item as a `Number`. You cannot add to it, because Java does not know which list it really is.
- `List<? super Integer>` accepts a `List<Integer>`, a `List<Number>` or a `List<Object>`. You can **put** Integers into it. Reading only gives you `Object`.

```java run
import java.util.ArrayList;
import java.util.List;

public class Main {
    static long totalMinutes(List<? extends Number> values) {
        long sum = 0;
        for (Number n : values) {
            sum += n.longValue();
        }
        return sum;
    }

    static void addTestRides(List<? super Integer> out) {
        out.add(5);
        out.add(12);
    }

    public static void main(String[] args) {
        System.out.println(totalMinutes(List.of(10, 25)));
        System.out.println(totalMinutes(List.of(40L)));

        List<Number> numbers = new ArrayList<>();
        List<Object> things = new ArrayList<>();
        addTestRides(numbers);
        addTestRides(things);
        System.out.println(numbers + " " + things);
    }
}
```

It prints `35`, `40` and `[5, 12] [5, 12]`. Calling `values.add(5)` inside `totalMinutes` would fail with `error: incompatible types: int cannot be converted to CAP#1`.

```callout jargon
**PECS** means "producer extends, consumer super". If a parameter produces items for you to read, use `? extends T`. If it consumes items you put in, use `? super T`.
```

You will see this in the JDK itself. `List<E>` has `sort(Comparator<? super E> c)`. A comparator consumes the items it compares, so a comparator written for any `Number` can also sort a `List<Integer>`.

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

### Generics disappear at run time

Generics are checked by the compiler, then thrown away. At run time, a `List<String>` and a `List<Integer>` are both plain `ArrayList` objects.

```java run
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> names = new ArrayList<>();
        List<Integer> numbers = new ArrayList<>();
        System.out.println(names.getClass() == numbers.getClass());
        System.out.println(names.getClass().getName());
    }
}
```

It prints `true` and `java.util.ArrayList`. This is called **type erasure**.

```callout jargon
**Type erasure** means the compiler removes type arguments after checking them. The running program does not know what `T` was.
```

So three things are impossible inside a generic class, because they need `T` at run time:

- `new T()` gives `error: unexpected type ... required: class found: type parameter T`.
- `new T[10]` gives `error: generic array creation`.
- `o instanceof List<String>` gives `error: Object cannot be safely cast to List<String>`.

The usual fix is to ask the caller for what you need, such as a `List<T>` instead of an array.

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

- A generic class declares a type parameter like `<T>` and uses `T` as a type inside. The compiler erases `T` after checking it.
- The user picks the real type: `Box<String>`, `Box<Integer>`. The diamond `<>` repeats it for you.
- A generic method puts `<T>` before its return type, and Java works out `T` from the arguments.
- `List<String>` is not a `List<Object>`. Generics are invariant. Use `? extends T` to read and `? super T` to write (PECS).
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

**4. Using a raw type.**

```java nocompile
List raw = new ArrayList();
raw.add(42);
List<String> names = raw;
String first = names.get(0);
```

The compiler only says `Note: Main.java uses unchecked or unsafe operations.` Then the program crashes: `java.lang.ClassCastException: class java.lang.Integer cannot be cast to class java.lang.String`. Java could not check the raw list. Always write the type argument: `List<String> raw = new ArrayList<>();`. Treat every "unchecked" warning as a bug to fix.

```callout jargon
A **raw type** is a generic type used without its `<...>`, like plain `List`. It exists only for very old code.
```
