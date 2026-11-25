## Hook

At the stall, Raju once wrote every price tag by hand, letter by letter. Then Meera Aunty bought a set of rubber stamps: one for the logo, one for "₹", one for each digit. Now a tag takes ten seconds, and every tag looks the same.

Building elements with `createElement` is writing by hand. In this ticket you make a stamp: one small function that turns a short description into a real element. Then you build the product card from it.

## Concept

### The problem: you can't see the shape

In the DOM module you drew cards with `createElement`. It works, but look at the code for one small card:

```js
function productCard(product) {
  const card = document.createElement("li");
  card.className = "product";
  card.dataset.id = product.id;
  const name = document.createElement("h3");
  name.textContent = product.name;
  const price = document.createElement("p");
  price.className = "price";
  price.textContent = rupees(product.paise);
  const add = document.createElement("button");
  add.className = "add";
  add.type = "button";
  add.textContent = "Add to cart";
  add.addEventListener("click", () => onAdd(product.id));
  card.append(name, price, add);
  return card;
}
```

Fifteen lines, and the card's shape (an `<li>` holding a heading, a price and a button) is hidden in the last line. Add a badge or a sold-out state, and it gets worse.

### Syntax: h

```js
h(tag, props, ...children)
```

- `tag` is the element's name, like `"li"` or `"button"`.
- `props` is an object of settings for the element, or `null` for none.
- `children` are everything that goes inside it: text, other elements, or arrays of them.

It returns one new element, not yet on the page. The same card becomes:

```js
const card = h("li", { class: "product", dataset: { id: "DC-1042" } },
  h("h3", null, "Golu the elephant"),
  h("p", { class: "price" }, "₹450.00"),
);
```

Now the code has the same shape as the HTML it makes.

```callout jargon
**Props** (short for properties) are the settings you hand to an element or a component, as one object. The name comes from React, which uses the same idea.
```

### What props can hold

`h` reads each prop by its name:

| Prop | What `h` does | Example |
|---|---|---|
| `class` | sets `className` | `{ class: "product" }` |
| `dataset` | copies each key into a `data-` attribute | `{ dataset: { id: "DC-1042" } }` |
| `onClick`, `onInput`, ... | adds a listener for `click`, `input`, ... | `{ onClick: () => add(id) }` |
| anything else | `setAttribute(name, value)` | `{ type: "button", "aria-label": "Add Golu" }` |

Two special values make optional props easy:
- `true` sets an empty attribute, so `{ disabled: true }` gives a disabled button.
- `false`, `null` and `undefined` set nothing at all, so `{ disabled: soldOut }` works whichever way `soldOut` is.

### Children: text stays text

Each child is added in order:
- An element is appended as it is.
- Anything else, like a string or a number, becomes a **text node**. So `"<b>Golu</b>"` shows those exact characters. It never turns into bold HTML.
- Arrays are flattened, so you can pass `products.map(...)` straight in.
- `null`, `undefined` and `false` are skipped. That makes "show this only if" easy.

```js
h("p", { class: "stock" },
  h("strong", null, "Stock: "),
  product.stock,
  product.stock <= 5 && " (only a few left)",
);
```

When the stock is above 5, the last child is `false`, and nothing is added.

```callout jargon
A **text node** is a piece of plain text inside an element. The browser never reads it as HTML, which is why `textContent` and text nodes are safe for customer data.
```

### Components are just functions

A **component** is a reusable piece of the page. Without a framework, the simplest component is a function: data in, element out.

```js
function productCard(product, onAdd) {
  return h("li", { class: "product", dataset: { id: product.id } },
    h("h3", null, product.name),
    h("p", { class: "price" }, rupees(product.paise)),
    h("button", { class: "add", type: "button", onClick: () => onAdd(product.id) }, "Add to cart"),
  );
}
```

```callout jargon
A **component** is a reusable piece of the interface, like a product card or a cart button. It takes data and gives back the elements that show it.
```

### Components inside components

Bigger components call smaller ones. The list doesn't know how a card is built. It only calls `productCard`:

```js
function productList(products, onAdd) {
  return h("ul", { class: "products" },
    products.map((product) => productCard(product, onAdd)),
  );
}
```

Watch one call build the whole tree:

