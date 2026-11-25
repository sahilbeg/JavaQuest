## Hook

A shopping list is a list of the same kind of thing: items. A receipt line is different. It's a fixed little row: the item, then the quantity, always in that order, always two parts.

TypeScript has a type for each. An **array type** says "a list, any length, every entry the same kind". A **tuple** says "exactly these parts, in exactly this order".

## Concept

### Syntax: array types

```ts
const prices: number[] = [15000, 12000, 45000];
```

`number[]` means "an array of numbers". `string[]` is an array of strings. You may also see `Array<number>`, which means exactly the same.

Every entry is now checked, including the ones you add later:

```ts
const prices: number[] = [15000, 12000];
prices.push("45000");
```

```text
error TS2345: Argument of type 'string' is not assignable to parameter of type 'number'.
```

When an array starts with values, TypeScript infers its type: `const codes = ["CST", "KEY"]` is a `string[]`. An empty array has nothing to infer from, so give it a type: `const codes: string[] = [];`.

```callout jargon
An **array type** like `number[]` describes a list of any length whose entries all have the same type.
```

### Array methods know the types

The array methods from web-04 work as before. TypeScript follows the types through each one, so you rarely annotate inside them:

```ts run
const prices: number[] = [15000, 12000, 45000];
const labels = prices.map((p) => `₹${p / 100}`);
const cheap = prices.filter((p) => p < 20000);
const total = prices.reduce((sum, p) => sum + p, 0);
console.log(labels.join(", "));
console.log(cheap.join(", "), total);
```

It prints `₹150, ₹120, ₹450`, then `15000, 12000 72000`.

- `p` is a `number` in every callback, because `prices` is a `number[]`.
- `map` gives back what the callback returns, so `labels` is a `string[]`.
- `filter` keeps the same type: `cheap` is a `number[]`.
- `reduce` starting from `0` gives a `number`.

One method needs care. `find` might find nothing, so `prices.find((p) => p > 50000)` has the type `number | undefined`. The `|` means "or", and you'll meet it in ticket 6.

### Syntax: readonly arrays

```ts
const COLOURS: readonly string[] = ["Marigold", "Indigo", "Mint"];
```

`readonly` in front of an array type means "nobody may change this list". The methods that change an array, like `push`, `pop` and `sort`, simply don't exist on it:

```ts
const COLOURS: readonly string[] = ["Marigold", "Indigo", "Mint"];
COLOURS.push("Rani pink");
```

```text
error TS2339: Property 'push' does not exist on type 'readonly string[]'.
```

Methods that make a **new** array, like `map`, `filter` and `slice`, still work. A function that only reads a list should take a `readonly` one. Then callers know it won't be changed.

```callout jargon
A **readonly array** is an array type that only allows reading. TypeScript removes every method that would change it.
```

### Syntax: tuples

```ts
const line: [string, number] = ["KEY", 2];
```

A **tuple** is an array with a fixed number of entries, each with its own type. `[string, number]` is exactly two entries: text first, then a number. You can also name the parts, which makes the type easier to read: `[code: string, qty: number]`. The names are only labels. The tuple is still `["KEY", 2]`.

TypeScript checks each position, and the length:

```ts
const line: [string, number] = ["KEY", 2, true];
```

```text
error TS2322: Type '[string, number, boolean]' is not assignable to type '[string, number]'.
  Source has 3 element(s) but target allows only 2.
```

```callout jargon
A **tuple** is a fixed-length array where each position has its own type, like `[string, number]`. Reading `line[0]` gives a `string`, and `line[1]` gives a `number`.
```

### Destructuring tuples

Destructuring from web-04 works on tuples, and each name gets the type of its position. A cart as a list of tuples has the type `[string, number][]`: an array whose entries are `[string, number]` pairs. Watch a loop add it up:

