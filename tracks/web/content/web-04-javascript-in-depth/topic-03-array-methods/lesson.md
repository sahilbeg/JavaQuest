## Hook

Sana's catalogue page asks five questions of one list. What are the names? Which items are in stock? What is all the stock worth? Which item is `DC-1042`? What is the cheapest-first order?

You could answer each with a `for` loop, a temporary array and a counter. Or you could say what you want in one line, and let the array do the looping. That is what array methods are for.

## Concept

### Methods that loop for you

An array has built-in methods that take a callback. The method runs the loop, and your callback says what to do with each item. You met callbacks in the last ticket.

```callout jargon
A **method** is a function that belongs to a value. You call it with a dot: `prices.map(...)` calls the `map` method of the array `prices`.
```

Most of these methods return a new value and leave the original array alone. `sort` is the exception, and you'll see how to handle it.

### Syntax: map

```js
const names = items.map((item) => item.name);
```

`map` calls the callback once per item and collects what it returns. The new array always has the same length as the old one.

```js run
const cart = [
  { code: "CST", paise: 15000, qty: 2 },
  { code: "BAG", paise: 89900, qty: 1 },
];
const codes = cart.map((line) => line.code);
const totals = cart.map((line) => line.paise * line.qty);
console.log(codes.join(", "));
console.log(totals.join(", "));
console.log(cart.length);
```

It prints `CST, BAG`, then `30000, 89900`. The cart still has its 2 lines, unchanged.

### Syntax: filter

```js
const inStock = items.filter((item) => item.stock > 0);
```

`filter` keeps the items where the callback returns `true`, and drops the rest. The new array can be shorter, or even empty.

```js run
const prices = [15000, 12000, 45000, 89900, 35000];
const under400 = prices.filter((p) => p < 40000);
console.log(under400.join(", "));
console.log(prices.length, under400.length);
```

It prints `15000, 12000, 35000`, then `5 3`.

```callout jargon
A **predicate** is a callback that answers yes or no by returning `true` or `false`. `filter` and `find` both take one.
```

### Syntax: find

```js
const golu = items.find((item) => item.id === "DC-1042");
```

`find` returns the first item where the callback returns `true`. It stops looking as soon as it finds one. If nothing matches, it returns `undefined`.

```js run
const items = [
  { id: "DC-1042", name: "Golu the elephant" },
  { id: "DC-1045", name: "Indigo tote bag" },
];
const golu = items.find((item) => item.id === "DC-1042");
const missing = items.find((item) => item.id === "DC-9999");
console.log(golu.name);
console.log(missing);
```

It prints `Golu the elephant`, then `undefined`. `find` gives one item; `filter` always gives an array.

### Syntax: reduce

```js
const total = prices.reduce((sum, p) => sum + p, 0);
```

`reduce` boils a whole array down to one value. The callback gets the running result so far and the next item, and returns the new running result. The `0` at the end is the starting value.

```animate
title: reduce adds up the prices
kind: code
language: js
code: |
  const prices = [15000, 12000, 45000];
  const total = prices.reduce((sum, p) => sum + p, 0);
  console.log(total);
steps:
  - line: 1
    vars: { prices: "[15000, 12000, 45000]" }
    say: Three prices in paise.
  - line: 2
    vars: { sum: 0 }
    say: reduce starts the running result, sum, at 0. That is the value after the callback.
  - line: 2
    vars: { p: 15000 }
    say: First call. sum is 0 and p is the first price, 15000.
  - line: 2
    vars: { sum: 15000 }
    say: The callback returns 0 + 15000. reduce keeps it as the new sum.
  - line: 2
    vars: { p: 12000, sum: 27000 }
    say: Second call. 15000 + 12000 gives a sum of 27000.
  - line: 2
    vars: { p: 45000, sum: 72000 }
    say: Third call. 27000 + 45000 gives 72000.
  - line: 2
    vars: { sum: ~, p: ~, total: 72000 }
    say: No items are left, so reduce returns the last sum. It goes into total.
  - line: 3
    out: "72000"
    say: One number from a whole array.
```

```callout jargon
The **accumulator** is the running result that `reduce` passes from one call to the next. Here it is `sum`.
```

### Syntax: sort with a compare function

```js
const cheapest = items.slice().sort((a, b) => a.paise - b.paise);
```

`sort` puts items in order. Give it a compare function that takes two items. Return a negative number if `a` goes first, a positive one if `b` goes first, and 0 for a tie. `a.paise - b.paise` does exactly that, cheapest first.

Two warnings. Without a compare function, `sort` compares items as text, so numbers come out wrong. And `sort` changes the array you call it on, so copy it first with `slice()`.

