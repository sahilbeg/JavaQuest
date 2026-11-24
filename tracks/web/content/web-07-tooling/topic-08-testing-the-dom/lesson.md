## Hook

A mystery shopper visits a shop pretending to be a customer. They pick up items, read the price tags and try to buy something that should not be for sale. Then they write down exactly what happened.

Tests for a page can do the same. They draw the page, read what is on it, and press its buttons like a customer. If the sold-out booties still have an Add to cart button, a test sees it before any real customer does.

## Concept

Last ticket's tests called a function and checked a number. Page code is different: it changes the DOM, the tree of elements you met in web-05. To test it, a test needs a page to draw on.

```callout jargon
**jsdom** is a pretend browser that runs inside Node. It has `document`, elements, `querySelector` and events, but no screen. Vitest can run tests "in jsdom", so page code works in a test exactly as it does in a browser.
```

jsdom has no screen, so it has no layout: every size and position is 0. You test what is in the page, its text, attributes and state, not how it looks.

Every ticket in this course with a page has been checked this way. Now you see how.

### Syntax: beforeEach

```js
beforeEach(() => {
  // runs before every test in this file
});
```

Each test should start from the same clean page. If one test leaves a card behind, the next test might find it and pass by accident.

```js
import { beforeEach } from "vitest";

beforeEach(() => {
  document.body.innerHTML = `<section id="shelf"></section>`;
});
```

Before every single test, the body is reset to one empty `#shelf`. `afterEach` works the same way, after every test, for tidying up.

```callout jargon
A test's **setup** is the work done before it runs, like resetting the page. Putting shared setup in `beforeEach` keeps each test short and independent.
```

### Finding things on the page

A test reads the page with the same `querySelector` you used in web-05:

```js
const card = document.querySelector("#shelf .product");
const price = card.querySelector(".price").textContent;
const soldOut = card.querySelector("button").disabled;
```

Then it checks what it found with `expect`, like any other value:

| Check | How |
|---|---|
| some text | `expect(el.textContent).toBe("₹450.00")` |
| an attribute | `expect(el.dataset.id).toBe("DC-1042")` |
| a button's state | `expect(button.disabled).toBe(true)` |
| how many | `expect(document.querySelectorAll(".product").length).toBe(1)` |

If `querySelector` finds nothing, it gives `null`, and `null.textContent` crashes the test. Write `?.` to get `undefined` instead. Then the test fails with your friendly message, not a crash:

```js
const name = document.querySelector("#shelf h3")?.textContent;
```

### Simulating a customer

```js
button.click();
```

`click()` fires a real click event on the element, so any listener on it runs. A disabled button ignores it, just like in a browser.

For typing, set the value, then fire the event the page listens for:

```js
input.value = "3";
input.dispatchEvent(new Event("input", { bubbles: true }));
```

`bubbles: true` lets the event travel up the tree, so listeners on parent elements hear it too, as event delegation in web-05 needs.

### Syntax: vi.fn, a mock function

```js
const onAdd = vi.fn();
```

The product card calls `onAdd(product.id)` when Add to cart is pressed. But the test has no real cart. It only wants to know: was `onAdd` called, how often, and with what?

```callout jargon
A **mock function** is a stand-in that does nothing but remember every call. `vi.fn()` makes one. `onAdd.mock.calls` is a list with one entry per call, and each entry is the list of arguments.
```

A mock is simple inside. Here is a hand-made one:

```js run
function fakeFn() {
  const calls = [];
  const fn = (...args) => {
    calls.push(args);
  };
  fn.calls = calls;
  return fn;
}

const onAdd = fakeFn();
onAdd("DC-1042");
onAdd("DC-1042");
console.log(onAdd.calls.length);
console.log(JSON.stringify(onAdd.calls));
```

`vi.fn()` keeps the same list in `onAdd.mock.calls`. So after one click on Golu's card:

```js
expect(onAdd.mock.calls, "one click adds Golu once").toEqual([["DC-1042"]]);
```

### A whole test, step by step

```animate
title: Checking the sold-out card
kind: scene
nodes:
  - { id: test, label: The test, emoji: "🧪" }
  - { id: page, label: jsdom page, emoji: "📄", value: unknown }
  - { id: card, label: renderProduct, emoji: "🧶" }
  - { id: mock, label: "onAdd = vi.fn()", emoji: "📝", value: 0 calls }
steps:
  - say: "beforeEach resets the page to one empty #shelf. No cards are left over from other tests."
    send: { from: test, to: page, label: "body = <section id=shelf>" }
    set: { page: empty shelf }
  - say: The test makes a mock onAdd and draws the sold-out baby booties.
    send: { from: test, to: card, label: "stock: 0, onAdd" }
  - say: "renderProduct builds the card into #shelf, with a disabled button."
    send: { from: card, to: page, label: append article }
    set: { page: "booties card, button disabled" }
  - say: The test reads the page. The stock text says Sold out, and button.disabled is true.
    send: { from: page, to: test, label: querySelector, back: true }
  - say: The test clicks the button like a customer. A disabled button ignores the click.
    send: { from: test, to: page, label: button.click() }
  - say: The mock still has 0 calls. Nothing was added to the cart. The test passes.
    focus: [mock]
```

