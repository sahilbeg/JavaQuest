## Hook

A doorbell doesn't know who lives in the house. It only rings. Whoever is home decides what to do: open the door, shout "coming!", or ignore it.

A good component works like a doorbell. The quantity picker shouldn't know about the cart. It should only ring: "someone wants 2 of DC-1042". The page that holds it decides what that means.

## Concept

### Don't reach into other components

Here is how the picker was written:

```js
addButton.addEventListener("click", () => {
  window.cart.add(this.getAttribute("item-id"), this.qty);
});
```

It works on the catalogue page. On the gift guide, which has no cart, it throws `TypeError: Cannot read properties of undefined (reading 'add')`. The picker depends on something it doesn't own, so it can't be reused.

```callout jargon
Two pieces of code are **coupled** when one needs the other's insides to work. Coupled components break together and can't be reused apart.
```

### Syntax: CustomEvent

```js
new CustomEvent("add-to-cart", { detail: { id: "DC-1042", qty: 2 }, bubbles: true, composed: true });
```

- The first argument is the event's name. Use lower case with dashes, and avoid built-in names like `click` or `change`.
- `detail` carries any data you like: an object is usual.
- `bubbles` and `composed` decide how far the event travels. They come up in a moment.

```callout jargon
A **CustomEvent** is an event you make and name yourself, carrying your own data in `detail`. Listeners hear it exactly like a click.
```

### Syntax: dispatchEvent

```js
this.dispatchEvent(new CustomEvent("add-to-cart", { detail: { id, qty }, bubbles: true }));
```

- `dispatchEvent` sends the event, starting at the element you call it on. In a component, that's usually `this`.
- Every listener for that name runs **straight away**, before `dispatchEvent` returns.
- The picker doesn't know or care who listens. Nobody listening is fine too.

```animate
title: A picker rings, the page answers
kind: code
language: js
code: |
  const picker = new EventTarget();
  picker.addEventListener("add-to-cart", (event) => {
    console.log(`cart gets ${event.detail.qty} × ${event.detail.id}`);
  });
  const event = new CustomEvent("add-to-cart", { detail: { id: "DC-1042", qty: 2 } });
  picker.dispatchEvent(event);
  console.log("picker is done");
steps:
  - line: 1
    say: Node has EventTarget, the part of every element that handles events. It stands in for the picker.
  - line: 2
    say: The page adds a listener for add-to-cart. Nothing runs yet; it only waits.
  - line: 5
    vars: { event: "add-to-cart" }
    say: The customer presses Add. The picker makes a CustomEvent, with the item and quantity in detail.
  - line: 6
    say: dispatchEvent sends it. Every listener for add-to-cart runs now, before dispatchEvent returns.
  - line: 3
    out: "cart gets 2 × DC-1042"
    say: The listener reads event.detail. The page, not the picker, decides what adding means.
  - line: 7
    out: "picker is done"
    say: Only then does the picker carry on. It never knew who was listening.
```

### Bubbles and composed

An event starts where it is dispatched. These two options decide how far up it goes:

| Option | Without it | With it |
|---|---|---|
| `bubbles: true` | only listeners on the picker itself hear it | it bubbles up through every ancestor, to `document` |
| `composed: true` | it stops at the edge of the shadow root it is in | it crosses shadow boundaries, out into the page |

A picker on its own needs `bubbles`. But a picker often sits **inside another component's** shadow root, like a product card's. Then it also needs `composed`, or the event never leaves the card. Setting both is the safe habit for events meant for the page.

```callout jargon
**Composed** means an event can leave a shadow root and carry on into the page outside it. Built-in events like `click` are composed; your custom events are not unless you say so.
```

### Retargeting: the page sees the host

When an event crosses out of a shadow root, the browser changes `event.target` for listeners outside. They see the **host** element, not the button deep inside it.

```animate
title: An event leaves two shadow roots
kind: scene
nodes:
  - { id: button, label: "Add button (picker's shadow)", emoji: "🔘", row: 2, col: 1 }
  - { id: picker, label: "<qty-picker>", emoji: "🔢", row: 1, col: 1 }
  - { id: card, label: "<product-card>", emoji: "🃏", row: 0, col: 1 }
  - { id: page, label: "document listener", emoji: "📄", value: waiting, row: 0, col: 4 }
steps:
  - say: The customer presses Add, inside the picker's shadow root. The picker dispatches add-to-cart from itself.
    focus: [button, picker]
  - say: The picker sits inside the product card's shadow root. With composed true, the event may leave it.
    send: { from: picker, to: card, label: "add-to-cart" }
  - say: It bubbles on, up to document, because bubbles is true too.
    send: { from: card, to: page, label: "add-to-cart" }
  - say: The page's listener runs. Its event.target is the product card, the outermost host it can see. The insides stay private.
    set: { page: "target: product-card" }
  - say: "event.detail still holds { id, qty }, so the page knows exactly what to add."
    focus: [page]
```

