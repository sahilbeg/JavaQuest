## Hook

At the stall, when a customer says "two coaster sets, please", Raju puts two in the bag at once. He doesn't phone the warehouse first and wait for a yes. If, now and then, the warehouse says they're out, he says sorry and takes one back.

Most of the time the answer is yes, so acting first makes every customer faster. This ticket teaches the cart to work like Raju.

## Concept

### Waiting for the server feels slow

Until now, the cart did this on every tap: ask the server, wait, then change the screen. On a slow connection the number sits still for seconds. Customers think the tap didn't work and tap again.

```callout jargon
An **optimistic update** changes the screen first, as if the server will say yes, and then asks the server. It is called optimistic because it expects success.
```

The opposite, waiting for the answer before changing anything, is called **pessimistic**. It is right for things that must never look done too early, such as a payment.

### Act first, then check

```animate
title: Tapping + on a slow connection
kind: scene
nodes:
  - { id: screen, label: Cart screen, emoji: "🛒", value: "Golu × 1" }
  - { id: store, label: Store, emoji: "🗃️", value: "qty 1" }
  - { id: api, label: dhaaga.in API, emoji: "🖥️", value: idle }
steps:
  - say: The customer taps + next to Golu. The store changes at once, and the screen follows.
    set: { store: "qty 2", screen: "Golu × 2" }
  - say: Then the change is sent to the server. The customer is already looking at 2.
    send: { from: store, to: api, label: "PATCH qty 2" }
    set: { api: busy }
  - say: Most days the server says yes, and there is nothing more to do.
    send: { from: api, to: store, label: "200 OK", back: true }
    set: { api: idle }
  - say: Another day the customer taps + again, and this time the server says no.
    set: { store: "qty 3", screen: "Golu × 3" }
    send: [{ from: store, to: api, label: "PATCH qty 3" }, { from: api, to: store, label: "500", back: true }]
  - say: The store puts the old number back, and the screen shows a short, friendly message.
    set: { store: "qty 2", screen: "Golu × 2. Couldn't update your cart." }
```

### Rolling back

```callout jargon
A **rollback** undoes an optimistic change when the server refuses it, so the screen matches what the server really has.
```

To roll back, remember the old value **before** you change anything, then put it back in `catch`. Also tell the customer, briefly. A number that quietly jumps back looks like a bug.

```js run
let hearts = ["DC-1042"];

async function pretendSave(works) {
  if (!works) throw new Error("Save failed: 500");
}

async function toggleHeart(id, works) {
  const before = hearts.includes(id);
  hearts = before ? hearts.filter((h) => h !== id) : [...hearts, id];
  console.log(`shown: ${hearts.join(", ")}`);
  try {
    await pretendSave(works);
  } catch {
    hearts = before ? [...hearts, id] : hearts.filter((h) => h !== id);
    console.log(`rolled back: ${hearts.join(", ")}`);
  }
}

async function main() {
  await toggleHeart("DC-2001", true);
  await toggleHeart("DC-3001", false);
}

main();
```

It prints `shown: DC-1042, DC-2001`, then `shown: DC-1042, DC-2001, DC-3001`, then `rolled back: DC-1042, DC-2001`.

### Roll back only what you changed

While a request is waiting, the customer keeps tapping. Suppose you saved a copy of the **whole** state, and put it all back on failure. Every other change made while you waited would vanish with it.

So roll back only your own piece: set that one line's qty back to its old number. Every other line keeps whatever it holds now.

### Syntax: sending a change with fetch

```js
fetch("/api/cart/DC-1042", {
  method: "PATCH",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ qty: 3 }),
});
```

- `method: "PATCH"` asks the server to change part of something. `"DELETE"` asks it to remove something.
- `body` is the data as JSON text, made with `JSON.stringify`.
- The `Content-Type` header tells the server the body is JSON.

### Undo instead of "Are you sure?"

A pop-up asking "Are you sure?" slows down every removal, and people learn to tap Yes without reading. A better pattern removes the item at once and shows a small bar: **Removed Indigo tote bag. Undo**.

```callout jargon
A **delayed commit** waits a few seconds before telling the server about a change. Until then, the change exists only in the browser, so Undo can cancel it without any request.
```

```animate
title: Remove, then Undo
kind: scene
nodes:
  - { id: cart, label: Cart, emoji: "🛒", value: "Golu, coasters, tote" }
  - { id: bar, label: Undo bar, emoji: "↩️", value: hidden }
  - { id: timer, label: 5 s timer, emoji: "⏱️", value: idle }
  - { id: api, label: dhaaga.in API, emoji: "🖥️", value: idle }
steps:
  - say: Mrs. Kapoor taps Remove on the tote bag. It leaves the cart at once.
    set: { cart: "Golu, coasters", bar: "Removed. Undo", timer: "5 s left" }
  - say: Nothing goes to the server yet. A five-second timer starts instead.
    focus: [timer, api]
  - say: She meant the coasters! She taps Undo. The timer is cancelled, and the tote bag returns to its old place.
    set: { cart: "Golu, coasters, tote", bar: hidden, timer: cancelled }
  - say: The server never heard a thing, so there is nothing to put right there.
    focus: [api]
  - say: Now she removes the coasters and leaves it. The timer ends, the bar goes, and only now is DELETE sent.
    set: { cart: "Golu, tote", bar: hidden, timer: done }
    send: { from: timer, to: api, label: "DELETE /api/cart/DC-2001" }
```

