## Hook

Meera Aunty packs every order in the same kind of gift box. The box doesn't care what goes in: coasters, a keychain, or Golu himself.

But once a box is packed, the sticker on the lid says exactly what is inside. Nobody opens a box marked "Keychain" and finds an elephant.

TypeScript has the same idea. A **generic** function is one box that works for any type. Each time you use it, TypeScript writes the sticker for you, so it still knows exactly what comes out.

## Concept

### The problem: one function, three copies

The catalogue code has three functions that do the same job. Only the types are different.

```ts
function firstItem(items: Item[]): Item | undefined {
  return items[0];
}
function firstOrder(orders: Order[]): Order | undefined {
  return orders[0];
}
function firstColour(colours: string[]): string | undefined {
  return colours[0];
}
```

Someone tried to merge them into one function with `any`:

```ts
function first(list: any[]): any {
  return list[0];
}
const golu = first(items);
console.log(golu.priceInPaise);
```

That compiles without a word, even though items have `paise`, not `priceInPaise`. The page shows `undefined`, and nobody is warned.

```callout jargon
**any** switches type checking off for a value. TypeScript lets you do anything with it, so it can't warn you about any mistake you make with it.
```

### Syntax: a generic function

```ts
function first<T>(list: T[]): T | undefined {
  return list[0];
}
```

`<T>` after the name declares a **type parameter**. Read it as "for some type T". The list holds T values, and what comes back is a T, or `undefined` for an empty list.

```callout jargon
A **type parameter** is a placeholder for a type, written in angle brackets. A **generic** function or type has at least one. `T` is the usual name, but any name works, like `TItem`.
```

### TypeScript fills in T for you

You don't say what T is when you call `first`. TypeScript looks at the argument and works it out.

```callout jargon
**Type inference** means TypeScript works out a type by itself, from the values you pass or assign, so you don't have to write it.
```

Watch two calls to the same function. Each call gets its own T.

```animate
title: One function, a new T for every call
kind: code
language: ts
code: |
  function first<T>(list: T[]): T | undefined {
    return list[0];
  }
  const paise = first([15000, 12000]);
  const code = first(["CST", "KEY"]);
  console.log(paise, code);
steps:
  - line: 4
    say: TypeScript sees an array of numbers, so for this call T is number. paise gets the type number | undefined.
  - line: 2
    vars: { list: "[15000, 12000]" }
    say: Inside, list is the array. When the code runs, the types are gone. It is plain JavaScript.
  - line: 4
    vars: { list: ~, paise: 15000 }
    say: The first element comes back, so paise holds 15000.
  - line: 5
    vars: { code: '"CST"' }
    say: A new call, a new T. This array holds strings, so T is string, and code is a string or undefined.
  - line: 6
    out: "15000 CST"
    say: One function served both calls, and each result kept its own type.
```

### Syntax: writing T yourself

```ts
const none = first<number>([]);
```

Sometimes there is nothing to infer from, like an empty array. Then you write the type in angle brackets after the function's name.

```ts run
function first<T>(list: T[]): T | undefined {
  return list[0];
}
const prices = [15000, 12000, 45000];
const colours = ["Marigold", "Indigo"];
console.log(first(prices));
console.log(first(colours));
console.log(first<number>([]));
```

It prints `15000`, `Marigold` and `undefined`. In your editor, hover over `first(prices)`: it says `number | undefined`.

### Syntax: more than one type parameter

```ts
function mapList<T, U>(list: T[], change: (item: T) => U): U[] {
  return list.map((item) => change(item));
}
```

Separate type parameters with commas. Here T is what goes in, and U is what comes out. TypeScript infers both: T from the list, and U from what your function returns.

```ts run
function mapList<T, U>(list: T[], change: (item: T) => U): U[] {
  const result: U[] = [];
  for (const item of list) {
    result.push(change(item));
  }
  return result;
}
const items = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-1043", name: "Marigold coaster set", paise: 15000 },
];
const names = mapList(items, (item) => item.name);
const rupees = mapList(items, (item) => item.paise / 100);
console.log(names.join(", "));
console.log(rupees.join(" + "));
```

It prints `Golu the elephant, Marigold coaster set`, then `450 + 150`. TypeScript knows `names` is a `string[]` and `rupees` is a `number[]`.

