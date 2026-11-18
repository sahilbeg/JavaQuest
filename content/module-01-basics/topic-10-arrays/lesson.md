## Hook

Picture the dock row at Riverside station. There are 12 docks side by side, each with a little number painted on it. Some hold a bike. Some are empty.

You could make 12 separate variables: `dock1`, `dock2`, and so on up to `dock12`. That gets messy fast. What if Uptown gets 20 docks? An **array** solves this. It is one variable that holds a whole row of values.

## Concept

```callout jargon
An **array** is a fixed-size row of values that all have the same type. Each value in it is called an **element**.
```

### Syntax: declaring and creating an array

```java
int[] rides = new int[5];
```

- `int[]` means "an array of ints". The square brackets mark it as an array.
- `new int[5]` creates a row of 5 slots.
- New slots start with a default value: `0` for numbers, `false` for `boolean`, `null` for `String`.

Watch an array of 3 ride times being made and filled:

```animate
title: A row of three slots
kind: code
code: |
  int[] rides = new int[3];
  rides[0] = 12;
  rides[2] = 30;
  System.out.println(rides[0] + " " + rides[1] + " " + rides[2]);
steps:
  - line: 1
    objects: { a1: { type: "int[]", fields: { "0": 0, "1": 0, "2": 0 } } }
    vars: { rides: "@a1" }
    say: new int[3] builds one object with 3 numbered slots, all 0. The variable rides holds an arrow to it.
  - line: 2
    objects: { a1: { type: "int[]", fields: { "0": 12, "1": 0, "2": 0 } } }
    say: rides[0] = 12 follows the arrow and puts 12 in slot 0, the first slot.
  - line: 3
    objects: { a1: { type: "int[]", fields: { "0": 12, "1": 0, "2": 30 } } }
    say: Slot 2 is the third and last slot. It gets 30.
  - line: 4
    out: 12 0 30
    say: Slot 1 was never set, so it still holds its default, 0.
```

### Default values and null

```callout jargon
**null** means 'no value yet'. A String slot holds no text until you put some in.
```

```java
boolean[] docks = new boolean[12];   // 12 docks, all false
double[] fares = new double[3];      // 0.0, 0.0, 0.0
String[] stations = new String[4];   // null, null, null, null
```

### Syntax: listing the values

When you already know the values, list them in curly braces.

```java
int[] rides = {12, 45, 7, 30};
String[] stations = {"Riverside", "Old Mill", "Harbor", "Uptown"};
```

Once created, an array cannot grow or shrink.

### Syntax: indexes start at 0

```java nocompile
String first = stations[0];   // "Riverside"
rides[0] = 15;                // change the first ride to 15 minutes
```

```callout jargon
An **index** is the position number of an element. Java counts from 0, so the first element is at index 0.
```

| Index | 0 | 1 | 2 | 3 |
|---|---|---|---|---|
| Value | Riverside | Old Mill | Harbor | Uptown |

The painted dock numbers at Riverside go from 1 to 12. The array indexes go from 0 to 11. To turn an index into a dock number, add 1.

### Syntax: length

```java nocompile
int count = stations.length;   // 4
```

`length` tells you how many elements an array has. Write it with no brackets after it. The last index is always `length - 1`.

```java nocompile
System.out.println(rides[rides.length - 1]);   // the last ride
```

### Syntax: looping with for

```java nocompile
for (int i = 0; i < rides.length; i++) {
    System.out.println(rides[i]);
}
```

The loop variable `i` walks through every index, from 0 up to `length - 1`:

