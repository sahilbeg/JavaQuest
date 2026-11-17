## Hook

Think of a hand of playing cards. Most people sort them the same way. They pick up one card at a time and slide it left until it sits in the right spot. You already know a sorting algorithm. You just never wrote it down.

The kiosk firmware has no `Arrays.sort`. Tsunade needs the nightly list of ride minutes sorted on the device. This page teaches three simple sorts you can write by hand in a few lines.

## Concept

To **sort** an array means to rearrange it so each value is less than or equal to the next. The three sorts here work **in place**. In place means they move values around inside the same array, with no second array.

All three use one small move: the **swap**.

### Syntax: swapping two items

```java nocompile
int temp = a[i];
a[i] = a[j];
a[j] = temp;
```

`temp` holds one value for a moment. Without it, the first assignment would wipe that value out.

### Syntax: bubble sort

**Bubble sort** walks along the array and compares each pair of neighbours. When the left one is bigger, it swaps them. One walk is called a **pass**. After each pass, the biggest unsorted value has "bubbled" to the end.

```java
static void bubble(int[] a) {
    for (int pass = 0; pass < a.length - 1; pass++) {
        boolean swapped = false;
        for (int j = 0; j < a.length - 1 - pass; j++) {
            if (a[j] > a[j + 1]) {
                int temp = a[j];
                a[j] = a[j + 1];
                a[j + 1] = temp;
                swapped = true;
            }
        }
        if (!swapped) {
            break;
        }
    }
}
```

- The inner loop stops at `a.length - 1 - pass`. The last `pass` items are already in their final place.
- `swapped` gives an **early stop**. A pass with no swap proves the array is sorted, so `break` ends the sort.

Here is bubble sort on `{5, 2, 8, 1}`:

| Pass | Compares and swaps | Array after the pass |
|---|---|---|
| 1 | swap 5 and 2, keep 5 and 8, swap 8 and 1 | 2, 5, 1, 8 |
| 2 | keep 2 and 5, swap 5 and 1 | 2, 1, 5, 8 |
| 3 | swap 2 and 1 | 1, 2, 5, 8 |

```java run
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int[] a = {5, 2, 8, 1};
        for (int pass = 0; pass < a.length - 1; pass++) {
            boolean swapped = false;
            for (int j = 0; j < a.length - 1 - pass; j++) {
                if (a[j] > a[j + 1]) {
                    int temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                    swapped = true;
                }
            }
            System.out.println("after pass " + (pass + 1) + ": " + Arrays.toString(a));
            if (!swapped) {
                break;
            }
        }
    }
}
```

`Arrays.toString` only prints the array. It does not sort anything.

### Syntax: selection sort

**Selection sort** looks through the unsorted part for the smallest value. It swaps that value into the next free spot at the front.

```java
static void selection(int[] a) {
    for (int i = 0; i < a.length - 1; i++) {
        int min = i;
        for (int j = i + 1; j < a.length; j++) {
            if (a[j] < a[min]) {
                min = j;
            }
        }
        int temp = a[i];
        a[i] = a[min];
        a[min] = temp;
    }
}
```

`min` holds the index of the smallest value found so far. Selection sort does at most one swap per round.

| i | Smallest from i to the end | Array after the swap |
|---|---|---|
| 0 | 1, at index 3 | 1, 2, 8, 5 |
| 1 | 2, at index 1 | 1, 2, 8, 5 |
| 2 | 5, at index 3 | 1, 2, 5, 8 |

### Syntax: insertion sort

**Insertion sort** works like sorting cards in your hand. The left part of the array is already sorted. It takes the next value, called the **key**, and shifts bigger values one place right until the key fits.

```java
static void insertion(int[] a) {
    for (int i = 1; i < a.length; i++) {
        int key = a[i];
        int j = i - 1;
        while (j >= 0 && a[j] > key) {
            a[j + 1] = a[j];
            j--;
        }
        a[j + 1] = key;
    }
}
```

- `j >= 0` stops the loop at the front of the array.
- `a[j] > key` stops the loop when a smaller or equal value is found.
- After the loop, `j + 1` is the gap where the key belongs.

| i | key | Shifted right | Array after |
|---|---|---|---|
| 1 | 2 | 5 | 2, 5, 8, 1 |
| 2 | 8 | nothing | 2, 5, 8, 1 |
| 3 | 1 | 8, 5, 2 | 1, 2, 5, 8 |

### How fast are they?

All three use a loop inside a loop. On n items, that means roughly n × n comparisons, written **O(n²)**.

| Sort | Worst case | Best case | In plain words |
|---|---|---|---|
| Bubble (early stop) | O(n²) | O(n), already sorted | Double the data, four times the time. |
| Selection | O(n²) | O(n²) | Always scans the whole rest of the array. |
| Insertion | O(n²) | O(n), already sorted | Nearly sorted data needs very few shifts. |

