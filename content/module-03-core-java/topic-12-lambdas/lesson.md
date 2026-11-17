## Hook

Kakashi's ride sorter has a class called `ByMinutes`. It also has `ByStation`, `ByType`, `ByBikeId` and five more. Each class is ten lines long. Only one line in each one does real work: the line that compares two rides.

Imagine writing a whole letter just to say "sort by minutes". A lambda lets you send that one line alone. This lesson shows you how.

## Concept

### Step 1: the old way, a Comparator class

You wrote classes like this in topic 06.

```java nocompile
class ByMinutes implements Comparator<Ride> {
    public int compare(Ride a, Ride b) {
        return Integer.compare(a.minutes(), b.minutes());
    }
}

rides.sort(new ByMinutes());
```

### Step 2: an anonymous class

An anonymous class is a class with no name. You write its body right where you need one object of it.

```java nocompile
rides.sort(new Comparator<Ride>() {
    public int compare(Ride a, Ride b) {
        return Integer.compare(a.minutes(), b.minutes());
    }
});
```

That saves a file, but most of it is still boilerplate. Boilerplate is code you must type that says nothing new.

### Step 3: a lambda

`Comparator` has only one method to fill in. So Java already knows the method name, the parameter types and the return type. You only need to give the parameters and the work.

```java nocompile
rides.sort((a, b) -> Integer.compare(a.minutes(), b.minutes()));
```

```callout jargon
A **lambda** is a short piece of code you can pass around like a value. It has parameters, an arrow `->`, and a body. Java turns it into an object for an interface with one abstract method, such as `Comparator`.
```

### Syntax: lambda parameters

```java nocompile
() -> body               // no parameters
r -> body                // one parameter, brackets optional
(a, b) -> body           // two or more parameters need brackets
(Ride a, Ride b) -> body // types are optional
```

Java works out the types from where you use the lambda. You can still write them if it helps you read the code.

### Syntax: lambda bodies

```java nocompile
r -> r.minutes() > 30
(a, b) -> {
    int diff = a.minutes() - b.minutes();
    return diff;
}
```

A one-expression body needs no braces and no `return`. Its value is the result. A body with braces is a normal block, so it needs `return` and semicolons.

### Syntax: Comparator.comparing

```java nocompile
rides.sort(Comparator.comparing(r -> r.station()));
```

`Comparator.comparing` builds a comparator for you. You give it a lambda that pulls out a sort key. A sort key is the value you want to sort by, such as the station name.

### Syntax: thenComparing and reversed

```java nocompile
rides.sort(Comparator.comparing(Ride::station).thenComparing(Ride::minutes));
rides.sort(Comparator.comparing(Ride::minutes).reversed());
```

`thenComparing` breaks ties. Two Harbor rides are compared by minutes next. `reversed()` flips the order, so the longest ride comes first.

### Syntax: method references

```java nocompile
r -> r.minutes()          // a lambda
Ride::minutes             // the same thing as a method reference

s -> s.toUpperCase()
String::toUpperCase
```

A method reference is a shorter lambda. Use it when the lambda only calls one method on its parameter. Write the class, two colons, then the method name with no brackets.

```callout tip
Byte says: Read `Ride::minutes` as "use each ride's minutes". Read `String::toUpperCase` as "make each string upper case".
```

### Syntax: forEach and removeIf

```java nocompile
rides.forEach(r -> System.out.println(r.bikeId()));
rides.removeIf(r -> r.minutes() < 2);
```

`forEach` runs the lambda once for each item, in order. `removeIf` deletes every item where the lambda returns `true`. Both work on the list you call them on.

## Worked example

Here is Kakashi's sorter with three orders, written with lambdas.