### Putting a line back in its place

When a removal is undone, the line should return to the exact spot it came from, not the bottom of the cart. Remember its index when you remove it, then rebuild the list around it:

```animate
title: Putting the coaster set back at index 1
kind: code
language: js
code: |
  const lines = ["DC-1042", "DC-3001"];
  const index = 1;
  const before = lines.slice(0, index);
  const after = lines.slice(index);
  const back = [...before, "DC-2001", ...after];
  console.log(back.join(" "));
steps:
  - line: 1
    objects: { l: { type: Array, fields: { "0": '"DC-1042"', "1": '"DC-3001"' } } }
    vars: { lines: "@l" }
    say: The cart after removing the coaster set. It used to sit between Golu and the tote bag.
  - line: 2
    vars: { index: 1 }
    say: We remembered its old index when we removed it.
  - line: 3
    objects: { b: { type: Array, fields: { "0": '"DC-1042"' } } }
    vars: { before: "@b" }
    say: slice(0, 1) copies everything before index 1. That is just Golu.
  - line: 4
    objects: { a: { type: Array, fields: { "0": '"DC-3001"' } } }
    vars: { after: "@a" }
    say: slice(1) copies everything from index 1 on. That is the tote bag.
  - line: 5
    objects: { n: { type: Array, fields: { "0": '"DC-1042"', "1": '"DC-2001"', "2": '"DC-3001"' } } }
    vars: { back: "@n" }
    say: Spread the two halves around the coaster set. It is a brand-new array, and lines is untouched.
  - line: 6
    out: "DC-1042 DC-2001 DC-3001"
    say: The coaster set is back in the middle, where the customer last saw it.
```

### When not to be optimistic

Optimistic updates suit small, common, safe changes: a qty, a heart, a removal with Undo. They don't suit changes that must really have happened before you say so: paying, placing an order, booking a delivery slot. Those wait for the server, with a loading state.

## Worked example

The wishlist heart, made optimistic in the store:

```js
async function toggleWish(store, id) {
  const wasSaved = store.getState().wishlist.includes(id);
  const flip = (saved) =>
    store.setState((state) => ({
      ...state,
      wishlist: saved ? [...state.wishlist, id] : state.wishlist.filter((w) => w !== id),
    }));

  flip(!wasSaved);
  try {
    const response = await fetch(`/api/wishlist/${id}`, { method: wasSaved ? "DELETE" : "PUT" });
    if (!response.ok) {
      throw new Error(`Wishlist failed: ${response.status}`);
    }
  } catch {
    flip(wasSaved);
    store.setState((state) => ({ ...state, error: "Couldn't save your heart. Please try again." }));
  }
}
```

1. `wasSaved` is the old value, remembered before anything changes.
2. `flip(!wasSaved)` changes the store at once, so the heart fills in straight away.
3. The request runs in the background. A 500 is turned into a throw, like a dropped connection.
4. `catch` flips only this one heart back, and leaves a short message.

## Try it

In the heart example above, add a third call to `main` that removes `"DC-1042"` and fails. What should `hearts` hold at the end? Run it and check.

## Recap

- An optimistic update changes the screen first and saves in the background, so taps feel instant even on slow connections.
- Remember the old value before changing anything. If the save fails, roll back and say so in a short message.
- Roll back only the piece you changed, so changes made while you waited survive.
- Prefer Undo over "Are you sure?". Remove at once, keep the line and its index, and send DELETE only when the Undo time runs out.
- Don't be optimistic about payments and orders. Wait for the server there.

## Common mistakes

**1. Remembering the old value too late.**

```js
async function changeQty(store, id, qty) {
  setQty(store, id, qty);
  const before = store.getState().lines.find((line) => line.id === id).qty;
}
```

`before` is read after the change, so it already holds the new qty. A rollback "puts back" the wrong number. Fix: read the old value first, then change.

**2. Restoring the whole old state.**

```js
async function changeQty(store, id, qty) {
  const snapshot = store.getState();
  setQty(store, id, qty);
  try {
    await save(id, qty);
  } catch {
    store.setState(() => snapshot);
  }
}
```

If the customer changed another line while this one was saving, that change vanishes too. Fix: roll back only this line's qty.

**3. Sending the DELETE straight away.**

```js
function remove(store, id) {
  takeOut(store, id);
  fetch(`/api/cart/${id}`, { method: "DELETE" });
}
```

Undo now has to un-delete on the server, which needs a second request and may fail. Fix: wait until the Undo time is over before sending anything.

**4. Putting an undone line at the end.**

```js
store.setState((state) => ({ ...state, lines: [...state.lines, line] }));
```

The tote bag jumps from the middle of the cart to the bottom, and the customer can't find it. Fix: remember its index and use `[...lines.slice(0, index), line, ...lines.slice(index)]`.
