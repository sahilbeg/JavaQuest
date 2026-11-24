## Hook

When the tote bags sell out at the stall, Meera Aunty doesn't rebuild the shelf. She sticks a "Sold out" label on the basket and turns the price tag over.

The web page works the same way. You found elements in the last ticket. Now you change them: new text, a new attribute, a new class. The element stays where it is, and the screen updates by itself.

## Concept

### Syntax: writing textContent

```js
element.textContent = "new text";
```

- Setting `textContent` replaces everything inside the element with plain text.
- The browser redraws the element at once. There is no "save" step.
- Any tags inside the string show up as visible characters. They never become real elements.

```js
const heading = document.querySelector('[data-id="DC-1042"] h3');
heading.textContent = "Golu the elephant";
```

### innerHTML, and why not for text

`innerHTML` also sets an element's content, but it **parses** the string as HTML first.

```js
heading.innerHTML = "Golu <em>the</em> elephant";   // "the" becomes a real <em> element
heading.textContent = "Golu <em>the</em> elephant"; // shows the angle brackets as text
```

That sounds handy, until the text comes from a person. Suppose a customer types this as the name to stitch on a gift:

```js
const stitch = '<img src="x" onerror="alert(1)">';
heading.innerHTML = stitch;   // runs the customer's code on your page
```

```callout jargon
**XSS** (cross-site scripting) is an attack where someone sneaks their own code into your page, often through text that you insert as HTML. Module react-10 covers it in depth.
```

```callout warning
Use `textContent` for any text, and always for text that a customer, a form or a server sent you. Reach for `innerHTML` only for HTML you wrote yourself.
```

### Syntax: getAttribute and setAttribute

```js
element.setAttribute("name", "value");
element.getAttribute("name");     // the value as a string, or null
element.removeAttribute("name");
```

- An **attribute** is the `name="value"` part written inside a tag in HTML.
- `setAttribute` adds the attribute or changes its value. The value is always a string.
- `getAttribute` gives `null` when the attribute is missing.

A screen reader reads a button's `aria-label` instead of its visible text. Dhaaga's buttons all say "Add to cart", so give each a clearer label:

```js
button.setAttribute("aria-label", "Add Golu the elephant to cart");
```

### Attributes and properties

An element object also has **properties** for the common attributes. They are often easier to use.

```callout jargon
A **property** is a value stored on the element object, read and set with a dot, like `button.disabled`. Many properties mirror an HTML attribute, but they have proper types: `true` and `false` instead of strings.
```

| You want | Property | Attribute way |
|---|---|---|
| switch a button off | `button.disabled = true` | `setAttribute("disabled", "")` |
| hide an element | `element.hidden = true` | `setAttribute("hidden", "")` |
| read an input | `input.value` | (the attribute is only the starting value) |
| an element's id | `element.id` | `getAttribute("id")` |

Some attributes are **boolean**: being there at all means "on". So `setAttribute("disabled", "false")` still disables the button. Use the property, `button.disabled = false`, to turn it on again.

### Syntax: dataset

```js
card.dataset.paise = "45000";   // writes data-paise="45000"
card.dataset.paise;             // reads "45000"
```

- `dataset` is the way into every `data-` attribute of an element.
- The name drops `data-`. A dashed name becomes camelCase: `data-item-id` is `dataset.itemId`.
- Values are always strings. Turn them into numbers with `Number(card.dataset.paise)` before you do maths.

### Syntax: classList

```js
card.classList.add("sold-out");
card.classList.remove("sold-out");
card.classList.contains("sold-out");       // true or false
card.classList.toggle("sold-out", isEmpty); // add if isEmpty is true, remove if false
```

- `classList` changes one class and leaves the others alone. The card keeps its `product` class.
- Class names here have **no dot**. The dot belongs only in selectors.
- With a second argument, `toggle` makes the class match a condition. That is perfect for "on when this is true".

### Classes, not styles

You could set the look from JavaScript: `card.style.opacity = "0.5"`. It works, but now the design lives in two places, and Sana can't change it in her CSS.

The better split: JavaScript says **what state** the card is in, by adding a class. The CSS decides **how that state looks**.

```css
.product.sold-out { opacity: 0.5; }
.product.low .stock { color: #B45309; font-weight: bold; }
```

Watch the two work together:

