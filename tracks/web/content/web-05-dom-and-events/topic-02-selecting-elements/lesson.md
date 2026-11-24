## Hook

In a library, you don't read every book to find one. You look for a label: "Cookery, shelf 3". The label narrows the search.

The page is a big tree, and walking it child by child gets slow and fragile. You already know a way to label elements: the CSS selectors from the CSS module, like `#products` and `.price`. JavaScript can search the DOM with those same selectors.

## Concept

### Syntax: querySelector

```js
const element = document.querySelector("selector");
```

- It searches the tree and returns the **first** element that matches the selector.
- It returns `null` when nothing matches.
- The selector is a string, written exactly as in a CSS file.

```js
const list = document.querySelector("#products");   // the element with id="products"
const firstPrice = document.querySelector(".price"); // the first element with class="price"
```

```callout jargon
A **selector** is a short pattern that picks out elements, like `#products` (an id), `.price` (a class) or `li` (a tag). CSS uses selectors to style elements. JavaScript uses the same ones to find them.
```

### Selectors you already know

Any CSS selector works. These are the ones you will use most:

| Selector | Finds |
|---|---|
| `"#cart"` | the element with `id="cart"` |
| `".product"` | elements with `class="product"` |
| `"h3"` | every `<h3>` element |
| `".product h3"` | an `<h3>` anywhere inside a `.product` |
| `".product.sold-out"` | elements with **both** classes |
| `"ul > li"` | an `<li>` that is a direct child of a `<ul>` |

The first character matters. `"#cart"` searches for an id, `".cart"` for a class, and `"cart"` for a `<cart>` tag, which does not exist.

### Syntax: attribute selectors

HTML lets you add your own attributes that start with `data-`. Dhaaga uses them to tag each card with its item id:

```html
<li class="product" data-id="DC-1042">...</li>
```

```js
const golu = document.querySelector('[data-id="DC-1042"]');
```

- Square brackets search by attribute: `[name="value"]`.
- `.product[data-id="DC-1042"]` means "a `.product` whose `data-id` is `DC-1042`". There is no space between the two parts.
- Put the value in quotes. An id like `DC-1042` has a dash, and the quotes keep it safe.

```callout jargon
A **data attribute** is an attribute you invent, named `data-` plus a word, such as `data-id`. Browsers ignore it, so it is a safe place to store facts like an item id on an element.
```

### Syntax: querySelectorAll

```js
const cards = document.querySelectorAll(".product");
```

- It returns **every** match, in page order, as a **NodeList**.
- When nothing matches, the NodeList is empty: `length` is 0. It is never `null`.
- A NodeList has `forEach` and `length`, but no `map` or `filter`. Spread it into an array for those.

```js
const titles = [...document.querySelectorAll(".product h3")].map((h3) => h3.textContent.trim());
```

```callout jargon
A **NodeList** is the list-like object `querySelectorAll` returns. Unlike `children`, it is a snapshot: elements added later do not appear in it.
```

### When nothing is found

`querySelector` and `querySelectorAll` behave differently when the search fails.

| Call | Nothing matches |
|---|---|
| `querySelector(".nope")` | `null` |
| `querySelectorAll(".nope")` | an empty NodeList |

Reading a property of `null` crashes your code. When an element may be missing, check first:

```js
const card = document.querySelector('[data-id="DC-9999"]');
if (card === null) {
  console.log("No such item");
}
```

### Syntax: searching inside an element

```js
const card = document.querySelector('[data-id="DC-1013"]');
const price = card.querySelector(".price");
```

- Every element has `querySelector` and `querySelectorAll` too, not only `document`.
- Called on an element, the search stays **inside** that element.
- This is the safe way to find "the price of this card": search the card, not the whole page.

Watch what goes wrong when the search starts from the whole page:

```animate
title: Search the card, not the page
kind: scene
nodes:
  - { id: js, label: Your code, emoji: "⚙️", row: 0, col: 0 }
  - { id: doc, label: document, emoji: "🌳", row: 0, col: 2 }
  - { id: note, label: "header p.price", emoji: "📝", value: "All prices in rupees", row: 1, col: 1 }
  - { id: card, label: "li DC-1013", emoji: "🔑", row: 1, col: 3 }
  - { id: price, label: "p.price", emoji: "🏷️", value: "₹120.00", row: 2, col: 3 }
links:
  - { from: doc, to: note }
  - { from: doc, to: card }
  - { from: card, to: price }