### Listening from the parent

The page adds **one** listener on a container, like event delegation in the DOM module:

```js
catalogue.addEventListener("add-to-cart", (event) => {
  cart.add(event.detail.id, event.detail.qty);
});
```

Every picker inside the catalogue, now or added later, reaches this one listener. A picker outside the catalogue, on the gift guide, doesn't. Each page wires its own meaning.

### Data down, events up

Components talk in two directions, and each has one channel:
- **Down:** a parent tells a child things with attributes and properties, like `max="4"` for the stock left.
- **Up:** a child tells its parent things with events, like `add-to-cart`.

A child never calls its parent's functions, and a parent never pokes inside a child's shadow root. That keeps every piece reusable.

## Worked example

The custom-order page has a yarn colour picker. It tells the page which colour was chosen, and the page shows it.

```js
class YarnPicker extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" }).innerHTML = `
      <button type="button" data-colour="Marigold">Marigold</button>
      <button type="button" data-colour="Indigo">Indigo</button>
      <button type="button" data-colour="Mint">Mint</button>`;
    this.shadowRoot.addEventListener("click", (event) => {
      const button = event.target.closest("button[data-colour]");
      if (button === null) {
        return;
      }
      this.dispatchEvent(new CustomEvent("colour-pick", {
        detail: { colour: button.dataset.colour },
        bubbles: true,
        composed: true,
      }));
    });
  }
}

customElements.define("yarn-picker", YarnPicker);

document.querySelector("#order").addEventListener("colour-pick", (event) => {
  document.querySelector("#chosen").textContent = `Colour: ${event.detail.colour}`;
});
```

1. One listener inside the shadow root handles all three buttons, with `closest`.
2. The picker dispatches `colour-pick` from itself, with the colour in `detail`.
3. It bubbles and is composed, so it reaches the form around it even through other components.
4. The page's listener reads `event.detail.colour`. The picker has no idea what `#chosen` is.

## Try it

Node has `EventTarget` and `CustomEvent` too. Run this, then add a third listener that prints `stock check for DC-1042`.

```js run
const picker = new EventTarget();

picker.addEventListener("add-to-cart", (event) => {
  console.log(`cart: +${event.detail.qty} ${event.detail.id}`);
});
picker.addEventListener("add-to-cart", (event) => {
  console.log(`toast: Added ${event.detail.qty} to your cart`);
});

picker.dispatchEvent(new CustomEvent("add-to-cart", { detail: { id: "DC-1042", qty: 2 } }));
picker.dispatchEvent(new CustomEvent("remove-from-cart", { detail: { id: "DC-1042" } }));
```

It prints the cart line, then the toast line. Listeners run in the order they were added. The `remove-from-cart` event has no listeners, so nothing happens, and nothing breaks.

## Recap

- A component announces what happened with a **CustomEvent**; it never reaches into other components.
- `new CustomEvent(name, { detail, bubbles: true, composed: true })`, sent with `this.dispatchEvent(...)`. Listeners run straight away.
- `bubbles` lets ancestors hear it. `composed` lets it leave shadow roots. Outside, `event.target` is **retargeted** to the host.
- The parent listens once, on a container, and reads `event.detail`.
- **Data down, events up**: attributes and properties go down; events come up.

## Common mistakes

**1. Forgetting bubbles.**

```js
this.dispatchEvent(new CustomEvent("add-to-cart", { detail: { id, qty } }));
```

The catalogue's listener never runs. Without `bubbles: true`, only listeners on the picker itself hear the event. Add `bubbles: true` (and `composed: true`).

**2. Putting the data on the event instead of in detail.**

```js
const event = new CustomEvent("add-to-cart", { id, qty, bubbles: true });
```

The listener reads `event.detail` and gets `null`. `CustomEvent` ignores options it doesn't know. Put your data in `detail: { id, qty }`.

**3. Reading the button from event.target in the page.**

```js
catalogue.addEventListener("add-to-cart", (event) => {
  const id = event.target.dataset.id;
});
```

`event.target` is the outermost host the page can see, maybe a product card without a `data-id`, so `id` is `undefined`. Send everything the page needs in `detail`.

**4. Naming a custom event like a built-in one.**

```js
this.dispatchEvent(new CustomEvent("change", { detail: { qty }, bubbles: true }));
```

Every `change` listener on the page, like the one on the delivery form, now runs for your picker too, and finds no `detail` it expects. Pick a name of your own: `qty-change`.
