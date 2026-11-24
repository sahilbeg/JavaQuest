## Hook

At the stall, the price board used to be chalk. Every Saturday Raju rubbed it out and rewrote it from the stock book. When the stock book changed, the board followed, because the board was only ever a copy of the book.

The catalogue page has the opposite problem. Its cards are typed into the HTML by hand, so `catalogue.json` and the page drift apart. This ticket makes the page a copy of the data: change the data, draw again, and the page follows.

## Concept

### Data first, page second

Keep the facts in plain JavaScript data, like the catalogue array from the last module:

```js
const catalogue = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000, colour: "Indigo", stock: 4 },
  { id: "DC-1001", name: "Marigold coaster set", paise: 15000, colour: "Marigold", stock: 12 },
  { id: "DC-1007", name: "Indigo tote bag", paise: 89900, colour: "Indigo", stock: 0 },
];
```

The page is a **drawing** of that data. You never edit the cards by hand. You change the data, then draw the page from it again.

```callout jargon
To **render** means to draw the page, or a part of it, from data. A **render function** takes data and builds the elements that show it.
```

### Syntax: one element from one object

```js
function productCard(product) {
  const card = document.createElement("li");
  card.className = "product";
  card.dataset.id = product.id;

  const name = document.createElement("h3");
  name.textContent = product.name;

  card.append(name);
  return card;
}
```

- It takes one object and returns one new, detached element.
- It uses only what is in the object, so the same product always draws the same card.
- All text goes in with `textContent`, so a name with tags stays plain text.

### Syntax: a whole list from an array

```js
list.replaceChildren(...products.map(productCard));
```

Read it from the inside out:
1. `products.map(productCard)` calls `productCard` for every product and gives an array of cards.
2. `...` spreads that array into separate arguments, because `replaceChildren` wants elements, not one array.
3. `replaceChildren` removes every old child and puts the new cards in, in one step.

```animate
title: The colour filter redraws the list
kind: scene
nodes:
  - { id: data, label: catalogue data, emoji: "📦", value: "5 products", row: 0, col: 0 }
  - { id: filter, label: Colour filter, emoji: "🎨", value: All, row: 1, col: 0 }
  - { id: render, label: renderCatalogue, emoji: "⚙️", row: 0, col: 2 }
  - { id: list, label: "ul#products", emoji: "📋", value: empty, row: 0, col: 4 }
steps:
  - say: The page loads. The list is empty, and all the facts live in the data.
    focus: [data, list]
  - say: main.js hands all five products to the render function.
    send: { from: data, to: render, label: "5 products" }
  - say: It maps each product to a card, then puts all five in the list at once with replaceChildren.
    send: { from: render, to: list, label: "replaceChildren(...5 cards)" }
    set: { list: "5 cards" }
  - say: A customer picks Indigo. Nothing is changed by hand. The data is filtered instead.
    focus: [filter]
    set: { filter: Indigo }
  - say: The filtered array, two products, goes to the same render function.
    send: { from: filter, to: render, label: "2 products" }
  - say: replaceChildren throws out all five old cards and puts in the two new ones. No leftovers, no doubles.
    send: { from: render, to: list, label: "replaceChildren(...2 cards)" }
    set: { list: "2 cards" }
```

### Redraw everything

Each time something changes, draw the whole list again from the data. It sounds wasteful, but it keeps things simple:
- The page always matches the data, because it is built from nothing but the data.
- There is no code that adds one card here and removes another there, so there is nothing to get out of step.

For tens or hundreds of items, redrawing is fast enough. React, from module 8, does the same idea more cleverly: you describe the page from data, and it changes only what differs.

### Empty states

When the filter matches nothing, an empty list looks broken. Show a friendly line instead:

```js
function renderCatalogue(list, products) {
  if (products.length === 0) {
    const empty = document.createElement("li");
    empty.className = "empty";
    empty.textContent = "No items match.";
    list.replaceChildren(empty);
    return;
  }
  list.replaceChildren(...products.map(productCard));
}
```