All three use O(1) extra memory. They only need a `temp` or `key` variable.

```callout tip
Insertion sort shines on nearly sorted data. Each key only moves a step or two, so the inner loop almost never runs. Yesterday's sorted list plus a few new rides is a perfect case.
```

### Stability

A sort is **stable** when equal values keep their original order. Imagine two rides of 12 minutes, B-1042 first and B-2001 second. A stable sort by minutes keeps B-1042 first.

- Bubble sort is stable. It only swaps when the left value is strictly bigger.
- Insertion sort is stable. It stops shifting at an equal value.
- Selection sort is not stable. Its long-distance swap can jump one value over an equal one.

```callout jargon
**Stable** matters when you sort records by one field. It keeps any earlier order, such as "by station", inside each group of equal minutes.
```

## Worked example

This program sorts rides by minutes with selection sort. Watch the two 12-minute rides.

```java run
public class Main {
    public static void main(String[] args) {
        Ride[] rides = {
            new Ride("B-1042", "Riverside", "classic", 12),
            new Ride("B-2001", "Harbor", "e-bike", 12),
            new Ride("B-3300", "Uptown", "cargo", 4)
        };
        for (int i = 0; i < rides.length - 1; i++) {
            int min = i;
            for (int j = i + 1; j < rides.length; j++) {
                if (rides[j].minutes() < rides[min].minutes()) {
                    min = j;
                }
            }
            Ride temp = rides[i];
            rides[i] = rides[min];
            rides[min] = temp;
        }
        for (Ride r : rides) {
            System.out.println(r.bikeId() + " " + r.minutes());
        }
    }
}

record Ride(String bikeId, String station, String type, int minutes) {}
```

1. Round 0 finds the 4-minute ride at index 2.
2. It swaps index 0 and index 2. B-1042 jumps from the front to the back.
3. Now B-2001 comes before B-1042, although B-1042 was first. Selection sort lost the original order.

Insertion sort on the same rides would print B-3300, B-1042, B-2001.

## Try it

Run insertion sort on a nearly sorted list and count the shifts. Then change `a` to `{9, 8, 7, 6, 5, 4}` and compare the count.

```java run
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int[] a = {3, 5, 7, 6, 9, 11};
        int shifts = 0;
        for (int i = 1; i < a.length; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= 0 && a[j] > key) {
                a[j + 1] = a[j];
                j--;
                shifts++;
            }
            a[j + 1] = key;
        }
        System.out.println(Arrays.toString(a) + " shifts: " + shifts);
    }
}
```

## Recap

- Bubble sort swaps neighbours. An early stop ends it after a pass with no swap.
- Selection sort picks the smallest remaining value and swaps it to the front.
- Insertion sort slides each key left into the sorted part. It is fast on nearly sorted data.
- All three are O(n²) in the worst case and sort in place with O(1) extra memory.
- Bubble and insertion sort are stable. Selection sort is not.

## Common mistakes

**1. Running the inner loop one step too far.** Bubble sort reads `a[j + 1]`, so `j` must stop one before the end.

```java run crash
public class Main {
    public static void main(String[] args) {
        int[] a = {5, 2, 8, 1};
        for (int j = 0; j < a.length; j++) {
            if (a[j] > a[j + 1]) {
                int temp = a[j];
                a[j] = a[j + 1];
                a[j + 1] = temp;
            }
        }
    }
}
```

You see `Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 4 out of bounds for length 4`. Fix: loop while `j < a.length - 1`.

**2. Swapping without a temp variable.** The first line overwrites a value before it is saved.

```java
int[] a = {5, 2};
a[0] = a[1];
a[1] = a[0];
System.out.println(a[0] + " " + a[1]);
```

This prints `2 2`. The 5 is gone. Fix: save it first with `int temp = a[0];`, then copy `temp` into `a[1]`.

**3. Setting `swapped` once, outside the passes.** After the first swap it stays `true`, so the early stop never fires.

```java
int[] a = {2, 1, 3, 4};
int passes = 0;
boolean swapped = false;
for (int pass = 0; pass < a.length - 1; pass++) {
    passes++;
    for (int j = 0; j < a.length - 1 - pass; j++) {
        if (a[j] > a[j + 1]) {
            int temp = a[j];
            a[j] = a[j + 1];
            a[j + 1] = temp;
            swapped = true;
        }
    }
    if (!swapped) {
        break;
    }
}
System.out.println(passes);
```

This prints `3`, though the array was sorted after pass 1 and pass 2 proved it. Fix: write `boolean swapped = false;` inside the outer loop, so each pass starts fresh.
