## The design

Meera Aunty's customers ask very different questions. The engine answers all of them with one function, `runQuery(items, query)`.

A query is a list of filters, plus an optional sort. Each filter is one member of a discriminated union, tagged by `by`:

```ts
type Filter =
  | { by: "colour"; colour: Colour }
  | { by: "price"; minPaise?: number; maxPaise?: number }
  | { by: "inStock" }
  | { by: "search"; text: string };
```

"Gifts under ₹500 that you have today, cheapest first" becomes:

```ts
const query: Query = {
  filters: [{ by: "price", maxPaise: 50000 }, { by: "inStock" }],
  sort: { key: "paise", direction: "asc" },
};
```

An item must pass **every** filter to be kept. Then the items left are sorted, if a sort was asked for.

```animate
title: Gifts under ₹500, in stock, cheapest first
kind: scene
nodes:
  - { id: all, label: Catalogue, emoji: "🧶", value: "8 items" }
  - { id: price, label: "by: price", emoji: "₹", value: "max 50000" }
  - { id: stock, label: "by: inStock", emoji: "📦" }
  - { id: sort, label: "sort: paise asc", emoji: "↕️" }
steps:
  - say: The whole catalogue goes in. The array passed in is never changed.
    focus: [all]
  - say: The price filter keeps items up to 50000 paise. The ₹899 tote bags drop out.
    send: { from: all, to: price, label: "8 items" }
    set: { price: "6 left" }
  - say: The in-stock filter drops anything with a stock of 0.
    send: { from: price, to: stock, label: "6 items" }
    set: { stock: "4 left" }
  - say: Last, the copy is sorted by paise, cheapest first. Items with the same price keep their catalogue order.
    send: { from: stock, to: sort, label: "4 items" }
    set: { sort: "KEY, CST, AMI, AMI" }
```

The engine has two helpers for Sana's page. `countBy(items, "colour")` counts the items per colour, for the numbers on the colour buttons. `toCard(item)` turns an item into what a product card shows.

Because `Filter` is a union with an exhaustive `switch`, adding a new kind of filter later, like `{ by: "code" }`, is a type error everywhere it isn't handled yet. That's how the engine can grow without breaking.

## What you build

Everything goes in `src/catalogue.ts`. The types, `rupees` and `assertNever` are already there. An item looks like `{ id: "DC-1042", code: "AMI", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 }`.

1. **`matches(item, filter)`** says whether one item passes one filter:
   - `colour`: the item's colour is exactly that colour.
   - `price`: `minPaise <= paise <= maxPaise`, ends included. Either limit may be missing, and `0` is a real limit.
   - `inStock`: the stock is above 0.
   - `search`: the name contains the text, ignoring capital letters and spaces around the text. Empty text matches everything.
   - any other `by`: throw, through `assertNever`.
2. **`runQuery(items, query)`** returns a new array of the items that pass every filter, in catalogue order. With `query.sort`, sort them by `name`, `paise` or `stock`, `"asc"` or `"desc"`. Ties keep catalogue order. Never change `items`.
3. **`countBy(items, key)`** returns a `Map` from each value of that key to how many items have it, in the order values first appear.
4. **`toCard(item)`** returns `{ id, name, price, soldOut }`, where `price` is `rupees(item.paise)` and `soldOut` is `true` when the stock is 0.

### Syntax: a key that must be sortable

```ts
type SortKey = keyof Pick<Item, "name" | "paise" | "stock">;
```

`Pick` keeps three fields, and `keyof` turns them into the union `"name" | "paise" | "stock"`. Sorting by `id` or `colour` is then a type error.

### Syntax: comparing by a key

```ts
function compare(a: Item, b: Item, key: SortKey): number {
  if (key === "name") {
    return a.name.localeCompare(b.name);
  }
  return a[key] - b[key];
}
```

After the `if`, `key` is `"paise" | "stock"`, so `a[key]` is a number and subtraction is allowed. Multiply the result by `-1` for `"desc"`.

```callout tip
`Array.prototype.sort` keeps items that compare equal in their original order. So sorting a filtered copy keeps ties in catalogue order for free.
```

## Recap

- One union, `Filter`, describes every question; `matches` handles each member in its own `case`, with `assertNever` as the guard.
- `runQuery` keeps items that pass every filter (`every`), then sorts a copy.
- `keyof Pick<Item, ...>` limits sorting to the keys that make sense.
- `countBy<K extends keyof Item>` returns a `Map<Item[K], number>`, typed by the key you pass.
- `toCard` builds exactly what the page shows, and nothing else.

## Common mistakes

**Testing a price limit with if (filter.maxPaise).** A limit of `0` is falsy, so it is ignored. Compare with `undefined` instead.

**Sorting the array that was passed in.** `items.sort(...)` reorders the catalogue for every other part of the page. `filter` already gives you a copy, so sort that.

**A default case that returns true.** Every new kind of filter would let every item through, without any error. Use `assertNever(filter)` so a forgotten kind is caught.
