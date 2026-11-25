## What happened

The Diwali sale opened at 18:00. Stock moves fast, so the product grid in `src/grid.js` asks for fresh stock every few seconds and calls `grid.update(products)` with the new numbers.

Each card has a "Name to stitch on (₹50)" box. By 18:30, Arjun had a pile of messages. Customers type a name, the stock refreshes, and the box is empty again, with the cursor gone. Then a second complaint: a few customers pressed Add to cart once and found two Golus in their cart.

The stock numbers are right. The prices are right. Nothing throws an error. And still the grid is unusable.

```callout analogy
Picture a shop assistant who, every time a price tag changes, sweeps every item off the shelf and puts out fresh ones. The prices are always right. But anything a customer was holding, or had written on a gift tag, is gone. And each sweep, the assistant also hires one more cashier to ring up the same sale.
```

## What you need

### What a refresh must keep

A refresh brings new numbers. It must not take away what the customer has done:

```animate
title: A stock refresh that keeps the customer's work
kind: scene
nodes:
  - { id: server, label: Stock refresh, emoji: "🔄", value: "Golu: 3 left", row: 0, col: 0 }
  - { id: grid, label: "grid.update", emoji: "⚙️", row: 0, col: 2 }
  - { id: card, label: "Golu's card", emoji: "🐘", value: "Only 4 left", row: 0, col: 4 }
  - { id: input, label: "Name box", emoji: "✍️", value: "Aarav, focused", row: 1, col: 4 }
steps:
  - say: The customer is typing Aarav into Golu's name box. The box has focus.
    focus: [input]
  - say: A refresh arrives with new stock for every product.
    send: { from: server, to: grid, label: "update(products)" }
  - say: The grid looks for the card that already shows DC-1042. It finds it, and keeps that very element.
    send: { from: grid, to: card, label: "same card" }
  - say: Only the parts that come from the data change. The stock text now says Only 3 left.
    set: { card: "Only 3 left" }
  - say: The name box is never touched. It still says Aarav, and the cursor is still in it.
    focus: [input]
```

Three rules come out of it:
1. **Keep the elements of products that are still there.** Find them by id, and change only their stock, price and button.
2. **Make, remove and move only what the data asks for**, and leave a card that is already in its place alone.
3. **Add listeners once**, not on every update.

### Syntax: making and filling, apart

A card needs two separate steps, because they run at different times:

```js
function createCard(product) {
  const li = document.createElement("li");
  li.dataset.id = product.id;
  return li;
}

function fillCard(li, product) {
  li.querySelector(".card__stock").textContent = stockText(product.stock);
}
```

- **Making** builds the shape and the empty boxes. It runs once per product, when it first appears.
- **Filling** sets everything that comes from data. It runs on every update, for new and kept cards alike, and never touches what the customer typed.

```callout jargon
A **refresh** (or poll) asks for fresh data again and again on a timer. Every refresh must update the page without undoing what the user is doing.
```

## The tiers

1. **Bronze** resolves the incident. After a refresh, a name typed into a card's box is still there, in every card.
2. **Silver:** a refresh keeps the card element of every product that stays, and changes its stock text and button. New products appear at their place, products that are gone disappear, and the cards follow the data's order. The box being typed in keeps its focus.
3. **Gold:** one press of Add to cart calls `onAdd` exactly once, with the trimmed stitched name, however many refreshes came before. A product that leaves and comes back starts with an empty box.

Open the preview, type a name, and wait for the next refresh.

## Recap

- Elements hold things your data doesn't: typed text, focus, the cursor. Throwing an element away throws them away too.
- Update a list by key: keep, fill, make, remove and move, never a full redraw when people are working in it.
- Keep **making** a card and **filling** it apart. Filling runs on every update and never touches the customer's input.
- Add a delegated listener once, outside any function that runs again and again.

## Common mistakes

**Fixing the text but rebuilding the input.** Writing the stock with `innerHTML` on the whole card rebuilds the name box too, so the typed name still disappears. Change only the stock, price and button elements.

**Copying the typed text across.** Saving each box's value before `replaceChildren` and writing it back afterwards brings the text back, but not the focus or the cursor, so the customer still loses their place. Keep the elements instead.

**Moving every card on every update.** Calling `append` for every card, in order, also "works", but moving a focused input can drop its focus. Move a card only when it isn't already where it should be.

**Fixing the listener by removing it.** Taking the listener out of `update` and forgetting to add it anywhere makes Add to cart do nothing at all. Add it once, in `createGrid`.
