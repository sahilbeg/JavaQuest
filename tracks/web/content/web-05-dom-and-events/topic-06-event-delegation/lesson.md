## Hook

Picture a school with forty classrooms. You could post a guard at every classroom door to take messages. Or you could have one receptionist at the front gate, because every message passes the gate on its way out.

Browser events travel like those messages. A click on a button doesn't stop at the button. It travels up through every element that holds it. So one listener, placed on the list, can hear every button inside it.

## Concept

### The problem with a listener per button

Here is how the catalogue wired its buttons:

```js
for (const button of document.querySelectorAll(".add")) {
  button.addEventListener("click", () => {
    addToCart(button.closest(".product").dataset.id);
  });
}
```

It works on the first load. Then three things go wrong:
1. Cards added later, like the Diwali specials, never got a listener. Their buttons are dead.
2. If the list is redrawn and the loop runs again, old buttons can end up with two listeners, so one tap adds two items.
3. Forty cards means forty listeners doing the same job.

### Events bubble

```callout jargon
**Bubbling** is how an event travels: it starts at the element you clicked, then visits its parent, its grandparent, and so on up to `document`. A listener on any of those elements hears it.
```

```animate
title: A click bubbles up to the list
kind: scene
nodes:
  - { id: icon, label: "span.icon 🛒", emoji: "👆", row: 3, col: 2 }
  - { id: button, label: "button.add", emoji: "🔘", row: 2, col: 2 }
  - { id: card, label: "li.product DC-1013", emoji: "🔑", row: 1, col: 2 }
  - { id: list, label: "ul#products", emoji: "📋", value: "1 listener", row: 0, col: 2 }
  - { id: handler, label: Your handler, emoji: "⚙️", row: 0, col: 4 }
links:
  - { from: icon, to: button }
  - { from: button, to: card }
  - { from: card, to: list }
steps:
  - say: A customer taps the little cart icon inside the keychain's Add button. The click starts at the span.
    focus: [icon]
  - say: The span has no listener. The event bubbles up to its parent, the button. No listener there either.
    send: { from: icon, to: button, label: click }
  - say: Up again, to the card. Still no listener.
    send: { from: button, to: card, label: click }
  - say: Up to the list. It has a click listener, so the browser runs it.
    send: { from: card, to: list, label: click }
    focus: [list]
  - say: "The handler gets the event. event.target is still the span, where it started; event.currentTarget is the list."
    send: { from: list, to: handler, label: "handler(event)" }
  - say: The handler walks up from the span to the nearest .add button, then to its card, and reads DC-1013.
    send: { from: handler, to: card, label: "closest('.product')" }
    focus: [card]
```

### Syntax: closest

```js
const button = event.target.closest(".add");
```

- `closest(selector)` checks the element itself, then its parent, then each ancestor in turn.
- It returns the first one that matches the selector, or `null` if none does.
- It is `querySelector` in reverse: it searches **up** the tree, not down.

That fixes the icon problem. Wherever inside the button the tap lands, `closest(".add")` finds the button.

### Syntax: event delegation

```js
list.addEventListener("click", (event) => {
  const button = event.target.closest(".add");
  if (button === null) {
    return;
  }
  const id = button.closest(".product").dataset.id;
  addToCart(id);
});
```

```callout jargon
**Event delegation** means putting one listener on a parent element, and letting it handle events for all the children inside, by checking where each event came from.
```

- One listener on the parent, added once.
- `closest` finds the button the click belongs to. A click on a heading or a gap gives `null`, so the handler returns early.
- Then `closest` again finds the card, and `dataset.id` says which item it is.

Cards added later work at once. They sit inside the same list, so their clicks bubble to the same listener.

### target and currentTarget

In a delegated handler, the two are different, and that's the point.

| Property | In the handler above |
|---|---|
| `event.target` | the element the tap landed on: the span, the button or a heading |
| `event.currentTarget` | always the list, where the listener sits |

So never use `event.target` as "the button". It is often something inside it. Always go through `closest`.

### Telling buttons apart with data-action

A cart line has three buttons: −, + and Remove. Give each a `data-action`, and keep the work in an object of functions:

```html
<li class="line" data-id="DC-1042">
  <button type="button" data-action="dec" aria-label="One less Golu the elephant">−</button>
  <button type="button" data-action="inc" aria-label="One more Golu the elephant">+</button>
  <button type="button" data-action="remove" aria-label="Remove Golu the elephant">Remove</button>
</li>
```