steps:
  - say: Raju looks up the keychain, DC-1013. The code needs its price.
    focus: [js]
  - say: It asks the whole document for the first element with class price.
    send: { from: js, to: doc, label: "querySelector('.price')" }
  - say: The search walks the page from the top. The note in the header has class price too, and it comes first.
    focus: [note]
    send: { from: note, to: js, label: "All prices in rupees", back: true }
  - say: Wrong element. So first find the keychain's own card by its data-id.
    focus: [card]
    send: { from: js, to: card, label: "[data-id=DC-1013]" }
  - say: Then search for .price inside that card only.
    send: { from: card, to: price, label: "card.querySelector('.price')" }
  - say: The search cannot leave the card, so it finds the keychain's own price.
    focus: [price]
    send: { from: price, to: js, label: "₹120.00", back: true }
```

### Syntax: a selector built from a variable

The item id usually comes from a variable. Build the selector with a template literal:

```js
function findProduct(root, id) {
  return root.querySelector(`.product[data-id="${id}"]`);
}
```

Keep the double quotes around `${id}` inside the backticks. Then `findProduct(document, "DC-1013")` searches for `.product[data-id="DC-1013"]`.

### getElementById, the older way

You will see this in older code:

```js
const list = document.getElementById("products");
```

It finds one element by id. Notice: no `#`, because it only ever searches ids. It works only on `document`, not on other elements. `querySelector` does everything it does and more, so this course uses `querySelector`.

## Worked example

The stall's yarn page lists stock per colour. Each colour has its grams inside it.

```html
<ul id="yarn">
  <li class="colour" data-name="Marigold"><span class="grams">420</span> g</li>
  <li class="colour low" data-name="Mint"><span class="grams">35</span> g</li>
  <li class="colour" data-name="Indigo"><span class="grams">610</span> g</li>
</ul>
```

```js
function gramsOf(root, name) {
  const colour = root.querySelector(`.colour[data-name="${name}"]`);
  if (colour === null) {
    return 0;
  }
  return Number(colour.querySelector(".grams").textContent);
}

function lowColours(root) {
  return [...root.querySelectorAll(".colour.low")].map((li) => li.dataset.name);
}

console.log(gramsOf(document, "Mint"));      // 35
console.log(gramsOf(document, "Rani pink")); // 0
console.log(lowColours(document));           // ["Mint"]
```

1. `gramsOf` builds the selector `.colour[data-name="Mint"]` and finds Mint's `<li>`.
2. It returns 0 when the colour is missing, instead of crashing on `null`.
3. It searches for `.grams` inside that `<li>` only, so it never reads another colour's grams.
4. `lowColours` finds elements with both classes, spreads the NodeList and maps each to its name. `dataset.name` reads `data-name`; the next ticket explains it.

## Try it

Open your ticket's preview and its Console. Try these searches:

```js
document.querySelector(".price").textContent;
document.querySelectorAll(".product").length;
document.querySelector('.product[data-id="DC-1013"] .price').textContent;
document.querySelector(".nothing-here");
```

The first answer is not a product's price. Why? Look at the page's header.

## Recap

- `querySelector(selector)` returns the first match or `null`. `querySelectorAll` returns every match as a NodeList, which may be empty.
- Selectors are CSS selectors: `#id`, `.class`, `tag`, `.a .b` for "inside", `.a.b` for "both".
- `[data-id="DC-1042"]` finds an element by an attribute. Build it from a variable with a template literal.
- Call `querySelector` on an element to search only inside it.
- Spread a NodeList into an array before using `map`.

## Common mistakes

**1. Forgetting the # or the dot.**

```js
const list = document.querySelector("products");
```

This searches for a `<products>` tag, finds none, and returns `null`. Write `"#products"` for an id and `".product"` for a class.

**2. Expecting querySelector to find them all.**

```js
const cards = document.querySelector(".product");
console.log(cards.length);   // undefined
```

`querySelector` returns one element, the first, and an element has no `length`. Use `querySelectorAll` for every match.

**3. Searching the whole page for a part of one card.**

```js
const card = document.querySelector('[data-id="DC-1013"]');
const price = document.querySelector(".price");
```

`price` is the first `.price` on the page, whatever card you found. Search inside the card: `card.querySelector(".price")`.

**4. Using map on a NodeList.**

```js
document.querySelectorAll("h3").map((h3) => h3.textContent);
```

The console shows `TypeError: document.querySelectorAll(...).map is not a function`. Spread it first: `[...document.querySelectorAll("h3")].map(...)`.

**5. Reading from null.**

```js
const price = document.querySelector('[data-id="DC-9999"]').textContent;
```

There is no DC-9999, so `querySelector` returns `null`. The console shows `TypeError: Cannot read properties of null (reading 'textContent')`. Store the result, and check it against `null` before you use it.