```animate
title: A class switches on Sana's CSS
kind: scene
nodes:
  - { id: js, label: "showStock(card, 0)", emoji: "⚙️", row: 0, col: 0 }
  - { id: card, label: "article.product", emoji: "👜", value: "product", row: 0, col: 2 }
  - { id: btn, label: "button.add", emoji: "🔘", value: enabled, row: 1, col: 2 }
  - { id: css, label: "Sana's CSS", emoji: "🎨", value: ".sold-out { opacity: 0.5 }", row: 0, col: 4 }
  - { id: screen, label: Screen, emoji: "📱", value: looks buyable, row: 1, col: 4 }
steps:
  - say: The tote bag just sold out. showStock runs with a stock of 0.
    focus: [js]
  - say: The card gets a second class. Its first class, product, stays where it is.
    send: { from: js, to: card, label: "classList.add('sold-out')" }
    set: { card: "product sold-out" }
  - say: The browser checks the CSS again. The .sold-out rule now matches this card.
    focus: [css]
    send: { from: css, to: card, label: ".sold-out matches" }
  - say: The card turns pale on screen. JavaScript changed a class, and the CSS decided the look.
    send: { from: card, to: screen, label: redraw }
    set: { screen: greyed out }
  - say: The button's disabled property turns on, so nobody can tap Add to cart.
    send: { from: js, to: btn, label: "disabled = true" }
    set: { btn: disabled }
  - say: New stock arrives. toggle with false removes the class, and the card looks normal again.
    send: { from: js, to: card, label: "toggle('sold-out', false)" }
    set: { card: product, screen: looks buyable, btn: enabled }
```

## Worked example

The stall tablet shows yarn per colour. A colour is "low" below 100 g, and empty at 0.

```html
<li class="colour" data-name="Mint">
  <span class="grams"></span>
  <button class="order" type="button">Order more</button>
</li>
```

```js
function showYarn(row, grams) {
  row.querySelector(".grams").textContent = `${grams} g`;
  row.dataset.grams = String(grams);
  row.classList.toggle("empty", grams === 0);
  row.classList.toggle("low", grams > 0 && grams < 100);
  const button = row.querySelector(".order");
  button.setAttribute("aria-label", `Order more ${row.dataset.name} yarn`);
  button.hidden = grams >= 100;
}

showYarn(document.querySelector('[data-name="Mint"]'), 35);
```

1. `textContent` writes `35 g` into the `.grams` span.
2. `dataset.grams` stores the number as the string `"35"`, in `data-grams`.
3. `toggle("empty", false)` makes sure `empty` is off. `toggle("low", true)` turns `low` on.
4. `dataset.name` reads `data-name`, so the label says "Order more Mint yarn".
5. The order button shows only when stock is under 100 g.

## Try it

The text part of a stock badge is plain JavaScript, so you can run it here. Change the numbers, then press **Run**.

```js run
function stockText(stock) {
  if (stock === 0) {
    return "Sold out";
  }
  if (stock <= 3) {
    return `Only ${stock} left`;
  }
  return "In stock";
}

console.log(stockText(0));
console.log(stockText(2));
console.log(stockText(12));
```

Then open your ticket's preview and try `document.querySelector(".product").classList.add("sold-out")` in its Console.

## Recap

- `textContent = ...` writes plain text. `innerHTML` parses HTML, so never give it text from a customer.
- `setAttribute` and `getAttribute` work on any attribute, always as strings. Properties like `disabled` and `hidden` take `true` and `false`.
- `dataset` reads and writes `data-` attributes, as strings.
- `classList.add`, `remove`, `contains` and `toggle(name, condition)` change one class and keep the others.
- JavaScript sets the state with a class. CSS decides how that state looks.

## Common mistakes

**1. Overwriting className.**

```js
card.className = "sold-out";
```

The card loses its `product` class, so every `.product` style and every `.product` search stops matching it. Use `card.classList.add("sold-out")`.

**2. A dot in the class name.**

```js
card.classList.add(".sold-out");
```

The card gets a class literally called `.sold-out`, and the `.sold-out` CSS rule never matches. Class names in `classList` have no dot.

**3. Turning a button back on with setAttribute.**

```js
button.setAttribute("disabled", "false");
```

The button stays disabled. `disabled` is a boolean attribute: being there at all means "off". Write `button.disabled = false;`.

**4. Doing maths with a dataset value.**

```js
const stock = card.dataset.stock;   // "4"
console.log(stock + 1);             // "41"
```

`dataset` values are strings, so `+` joins them as text. Convert first: `Number(card.dataset.stock) + 1` gives `5`.

**5. Putting customer text into innerHTML.**

```js
heading.innerHTML = customerName;
```

A name like `<b>Asha</b>` shows in bold, and a nastier one can run code on your page. Use `heading.textContent = customerName;`.
