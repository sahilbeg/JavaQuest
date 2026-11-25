## What happened

This morning Arjun removed the Diwali lantern keychain, `DC-1031`, from the catalogue. Customers who already had one in their cart opened the site and saw this:

```text
Your cart
3 items
undefined × 2          ₹NaN        [Remove]
Golu the elephant × 1  ₹450.00     [Remove]
Subtotal: ₹NaN
Delivery: ₹60.00
Total: ₹NaN
```

Nothing crashed. `tsc` reported no errors. The cart code is in `Cart.tsx`, and it looks items up in two tables from the stall's price sheet, `NAMES` and `PRICES`, in `catalogue.ts`.

```callout analogy
Picture a price list that says "every item has a price", taped above an empty shelf. Ask for something that isn't sold any more, and the list still promises an answer. The answer is blank.
```

## What you need

### Why the types didn't help

```tsx
export const PRICES: Record<string, number> = {
  "DC-1042": 45000,
};

const paise = PRICES["DC-1031"];
```

`Record<string, number>` says: "for every string key, there is a number". That promise is false for most strings. TypeScript believes it anyway, so `paise` has the type `number`, while at run time it is `undefined`.

Then `undefined * 2` is `NaN`, and `` `${undefined} × 2` `` is the text `undefined × 2`.

```callout jargon
An **index lookup** reads an object by a key held in a variable, like `PRICES[id]`. With a `Record<string, ...>`, its type says the value is always there, even when it isn't.
```

### Watch the missing item travel

```animate
title: One retired keychain breaks the whole total
kind: scene
nodes:
  - { id: line, label: "Cart line", emoji: "🧾", value: "DC-1031 × 2" }
  - { id: prices, label: "PRICES", emoji: "📒", value: "Record<string, number>" }
  - { id: row, label: "Row", emoji: "📄" }
  - { id: subtotal, label: "Subtotal", emoji: "➕", value: "₹450.00" }
steps:
  - say: The customer's cart still holds two lantern keychains, DC-1031.
    focus: [line]
  - say: The cart looks up the price. The type says a number comes back.
    send: { from: line, to: prices, label: "PRICES[\"DC-1031\"]" }
  - say: The key isn't there, so the real value is undefined. Nothing checks it.
    send: { from: prices, to: row, label: "undefined", back: true }
  - say: undefined times 2 is NaN. The row shows ₹NaN, and the name shows undefined.
    set: { row: "undefined × 2  ₹NaN" }
  - say: NaN plus anything is NaN, so one missing item wipes out the subtotal and the total.
    send: { from: row, to: subtotal, label: "+ NaN" }
    set: { subtotal: "₹NaN" }
```

### Syntax: a lookup that admits it can miss

```tsx
interface Found {
  name: string;
  paise: number;
}

function findItem(id: string): Found | undefined {
  ...
}
```

Make the possible miss part of the type. A function returning `Found | undefined` forces every caller to check before using the result. Then `tsc` points at every place that forgot.

```tsx
const item = findItem(line.id);
if (item === undefined) {
  // draw the "no longer available" row
} else {
  // here item is a Found
}
```

```callout tip
`Partial<Record<string, number>>`, or `Record<string, number | undefined>`, says the same thing in the table's own type. Many teams also turn on `noUncheckedIndexedAccess` in tsconfig, which adds `| undefined` to every index lookup.
```

### Syntax: is this key really there?

```tsx
Object.hasOwn(PRICES, id)
```

A plain object isn't empty underneath. It inherits methods like `toString` and `constructor`. So `PRICES["toString"]` is a function, not `undefined`, and a check for `undefined` lets it through.

`Object.hasOwn(obj, key)` is true only for keys written in the object itself. Use it before trusting an id that came from outside, like a share link.

```callout jargon
An **own property** is one written on the object itself, not inherited. `Object.hasOwn` checks for own properties only.
```

## The tiers

1. **Bronze** resolves the incident: a missing item never shows `undefined` or `NaN`. Its line reads `DC-1031 is no longer available`, and the subtotal and total leave it out.
2. **Silver**: the missing line has a **Remove** button (labelled `Remove DC-1031`) that removes it, and the item count only counts items that can still be bought.
3. **Gold**: ids like `toString` and `constructor`, from a broken share link, are treated as missing too, not as real items.

## Recap

- `Record<string, T>` promises a value for every key. A lookup of a missing key is really `undefined`, but its type says `T`.
- Make misses visible in the types: a lookup that returns `T | undefined`, which callers must check.
- `undefined` in arithmetic gives `NaN`, and one `NaN` spoils every total after it.
- Plain objects inherit keys like `toString`. Check `Object.hasOwn` before trusting an outside id.

## Common mistakes

**1. Hiding it with a default.** `PRICES[line.id] ?? 0` removes the `NaN`, but the row still says `undefined × 2` and charges nothing for it, as if it were free. Show the line as no longer available instead.

**2. Checking only for undefined.** `if (NAMES[id] !== undefined)` passes bronze and silver, but `NAMES["toString"]` is a function, so gold fails. Use `Object.hasOwn`.

**3. Counting missing items.** If the count still adds every line's quantity, the header says `3 items` while only one can be bought. Count from the lines that were found.
