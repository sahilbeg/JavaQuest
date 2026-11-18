## Hook

Each Spoke station has a little sensor box. Every minute it reports which docks hold a bike. It does not send a list. It sends one number, such as `2563`. Written in binary, that number is a row of switches: `101000000011`. Each 1 means "a bike is here".

One number for twelve docks is cheap to send and quick to check. But you have to read it switch by switch, and a small slip turns it into nonsense. This ticket teaches you to read and flip those switches with confidence.

## Concept

### Bits, a quick refresher

You met bits and bytes in [how computers work](/t/module-00-foundations/topic-01-how-computers-work). Here is the short version.

```callout jargon
A **bit** is a single 0 or 1. **Binary** writes numbers with bits only. Each place is worth double the place to its right: 1, 2, 4, 8, 16 and so on.
```

So `1010` is 8 + 2 = 10. Bit positions are counted from the right, starting at 0. In `1010`, bits 1 and 3 are set.

```callout jargon
A bit is **set** when it is 1 and **clear** when it is 0.
```

### Syntax: binary in Java

```java
public class Main {
    public static void main(String[] args) {
        System.out.println(0b1010);
        System.out.println(Integer.toBinaryString(5));
        System.out.println(Integer.parseInt("1010", 2));
    }
}
```

`0b` starts a number written in binary, so `0b1010` is 10. `Integer.toBinaryString(5)` gives the text `101`. `Integer.parseInt("1010", 2)` reads binary text back into the number 10.

### Two's complement: how negatives look

An `int` always has exactly 32 bits. The leftmost one, bit 31, is the **sign bit**. When it is 1, the number is negative.

```callout jargon
**Two's complement** is how Java stores negative whole numbers. To get -x, flip every bit of x, then add 1.
```

Try it with 1. Flipping 32 bits of `000...0001` gives `111...1110`. Adding 1 gives `111...1111`. So -1 is thirty-two 1s.

```java
public class Main {
    public static void main(String[] args) {
        System.out.println(Integer.toBinaryString(-1));
        System.out.println(Integer.toBinaryString(-8));
        System.out.println(~5 + " " + (-5 - 1));
        System.out.println(Integer.MAX_VALUE + 1);
    }
}
```

The output is `11111111111111111111111111111111`, then `11111111111111111111111111111000`, then `-6 -6`, then `-2147483648`.

`~` flips every bit. Since -x is "flip, then add 1", flipping alone gives one less: `~x == -x - 1`. And `Integer.MAX_VALUE + 1` wraps around to `Integer.MIN_VALUE`, which is a 1 followed by 31 zeros.

### The bitwise operators

These operators work on each pair of bits separately, position by position. Here they are on 12 (`1100`) and 10 (`1010`).

```text
  AND           OR            XOR
  1100  (12)    1100  (12)    1100  (12)
& 1010  (10)  | 1010  (10)  ^ 1010  (10)
  ----          ----          ----
  1000  (8)     1110  (14)    0110  (6)
```

- `&` (AND) gives 1 only where both bits are 1.
- `|` (OR) gives 1 where either bit is 1.
- `^` (XOR) gives 1 where the bits differ.
- `~` (NOT) flips every bit of one value.

`~12` is -13, because the 28 leading zeros flip to 1s as well. That follows `~x == -x - 1`.

```callout jargon
**XOR** means "exclusive or": one or the other, but not both.
```

```callout warning
`&` and `|` are not `&&` and `||`. The double ones work on booleans and can skip their right side. The single ones, used on numbers, work bit by bit and always use both sides.
```

### Syntax: shifts

```java nocompile
int bigger = x << k;   // move every bit k places left, zeros come in on the right
int half = x >> k;     // move right, copies of the sign bit come in on the left
int raw = x >>> k;     // move right, zeros come in on the left
```

`5 << 2` is `20`: `101` becomes `10100`. Each step left doubles the number, until a 1 reaches bit 31, the sign bit, and the number turns negative. `1 << 31` is `-2147483648`. Each step right with `>>` halves it, rounding down.

The two right shifts only differ for negative numbers.

```java
public class Main {
    public static void main(String[] args) {
        System.out.println((-16 >> 2) + " " + (-16 >>> 2));
        System.out.println((16 >> 2) + " " + (16 >>> 2));
        System.out.println((-1 >> 1) + " " + (-1 >>> 1));
    }
}
```

This prints `-4 1073741820`, then `4 4`, then `-1 2147483647`. `>>` keeps a negative number negative. `>>>` treats the bits as plain switches and fills with zeros. Notice that `-1 >> 1` is still `-1`.