```animate
title: productList builds a tree of elements
kind: scene
nodes:
  - { id: data, label: "products (2)", emoji: "📦", row: 0, col: 0 }
  - { id: list, label: productList, emoji: "📋", row: 0, col: 2 }
  - { id: card, label: productCard, emoji: "🃏", row: 1, col: 2 }
  - { id: h, label: "h()", emoji: "🔨", row: 1, col: 4 }
  - { id: page, label: "ul.products", emoji: "🖼️", value: empty, row: 0, col: 4 }
steps:
  - say: The page hands two products to productList. It doesn't build any card itself.
    send: { from: data, to: list, label: "[Golu, keychain]" }
  - say: products.map calls productCard once for Golu.
    send: { from: list, to: card, label: "productCard(Golu)" }
  - say: productCard describes the card with h. Each h call makes one element and puts its children inside.
    send: { from: card, to: h, label: "h('li', ...)" }
  - say: The finished li comes back up. A second call does the same for the keychain.
    send: { from: h, to: card, label: "li + li", back: true }
  - say: productList passes the array of cards to one more h call. h flattens the array into the ul.
    send: { from: card, to: list, label: "[li, li]", back: true }
  - say: One detached ul holding two cards. Putting it on the page is one replaceChildren.
    send: { from: list, to: page, label: "replaceChildren(ul)" }
    set: { page: "2 cards" }
```

## Worked example

The stall tablet lists yarn colours. Each row shows the grams left, and a warning under 100 g.

```js
function yarnRow(entry) {
  const low = entry.grams < 100;
  return h("li", { class: low ? "yarn low" : "yarn", dataset: { colour: entry.colour } },
    h("span", { class: "swatch", "aria-hidden": "true" }),
    `${entry.colour}: ${entry.grams} g`,
    low && h("strong", null, " Order more"),
  );
}

function yarnList(entries) {
  return h("ul", { class: "yarn-list" }, entries.map(yarnRow));
}

document.querySelector("#yarn").replaceChildren(
  yarnList([{ colour: "Mint", grams: 35 }, { colour: "Marigold", grams: 420 }]),
);
```

1. `yarnRow` is a component: one entry in, one `<li>` out.
2. The class is picked with a ternary. The `data-colour` comes from `dataset`.
3. The swatch has `aria-hidden`, a normal attribute, so `h` uses `setAttribute`.
4. The text is a template string, added as a text node.
5. `low && ...` gives `false` for Marigold, so no warning is added.
6. `yarnList` passes the mapped array as one child. `h` flattens it.

## Try it

Node has no `document`, so this `h` builds plain objects instead of elements, and `show` prints them as HTML. The idea is the same. Run it, then add a third child: a `p` with class `stock` saying `Only 4 left`.

```js run
function h(tag, props, ...children) {
  const kept = children.flat().filter((child) => child !== null && child !== undefined && child !== false);
  return { tag, props: props ?? {}, children: kept };
}

function show(node) {
  if (typeof node !== "object") {
    return String(node);
  }
  const cls = node.props.class ? ` class="${node.props.class}"` : "";
  return `<${node.tag}${cls}>${node.children.map(show).join("")}</${node.tag}>`;
}

const soldOut = false;
const card = h("li", { class: "product" },
  h("h3", null, "Golu the elephant"),
  soldOut && h("p", { class: "badge" }, "Sold out"),
  h("p", { class: "price" }, "₹450.00"),
);
console.log(show(card));
```

It prints `<li class="product"><h3>Golu the elephant</h3><p class="price">₹450.00</p></li>`. The `false` from `soldOut && ...` was skipped.

## Recap

- `h(tag, props, ...children)` builds one element from a short description, so the code has the shape of the page.
- **Props**: `class`, `dataset`, `onX` listeners, and plain attributes. `true` sets an empty attribute; `false`, `null` and `undefined` set nothing.
- **Children**: elements go in as they are, everything else as a text node. Arrays are flattened, and `null`, `undefined` and `false` are skipped.
- A **component** is a function: data in, element out. Bigger components call smaller ones.

## Common mistakes

**1. Building children from an HTML string.**

```js
h("h3", null, `<em>${product.name}</em>`);
```

The heading shows `<em>Golu the elephant</em>`, angle brackets and all, because text children are text. Pass an element instead: `h("h3", null, h("em", null, product.name))`.

**2. Using && with a number.**

```js
h("p", null, product.stock && `${product.stock} left`);
```

With a stock of 0, the card says `0`. `0 && ...` gives `0`, and `0` is not skipped like `false`. Compare first: `product.stock > 0 && ...`.

**3. Calling the handler instead of passing it.**

```js
h("button", { onClick: onAdd(product.id) }, "Add to cart");
```

`onAdd` runs once while the card is built, and the button does nothing when pressed. Pass a function: `onClick: () => onAdd(product.id)`.

**4. Writing className as a prop.**

```js
h("li", { className: "product" });
```

The card's styles never apply. `h` only knows the prop `class`, so it set an attribute called `classname`, which the browser ignores. Write `{ class: "product" }`.

**5. Expecting h to put the element on the page.**

```js
productList(products, onAdd);
```

Nothing appears. `h` and your components only build detached elements. Put the result on the page yourself: `catalogue.replaceChildren(productList(products, onAdd))`.
