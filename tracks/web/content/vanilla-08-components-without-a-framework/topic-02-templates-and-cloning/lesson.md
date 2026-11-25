## Hook

At the stall, the order slips come from a printed pad. Each slip already has the boxes for name, item and price. Raju tears one off, fills it in, and hands it over. He never draws the boxes himself, and the pad itself never gets written on.

The `<template>` element is that pad. The card's markup sits in the HTML, where Sana can read and change it. Your code tears off a copy, fills it, and puts it on the page.

## Concept

### Markup that lives in the page

`h` from the last ticket keeps the card's shape in JavaScript. That suits developers. A designer, though, wants to see plain HTML, and to change a label without touching code.

So the shape moves into the page:

```html
<template id="product-card">
  <li class="product">
    <img class="photo" alt="">
    <h3 class="name"></h3>
    <p class="price"></p>
    <button class="add" type="button">Add to cart</button>
  </li>
</template>
```

### Syntax: the template element

```html
<template id="name"> ...markup... </template>
```

- What's inside is parsed as HTML, but it is **inert**: it isn't shown, its images don't load, and its scripts don't run.
- `querySelector` on the page doesn't find things inside it. They aren't part of the page yet.
- In JavaScript, `template.content` holds the markup, ready to copy.

```callout jargon
**Inert** means present but asleep. Inert markup is parsed and kept, but the browser doesn't show it, load it or run it.
```

### Syntax: cloning the content

```js
const copy = template.content.cloneNode(true);
```

- `template.content` is a **DocumentFragment**: a small box of nodes that isn't on the page.
- `cloneNode(true)` copies it **deeply**: the box and everything inside, at every level. `cloneNode()` without `true` copies only the empty box.
- The template stays untouched, so you can clone it again for every card.

```callout jargon
A **DocumentFragment** is a lightweight container for nodes that isn't part of the page. When you append it somewhere, its children move there and the fragment is left empty.
```

### Fill the copy, not the template

The copy is a normal tree of elements. Find the parts inside it and fill them in:

```js
const card = template.content.cloneNode(true).firstElementChild;
card.dataset.id = product.id;
card.querySelector(".name").textContent = product.name;
card.querySelector(".price").textContent = rupees(product.paise);
```

- `firstElementChild` takes the `<li>` out of the fragment. Now you hold the card itself, which is easier to return and test.
- Search with `card.querySelector`, not `document.querySelector`. The card isn't on the page yet.
- Text goes in with `textContent`, as always, so a product name can never turn into HTML.

### Fragments empty themselves

If you append a whole fragment, its children move out of it:

```animate
title: From template to page
kind: scene
nodes:
  - { id: template, label: "<template>", emoji: "📜", value: "the card's markup", row: 0, col: 0 }
  - { id: copy, label: DocumentFragment, emoji: "📄", value: empty, row: 0, col: 2 }
  - { id: list, label: "ul#catalogue", emoji: "📋", value: "0 cards", row: 0, col: 4 }
  - { id: product, label: "Golu's data", emoji: "🐘", row: 1, col: 2 }
steps:
  - say: The template holds the card's markup. It is inert, so nothing shows yet.
    focus: [template]
  - say: cloneNode(true) makes a deep copy of the content. The template itself is not changed.
    send: { from: template, to: copy, label: "cloneNode(true)" }
    set: { copy: "an li, blank" }
  - say: The code fills the copy from the data. The name and price go in with textContent.
    send: { from: product, to: copy, label: "fill" }
    set: { copy: "an li for Golu" }
  - say: Appending the fragment moves its children onto the page. The fragment is now empty.
    send: { from: copy, to: list, label: "append" }
    set: { copy: empty, list: "1 card" }
  - say: The next card starts with a fresh clone. The template is ready for as many copies as you need.
    send: { from: template, to: copy, label: "cloneNode(true)" }
    set: { copy: "an li, blank" }
```

That is why taking `firstElementChild` is handy. You keep a real element in your hand, not a box that empties itself.

### Template or h?

Both build the same cards. Pick by who changes the markup:

| Use a template when | Use h when |
|---|---|
| the markup is big and mostly fixed | the shape changes a lot with the data |
| designers edit it as HTML | developers own it |
| it should be readable in the page | it must work without a page, like in a test |

Both rules stay the same: build detached, fill with `textContent`, then put it on the page.

## Worked example

The Aundh shop's screen lists today's pickups. The row markup lives in the page:

```html
<template id="pickup-row">
  <li class="pickup">
    <span class="order"></span>
    <span class="customer"></span>
    <span class="time"></span>
  </li>
</template>
<ul id="pickups"></ul>
```

```js
const rowTemplate = document.querySelector("#pickup-row");

function pickupRow(pickup) {
  const row = rowTemplate.content.cloneNode(true).firstElementChild;
  row.dataset.order = pickup.orderId;
  row.querySelector(".order").textContent = pickup.orderId;
  row.querySelector(".customer").textContent = pickup.customer;
  row.querySelector(".time").textContent = pickup.time;
  return row;
}

document.querySelector("#pickups").replaceChildren(
  ...[{ orderId: "order-17", customer: "Mrs. Joshi", time: "11:30" }].map(pickupRow),
);
```

1. The template is found once, outside the function.
2. Every call clones it deeply, so each row is a fresh copy.
3. `firstElementChild` gives the `<li>`. The parts are found inside the row, not the page.
4. All three values go in as text. `replaceChildren` puts every row on the page at once.

## Try it

`cloneNode(true)` is a deep copy, and `cloneNode()` a shallow one. Plain objects have the same difference. Run this, then push a tag into `deep.tags` and print `blank.tags.length` again.

```js run
const blank = { name: "", tags: [] };
const shallow = { ...blank };
const deep = structuredClone(blank);

shallow.tags.push("Diwali");
console.log(blank.tags.length);
console.log(deep.tags.length);
```

It prints `1`, then `0`. The shallow copy shares its inner array with `blank`, so the push changed both. The deep copy has its own.

## Recap

- A `<template>` keeps markup in the page, **inert**: parsed but not shown, loaded or run.
- `template.content.cloneNode(true)` makes a deep copy, a **DocumentFragment**. The template stays as it was.
- Take `firstElementChild` to hold the card itself, then fill it with `querySelector` and `textContent`.
- Appending a fragment moves its children out and leaves it empty.
- Use a template when the markup is mostly fixed or edited as HTML, and `h` when the shape follows the data.

## Common mistakes

**1. Cloning without true.**

```js
const card = template.content.cloneNode().firstElementChild;
```

`card` is `null`, so the next line throws `TypeError: Cannot read properties of null (reading 'querySelector')`. `cloneNode()` copies only the empty fragment. Write `cloneNode(true)`.

**2. Filling the template instead of the copy.**

```js
template.content.querySelector(".name").textContent = product.name;
const card = template.content.cloneNode(true).firstElementChild;
```

It looks right for one card. But the template now holds Golu's name, and a card that forgets to set its name shows Golu's. Always clone first, then fill the copy.

**3. Searching the page for a card that isn't there yet.**

```js
const card = template.content.cloneNode(true).firstElementChild;
document.querySelector(".name").textContent = product.name;
```

This finds the name of some other card already on the page, or `null`. The new card is still detached. Search inside it: `card.querySelector(".name")`.

**4. Appending the template element itself.**

```js
list.append(template);
```

Nothing shows. A `<template>` stays inert wherever it is, and now it has left its place in the page. Append a filled clone instead.
