## Hook

At a cricket match, the scorer keeps the scorebook. The big scoreboard only shows what the book says. Now imagine three people each walking up to the board and adding runs by hand, without the book. Before lunch, the board is wrong, and nobody knows by how much.

Golu's item page works like that board. The + button patches the quantity, the gift wrap box patches the total, and the stitch box patches it again. Each patch is fine alone. Together, in the wrong order, the total is off. This ticket brings in a scorebook.

## Concept

### The page as memory: the bug

Here is how the item page was written. Each handler reads numbers back from the page and changes them:

```js
plus.addEventListener("click", () => {
  const qty = Number(qtyText.textContent) + 1;
  qtyText.textContent = String(qty);
  total.textContent = rupees(item.paise * qty);
});

wrap.addEventListener("change", () => {
  const now = parsePaise(total.textContent);
  total.textContent = rupees(wrap.checked ? now + 3000 : now - 3000);
});
```

Tick gift wrap, and the total gains ₹30. Press +, and the + handler rewrites the total without the wrap. Untick it, and ₹30 comes off a total that never had it. Every handler knows only its own piece, and the page is the only memory they share.

### One state object

```callout jargon
A **single source of truth** is one place that holds a fact. Everything else, like what the page shows, is worked out from it and never stored twice.
```

Put every choice the customer makes into one object, the **state**:

```js
let state = { qty: 1, giftWrap: false, stitch: "" };
```

The page never holds the truth again. It only shows the state.

### Syntax: render from state

```js
function render() {
  qtyText.textContent = String(state.qty);
  minus.disabled = state.qty <= 1;
  wrap.checked = state.giftWrap;
  total.textContent = rupees(itemTotal(item, state));
}
```

- `render` reads **only** `state` (and fixed things like `item`). It never reads numbers back from the page.
- It sets **every** part of the widget each time, so nothing can be left over from before.
- Run it as often as you like. The same state always draws the same page.

### Syntax: update, then render

```js
function update(changes) {
  state = { ...state, ...changes };
  render();
}
```

- `{ ...state, ...changes }` makes a **new** object: all the old fields, with the changed ones on top.
- Then `render()` draws the page from the new state.
- Every change goes through `update`, so the page can never forget to follow.

Watch the loop run. This version prints instead of drawing, so it runs in Node:

```animate
title: Change the state, then draw
kind: code
language: js
code: |
  let state = { qty: 1, giftWrap: false };
  function render() {
    const total = 45000 * state.qty + (state.giftWrap ? 3000 : 0);
    console.log(`${state.qty} × Golu: ${total} paise`);
  }
  function update(changes) {
    state = { ...state, ...changes };
    render();
  }
  render();
  update({ giftWrap: true });
  update({ qty: state.qty + 1 });
steps:
  - line: 1
    objects: { s1: { type: State, fields: { qty: 1, giftWrap: "false" } } }
    vars: { state: "@s1" }
    say: One object holds every choice. This is the state.
  - line: 10
    say: The page starts by drawing the state once.
  - line: 3
    vars: { total: 45000 }
    say: render works the total out from the state. One Golu, no wrap, 45000 paise.
  - line: 4
    out: "1 × Golu: 45000 paise"
    vars: { total: ~ }
    say: Then it shows it. In the browser this line would set textContent.
  - line: 11
    say: The customer ticks gift wrap. The handler only describes the change.
  - line: 7
    objects: { s2: { type: State, fields: { qty: 1, giftWrap: "true" } } }
    vars: { state: "@s2", changes: "{ giftWrap: true }" }
    say: update builds a new state, with the old fields and the change on top.
  - line: 8
    say: Then it calls render. Nothing patches the page directly.
  - line: 4
    out: "1 × Golu: 48000 paise"
    vars: { changes: ~ }
    say: The total is worked out again from scratch, so the ₹30 is counted exactly once.
  - line: 12
    say: The customer presses +. The new quantity is worked out from the state, not read from the page.
  - line: 7
    objects: { s3: { type: State, fields: { qty: 2, giftWrap: "true" } } }
    vars: { state: "@s3" }
    say: A new state again, with qty 2. giftWrap comes along from the old one.
  - line: 4
    out: "2 × Golu: 93000 paise"
    say: Two Golus plus one gift wrap. The order of the clicks no longer matters.
```

### Derived values are worked out, not stored

The total depends on the quantity, the wrap and the stitching. So don't keep a `total` field in the state. Work it out in `render`, every time.

```callout jargon
A **derived value** is one you can work out from the state, like a total from a quantity and a price. Store the state; derive the rest.
```

