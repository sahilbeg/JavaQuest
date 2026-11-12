## Hook

Picture Theo looking for bike B-1042 in a paper logbook. The logbook holds every ride of the year. Theo starts at page one and reads each line until he finds the bike.

In January the logbook is thin, so the search is quick. By November it holds thousands of rides. Theo is no slower at reading. The book simply got bigger, and his method reads every line.

The kiosk has the same problem. This page teaches you to predict how a method slows down as the data grows, before any rider has to wait.

## Concept

### Count steps instead of timing

You could time a method with a stopwatch. The result changes with the laptop, the weather and what else is running. A better question is: "How many steps does it take for `n` items?"

```callout jargon
**n** is the size of the input, such as the number of rides in a list. A **step** is one small piece of work, such as one comparison or one pass through a loop body.
```

Here is a loop that does one step per ride.

```java
int n = 5;
int steps = 0;
for (int i = 0; i < n; i++) {
    steps++;
}
System.out.println(steps);   // 5
```

Double `n` and the steps double too. That pattern of growth is what matters.

### Syntax: Big-O notation

```java nocompile
O(1)   O(log n)   O(n)   O(n log n)   O(n^2)
```

**Big-O** is a short label for how the number of steps grows as `n` grows. Read `O(n)` as "order n". It describes the shape of the growth, without exact numbers.

| Big-O | Name | In plain words | Example |
|---|---|---|---|
| O(1) | constant | the same work for any amount of data | read `docks[3]` |
| O(log n) | logarithmic | doubling the data adds one step | halve the list again and again |
| O(n) | linear | doubling the data doubles the time | look at every ride once |
| O(n log n) | n log n | a bit worse than linear | good sorting (later in this module) |
| O(n^2) | quadratic | doubling the data makes it four times slower | compare every ride with every ride |

```callout jargon
**log n** (log base 2) is how many times you can halve `n` before you reach 1. For 8 that is 3, because 8, 4, 2, 1 takes three halvings.
```

### The growth table

Numbers make the difference real. The values below are approximate step counts.

| n | O(1) | O(log n) | O(n) | O(n log n) | O(n^2) |
|---|---|---|---|---|---|
| 10 | 1 | 3 | 10 | 33 | 100 |
| 100 | 1 | 7 | 100 | 664 | 10,000 |
| 1000 | 1 | 10 | 1000 | 9,966 | 1,000,000 |

At 1000 rides, an O(n^2) method does one million steps. An O(log n) method does ten.

### Syntax: a loop inside a loop

```java
int n = 4;
int steps = 0;
for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
        steps++;
    }
}
System.out.println(steps);   // 16
```

The outer loop runs `n` times. Each time, the inner loop runs `n` times. That gives `n * n` steps, which is O(n^2).

### Syntax: a halving loop

```java
int n = 16;
int steps = 0;
while (n > 1) {
    n = n / 2;
    steps++;
}
System.out.println(steps);   // 4
```

`n` goes 16, 8, 4, 2, 1. That takes four steps, so this loop is O(log n).

### Drop the constants and the small terms

Big-O keeps only the part that grows fastest. Here is why.

- A loop that does 3 steps per item does `3n` steps. Big-O calls it O(n). Doubling the data still doubles the time.
- A method with a nested loop plus one extra loop does `n^2 + n` steps. For n = 1000 that is 1,001,000. The `+ n` barely matters, so it is O(n^2).
- A method that always does 50 steps is O(1). The 50 does not grow.

```callout tip
Byte says: ask "what happens when the data doubles?" The answer tells you the Big-O faster than any formula.
```

### Best case and worst case

Searching a list for B-1042 can be lucky. If B-1042 is first, the search takes one step. That is the **best case**.

If B-1042 is last, or missing, the search reads every item. That is the **worst case**. Big-O usually describes the worst case, because that is when riders wait.

### Space complexity

Time is one cost. Memory is another. **Space complexity** is how much extra memory a method needs as `n` grows.

- A method that keeps one counter uses O(1) space.
- A method that copies the list into a new list uses O(n) space.

### What common collections cost

You already use these collections. Now you can see their price tags.