```java run
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Ride> rides = new ArrayList<>();
        rides.add(new Ride("B-1042", "Riverside", "ebike", 25));
        rides.add(new Ride("B-2001", "Harbor", "classic", 8));
        rides.add(new Ride("B-1500", "Harbor", "ebike", 3));

        rides.sort((a, b) -> Integer.compare(a.minutes(), b.minutes()));
        System.out.println("By minutes:");
        rides.forEach(r -> System.out.println("  " + r.bikeId() + " " + r.minutes()));

        rides.sort(Comparator.comparing(Ride::station).thenComparing(Ride::minutes));
        System.out.println("By station, then minutes:");
        rides.forEach(r -> System.out.println("  " + r.station() + " " + r.minutes()));

        rides.removeIf(r -> r.minutes() < 5);
        System.out.println("Rides left: " + rides.size());
    }
}

record Ride(String bikeId, String station, String type, int minutes) {}
```

Line by line:

1. The `record Ride` line at the bottom defines the ride shape, as in module 2.
2. The first `sort` uses a lambda with two parameters. It orders by minutes: 3, 8, 25.
3. `forEach` prints each ride. The lambda receives one ride at a time.
4. The second `sort` uses `comparing` with a method reference. Harbor comes before Riverside, and the two Harbor rides go by minutes.
5. `removeIf` drops the 3 minute ride, so 2 rides are left.

Method references also work with `forEach`. Here `System.out::println` means "print each item". `System.out` is an object, so the part before `::` can also be an object.

```java run
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> ids = new ArrayList<>(List.of("b-1042", "b-2001"));
        List<String> upper = new ArrayList<>();
        for (String id : ids) {
            upper.add(id.toUpperCase());
        }
        upper.forEach(System.out::println);
    }
}
```

## Try it

Change the sort below so the longest ride comes first. Then add a second line that removes every cargo ride with `removeIf`. Run it and check the output.

```java run
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Ride> rides = new ArrayList<>();
        rides.add(new Ride("B-1042", "Uptown", "cargo", 40));
        rides.add(new Ride("B-2001", "Old Mill", "classic", 12));
        rides.add(new Ride("B-1500", "Harbor", "ebike", 30));
        rides.sort(Comparator.comparing(Ride::minutes));
        rides.forEach(System.out::println);
    }
}

record Ride(String bikeId, String station, String type, int minutes) {}
```

## Recap

- A lambda is `parameters -> body`. It replaces a whole class when the interface has one method to fill in.
- A one-expression body needs no braces and no `return`.
- `Comparator.comparing(key)` sorts by a key. `thenComparing` breaks ties and `reversed()` flips the order.
- A method reference such as `Ride::minutes` is a shorter lambda that calls one method.
- `forEach` runs a lambda for every item. `removeIf` deletes the items that match.

## Common mistakes

**1. Writing return without braces.**

```java nocompile
rides.sort((a, b) -> return a.minutes() - b.minutes());
```

The compiler says `error: illegal start of expression`. Fix: drop `return`, as in `(a, b) -> a.minutes() - b.minutes()`. Or keep it inside braces: `(a, b) -> { return a.minutes() - b.minutes(); }`.

**2. Adding brackets to a method reference.**

```java nocompile
rides.sort(Comparator.comparing(Ride::minutes()));
```

The compiler says `error: ')' or ',' expected`. Fix: a method reference has no brackets, so write `Ride::minutes`.

**3. Removing items inside a for-each loop.**

```java
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Integer> minutes = new ArrayList<>(List.of(3, 25, 1, 40));
        for (Integer m : minutes) {
            if (m < 5) {
                minutes.remove(m);
            }
        }
        System.out.println(minutes);
    }
}
```

This compiles, then crashes with `java.util.ConcurrentModificationException`. The loop notices that the list changed under it. Fix: write `minutes.removeIf(m -> m < 5);` and drop the loop.

**4. Changing a variable after a lambda uses it.**

```java nocompile
int limit = 5;
rides.removeIf(r -> r.minutes() < limit);
limit = 10;
```

The compiler says `error: local variables referenced from a lambda expression must be final or effectively final`. Fix: don't change a variable after a lambda uses it.