```animate
title: Adding up a cart of tuples
kind: code
language: ts
code: |
  const cart: [string, number][] = [["CST", 2], ["KEY", 1]];
  let total = 0;
  for (const [code, qty] of cart) {
    const paise = code === "CST" ? 15000 : 12000;
    total += paise * qty;
  }
  console.log(total);
steps:
  - line: 1
    objects: { c1: { type: "[string, number][]", fields: { "0": '["CST", 2]', "1": '["KEY", 1]' } } }
    vars: { cart: "@c1" }
    say: A cart is an array of tuples. Each tuple is a code, then a quantity.
  - line: 2
    vars: { total: 0 }
    say: The running total starts at 0, so it's inferred as a number.
  - line: 3
    vars: { code: '"CST"', qty: 2 }
    say: Destructuring splits the first tuple. code is a string and qty is a number, because of their positions.
  - line: 4
    vars: { paise: 15000 }
    say: A coaster set costs 15000 paise.
  - line: 5
    vars: { total: 30000 }
    say: Two sets make 30000.
  - line: 3
    vars: { code: '"KEY"', qty: 1, paise: ~ }
    say: The loop moves to the second tuple.
  - line: 5
    vars: { paise: 12000, total: 42000 }
    say: One keychain adds 12000, so the total is 42000.
  - line: 7
    vars: { code: ~, qty: ~, paise: ~ }
    out: "42000"
    say: The loop is done, and the total prints.
```

### Returning a tuple

A function can give back two values at once as a tuple. The return type makes the order part of the promise:

```ts run
function splitRupees(paise: number): [number, number] {
  return [Math.floor(paise / 100), paise % 100];
}
const [rupees, left] = splitRupees(15050);
console.log(rupees, left);
```

It prints `150 50`. You'll see this pattern again in React, where `useState` gives back a `[value, setValue]` pair.

## Worked example

Meera Aunty wants to know how many small toys each yarn colour can still make, at 40 g a toy:

```ts run
const yarn: [colour: string, grams: number][] = [
  ["Marigold", 130],
  ["Indigo", 85],
  ["Mint", 300],
];

function toysFrom(stock: readonly [string, number][]): [string, number][] {
  return stock.map(([colour, grams]): [string, number] => [colour, Math.floor(grams / 40)]);
}

for (const [colour, toys] of toysFrom(yarn)) {
  console.log(`${colour}: ${toys} toys`);
}
```

1. `yarn` is an array of named tuples: a colour, then grams.
2. `toysFrom` takes a `readonly` list, so it promises not to change the stock. `map` is allowed, because it makes a new array.
3. The callback destructures each tuple. Its return type, `: [string, number]`, tells TypeScript the new array literal is a tuple, not a plain array.
4. The loop destructures again and prints `Marigold: 3 toys`, `Indigo: 2 toys` and `Mint: 7 toys`.

## Try it

Run this. Then add a third tuple, `["BTY", 35000]`, and print the most expensive item too.

```ts run
const items: [code: string, paise: number][] = [
  ["KEY", 12000],
  ["AMI", 45000],
];
const cheapest = [...items].sort((a, b) => a[1] - b[1])[0];
console.log(cheapest[0], cheapest[1]);
```

## Recap

- `number[]` is an array of numbers, and every entry is checked, including ones you `push` later.
- Array methods carry the types along: `map` gives a new array of whatever its callback returns.
- `readonly string[]` is a list nobody may change. `push` and `sort` don't exist on it.
- A tuple like `[code: string, qty: number]` has a fixed length and a type for each position.
- Destructuring a tuple gives each name the type of its position, and a function can return a tuple.

## Common mistakes

**1. Pushing the wrong type into an array.**

```ts
const prices: number[] = [];
prices.push("12000");
```

```text
error TS2345: Argument of type 'string' is not assignable to parameter of type 'number'.
```

Convert the text before it goes in: `prices.push(Number("12000"));`.

**2. Sorting a list you were only meant to read.**

```ts
function cheapestFirst(list: readonly [string, number][]) {
  return list.sort((a, b) => a[1] - b[1]);
}
```

```text
error TS2339: Property 'sort' does not exist on type 'readonly [string, number][]'.
```

`sort` changes the array in place, and that's how the price list got reordered for everyone. Copy first: `[...list].sort(...)`.

**3. Putting a tuple's parts in the wrong order.**

```ts
const line: [string, number] = [2, "KEY"];
```

```text
error TS2322: Type 'number' is not assignable to type 'string'.
error TS2322: Type 'string' is not assignable to type 'number'.
```

Each position has its own type. The code comes first, then the quantity: `["KEY", 2]`.

**4. Leaving an empty array without a type.**

```ts
const codes = [];
```

There is nothing in it to infer from. TypeScript starts it as an `any[]` and guesses the type from what you push later. Where it can't guess, strict mode reports an error about an implicit `any[]`. Say what goes in it up front: `const codes: string[] = [];`.