```animate
title: i walks along the array
kind: code
code: |
  int[] rides = {12, 45, 7};
  for (int i = 0; i < rides.length; i++) {
      System.out.println("Ride " + i + ": " + rides[i]);
  }
steps:
  - line: 1
    objects: { a1: { type: "int[]", fields: { "0": 12, "1": 45, "2": 7 } } }
    vars: { rides: "@a1" }
    say: The array has 3 slots, with indexes 0, 1 and 2. So rides.length is 3.
  - line: 2
    vars: { i: 0 }
    say: i starts at 0, the first index. 0 < 3 is true.
  - line: 3
    out: "Ride 0: 12"
    say: rides[i] reads the slot that i names. Right now that is slot 0.
  - line: 2
    vars: { i: 1 }
    say: i++ moves i to the next slot. 1 < 3 is true.
  - line: 3
    out: "Ride 1: 45"
    say: The same line now reads slot 1.
  - line: 2
    vars: { i: 2 }
    say: i is 2, the last index. 2 < 3 is still true.
  - line: 3
    out: "Ride 2: 7"
    say: The last slot is read.
  - line: 2
    vars: { i: 3 }
    say: i is 3 and 3 < 3 is false, so the loop stops. rides[3] does not exist, and it is never read.
```

Use this loop when you need the index itself, for example to print a dock number.

```java nocompile
for (int i = 0; i < docks.length; i++) {
    if (docks[i]) {
        System.out.println("Dock " + (i + 1) + " has a bike");
    }
}
```

### Syntax: the enhanced for loop

```java nocompile
for (int minutes : rides) {
    System.out.println(minutes);
}
```

Read it as "for each `minutes` in `rides`". Java hands you each element in turn. You get the values, but you do not get the index.

```java nocompile
int total = 0;
for (int minutes : rides) {
    total = total + minutes;
}
```

```callout tip
Use the enhanced for loop when you only need the values. Use the classic for loop when you need the index too.
```

### What happens with a bad index

```java
int[] rides = {12, 45, 7};
System.out.println(rides[3]);
```

The array has indexes 0, 1 and 2. Index 3 does not exist. The program compiles, but it crashes when it runs:

```
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 3 out of bounds for length 3
```

```callout jargon
An **exception** is an error that happens while the program runs. `ArrayIndexOutOfBoundsException` means you used an index below 0 or at least `length`.
```

### Syntax: 2D arrays

A 2D array is an array of arrays. Think of a table with rows and columns. Here each row is a station and each column is a dock.

```java
boolean[][] stations = new boolean[4][12];   // 4 stations, 12 docks each
stations[0][2] = true;                       // Riverside, dock 3 has a bike
```

The first index picks the row. The second index picks the column. `stations.length` is the number of rows, and `stations[0].length` is the number of columns in row 0.

```java
int[][] rides = {
    {10, 20},       // Riverside
    {5, 15, 25}     // Old Mill
};
System.out.println(rides[1][2]);   // 25
```

```mermaid
flowchart LR
  S["stations"] --> R0["row 0: Riverside docks"]
  S --> R1["row 1: Old Mill docks"]
  S --> R2["row 2: Harbor docks"]
  S --> R3["row 3: Uptown docks"]
```

### Syntax: arrays and methods

A method can take an array as a parameter. Write the array type, like `int[]`, before the parameter name.

```java nocompile
public static int total(int[] rides) {
    int sum = 0;
    for (int r : rides) {
        sum += r;
    }
    return sum;
}
```

### Syntax: returning an array

A method can also return an array. Create it with `new`, fill it, then return it.

```java nocompile
public static int[] countPerRow(boolean[][] rows) {
    int[] counts = new int[rows.length];
    for (int s = 0; s < rows.length; s++) {
        counts[s] = countBikes(rows[s]);
    }
    return counts;
}
```

In a 2D array, each row is an array of its own. Here `rows[s]` is a `boolean[]`, so you can pass it to a method that takes `boolean[]`.

```java run
public class Main {
    public static int total(int[] rides) {
        int sum = 0;
        for (int r : rides) {
            sum += r;
        }
        return sum;
    }

    public static int countBikes(boolean[] docks) {
        int count = 0;
        for (boolean hasBike : docks) {
            if (hasBike) {
                count++;
            }
        }
        return count;
    }

    public static int[] countPerRow(boolean[][] rows) {
        int[] counts = new int[rows.length];
        for (int s = 0; s < rows.length; s++) {
            counts[s] = countBikes(rows[s]);
        }
        return counts;
    }

    public static void main(String[] args) {
        int[] rides = {12, 45, 7};
        System.out.println("Total minutes: " + total(rides));
        boolean[][] rows = {
            {true, false, true},
            {false, false}
        };
        int[] counts = countPerRow(rows);
        System.out.println("Row 0 bikes: " + counts[0]);
        System.out.println("Row 1 bikes: " + counts[1]);
    }
}
```