### Test what the customer sees

Check what a person would notice: the text, the button, whether it works. Do not check how the code built it, like which variable names it used.

Then the code can be rewritten freely. As long as the customer sees the same card, the tests stay green. In react-10 you will use **Testing Library**, which finds elements by what people see, like "the button named Add to cart".

## Worked example

Here are two tests like the ones in this ticket's checks, read line by line.

```js
import { describe, it, expect, beforeEach, vi } from "vitest";
import { renderProduct } from "../src/productCard.js";

const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 2 };

beforeEach(() => {
  document.body.innerHTML = `<section id="shelf"></section>`;
});

describe("renderProduct", () => {
  it("calls onAdd with the item id when Add to cart is clicked", () => {
    const onAdd = vi.fn();
    renderProduct(document.querySelector("#shelf"), golu, onAdd);
    document.querySelector("#shelf button")?.click();
    expect(onAdd.mock.calls, "one click should call onAdd once, with DC-1042").toEqual([["DC-1042"]]);
  });

  it("shows the price in rupees", () => {
    renderProduct(document.querySelector("#shelf"), golu, vi.fn());
    const price = document.querySelector("#shelf .price")?.textContent;
    expect(price, "45000 paise should show as ₹450.00").toBe("₹450.00");
  });
});
```

1. `vi` comes from `"vitest"` with the rest. Globals are off, so everything is imported.
2. `golu` is shared test data: the amigurumi elephant, two left.
3. `beforeEach` gives every test a fresh, empty shelf.
4. The first test arranges a mock, acts by drawing and clicking, and asserts on the mock's calls.
5. The second test reads the `.price` text. `?.` keeps a missing card from crashing the test.

Your fix is in `src/productCard.js`. `stockText(stock)` returns "Sold out", "Only 2 left" or "In stock". `renderProduct` draws the card, disables Add to cart when the stock is 0, and calls `onAdd(product.id)` on a click.

## Try it

Press Run on the hand-made mock above.

1. Call `onAdd("DC-1077")` once more before the prints. What do the two lines show now?
2. Change `fakeFn` so it also counts calls in a number, `fn.count`. Print it.
3. Read `test/productCard.test.js` in this ticket. Find the test that makes sure a product name is shown as text, not as HTML. Why would that matter if text on the page ever came from customers, like a name stitched on a tote bag?

## Recap

- jsdom is a pretend browser inside Node. Tests can draw page code into it and read the result, but there is no layout.
- `beforeEach` resets the page before every test, so tests never depend on each other.
- Tests find elements with `querySelector` and check text, attributes and `disabled` with `expect`. Use `?.` so a missing element fails with your message.
- `element.click()` and `dispatchEvent` act like a customer. `vi.fn()` makes a mock that records every call in `.mock.calls`.
- Test what the customer sees, not how the code is built.

## Common mistakes

**1. Tests that share a page.**

```js
document.body.innerHTML = `<section id="shelf"></section>`;

it("draws Golu", () => { /* ... */ });
it("draws the booties", () => { /* ... */ });
```

What you see: the second test finds two cards, or passes only when it runs after the first. Change the order, and it breaks.

Fix: put the reset inside `beforeEach`, so it runs before every test.

**2. Checking a mock before the click.**

```js
const onAdd = vi.fn();
renderProduct(shelf, golu, onAdd);
expect(onAdd.mock.calls.length).toBe(1);
document.querySelector("#shelf button").click();
```

What you see: `expected 0 to be 1`. The check ran before anything was clicked.

Fix: arrange, act, then assert. Click first, then check `onAdd.mock.calls`.

**3. Crashing on a missing element.**

```js
const name = document.querySelector("#shelf h3").textContent;
```

What you see, when the card is not drawn: `TypeError: Cannot read properties of null (reading 'textContent')`. It says nothing about which rule broke.

Fix: write `document.querySelector("#shelf h3")?.textContent`, and give `expect` a message that says what should be there.

**4. Building the card with innerHTML.**

```js
container.innerHTML = `<article><h3>${product.name}</h3></article>`;
```

What you see: a product named `<b>Golu</b>` shows in bold, because the name was read as HTML. A name with a `<script>` or an `<img onerror>` in it could run code on every customer's phone.

Fix: build elements with `document.createElement`, and set text with `textContent`. Text is then always shown as text.
