## The design

The catalogue page of dhaaga.in should answer customers' questions by itself, and never sell more than the shop has. This is the agreed layout:

```text
Dhaaga & Co.                                  Cart (2)
Search [ golu               ]
[All] [Marigold] [Indigo] [Rani pink] [Mint] [Off-white]
Showing 1 of 6 items
┌────────────────────────┐
│ Golu the elephant      │
│ ₹450.00                │
│ Indigo                 │
│ Only 2 left            │
│ [Add]  (greyed out)    │
└────────────────────────┘
```

The six products are in `products.ts`, already finished. Each has a typed stock:

| id | name | price | colour | stock |
|---|---|---|---|---|
| DC-1042 | Golu the elephant | ₹450 | Indigo | 2 in stock |
| DC-1043 | Marigold coaster set | ₹150 | Marigold | 12 in stock |
| DC-1044 | Mint keychain | ₹120 | Mint | sold out |
| DC-1045 | Indigo tote bag | ₹899 | Indigo | 5 in stock |
| DC-1046 | Rani pink baby booties | ₹350 | Rani pink | 1 in stock |
| DC-1047 | Off-white coaster set | ₹150 | Off-white | 8 in stock |

```tsx
export type Stock = { kind: "in-stock"; count: number } | { kind: "sold-out" };
```

The rules:

- **Search** matches any part of the name. Capital letters and spaces at the ends don't matter, so `  GOLU ` finds Golu the elephant.
- **Colour** shows only that colour. **All** shows every colour. Search and colour work together.
- **The cart** remembers how many of each product were added. The header shows the total. Searching or filtering never resets it.
- **Stock** limits the cart. A sold-out product can't be added, and you can't add more of a product than its stock count.

Here is one tap, from the button to the new page:

```animate
title: Adding the last Golu
kind: scene
nodes:
  - { id: card, label: "Golu's card", emoji: "🐘", value: "Only 2 left" }
  - { id: app, label: "CatalogueApp state", emoji: "🧠", value: "cart: { DC-1042: 1 }" }
  - { id: header, label: "Header", emoji: "🛒", value: "Cart (1)" }
  - { id: button, label: "Golu's Add", emoji: "➕", value: "enabled" }
steps:
  - say: One Golu is already in the cart, and two are in stock. The customer taps Add again.
    focus: [card]
  - say: The card can't change the cart itself. It calls onAdd with Golu's id.
    send: { from: card, to: app, label: "onAdd(\"DC-1042\")" }
  - say: CatalogueApp makes a new cart object with Golu's count raised to 2.
    set: { app: "cart: { DC-1042: 2 }" }
  - say: The header adds up every count in the cart. It now shows Cart (2).
    send: { from: app, to: header, label: "count={2}" }
    set: { header: "Cart (2)" }
  - say: The card gets inCart={2}. With 2 in the cart and 2 in stock, Add is disabled.
    send: { from: app, to: button, label: "inCart={2}" }
    set: { button: "disabled" }
```

## What you build

### ProductCard (in ProductCard.tsx)

`stockText(stock: Stock): string` returns:

- `In stock` for more than 3 in stock
- `Only 2 left` for 3 or fewer in stock
- `Sold out` for a sold-out product

`ProductCard({ product, inCart, onAdd })` returns an `<article className="product-card">` with:

- the name in an `<h3>`
- `rupees(product.paise)` in `<p className="price">`
- the colour in `<p className="colour">`
- `stockText(product.stock)` in `<p className="stock">`
- a button with the text `Add`, labelled ``aria-label={`Add ${product.name} to cart`}``, that calls `onAdd(product.id)`. It is disabled when the product is sold out, or when `inCart` has reached the stock count.

### CatalogueApp (in CatalogueApp.tsx)

`CatalogueApp({ products })` keeps three pieces of typed state: the search text (`""`), the chosen colour (`Colour | "All"`, starting at `"All"`) and the cart (`Record<string, number>`, starting empty). It shows, in this order:

1. A `<header>` with `<h1>Dhaaga & Co.</h1>` and `<p className="cart-count">Cart (2)</p>`, with the real total.
2. `<label htmlFor="search">Search</label>` and a controlled `<input id="search" type="search">`.
3. A `<div className="colour-filter">` with one button for `All`, then one per colour in `COLOURS`, in that order. The chosen one has `aria-pressed` true. Tapping a button chooses it.
4. `<p className="showing">Showing 1 of 6 items</p>`, with the real numbers.
5. A `<section className="grid">` with one `ProductCard` per visible product, keyed by its id, each with its own `inCart` count. When nothing is visible, show `<p className="empty">No items match your search.</p>` instead of the section.

### Syntax: counting with a Record

```tsx
const [cart, setCart] = useState<Record<string, number>>({});

setCart({ ...cart, [id]: (cart[id] ?? 0) + 1 });
const count = Object.values(cart).reduce((sum, n) => sum + n, 0);
```

A product that was never added has no key, so `cart[id]` is really `undefined`, whatever its type says. `?? 0` turns that into zero.

```callout tip
Build it in small steps with the preview open: the card first, then the cart count, then search, then the colour buttons, then the stock limit.
```

## Recap

- Typed props and a `Stock` union let `ProductCard` show the right text, and refuse to add what isn't there.
- `CatalogueApp` owns the state, and passes `inCart` down and `onAdd` up.
- The visible list and the cart count are derived on every render, never stored.
- The list is keyed by id, and the search box is a controlled input.

## Common mistakes

**1. Keeping the visible list in state.** Every handler would have to remember to update it, and one would forget. Work it out from the search text and colour on every render.

**2. Keeping the cart count inside each card.** The header can't see it, and a card that is filtered out forgets its count. Keep the cart in `CatalogueApp` and pass `inCart` down.

**3. Reading cart[id] without a default.** `cart[id] + 1` is `NaN` for a product that was never added, even though the type says `number`. Write `(cart[id] ?? 0) + 1`.
