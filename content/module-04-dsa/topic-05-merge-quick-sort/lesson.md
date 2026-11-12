## Hook

Theo needs a sorted list of every ride from this year for the board report. That is millions of ride times. The report uses insertion sort from the last ticket, and it now runs all night.

Insertion sort is O(n^2). Double the rides and it gets four times slower. A year of rides is far too many for that.

Think about sorting a huge pile of paper receipts. You could split the pile in two and give each half to a friend. Each friend splits again and passes it on. Tiny piles are easy to sort. Then everyone hands their sorted piles back and joins them. This page teaches two sorts built on that idea.

## Concept

### Divide and conquer

```callout jargon
**Divide and conquer** means: split a big problem into smaller copies of the same problem, solve those, then combine the answers. It fits recursion well, because each small copy is solved by the same method.
```

Both sorts on this page use divide and conquer. They split the work in different places.

- **Merge sort** splits without thinking, then does the hard work when it joins the halves.
- **Quicksort** does the hard work when it splits, so joining needs no work at all.

### Merging two sorted arrays

Merging is the heart of merge sort. You have two arrays that are already sorted. You want one sorted array with every item.

Look at the front item of each array. Copy the smaller one into the result and move past it. Repeat. When one array runs out, copy the rest of the other.

### Syntax: merge

```java
static int[] merge(int[] left, int[] right) {
    int[] result = new int[left.length + right.length];
    int i = 0;
    int j = 0;
    int k = 0;
    while (i < left.length && j < right.length) {
        if (left[i] <= right[j]) {
            result[k] = left[i];
            i++;
        } else {
            result[k] = right[j];
            j++;
        }
        k++;
    }
    while (i < left.length) {
        result[k] = left[i];
        i++;
        k++;
    }
    while (j < right.length) {
        result[k] = right[j];
        j++;
        k++;
    }
    return result;
}
```

- `i` points into `left`, `j` points into `right`, and `k` points into `result`.
- The first loop runs while both arrays still have items.
- The last two loops copy the leftovers. Only one of them does any work.

Each item is copied once, so merging `n` items takes O(n) steps.

### Merge sort

Merge sort uses merging over and over:

1. If the array has 0 or 1 items, it is already sorted. Stop.
2. Split it into a left half and a right half.
3. Merge sort each half. This is the recursive call.
4. Merge the two sorted halves.

```mermaid
flowchart TB
  A["38 27 43 3"] --> B["38 27"]
  A --> C["43 3"]
  B --> D["38"]
  B --> E["27"]
  C --> F["43"]
  C --> G["3"]
  D --> H["27 38"]
  E --> H
  F --> I["3 43"]
  G --> I
  H --> J["3 27 38 43"]
  I --> J
```

The top half of the picture splits. The bottom half merges.

### Syntax: copying part of an array

```java
int[] a = {38, 27, 43, 3};
int[] left = java.util.Arrays.copyOfRange(a, 0, 2);   // {38, 27}
int[] right = java.util.Arrays.copyOfRange(a, 2, 4);  // {43, 3}
```

`Arrays.copyOfRange(array, from, to)` makes a new array. It includes index `from` and stops before index `to`.

### Why merge sort is O(n log n)

Each split halves the array. You can halve `n` items about `log n` times before you reach single items. At each level of halving, the merges copy all `n` items once. So the work is `n` steps per level, times `log n` levels.

```callout warning
Merge sort needs extra memory. Every merge builds a new array, so it uses O(n) extra space. That is the price for its steady speed.
```

Merge sort is also **stable**. Stable means two equal items keep their original order. The `<=` in `merge` does this: on a tie, it takes the item from the left half first.

### Quicksort

Quicksort picks one item and calls it the **pivot**. It then **partitions** the array. Partition means: move every item that is `<=` the pivot to the left, move every bigger item to the right, and put the pivot between them.

After a partition, the pivot sits in its final sorted place. Quicksort then sorts the left part and the right part the same way.

### Syntax: Lomuto partition

The **Lomuto** scheme is the simplest way to partition. It uses the last item as the pivot.

```java
static int partition(int[] a, int low, int high) {
    int pivot = a[high];
    int i = low;
    for (int j = low; j < high; j++) {
        if (a[j] <= pivot) {
            int temp = a[i];
            a[i] = a[j];
            a[j] = temp;
            i++;
        }
    }
    int temp = a[i];
    a[i] = a[high];
    a[high] = temp;
    return i;
}
```

- `i` marks where the next small item goes.
- `j` scans every item except the pivot.
- At the end, the pivot swaps into index `i`. The method returns that index.

### Syntax: quicksort with a helper

```java nocompile
static void quickSort(int[] a, int low, int high) {
    if (low >= high) {
        return;
    }
    int p = partition(a, low, high);
    quickSort(a, low, p - 1);
    quickSort(a, p + 1, high);
}
```

The pivot at `p` is already in place, so neither recursive call includes it.

```callout analogy
Partition is like a teacher saying "everyone shorter than Sam, stand to the left". Sam ends up in the right spot, and two smaller groups are left to sort.
```

### Quicksort's bad case

On average each pivot lands near the middle. The array halves each time, so quicksort is O(n log n). It needs no extra arrays, and in practice it is very fast.

Now give it an array that is already sorted. The last item is the biggest, so every item goes left. The split is `n - 1` items and 0 items. That happens at every level, so quicksort does O(n^2) steps. Smarter pivot choices, such as a middle item, make this rare.

### The costs side by side