### Syntax: test, set, clear and toggle bit i

`1 << i` is a number with only bit `i` set. Combine it with the operators to work on one switch.

```java nocompile
boolean on = (value & (1 << i)) != 0;  // test: is bit i set?
value = value | (1 << i);              // set: turn bit i on
value = value & ~(1 << i);             // clear: turn bit i off
value = value ^ (1 << i);              // toggle: flip bit i
```

Clearing needs `~` first. `~(1 << i)` has every bit set except bit `i`. AND with it keeps everything else and knocks out bit `i`.

Another way to test bit `i` is `((value >>> i) & 1) == 1`. It slides bit `i` down to position 0 and keeps only that bit.

```callout jargon
A **mask** is a number used to pick out, set or clear certain bits. `1 << i` is the mask for bit `i`.
```

### Watch the flags change

```animate
title: A bike's features in one int
kind: code
code: |
  int lights = 1;
  int basket = 2;
  int gps = 8;
  int bike = 0;
  bike = bike | lights;
  bike = bike | gps;
  System.out.println(Integer.toBinaryString(bike));
  System.out.println((bike & gps) != 0);
  bike = bike & ~lights;
  bike = bike ^ basket;
  System.out.println(Integer.toBinaryString(bike));
steps:
  - line: 1
    vars: { lights: '1 (0001)', basket: '2 (0010)', gps: '8 (1000)' }
    say: Each feature gets its own bit. Lights is bit 0, basket bit 1, GPS bit 3.
  - line: 4
    vars: { bike: '0 (0000)' }
    say: A new bike starts with every switch off.
  - line: 5
    vars: { bike: '1 (0001)' }
    say: OR with lights turns bit 0 on. Every other bit stays as it was.
  - line: 6
    vars: { bike: '9 (1001)' }
    say: OR with gps turns bit 3 on too. One int now holds two features.
  - line: 7
    out: "1001"
    say: toBinaryString shows the switches. Leading zeros are left out.
  - line: 8
    out: "true"
    say: AND with gps keeps only bit 3. It is not 0, so the bike has GPS.
  - line: 9
    vars: { bike: '8 (1000)' }
    say: "~lights is every bit except bit 0. AND with it clears the lights bit only."
  - line: 10
    vars: { bike: '10 (1010)' }
    say: XOR with basket flips bit 1. It was off, so now it is on.
  - line: 11
    out: "1010"
    say: The bike now has a basket and GPS, and no lights.
```

### Bitmasks as small sets

An `int` with one bit per feature is a tiny set. Each feature is either in or out.

```java nocompile
features = features | flag;                // add the features in flag
features = features & ~flag;               // remove them
int both = a | b;                          // join two sets
int shared = a & b;                        // keep only what both share
boolean hasAll = (features & flag) == flag;
boolean hasAny = (features & flag) != 0;
```

The last two lines look alike, but they differ when `flag` holds several bits. A bike with lights only has **any** of `LIGHTS | GPS`, but not **all** of them.

```callout tip
Gojo says: in everyday Java, EnumSet gives you this speed with readable names. It stores small enum sets as bits inside a long. Use raw masks when you store, send or hash the bits yourself.
```

Real systems use masks everywhere. File permissions on Linux are bits: read 4, write 2, run 1. `HashMap` picks a bucket with `hash & (buckets - 1)`, because its bucket count is always a power of two.

### n & (n - 1): drop the lowest 1

Subtracting 1 flips the lowest 1 bit to 0 and every 0 below it to 1. AND with the original then wipes out that whole tail.

| | binary |
|---|---|
| `n` = 40 | `101000` |
| `n - 1` = 39 | `100111` |
| `n & (n - 1)` = 32 | `100000` |

Two classic uses follow.

- **Count the 1 bits.** Repeat `n = n & (n - 1)` until `n` is 0, counting the steps. It takes one step per 1 bit, not 32 steps.
- **Power of two check.** A power of two has exactly one 1 bit. So `n > 0 && (n & (n - 1)) == 0`.

The `n > 0` matters. `0 & -1` is 0, and `Integer.MIN_VALUE & Integer.MAX_VALUE` is 0 as well. Neither is a power of two.

### XOR tricks

XOR has three rules that make it feel like magic.

- `a ^ a == 0`: anything XOR itself cancels out.
- `a ^ 0 == a`: XOR with 0 changes nothing.
- The order does not matter: `a ^ b ^ a == b`.

So if every dock id appears twice except one, XOR them all. Each pair cancels, and the lonely id is left. For `{12, 7, 40, 7, 12}` the answer is `40`.

