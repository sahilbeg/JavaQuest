## What happened

The Diwali sale started at 10:00. By 11:00, Arjun had forty-one messages that said the same thing: "My cart was full, my phone reloaded the page, and now it's empty."

The cart code in `src/cart.js` was meant to save the cart in localStorage and bring it back on the next visit. It does call `saveCart`. It does call `loadCart`. Nothing throws an error. And still, every refresh empties the cart.

There isn't one bug here. There are three, stacked on top of each other, and each one hides the next.

```callout analogy
Picture a shop that writes each order in its notebook one step too early. It files the notebook on the wrong shelf. And it copies down only the item names, never how many. Fix any one of those, and the orders still come out wrong.
```

## What you need

### The save and load cycle

A cart that survives a refresh needs both halves, done right:

```animate
title: A cart that survives a refresh
kind: scene
nodes:
  - { id: customer, label: Customer, emoji: "🙋", row: 0, col: 0 }
  - { id: state, label: "lines (state)", emoji: "🧠", value: "[]", row: 0, col: 2 }
  - { id: storage, label: localStorage, emoji: "📓", value: empty, row: 1, col: 2 }
  - { id: page, label: Cart on screen, emoji: "🛒", value: empty, row: 0, col: 4 }
steps:
  - say: The customer taps Add on Golu. The state changes first, to one line with Golu.
    send: { from: customer, to: state, label: "add DC-1042" }
    set: { state: "Golu × 1" }
  - say: Only after the change is the new state saved. Saving first would store the cart as it was a moment ago.
    send: { from: state, to: storage, label: "save the new lines" }
    set: { storage: "Golu × 1" }
  - say: Then the cart is drawn from the state.
    send: { from: state, to: page, label: render }
    set: { page: "Golu × 1" }
  - say: The phone reloads the page. Every variable is gone. localStorage stays.
    set: { state: "[]", page: empty }
  - say: On start, the code reads the same key it saved under, and uses it as the state.
    send: { from: storage, to: state, label: "load, same key", back: true }
    set: { state: "Golu × 1" }
  - say: The cart is drawn again. If the saved text was missing or broken, the state would be a safe empty cart instead of a crash.
    send: { from: state, to: page, label: render }
    set: { page: "Golu × 1" }
```

Three rules come out of it:
1. **Save after every change**, with the new state.
2. **Load on start**, from the very same key, with a safe default when nothing is saved or the text is broken.
3. **Save everything you need to rebuild the state**, and check what you load before you trust it.

### Syntax: checking one saved line

Saved data can be old, half-written or edited by hand. Before a line goes into the cart, check its shape:

```js
function isLine(value) {
  return typeof value === "object" && value !== null
    && typeof value.id === "string"
    && Number.isInteger(value.qty) && value.qty > 0;
}
```

- `typeof value === "object"` is also true for `null`, so check `value !== null` too.
- `Number.isInteger` is `false` for `"2"`, `1.5` and `NaN`, so a quantity typed as text never gets in.
- Use it with `filter` to keep the good lines and quietly drop the bad ones.

```callout jargon
**Validating** data means checking that it has the shape and values you expect before you use it. Anything that comes from outside your code, even your own saved data from last week, should be validated.
```

## The tiers

1. **Bronze** resolves the incident. Every change to the cart is saved straight away, and the saved value includes the item just added.
2. **Silver:** on start, the cart loads what was saved, from the same key. A first visit shows the empty cart. A broken saved value, like `{oops`, shows the empty cart instead of crashing, and the next add saves a good cart again.
3. **Gold:** quantities survive a refresh: two Golus come back as two, and the total counts them. A saved value that isn't a list gives an empty cart. Inside a list, only lines with a string `id` from the catalogue and a whole-number `qty` above 0 are kept.

Open the preview, add some items, then reload the preview to watch the bug. DevTools' **Application** panel shows what is really saved under `dhaaga:cart`.

## Recap

- Save **after** the state changes, never before, or the saved copy is always one step behind.
- Load on start from the **same key** you save under. Keep the key in one constant.
- Never trust saved text: `null` means a first visit, `JSON.parse` can throw, and valid JSON can still have the wrong shape.
- Save the whole state you need to rebuild the page, and validate each part when you load it.

## Common mistakes

**Fixing one bug and stopping.** Bronze passing doesn't mean the cart survives a refresh. Each tier's checks find the next bug that was hiding behind the last one.

**Catching the error but keeping the bad value.** A `try`/`catch` around `JSON.parse` stops the crash from `{oops`. It does nothing for `{"DC-1042":2}`, which parses fine but isn't a list. Check `Array.isArray` too.

**Converting quantities instead of checking them.** `Number("two")` is `NaN`, and `NaN` Golus make a total of `₹NaN`. Drop a line whose `qty` isn't already a whole number above 0.

**Keeping a line for an item that no longer exists.** A saved `DC-9999` has no product in the catalogue, so drawing it crashes on `product.name`. Keep only lines whose id is in the catalogue.