| Operation | Big-O | Why |
|---|---|---|
| `list.get(i)` on an `ArrayList` | O(1) | jumps straight to slot `i` |
| `list.add(x)` at the end of an `ArrayList` | O(1) usually | puts it in the next free slot |
| `list.contains(x)` on an `ArrayList` | O(n) | checks items one by one |
| `set.contains(x)` on a `HashSet` | O(1) on average | uses the hash code to jump to the right spot |
| `map.get(key)` on a `HashMap` | O(1) on average | same trick as `HashSet` |

You will see how a hash table pulls off that O(1) jump later in this module.

## Worked example

This program counts the steps of a linear loop and a nested loop for three sizes.

```java run
public class Main {
    static int linear(int n) {
        int steps = 0;
        for (int i = 0; i < n; i++) {
            steps++;
        }
        return steps;
    }

    static int nested(int n) {
        int steps = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                steps++;
            }
        }
        return steps;
    }

    public static void main(String[] args) {
        int[] sizes = {10, 100, 1000};
        for (int n : sizes) {
            System.out.println(n + ": linear " + linear(n) + ", nested " + nested(n));
        }
    }
}
```

Line by line:

- `linear` adds 1 to `steps` each time the loop body runs. It returns `n`.
- `nested` runs its inner loop `n` times for each of the `n` outer passes. It returns `n * n`.
- `main` tries each size. The output shows linear growing by 10 times and nested growing by 100 times.

Now the ticket. The kiosk stores the bikes that are out in a `List<String>`. Checking `contains` reads every id, which is O(n). A `HashSet<String>` answers the same question in O(1) on average.

```java run
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        List<String> out = List.of("B-1001", "B-1002", "B-1042", "B-2001");
        int checked = 0;
        for (String id : out) {
            checked++;
            if (id.equals("B-2001")) {
                break;
            }
        }
        System.out.println("List checked " + checked + " ids");

        Set<String> outSet = new HashSet<>(out);
        System.out.println("Set says " + outSet.contains("B-2001"));
    }
}
```

The list loop had to check four ids. With a year of rides, it checks thousands. The set does one hash lookup however many bikes are out.

## Try it

Change `n` below to 1000, then 1,000,000. Watch how slowly the halving count grows.

```java run
public class Main {
    public static void main(String[] args) {
        int n = 100;
        int steps = 0;
        while (n > 1) {
            n = n / 2;
            steps++;
        }
        System.out.println("Halvings: " + steps);
    }
}
```

For 100 it prints 6. A million needs only 19 halvings.

## Recap

- Count steps as `n` grows instead of timing with a clock.
- O(1) stays flat, O(log n) barely grows, O(n) doubles with the data and O(n^2) grows four times when the data doubles.
- Drop constants and smaller terms: `3n + 5` is O(n), and `n^2 + n` is O(n^2).
- Big-O usually means the worst case. Space complexity counts extra memory the same way.
- `ArrayList.contains` is O(n). `HashSet.contains` is O(1) on average.

## Common mistakes

**1. Counting the nested loop as O(n).**

```java
int n = 10;
int steps = 0;
for (int i = 0; i < n; i++) {
    for (int j = 0; j < n; j++) {
        steps++;
    }
}
System.out.println(steps);
```

You expect 10, but the program prints:

```text
100
```

Fix: multiply the loop counts. Two loops of `n`, one inside the other, give `n * n`, so O(n^2).

**2. Keeping constants in the Big-O.**

```java nocompile
// "This loop does 2 steps per item, so it is O(2n)."
for (int i = 0; i < n; i++) {
    total += docks[i];
    count++;
}
```

There is no error message here. The label is just wrong for Big-O. Fix: drop the 2. Doubling `n` still doubles the work, so the loop is O(n).

**3. Calling `contains` on a list inside a loop.**

```java nocompile
for (String id : todaysRides) {
    if (allRidesThisYear.contains(id)) {   // allRidesThisYear is an ArrayList
        busy++;
    }
}
```

This compiles and gives the right answer, but slowly. Each `contains` is O(n), and the loop runs it n times. That makes O(n^2). Fix: put the year's ids in a `HashSet<String>` once, then call `contains` on the set.