```java nocompile
int result = 0;
for (int id : ids) {
    result ^= id;
}
```

This is O(n) time and O(1) space. A `HashMap` of counts also works, but it needs O(n) extra space.

### Every subset with a mask loop

With `n` items, each one is either in or out. That gives 2 × 2 × ... = 2ⁿ combinations. The numbers `0` to `2ⁿ - 1` are exactly every pattern of `n` bits, so each number is one combination.

```callout jargon
A **subset** is any choice of items from a set, including none of them and all of them. A set of `n` items has 2ⁿ subsets.
```

### Syntax: the subset loop

```java nocompile
for (int mask = 0; mask < (1 << n); mask++) {
    for (int i = 0; i < n; i++) {
        if ((mask & (1 << i)) != 0) {
            // item i is in this subset
        }
    }
}
```

`1 << n` is 2ⁿ. The inner loop reads which items the mask picks. The total cost is O(2ⁿ × n) time and O(1) extra space. 2ⁿ grows fast: 20 items already give 1,048,576 subsets. So this fits problems with about 20 items or fewer.

### Bigger than 32 bits: long and the shift trap

An `int` has bits 0 to 31. For a 64-dock station, use a `long`, which has bits 0 to 63. The trap is the `1` you shift.

```java
public class Main {
    public static void main(String[] args) {
        System.out.println((1 << 32) + " " + (1 << 33) + " " + (1 << 40));
        System.out.println(1L << 40);
    }
}
```

This prints `1 2 256` and then `1099511627776`. For an `int`, Java uses only the lowest 5 bits of the shift distance. So `1 << 32` is `1 << 0`, and `1 << 40` is `1 << 8`. No error, just the wrong bit. For a `long`, Java uses the lowest 6 bits, so `1L << 40` is right.

```callout warning
`long mask = 1 << 40;` still computes an int first, and gets 256. Write `1L << 40`, so the shift itself happens on a long.
```

### Syntax: library helpers

```java nocompile
Integer.bitCount(2563)             // 4: how many 1 bits
Integer.toBinaryString(2563)       // "101000000011"
Integer.numberOfTrailingZeros(40)  // 3: zeros below the lowest 1
Integer.highestOneBit(100)         // 64: only the top 1 bit kept
Long.bitCount(-1L)                 // 64
```

Every one of these has a `Long` twin. They run in O(1), and many CPUs do `bitCount` in a single instruction. Write the tricks by hand to learn them, then use the library in real code.

### How fast is it?

| Task | Time | Space |
|---|---|---|
| test, set, clear, toggle one bit | O(1) | O(1) |
| has-all or has-any on a mask | O(1) | O(1) |
| count 1 bits with `n & (n - 1)` | O(number of 1 bits), at most 32 | O(1) |
| power of two check | O(1) | O(1) |
| unpaired value by XOR | O(n) | O(1) |
| every subset of `n` items | O(2ⁿ × n) | O(1) extra |

A mask also saves memory. Five features fit in one 4-byte `int`, instead of five separate `boolean` fields.

### In an interview

Listen for these clues:

- "Every element appears twice except one." Think XOR.
- "Do it in O(1) extra space" on a list of numbers. XOR or a mask may help.
- "Count the 1 bits", "power of two", "flags", "permissions".
- "All subsets" or "all combinations" with `n` up to about 20. Think of a mask loop.

Classic problems that use these tricks:

1. Single number: find the value without a partner.
2. Number of 1 bits, and counting the bits for every number from 0 to n.
3. Power of two.
4. Subsets: list every combination of the items.
5. Missing number: XOR 0 to n with the array, and the missing one is left.

## Worked example

The Harbor sensor sends `2563` for its 12 docks. This program reads it.

```java run
public class Main {
    static int countSetBits(int value) {
        int count = 0;
        while (value != 0) {
            value &= value - 1;
            count++;
        }
        return count;
    }

    public static void main(String[] args) {
        int sensor = 2563;
        System.out.println("Sensor bits: " + Integer.toBinaryString(sensor));

        StringBuilder busy = new StringBuilder();
        for (int dock = 0; dock < 12; dock++) {
            if ((sensor & (1 << dock)) != 0) {
                busy.append(dock).append(' ');
            }
        }
        System.out.println("Occupied docks: " + busy.toString().trim());
        System.out.println("Bikes docked: " + countSetBits(sensor));
        System.out.println("Free docks: " + (12 - countSetBits(sensor)));
        System.out.println("Lowest occupied dock: " + Integer.numberOfTrailingZeros(sensor));

        sensor = sensor & ~(1 << 9);
        System.out.println("After the bike in dock 9 leaves: " + Integer.toBinaryString(sensor));
        System.out.println("Library count agrees: " + (Integer.bitCount(sensor) == countSetBits(sensor)));
    }
}
```