### Syntax: generic types

```ts
type Shelf<T> = {
  name: string;
  things: T[];
};
```

Types can have type parameters too. Here you fill in T when you use the type, so `Shelf<string>` is a shelf of strings.

```ts
const keychains: Shelf<string> = { name: "Keychains", things: ["Mint", "Indigo"] };
const prices: Shelf<number> = { name: "Price board", things: [15000, 12000] };
```

You've already used generic types that come with JavaScript: `Array<T>` (the long way to write `T[]`), `Set<T>`, `Map<K, V>` and `Promise<T>`.

### Why not unknown?

`unknown` is safe, but it forgets. A function that returns `unknown` makes you check the type all over again.

```ts
function firstUnknown(list: unknown[]): unknown {
  return list[0];
}
const price = firstUnknown([15000, 12000]);
price.toFixed(2);
```

```text
error TS18046: 'price' is of type 'unknown'.
```

A type parameter keeps the link between input and output. What goes in decides what comes out.

## Worked example

Raju wants to know how many orders were paid by each mode. Here is one counting helper that works for any list.

```ts run
function countBy<T>(list: T[], keyOf: (item: T) => string): Map<string, number> {
  const counts = new Map<string, number>();
  for (const item of list) {
    const key = keyOf(item);
    counts.set(key, (counts.get(key) ?? 0) + 1);
  }
  return counts;
}

const orders = [
  { id: "order-17", mode: "UPI" },
  { id: "order-18", mode: "CASH" },
  { id: "order-19", mode: "UPI" },
];
const byMode = countBy(orders, (order) => order.mode);
console.log(byMode.get("UPI"), byMode.get("CASH"), byMode.get("CARD"));
```

Line by line:

1. `countBy<T>` works for a list of anything. `keyOf` turns one element into the text you count by.
2. `new Map<string, number>()` passes type arguments to a generic class: keys are strings, counts are numbers.
3. For each element, `keyOf(item)` gives its key. `counts.get(key) ?? 0` is the count so far, or 0 the first time.
4. At the call, TypeScript infers T as `{ id: string; mode: string }`. So inside the arrow, `order.mode` is checked. A typo like `order.mood` would be an error.
5. It prints `2 1 undefined`. Nobody paid by card, so that key was never set.

## Try it

Run this, then write a `lastOf<T>(list: T[]): T | undefined` next to `first`. Call it on `colours` and on `[15000, 12000]`, and hover over each result in your editor to see its type.

```ts run
function first<T>(list: T[]): T | undefined {
  return list[0];
}
const colours = ["Mint", "Indigo", "Mint"];
console.log(first(colours));
```

## Recap

- A generic function has type parameters, like `<T>`, so one function works for many types.
- TypeScript infers T from the arguments. Write it yourself, as in `first<number>([])`, only when there is nothing to infer from.
- Use several type parameters, like `<T, U>`, when the output's type differs from the input's.
- Types can be generic too: `Shelf<string>`, `Map<string, number>`, `Promise<T>`.
- Unlike `any` or `unknown`, a type parameter keeps the link between what goes in and what comes out.

## Common mistakes

**1. Reaching for any instead of a type parameter.**

```ts
function first(list: any[]): any {
  return list[0];
}
const golu = first([{ id: "DC-1042", paise: 45000 }]);
console.log(golu.price);
```

TypeScript says nothing, and the page shows `undefined`. Fix: `function first<T>(list: T[]): T | undefined`. Now `golu.price` is an error, because TypeScript knows the element has `id` and `paise`.

**2. A type parameter TypeScript can't infer.**

```ts
function emptyShelf<T>(): T[] {
  return [];
}
const shelf = emptyShelf();
const top = shelf[0];
top.toUpperCase();
```

```text
error TS18046: 'top' is of type 'unknown'.
```

No argument mentions T, so TypeScript has nothing to infer from and picks `unknown`. Fix: write the type at the call, `emptyShelf<string>()`.

**3. Using T as if it were a value.**

```ts
function describe<T>(value: T): string {
  if (T === "string") {
    return "text";
  }
  return "something else";
}
```

```text
error TS2693: 'T' only refers to a type, but is being used as a value here.
```

Types disappear when the code runs, so there is no T to compare. Fix: check the value instead, with `typeof value === "string"`.