### Two names, one array

An array is an object, so an array variable holds an arrow to it, called a **reference** (topic 04). The `=` sign copies the arrow, not the array. After `int[] b = a;` there are two names for one array.

```animate
title: Two arrows, one array
kind: code
code: |
  int[] a = {12, 45, 7};
  int[] b = a;
  b[0] = 99;
  System.out.println(a[0]);
  int[] c = a.clone();
  c[0] = 1;
  System.out.println(a[0] + " " + c[0]);
steps:
  - line: 1
    objects: { a1: { type: "int[]", fields: { "0": 12, "1": 45, "2": 7 } } }
    vars: { a: "@a1" }
    say: Java builds one array of ride minutes. a holds an arrow to it.
  - line: 2
    vars: { b: "@a1" }
    say: b = a copies the arrow, not the array. Now two arrows point at the same array.
  - line: 3
    objects: { a1: { type: "int[]", fields: { "0": 99, "1": 45, "2": 7 } } }
    say: b[0] = 99 follows b's arrow and changes slot 0 of that one array.
  - line: 4
    out: "99"
    say: a follows its arrow to the same array, so a[0] is 99 too. Nothing was copied.
  - line: 5
    objects: { a2: { type: "int[]", fields: { "0": 99, "1": 45, "2": 7 } } }
    vars: { c: "@a2" }
    say: clone() builds a second array with the same values. c points at the new one.
  - line: 6
    objects: { a2: { type: "int[]", fields: { "0": 1, "1": 45, "2": 7 } } }
    say: Changing the copy leaves the original alone.
  - line: 7
    out: "99 1"
    say: Two separate arrays now, so they can hold different values.
```

```callout jargon
**Aliasing** means two variables point at the same object. A change made through one name shows up through the other.
```

In production, aliasing causes bugs that seem to come from nowhere. One part of the code "borrows" an array, sorts it, and every other part sees the new order. Copy an array before you hand it to code that may change it.

### Syntax: printing and comparing arrays

```java nocompile
String text = Arrays.toString(rides);
boolean same = Arrays.equals(rides, other);
```

Printing an array directly does not show its values. `System.out.println(rides)` prints something like `[I@2473b9ce`. That means "an int array", followed by a code Java made for it. The code can differ between machines and runs.

The `java.util.Arrays` class has helpers. Add `import java.util.Arrays;` at the top of the file.

```java run
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int[] rides = {12, 45, 7};
        int[] same = {12, 45, 7};
        System.out.println(Arrays.toString(rides));
        System.out.println(rides == same);
        System.out.println(Arrays.equals(rides, same));
    }
}
```

It prints `[12, 45, 7]`, `false` and `true`. `==` asks "are these the same arrow?" Two separate arrays are never `==`, even with equal values. `Arrays.equals` compares the values slot by slot.

### Syntax: copying an array

```java nocompile
int[] copy = Arrays.copyOf(rides, rides.length);
```

`Arrays.copyOf` builds a new array and copies the values in. The second number is the new length. A longer length pads the end with default values, so `Arrays.copyOf(rides, 5)` gives `[12, 45, 7, 0, 0]`.

`rides.clone()` also makes a copy of the same length. Either way, the copy is a separate array, so changing it leaves the original alone.

### Syntax: fill and sort

```java nocompile
Arrays.fill(docks, 1);
Arrays.sort(times);
```

```java
int[] docks = new int[4];
java.util.Arrays.fill(docks, 1);        // [1, 1, 1, 1]
int[] times = {30, 7, 45, 12};
java.util.Arrays.sort(times);           // [7, 12, 30, 45]
String[] stations = {"Uptown", "Harbor", "Old Mill"};
java.util.Arrays.sort(stations);        // [Harbor, Old Mill, Uptown]
```