It prints:

```text
Sensor bits: 101000000011
Occupied docks: 0 1 9 11
Bikes docked: 4
Free docks: 8
Lowest occupied dock: 0
After the bike in dock 9 leaves: 100000000011
Library count agrees: true
```

Line by line:

- `countSetBits` drops the lowest 1 bit each time round, so it loops 4 times for 4 bikes. It also ends for negative numbers, because every step removes a 1.
- The `for` loop tests each dock with `sensor & (1 << dock)`. A result other than 0 means a bike is there.
- 12 docks minus 4 bikes leaves 8 free docks.
- `numberOfTrailingZeros` counts the zeros below the lowest 1. Bit 0 is set, so the answer is 0.
- `sensor & ~(1 << 9)` clears dock 9 and leaves every other dock alone.
- `Integer.bitCount` gives the same count as the hand-made loop.

## Try it

A rider can add a basket (300 cents), a child seat (500 cents) and a bell (200 cents). The mask loop lists every combination. Run it, then change `budgetCents` to `800`, or add a fourth add-on.

```java run
public class Main {
    public static void main(String[] args) {
        String[] addOns = {"basket", "child seat", "bell"};
        long[] pricesCents = {300, 500, 200};
        long budgetCents = 600;
        int n = addOns.length;
        for (int mask = 0; mask < (1 << n); mask++) {
            long total = 0;
            StringBuilder chosen = new StringBuilder();
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    total += pricesCents[i];
                    chosen.append(addOns[i]).append(' ');
                }
            }
            String fits = total <= budgetCents ? "fits" : "too much";
            System.out.println(mask + " [" + chosen.toString().trim() + "] " + total + " cents, " + fits);
        }
    }
}
```

Three add-ons give 2³ = 8 lines, masks 0 to 7. Mask 5 is `101` in binary: the basket and the bell.

## Recap

- An `int` is 32 bits. Negative numbers use two's complement, so -1 is all ones and `~x == -x - 1`.
- `&`, `|`, `^` and `~` work bit by bit. `<<` shifts left, `>>` shifts right keeping the sign, and `>>>` shifts right filling with zeros.
- Test, set, clear and toggle bit `i` with `1 << i` and `&`, `|`, `& ~`, `^`. A mask of flags is a tiny, fast set.
- `n & (n - 1)` drops the lowest 1 bit, for counting bits and spotting powers of two. XOR cancels pairs.
- A mask loop from 0 to `(1 << n) - 1` visits all 2ⁿ subsets. Past bit 31, use a `long` and `1L <<`.

## Common mistakes

**1. Leaving out the brackets around &.**

```java nocompile
if (features & GPS != 0) {
    System.out.println("has GPS");
}
```

This does not compile: `error: bad operand types for binary operator '&'`, with `first type: int` and `second type: boolean`. `!=` runs before `&`, so Java tries `features & true`. Fix: `if ((features & GPS) != 0)`.

**2. Checking "any" when you mean "all".**

```java nocompile
static boolean has(int features, int flag) {
    return (features & flag) != 0;
}
```

A bike with only lights passes `has(bike, LIGHTS | GPS)`, because one shared bit is enough for `!= 0`. Fix: `(features & flag) == flag` checks that every bit of `flag` is there.

**3. Shifting an int past bit 31.**

```java nocompile
long docks = 0;
docks = docks | (1 << 40);
```

`docks` becomes `256`, which is dock 8, not dock 40. The `1` is an `int`, so Java shifts by 40 mod 32 = 8. Fix: `docks | (1L << 40)`.

**4. A >> loop on a negative number.**

```java nocompile
int count = 0;
while (value != 0) {
    count += value & 1;
    value >>= 1;
}
```

For a negative `value` this never ends. `>>` copies the sign bit in from the left, so `-1 >> 1` is still `-1`. Fix: shift with `>>>=`, which fills with zeros, or use `value &= value - 1`.

**5. Calling 0 a power of two.**

```java nocompile
static boolean isPowerOfTwo(int n) {
    return (n & (n - 1)) == 0;
}
```

`isPowerOfTwo(0)` returns `true`, because `0 & -1` is 0. So does `isPowerOfTwo(Integer.MIN_VALUE)`. Fix: `n > 0 && (n & (n - 1)) == 0`.