The early `return` skips the normal drawing. `replaceChildren(empty)` still clears any old cards first.

```callout jargon
An **empty state** is what a list shows when it has nothing in it: a short message, so the customer knows the page works and just has no results.
```

### Draw in one place, listen in another

A render function runs many times. A listener should be added once. Keep them apart:

```js
listenForAdds(list, addToCart);          // once, at the start: delegation on the list
renderCatalogue(list, catalogue);        // as often as you like
```

Delegation, from two tickets ago, is what makes this work. The list element itself stays when its children are redrawn, so its one listener keeps hearing every new card's button.

## Worked example

The stall tablet shows yarn stock from data, with the lowest stock first.

```js
const yarn = [
  { colour: "Marigold", grams: 420 },
  { colour: "Mint", grams: 35 },
  { colour: "Indigo", grams: 610 },
];

function yarnRow(entry) {
  const row = document.createElement("li");
  row.className = "colour";
  row.dataset.colour = entry.colour;
  row.classList.toggle("low", entry.grams < 100);
  row.textContent = `${entry.colour}: ${entry.grams} g`;
  return row;
}

function renderYarn(list, entries) {
  const sorted = [...entries].sort((a, b) => a.grams - b.grams);
  list.replaceChildren(...sorted.map(yarnRow));
}

renderYarn(document.querySelector("#yarn"), yarn);
```

1. `yarnRow` turns one entry into one `<li>`, marking it `low` under 100 g.
2. `renderYarn` copies the array before sorting, so the data itself keeps its order.
3. `map` makes the rows, the spread passes them on, and `replaceChildren` swaps out whatever was there.
4. After 40 g of Mint is used, the code changes the data and calls `renderYarn` again. The list follows.

## Try it

The data side of rendering is plain JavaScript. Run this, then make `countText` say "No items" for an empty list.

```js run
const catalogue = [
  { name: "Golu the elephant", colour: "Indigo" },
  { name: "Marigold coaster set", colour: "Marigold" },
  { name: "Indigo tote bag", colour: "Indigo" },
];

function countText(count) {
  return count === 1 ? "1 item" : `${count} items`;
}

const indigo = catalogue.filter((product) => product.colour === "Indigo");
console.log(countText(indigo.length));
console.log(indigo.map((product) => product.name).join(", "));
```

It prints `2 items`, then the two Indigo names.

## Recap

- Keep the facts in data. The page is a drawing of the data, made by a **render** function.
- A card function turns one object into one detached element, with all text through `textContent`.
- `list.replaceChildren(...items.map(card))` swaps the whole list for fresh elements, so a redraw never doubles it.
- When there is nothing to show, draw an **empty state** message.
- Render as often as you like. Add the delegated listener once, outside the render function.

## Common mistakes

**1. Appending instead of replacing.**

```js
function renderCatalogue(list, products) {
  list.append(...products.map(productCard));
}
```

The first draw looks right. After one filter change, the old cards are still there, with the new ones below. Use `replaceChildren`.

**2. Forgetting the spread.**

```js
list.replaceChildren(products.map(productCard));
```

The page shows the text `[object HTMLLIElement],[object HTMLLIElement]`. `replaceChildren` got one array, not elements, so it turned the array into a string. Write `replaceChildren(...products.map(productCard))`.

**3. Adding the listener inside the render function.**

```js
function renderCatalogue(list, products) {
  list.replaceChildren(...products.map(productCard));
  list.addEventListener("click", (event) => onClick(event));
}
```

Every redraw adds one more listener to the list, so after three filter changes one tap adds three items. Add it once, outside.

**4. Building the cards as an HTML string.**

```js
list.innerHTML = products.map((p) => `<li>${p.name}</li>`).join("");
```

It works until a product name holds a `<` or a tag, and then that text turns into HTML. Build elements and use `textContent`.
