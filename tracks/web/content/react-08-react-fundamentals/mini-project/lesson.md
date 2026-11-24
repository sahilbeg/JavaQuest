## The design

The catalogue page of dhaaga.in should answer customers' questions by itself. This is the agreed layout:

```text
Dhaaga & Co.                                Cart (2)
Search [ coaster            ]
[All] [Marigold] [Indigo] [Rani pink] [Mint] [Off-white]
Showing 2 of 6 items
┌──────────────────────┐ ┌──────────────────────┐
│ Marigold coaster set │ │ Off-white coaster set│
│ ₹150.00              │ │ ₹150.00              │
│ Marigold             │ │ Off-white            │
│ [Add]                │ │ [Add]                │
└──────────────────────┘ └──────────────────────┘
```

The six products are in `products.js`, already finished:

| id | name | price | colour |
|---|---|---|---|
| DC-1042 | Golu the elephant | ₹450 | Indigo |
| DC-1043 | Marigold coaster set | ₹150 | Marigold |
| DC-1044 | Mint keychain | ₹120 | Mint |
| DC-1045 | Indigo tote bag | ₹899 | Indigo |
| DC-1046 | Rani pink baby booties | ₹350 | Rani pink |
| DC-1047 | Off-white coaster set | ₹150 | Off-white |

The rules:

- **Search** matches any part of the name. Capital letters and spaces at the ends don't matter, so `  GOLU ` finds Golu the elephant.
- **Colour** shows only that colour. **All** shows every colour. Search and colour work together.
- **The cart count** goes up by one for every Add. Searching or filtering never resets it.

Here is the whole page working, from one tap to the new list:

```animate
title: One tap on a colour
kind: scene
nodes:
  - { id: chip, label: "Indigo button", emoji: "🔵" }
  - { id: state, label: "CatalogueApp state", emoji: "🧠", value: "colour: All" }
  - { id: filter, label: "filter()", emoji: "🔍" }
  - { id: grid, label: "Product grid", emoji: "🧶", value: "6 cards" }
  - { id: cart, label: "Cart count", emoji: "🛒", value: "Cart (2)" }
steps:
  - say: Two things are already in the cart. The customer taps the Indigo button.
    focus: [chip, cart]
  - say: The button calls the setter, and the chosen colour in state becomes Indigo.
    send: { from: chip, to: state, label: "setColour(\"Indigo\")" }
    set: { state: "colour: Indigo" }
  - say: CatalogueApp re-renders. The visible list is worked out again from the products, the search text and the colour.
    send: { from: state, to: filter, label: "query, colour" }
  - say: Only Golu and the tote bag are Indigo, so two cards are drawn, each keyed by its id.
    send: { from: filter, to: grid, label: "2 products" }
    set: { grid: "2 cards" }
  - say: The cart count is separate state in the same component. Filtering did not touch it.
    focus: [cart]
```

## What you build

### ProductCard (in ProductCard.jsx)

`ProductCard({ product, onAdd })` returns an `<article className="product-card">` with:

- the name in an `<h3>`
- `rupees(product.paise)` in `<p className="price">`
- the colour in `<p className="colour">`, for example `Indigo`
- a button with the text `Add` and the label ``aria-label={`Add ${product.name} to cart`}``, which calls `onAdd(product.id)`

### CatalogueApp (in CatalogueApp.jsx)

`CatalogueApp({ products })` keeps three pieces of state: the search text (`""`), the chosen colour (`"All"`) and the cart count (`0`). It shows, in this order:

1. A `<header>` with `<h1>Dhaaga & Co.</h1>` and `<p className="cart-count">Cart (2)</p>`, with the real count.
2. `<label htmlFor="search">Search</label>` and a controlled `<input id="search" type="search">`.
3. A `<div className="colour-filter">` with one button for `All`, then one per colour in `COLOURS`, in that order. The chosen button has `aria-pressed` true and `className="chip chip--selected"`. The others have `aria-pressed` false and `className="chip"`. Tapping a button chooses it.
4. `<p className="showing">Showing 2 of 6 items</p>`, with the real numbers.
5. A `<section className="grid">` with one `ProductCard` per visible product, keyed by its id. Each card's Add adds one to the cart count. When no product is visible, the section shows `<p className="empty">No items match your search.</p>` instead.

### Syntax: two filters at once

```jsx
const text = query.trim().toLowerCase();
const visible = products.filter(
  (p) => p.name.toLowerCase().includes(text) && (colour === "All" || p.colour === colour),
);
```

`visible` is derived, so it is never stored in state. An empty search, `""`, is included in every name, so it matches everything.

```callout tip
Build it in small steps and keep the preview open. Cards first, then the cart count, then search, then the colour buttons.
```

## Recap

- Small components with props: `ProductCard` only shows one product and reports Add.
- State lives in the one component that needs it all: the search text, the colour and the cart count.
- The visible list is derived from state on every render, and keyed by id.
- Controlled inputs, `aria-pressed` buttons and modifier classes tie it to the design.

## Common mistakes

**1. Keeping the filtered list in state.** If `visible` is state, every handler must remember to update it, and one will forget. Work it out from the search text and colour on every render.

**2. Comparing without lower case on both sides.** `p.name.includes(query.toLowerCase())` never finds `Golu`, because the name has a capital G. Lower-case both sides.

**3. Putting the cart count in ProductCard.** Then the summary at the top cannot see it, and a card that is filtered out forgets its count. Keep the count in `CatalogueApp` and pass `onAdd` down.