```js run
const prices = [89900, 12000, 3000];
console.log(prices.slice().sort().join(", "));
console.log(prices.slice().sort((a, b) => a - b).join(", "));
console.log(prices.join(", "));
```

As text, `"12000"` comes before `"3000"`, because `1` comes before `3`. The compare function fixes it. The last line shows the original order was kept, thanks to `slice()`.

```callout tip
Newer JavaScript also has `toSorted`, which sorts a copy for you: `items.toSorted((a, b) => a.paise - b.paise)`. Both ways are fine.
```

### Chaining: one method after another

`filter` and `map` return arrays, so you can call another method straight on the result. Read a chain from top to bottom, like a recipe.

```js
const cheapNames = items
  .filter((item) => item.paise < 40000)
  .map((item) => item.name);
```

## Worked example

Three of Sana's questions, answered from one list.

```js run
const catalogue = [
  { id: "DC-1042", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 },
  { id: "DC-1043", name: "Marigold coaster set", colour: "Marigold", paise: 15000, stock: 12 },
  { id: "DC-1044", name: "Mint owl keychain", colour: "Mint", paise: 12000, stock: 0 },
  { id: "DC-1045", name: "Indigo tote bag", colour: "Indigo", paise: 89900, stock: 5 },
];

const inStockNames = catalogue
  .filter((item) => item.stock > 0)
  .sort((a, b) => a.paise - b.paise)
  .map((item) => item.name);
console.log(inStockNames.join(" | "));

const units = catalogue.reduce((sum, item) => sum + item.stock, 0);
console.log("Units on the shelf: " + units);

const bag = catalogue.find((item) => item.colour === "Indigo" && item.paise > 50000);
console.log(bag.id);
```

Line by line:

1. `filter` drops the keychain, which has no stock. It returns a new array of three items.
2. `sort` orders that new array by price. The catalogue itself is untouched, because `filter` already made a copy.
3. `map` turns each item into its name. It prints `Marigold coaster set | Golu the elephant | Indigo tote bag`.
4. `reduce` adds up the stock, starting from 0: 3 + 12 + 0 + 5 = 20.
5. `find` returns the first Indigo item above ₹500, the tote bag `DC-1045`.

## Try it

Add a line that prints the names of the Indigo items, joined with `" and "`. Then use `reduce` to work out the value of the stock: each item's `paise` times its `stock`, all added up.

```js run
const catalogue = [
  { id: "DC-1042", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 },
  { id: "DC-1043", name: "Marigold coaster set", colour: "Marigold", paise: 15000, stock: 12 },
  { id: "DC-1045", name: "Indigo tote bag", colour: "Indigo", paise: 89900, stock: 5 },
];
const prices = catalogue.map((item) => item.paise);
console.log(prices.join(", "));
```

## Recap

- `map` changes every item and keeps the length. `filter` keeps the items that pass a test.
- `find` returns the first match, or `undefined` when nothing matches.
- `reduce` turns a whole array into one value. Always give it a starting value, like `0`.
- `sort` needs a compare function for numbers, like `(a, b) => a - b`. It changes the array, so sort a copy made with `slice()`.
- Methods that return arrays can be chained: `filter`, then `sort`, then `map`.

## Common mistakes

**1. Braces in a map callback, with no return.**

```js run
const prices = [15000, 12000];
const doubled = prices.map((p) => { p * 2; });
console.log(doubled.length, doubled[0]);
```

It prints `2 undefined`. The callback returns nothing, so `map` collects `undefined` for every item. Fix: `prices.map((p) => p * 2)`.

**2. Sorting the original list by accident.**

```js run
const catalogue = [
  { name: "Indigo tote bag", paise: 89900 },
  { name: "Mint owl keychain", paise: 12000 },
];
const cheapest = catalogue.sort((a, b) => a.paise - b.paise);
console.log(catalogue[0].name);
```

It prints `Mint owl keychain`: the catalogue itself was reordered. Every other part of the page now sees the new order. Fix: `catalogue.slice().sort(...)`.

**3. Calling reduce on an empty array with no starting value.**

```js run crash
const prices = [];
console.log(prices.reduce((sum, p) => sum + p));
```

Node stops with `TypeError: Reduce of empty array with no initial value`. An empty cart is normal, so this crash would reach customers. Fix: always pass the start, `prices.reduce((sum, p) => sum + p, 0)`.

**4. Using filter when you want one item.**

```js run
const items = [{ id: "DC-1042", name: "Golu the elephant" }];
const golu = items.filter((item) => item.id === "DC-1042");
console.log(golu.name);
```

It prints `undefined`, because `golu` is an array holding one item. Arrays have no `name`. Fix: use `find`, which returns the item itself.