| Sort | Usual time | Worst time | Extra memory | Stable |
|---|---|---|---|---|
| Insertion sort | O(n^2) | O(n^2) | O(1) | yes |
| Merge sort | O(n log n) | O(n log n) | O(n) | yes |
| Quicksort | O(n log n) | O(n^2) | O(log n) for the calls | no |

In plain words, O(n log n) means doubling the data makes the time a bit more than double. For a million rides, that is about 20 million steps. O(n^2) would be a million million.

```callout tip
Java's own sorts use these ideas. `Arrays.sort` on an `int[]` uses a tuned quicksort. Sorting objects, as `Collections.sort` does, uses a stable merge sort variant. You build them by hand here so you know what they cost.
```

## Worked example

This program merge sorts five ride times and prints each merge as it happens.

```java run
import java.util.Arrays;

public class Main {
    static int[] merge(int[] left, int[] right) {
        int[] result = new int[left.length + right.length];
        int i = 0;
        int j = 0;
        int k = 0;
        while (i < left.length && j < right.length) {
            if (left[i] <= right[j]) {
                result[k] = left[i];
                i++;
            } else {
                result[k] = right[j];
                j++;
            }
            k++;
        }
        while (i < left.length) {
            result[k] = left[i];
            i++;
            k++;
        }
        while (j < right.length) {
            result[k] = right[j];
            j++;
            k++;
        }
        return result;
    }

    static int[] mergeSort(int[] a) {
        if (a.length < 2) {
            return a;
        }
        int mid = a.length / 2;
        int[] left = mergeSort(Arrays.copyOfRange(a, 0, mid));
        int[] right = mergeSort(Arrays.copyOfRange(a, mid, a.length));
        int[] merged = merge(left, right);
        System.out.println(Arrays.toString(left) + " + " + Arrays.toString(right) + " -> " + Arrays.toString(merged));
        return merged;
    }

    public static void main(String[] args) {
        int[] minutes = {42, 7, 19, 30, 3};
        System.out.println(Arrays.toString(mergeSort(minutes)));
    }
}
```

1. `mergeSort` splits `{42, 7, 19, 30, 3}` at `mid = 2` into `{42, 7}` and `{19, 30, 3}`.
2. `{42, 7}` splits into single items, and merging gives `[7, 42]`.
3. `{19, 30, 3}` splits into `{19}` and `{30, 3}`. Then `[3, 30]` merges with `[19]` to give `[3, 19, 30]`.
4. The last merge joins `[7, 42]` and `[3, 19, 30]` into `[3, 7, 19, 30, 42]`.

This version returns a new array. The practice version copies the result back into `a` instead.

## Try it

Run this quicksort. It prints the array after each partition. Then change `minutes` to an already sorted array such as `{1, 2, 3, 4, 5}`, and count the partitions.

```java run
import java.util.Arrays;

public class Main {
    static int partition(int[] a, int low, int high) {
        int pivot = a[high];
        int i = low;
        for (int j = low; j < high; j++) {
            if (a[j] <= pivot) {
                int temp = a[i];
                a[i] = a[j];
                a[j] = temp;
                i++;
            }
        }
        int temp = a[i];
        a[i] = a[high];
        a[high] = temp;
        return i;
    }

    static void quickSort(int[] a, int low, int high) {
        if (low >= high) {
            return;
        }
        int p = partition(a, low, high);
        System.out.println("pivot " + a[p] + " at " + p + ": " + Arrays.toString(a));
        quickSort(a, low, p - 1);
        quickSort(a, p + 1, high);
    }

    public static void main(String[] args) {
        int[] minutes = {25, 12, 40, 5, 18};
        quickSort(minutes, 0, minutes.length - 1);
        System.out.println(Arrays.toString(minutes));
    }
}
```

With the sorted array, every pivot is the biggest item left. Each partition peels off just one item.

## Recap

- Divide and conquer splits a problem into smaller copies, solves them, then combines the answers.
- Merge sort splits in half, sorts each half, then merges. It is always O(n log n), stable, and needs O(n) extra memory.
- Quicksort partitions around a pivot, then sorts each side. It is O(n log n) on average and needs no extra arrays.
- With the last item as pivot, quicksort is O(n^2) on an already sorted array.
- Java's `Arrays.sort` and `Collections.sort` are built on these same ideas.

## Common mistakes

**Forgetting the leftovers in merge.**

```java
import java.util.Arrays;

public class Main {
    static int[] merge(int[] left, int[] right) {
        int[] result = new int[left.length + right.length];
        int i = 0;
        int j = 0;
        int k = 0;
        while (i < left.length && j < right.length) {
            if (left[i] <= right[j]) {
                result[k] = left[i];
                i++;
            } else {
                result[k] = right[j];
                j++;
            }
            k++;
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(merge(new int[] {3, 9}, new int[] {1, 4, 12})));
    }
}
```

The output is `[1, 3, 4, 9, 0]`. The loop stops when `left` runs out, so 12 never gets copied. Add the two leftover loops after the main loop.

**No base case in the recursion.**

```java nocompile
static void quickSort(int[] a, int low, int high) {
    int p = partition(a, low, high);
    quickSort(a, low, p - 1);
    quickSort(a, p + 1, high);
}
```

The program crashes with `java.lang.ArrayIndexOutOfBoundsException: Index -1 out of bounds for length 5`. The calls keep going on empty ranges. Start with `if (low >= high) { return; }`.

**Including the pivot in the next call.**

```java nocompile
int p = partition(a, low, high);
quickSort(a, low, p);
quickSort(a, p + 1, high);
```

When the pivot is the biggest item, `p` equals `high`, so the call repeats the same range forever. Java stops with `java.lang.StackOverflowError`. The pivot is already in place, so use `p - 1`.
