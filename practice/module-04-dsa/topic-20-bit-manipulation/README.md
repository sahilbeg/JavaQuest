# Bit manipulation

Fix the bit helpers for bike features and dock sensors. Both classes live in package `jq.m04.bitmanipulation`. Every method is `static`.

## Part 1: `BikeFeatures`

A bike's features are one `int`, one bit per feature. The constants are already there: `LIGHTS` (bit 0), `BASKET` (bit 1), `CHILD_SEAT` (bit 2), `GPS` (bit 3) and `BELL` (bit 4). A `flag` may combine several features, such as `LIGHTS | GPS`.

1. **TODO 1:** `has(features, flag)` is `true` only when the bike has **every** feature in `flag`. With `LIGHTS | BASKET`, `has(.., LIGHTS | GPS)` is `false`.
2. **TODO 2:** `add(features, flag)` turns every bit of `flag` on. Adding a feature the bike already has changes nothing.
3. **TODO 3:** `remove(features, flag)` turns every bit of `flag` off. Removing a feature the bike does not have changes nothing.
4. **TODO 4:** `toggle(features, flag)` flips every bit of `flag`.
5. **TODO 5:** `addOnCombos(long[] pricesCents, long budgetCents)` returns every combination of add-ons whose total price is at most `budgetCents`. Each combination is a mask: bit `i` set means add-on `i` is chosen. Return the masks in increasing order, as a `List<Integer>`. Mask `0`, no add-ons, costs 0 and is always in the list when the budget is 0 or more. With no add-ons the answer is `[0]`. There are at most 20 add-ons.

## Part 2: `BitTricks`

6. **TODO 6:** `isBitSet(value, bit)` is `true` when bit number `bit` of `value` is 1. Bit 0 is the lowest bit, and bit 31 is the sign bit, so `isBitSet(-1, 31)` is `true`. Throw `IllegalArgumentException` when `bit` is below 0 or above 31.
7. **TODO 7:** `countSetBits(value)` returns how many bits are 1 in the 32-bit form of `value`. Negative numbers count in two's complement: `countSetBits(-1)` is 32 and `countSetBits(Integer.MIN_VALUE)` is 1. Write the loop yourself with `value & (value - 1)`. It must finish for negative numbers.
8. **TODO 8:** `isPowerOfTwo(value)` is `true` for 1, 2, 4, 8 and so on. It is `false` for 0, for every negative number (including `Integer.MIN_VALUE`), and for numbers with more than one 1 bit.
9. **TODO 9:** `unpairedDock(int[] dockIds)`: every id appears exactly twice except one, which appears once. Return that one. Ids may be 0 or negative. The array always has at least one id.
10. **TODO 10:** `occupiedMask(int... docks)` returns a `long` for a 64-dock station: bit `d` is 1 when dock `d` is in the list. Docks are 0 to 63, so docks 32 to 63 need a `long` shift. A dock listed twice still sets one bit. No docks give `0`. Throw `IllegalArgumentException` for a dock below 0 or above 63.

Tests that could loop forever stop after 2 seconds.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