- `Arrays.fill` puts the same value in every slot.
- `Arrays.sort` puts the values in order, smallest first. Strings go in alphabetical order, with capital letters before small ones. It changes the array itself and gives back nothing.

Writing `java.util.Arrays` in full works without an import. Usually you import it once instead.

### A method gets a copy of the arrow

Topic 09 showed that Java passes a copy. With an array, the copy is a copy of the arrow. So the method's arrow points at the caller's array, and changes to its slots are seen by the caller. Pointing the parameter at a new array is not seen.

```java run
import java.util.Arrays;

public class Main {
    public static void addMinute(int[] rides) {
        rides[0] = rides[0] + 1;       // changes the caller's array
        rides = new int[] {0, 0, 0};   // moves only the method's own arrow
        rides[1] = 99;                 // changes the new array, which nobody else sees
    }

    public static void main(String[] args) {
        int[] rides = {12, 45, 7};
        addMinute(rides);
        System.out.println(Arrays.toString(rides));
    }
}
```

It prints `[13, 45, 7]`. The first line followed the shared arrow. After the second line, the method's arrow pointed somewhere else, so `99` never reached `main`.

## Worked example

This program counts the bikes at a small row of docks.

```java run
public class Main {
    public static void main(String[] args) {
        boolean[] docks = {true, false, true, true, false};
        int bikes = 0;
        for (boolean hasBike : docks) {
            if (hasBike) {
                bikes++;
            }
        }
        System.out.println("Docks: " + docks.length);
        System.out.println("Bikes: " + bikes);
        System.out.println("First dock has a bike: " + docks[0]);
    }
}
```

1. `docks` holds 5 booleans. `true` means the dock holds a bike.
2. `bikes` starts at 0.
3. The enhanced for loop hands each value to `hasBike`, one at a time.
4. Each `true` adds 1 to `bikes`, so `bikes` ends at 3.
5. `docks.length` is 5, and `docks[0]` is the first element, `true`.

## Try it

This program loops over a 2D array of ride minutes and prints a total per station. Change a number, or add a third row, then press **Run**.

```java run
public class Main {
    public static void main(String[] args) {
        int[][] rides = {
            {10, 20, 5},
            {30, 15}
        };
        for (int s = 0; s < rides.length; s++) {
            int total = 0;
            for (int minutes : rides[s]) {
                total = total + minutes;
            }
            System.out.println("Station " + s + ": " + total + " min");
        }
    }
}
```

Now change `s < rides.length` to `s <= rides.length` and run it again. Read the exception, then change it back.

## Recap

- An array holds a fixed number of values of one type.
- Indexes start at 0, and the last index is `array.length - 1`.
- `b = a` copies the arrow, not the array. Use `Arrays.toString`, `Arrays.equals` and `Arrays.copyOf` to print, compare and copy.
- Use a classic for loop when you need the index, and an enhanced for loop when you only need the values.
- A 2D array is an array of rows: `grid[row][column]`.

## Common mistakes

**Going one past the end.**

```java
int[] rides = {12, 45, 7};
for (int i = 0; i <= rides.length; i++) {
    System.out.println(rides[i]);
}
```

The program prints 12, 45 and 7, then crashes with `java.lang.ArrayIndexOutOfBoundsException: Index 3 out of bounds for length 3`. Use `i < rides.length`.

**Writing length with brackets.**

```java nocompile
int[] rides = {12, 45, 7};
int count = rides.length();
```

Java says: `cannot find symbol` with `symbol: method length()`. For arrays, `length` has no brackets: `rides.length`.

**Forgetting that dock numbers start at 1.**

```java
boolean[] docks = {true, false, true};
for (int i = 0; i < docks.length; i++) {
    if (!docks[i]) {
        System.out.println("Empty dock: " + i);
    }
}
```

This prints `Empty dock: 1`, but the empty dock is painted with the number 2. The index starts at 0. Print `(i + 1)` to show the dock number.
