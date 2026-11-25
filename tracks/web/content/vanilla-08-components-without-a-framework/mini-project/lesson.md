## The design

Sana's drawing has two pieces that any page can use:

```text
Header      Dhaaga & Co.                           [ Cart (3) ]   <- <cart-count>

Catalogue   +-----------------------+  +-----------------------+
            | Golu the elephant     |  | Marigold coaster set  |   <- <product-card>
            | ₹450.00               |  | ₹150.00               |
            | Only 4 left           |  | In stock              |
            | [ Add to cart ]       |  | [ Add to cart ]       |
            +-----------------------+  +-----------------------+
```

- A **product card** shows the name, the price in ₹, a stock line (`Sold out`, `Only 4 left` or `In stock`) and an Add to cart button, disabled when sold out. Its look lives in its own shadow root, so no page's CSS can bend it.
- A **cart count** says how many pieces are in the cart, like `Cart (3)`, and follows every change.
- The **catalogue** is a list of product cards drawn from data. When stock changes, the same cards update; they are never thrown away.

One store holds `{ products, lines }`. The pieces never call each other. They talk through the store and through one event:

```animate
title: One tap on Add to cart
kind: scene
nodes:
  - { id: card, label: "<product-card> Golu", emoji: "🐘", row: 0, col: 0 }
  - { id: catalogue, label: "catalogue (root)", emoji: "📋", row: 0, col: 2 }
  - { id: store, label: "store", emoji: "🧠", value: "lines: []", row: 1, col: 2 }
  - { id: count, label: "<cart-count>", emoji: "🛒", value: "Cart (0)", row: 0, col: 4 }
steps:
  - say: "The customer taps Add inside Golu's shadow root. The card dispatches add-to-cart, with detail { id }."
    focus: [card]
  - say: The event bubbles and is composed, so it leaves the card and reaches the one listener on the catalogue.
    send: { from: card, to: catalogue, label: "add-to-cart" }
  - say: The listener asks the store for a change, using addLine to make a new array of lines.
    send: { from: catalogue, to: store, label: "setState(addLine)" }
    set: { store: "lines: Golu × 1" }
  - say: The store tells every subscriber. The cart count draws Cart (1).
    send: { from: store, to: count, label: "new state" }
    set: { count: "Cart (1)" }
  - say: The catalogue is a subscriber too. The products didn't change, so every card is kept as it was.
    send: { from: store, to: catalogue, label: "new state" }
```

## What you build

A product is `{ id, name, paise, stock }`, a cart line is `{ id, qty }`, and the store's state is `{ products, lines }`. `src/store.js` (the store from this module) and `src/money.js` (`rupees`) are ready to use.

**`src/product-card.js`: `<product-card>`**
1. An open shadow root, attached in the constructor, holding `CARD_HTML`: a `<style>` with a `:host` rule, and `.name`, `.price`, `.stock` and an `.add` button.
2. A `product` property. Setting it stores the product and draws it: the name, `rupees(paise)`, `stockText(stock)` and the button, disabled at 0. Reading it gives the product back.
3. `stockText(stock)`: `Sold out` for 0, `Only 4 left` for 1 to 5, `In stock` above 5.
4. Pressing Add dispatches `add-to-cart` from the card, with `detail: { id }`, `bubbles: true` and `composed: true`.

**`src/cart-count.js`: `<cart-count>`**
5. `countItems(lines)`: the sum of every `qty`.
6. It gets the store as a `store` property before it joins the page. It shows `Cart (3)` straight away, follows every change, and unsubscribes when it leaves the page.

**`src/catalogue.js`**
7. `addLine(lines, id)` returns a **new** array: one more on that line, or a new line `{ id, qty: 1 }` at the end. The old array and its lines never change.
8. `createCatalogue(root, store)` draws one `<product-card>` per product in `store.getState().products`, in order, each with `data-id` and its `product` set. It follows the store: when the products change, it keeps the card of every id that stays (only setting its new `product`), makes cards for new ids, removes the others, and keeps the order.
9. It adds **one** `add-to-cart` listener on `root`, which adds the item to `lines` through `store.setState`.

### How the pieces fit

Every idea in this module has a job here:

| Idea | Where it's used |
|---|---|
| Custom elements | `<product-card>` and `<cart-count>` |
| Shadow DOM | the card's look can't be bent by page CSS |
| CustomEvent | the card announces `add-to-cart`; it never touches the cart |
| A small store | one `{ products, lines }` for every piece |
| Keyed updates | stock changes keep the same cards |
| Cleanup | `<cart-count>` unsubscribes when removed |

```callout tip
Build in the order of the list: the card first, then the count, then the catalogue. Each part has its own checks, so you can see one part pass before you start the next.
```

## Recap

- Small pieces with one job each: a card that shows a product, a count that shows the cart, and a catalogue that wires them to the store.
- Data goes **down** as properties (`card.product`, `count.store`); news comes **up** as events (`add-to-cart`).
- One store holds the state. Changes go through `setState`, and every subscriber redraws.
- The catalogue updates by key, so cards survive a stock change, and its listener is added once.

## Common mistakes

**Calling the store from inside the card.** A card that imports the store and adds to the cart itself works on one page only. Dispatch `add-to-cart` and let the catalogue decide.

**Drawing in connectedCallback only.** If the card draws only when it joins the page, a later `card.product = ...` with new stock changes nothing on screen. Draw in the `product` setter.

**Rebuilding every card on every store change.** Adding to the cart changes the state too, so a catalogue that redraws everything on each change throws away every card each time anything is added. Reuse the card of each id.

**Changing the lines array in place.** `line.qty += 1` changes the old state, so anything holding the old lines sees them change under its feet. Return a new array from `addLine`, with a new object for the changed line.

**Adding the add-to-cart listener inside the sync function.** It runs on every store change, so one tap soon adds three Golus. Add it once, in `createCatalogue`.