If you stored the total too, every handler would have to remember to update it. That is the old bug again, in a new place.

### Events only change state

The rule for handlers is short: work out the change, call `update`, and stop.

```js
plus.addEventListener("click", () => {
  if (state.qty < max) {
    update({ qty: state.qty + 1 });
  }
});

wrap.addEventListener("change", () => update({ giftWrap: wrap.checked }));
```

A handler never sets `textContent` or `disabled` itself. That is `render`'s job, and only `render`'s.

### Boxes the customer types in

There is one exception to "render sets everything". Don't write `.value` back into a text box while the customer is typing in it: the cursor would jump. Let the box lead. Its `input` handler copies the text into the state, and `render` draws everything **else** from it.

### Why this matters

This pattern, **state, then render**, is the heart of every modern frontend. In module 8, React does the `render` part for you: you describe the page from the state, and it updates only what changed. You are building the habit now, by hand.

## Worked example

Offices build Diwali hampers of coasters and keychains. Delivery in Pune is ₹60, free above ₹999.

```js
let state = { coasters: 0, keychains: 0 };

function subtotal(s) {
  return s.coasters * 15000 + s.keychains * 12000;
}

function render() {
  const sub = subtotal(state);
  document.querySelector("#coasters").textContent = String(state.coasters);
  document.querySelector("#keychains").textContent = String(state.keychains);
  document.querySelector("#subtotal").textContent = rupees(sub);
  document.querySelector("#delivery").textContent = sub > 99900 ? "Free delivery" : "Delivery ₹60.00";
}

function update(changes) {
  state = { ...state, ...changes };
  render();
}

document.querySelector("#hamper").addEventListener("click", (event) => {
  const button = event.target.closest("button[data-add]");
  if (button === null) {
    return;
  }
  const field = button.dataset.add;
  update({ [field]: state[field] + 1 });
});

render();
```

1. The state holds two counts. The subtotal and the delivery text are derived in `render`.
2. One delegated listener handles both Add buttons. `data-add="coasters"` names the field to change.
3. `{ [field]: ... }` uses the field's name from a variable as the key.
4. Every click goes through `update`, so the counts, the subtotal and the delivery line always agree.

## Try it

The state loop is plain JavaScript. Run it, then add a third `update` that takes one coaster set away.

```js run
let state = { coasters: 0, keychains: 0 };

function subtotal(s) {
  return s.coasters * 15000 + s.keychains * 12000;
}

function render() {
  const sub = subtotal(state);
  const delivery = sub > 99900 ? 0 : 6000;
  console.log(`subtotal ${sub}, delivery ${delivery}`);
}

function update(changes) {
  state = { ...state, ...changes };
  render();
}

update({ coasters: 2 });
update({ keychains: state.keychains + 6 });
```

It prints `subtotal 30000, delivery 6000`, then `subtotal 102000, delivery 0`: the hamper passed ₹999, so delivery became free.

## Recap

- Keep every choice in one **state** object, the single source of truth. The page only shows it.
- `render()` draws the whole widget from the state, and reads nothing back from the page.
- `update(changes)` makes a new state with `{ ...state, ...changes }`, then calls `render()`.
- Work out **derived values**, like the total, inside `render`. Never store them in the state.
- Handlers only call `update`. Text boxes lead: their `input` handler copies the text into the state.

## Common mistakes

**1. Reading the state back from the page.**

```js
const qty = Number(qtyText.textContent) + 1;
```

The page becomes the memory again, and any other code that changes the text changes your quantity. Use `state.qty + 1`.

**2. Storing a derived value.**

```js
update({ qty: state.qty + 1, total: state.total + item.paise });
```

The gift wrap handler forgets to change `total`, so it drifts from the truth. Leave `total` out of the state and work it out in `render`.

**3. Changing the state but not drawing.**

```js
plus.addEventListener("click", () => {
  state.qty += 1;
});
```

The state says 2, but the page still shows 1, because `render` never ran. Go through `update`, which always renders.

**4. Changing the old state object in place.**

```js
function update(changes) {
  Object.assign(state, changes);
  render();
}
```

It seems to work, but anything that kept the old state now sees it change under its feet. Build a new object: `state = { ...state, ...changes }`.

**5. Patching the page inside a handler.**

```js
wrap.addEventListener("change", () => {
  update({ giftWrap: wrap.checked });
  total.textContent = rupees(itemTotal(item, state) + 3000);
});
```

`render` already set the right total, and this line adds the wrap a second time. Handlers call `update` and nothing else.
