## The design

Sana's catalogue page has a row of controls above the items: colour buttons, a price slider, an "In stock only" switch, a search box and a sort menu. Whenever a customer changes one, the page asks the engine for the items to show.

The engine is one function, `filterCatalogue(items, options)`. The page passes the whole catalogue and an options object. Any option can be left out, and a left-out option filters nothing.

```js
filterCatalogue(catalogue, {
  colour: "Indigo",
  maxPaise: 50000,
  inStockOnly: true,
  sort: "price-asc",
});
```

That is Meera Aunty's evening question: something in Indigo, under ₹500, in stock, cheapest first. The engine works like a row of sieves. Each one lets fewer items through, and sorting comes last.

```animate
title: Indigo, under ₹500, in stock, cheapest first
kind: scene
nodes:
  - { id: all, label: Catalogue, emoji: "🧶", value: "8 items", row: 0, col: 0 }
  - { id: colour, label: Colour, emoji: "🎨", row: 0, col: 1 }
  - { id: price, label: Price, emoji: "₹", row: 0, col: 2 }
  - { id: stock, label: In stock, emoji: "📦", row: 1, col: 2 }
  - { id: search, label: Search, emoji: "🔍", row: 1, col: 1 }
  - { id: sort, label: Sort, emoji: "↕️", row: 1, col: 0 }
steps:
  - say: The page hands over all 8 items and the options. The list it was given will never be changed.
    focus: [all]
  - say: The colour sieve keeps the Indigo items. Golu, the tote bag and the Indigo coaster set pass.
    send: { from: all, to: colour, label: "8 items" }
    set: { colour: "3 left" }
  - say: The price sieve keeps items up to ₹500. The ₹899 tote bag drops out.
    send: { from: colour, to: price, label: "3 items" }
    set: { price: "2 left" }
  - say: Both are in stock, so the stock sieve keeps them both.
    send: { from: price, to: stock, label: "2 items" }
    set: { stock: "2 left" }
  - say: No search text was typed, so the search sieve lets everything through.
    send: { from: stock, to: search, label: "2 items" }
    set: { search: "2 left" }
  - say: Last, the sort puts the ₹150 coaster set before ₹450 Golu.
    send: { from: search, to: sort, label: "2 items" }
    set: { sort: "DC-1049, DC-1042" }
```

The page also needs two small helpers. `colourOptions(items)` gives the colour buttons, each colour once and in A to Z order. `priceRange(items)` gives the ends of the price slider.

## What you build

Everything goes in `src/filters.js`. Items look like `{ id: "DC-1042", code: "AMI", name: "Golu the elephant", colour: "Indigo", paise: 45000, stock: 3 }`.

1. `filterCatalogue(items, options)` returns a **new** array, and never changes `items`. Its options are:
   - `colour`: keep items of this colour, ignoring capital letters. Missing or `""` means any colour.
   - `minPaise` and `maxPaise`: keep items whose price is within the range, ends included. Either may be missing. `0` is a real limit.
   - `inStockOnly`: when `true`, keep only items with `stock` above 0.
   - `search`: keep items whose `name` contains this text, ignoring capital letters and spaces around the text. Missing, `""` or only spaces means no search.
   - `sort`: `"price-asc"` for cheapest first, `"price-desc"` for dearest first. Missing means catalogue order. Items with the same price keep their catalogue order.
2. `colourOptions(items)` returns each colour once, sorted A to Z.
3. `priceRange(items)` returns `{ minPaise, maxPaise }`, the cheapest and dearest prices. For an empty list it returns `null`.

### Syntax: options with defaults

```js
const { colour = "", minPaise, maxPaise, inStockOnly = false, search = "", sort } = options;
```

Destructuring unpacks every option in one line. Defaults fill in the ones the page left out. A function signature of `filterCatalogue(items, options = {})` means the options object itself may be missing too.

### Syntax: "was this option given?"

```js
if (minPaise !== undefined) {
  result = result.filter((item) => item.paise >= minPaise);
}
```

Don't write `if (minPaise)`. A minimum of `0` is falsy, so that line would skip a real limit. Compare with `undefined` instead.

### Syntax: case-insensitive matching

```js
const wanted = search.trim().toLowerCase();
const matches = (item) => item.name.toLowerCase().includes(wanted);
```

Lower-case both sides before comparing, so "GOLU", "golu" and "Golu" all match. `trim()` removes spaces typed by accident around the text.

```callout tip
Filter first and sort last. Sorting a short list is less work, and sorting the filtered copy can never reorder the catalogue itself.
```

## Recap

- One function, many optional filters: unpack the options with defaults, then narrow a copy of the list one step at a time.
- Check for a missing option with `!== undefined`, so `0` still counts as a limit.
- Lower-case both sides, and trim the search text, for matching that ignores capitals.
- Sort last, on the copy. Never change the catalogue the page gave you.
- `[...new Set(colours)].sort()` gives each colour once, A to Z.

## Common mistakes

**Testing an option with if (minPaise).** A slider set to ₹0 is ignored, and so is a `maxPaise` of 0. Use `minPaise !== undefined`.

**Sorting the list the page passed in.** `items.sort(...)` reorders the catalogue for every other part of the page. Sort the filtered copy.

**Forgetting to trim the search.** A customer who types "golu " with a space finds nothing. Call `trim()` before you match.