```js
const actions = {
  inc: (id) => changeQty(id, 1),
  dec: (id) => changeQty(id, -1),
  remove: (id) => removeLine(id),
};

lines.addEventListener("click", (event) => {
  const button = event.target.closest("button[data-action]");
  if (button === null) {
    return;
  }
  const run = actions[button.dataset.action];
  if (run !== undefined) {
    run(button.closest("[data-id]").dataset.id);
  }
});
```

The listener never needs to change. A new kind of button needs only a new `data-action` and a new entry in `actions`.

### Stopping the bubble

`event.stopPropagation()` stops an event from bubbling any higher. It sounds useful, but it hides clicks from every delegated listener above, including ones a teammate adds next month. Avoid it unless you are sure nothing above needs the event.

## Worked example

The stall tablet lists yarn colours. Each row has "Order more" and "Used 40 g" buttons. One listener handles both, for every row.

```html
<ul id="yarn">
  <li class="colour" data-name="Mint">
    Mint: <span class="grams">300</span> g
    <button type="button" data-action="order">Order more</button>
    <button type="button" data-action="use">Used 40 g</button>
  </li>
</ul>
```

```js
const yarn = document.querySelector("#yarn");

const actions = {
  order: (row) => row.classList.add("ordered"),
  use: (row) => {
    const grams = row.querySelector(".grams");
    grams.textContent = String(Number(grams.textContent) - 40);
  },
};

yarn.addEventListener("click", (event) => {
  const button = event.target.closest("button[data-action]");
  if (button === null) {
    return;
  }
  const run = actions[button.dataset.action];
  if (run !== undefined) {
    run(button.closest(".colour"));
  }
});
```

1. The listener sits on the `<ul>`, once, however many colours there are.
2. A click on the word "Mint" finds no button, so `closest` gives `null` and the handler stops.
3. A click on "Used 40 g" finds the button, reads `data-action="use"` and runs `actions.use`.
4. That function gets the row, found with `closest(".colour")`, and takes 40 g off its count.

## Try it

The lookup part of delegation is plain JavaScript. Run it, then add an action called `gift` that prints `gift wrap DC-1042`.

```js run
const actions = {
  inc: (id) => console.log(`one more ${id}`),
  remove: (id) => console.log(`remove ${id}`),
};

function handle(action, id) {
  const run = actions[action];
  if (run === undefined) {
    console.log(`ignored ${action}`);
    return;
  }
  run(id);
}

handle("inc", "DC-1042");
handle("remove", "DC-1013");
handle("gift", "DC-1042");
```

## Recap

- Events **bubble**: from the element clicked, up through every ancestor, to `document`.
- **Event delegation**: one listener on a parent handles clicks for every child, including children added later.
- `event.target.closest(selector)` finds the button a click belongs to, or `null` for a click elsewhere.
- `data-action` names what a button does. An object of functions maps each action to its work.
- `event.target` is where the click landed. `event.currentTarget` is where the listener sits.

## Common mistakes

**1. Using event.target as the button.**

```js
list.addEventListener("click", (event) => {
  const id = event.target.closest(".product").dataset.id;
  if (event.target.classList.contains("add")) {
    addToCart(id);
  }
});
```

A tap on the icon inside the button is ignored, because the target is the span, not the button. Use `event.target.closest(".add")` and check it against `null`.

**2. Forgetting the null check.**

```js
const button = event.target.closest(".add");
addToCart(button.closest(".product").dataset.id);
```

A click on a card's heading gives `TypeError: Cannot read properties of null (reading 'closest')`. Return early when `button` is `null`.

**3. Adding listeners inside a function that redraws.**

```js
function drawCatalogue() {
  list.replaceChildren(...cards);
  list.addEventListener("click", (event) => onListClick(event));
}
```

The list itself stays, and each redraw makes a new arrow function, so every redraw stacks another listener on it. After three redraws, one tap adds three items. Add the delegated listener once, outside the redraw.

**4. Stopping the event on the way up.**

```js
button.addEventListener("click", (event) => event.stopPropagation());
```

The list's delegated listener never hears this button again. Remove `stopPropagation` unless nothing above needs the click.
