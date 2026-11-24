## The design

Sana's drawing has three parts:

```text
Header       Dhaaga & Co.                        [ Cart (3) ]

Catalogue    Golu the elephant         [Add to cart]
             Marigold coaster set      [Add to cart]
             Rani pink keychain        [Add to cart]

Drawer       Your cart                              [Close]
(when open)  Golu the elephant      [−] 2 [+]       ₹900.00
             Marigold coaster set   [−] 1 [+]       ₹150.00
             Total                                ₹1,050.00
```

- The **cart button** says how many pieces are in the cart, like `Cart (3)`. Tapping it opens the drawer, and tapping it again closes it.
- The **drawer** lists each line with its name, a − button, the quantity, a + button and the line's amount in ₹. The total sits at the bottom.
- With nothing in it, the drawer says "Your cart is empty." instead of a list.
- **Add to cart** adds one piece and updates the count. It doesn't open the drawer, so shoppers can keep browsing.
- − on a line with one piece removes the line.

It must work for everyone, not just mouse users:
- The cart button tells screen readers whether the drawer is open.
- When the drawer opens, keyboard focus moves into it, onto the Close button. When it closes, focus goes back to the cart button.
- Escape closes the drawer.

Here is one round trip, using every idea from this module:

```animate
title: Opening the drawer and adding one more
kind: scene
nodes:
  - { id: shopper, label: Shopper, emoji: "🙋", row: 0, col: 0 }
  - { id: toggle, label: "Cart (2) button", emoji: "🛒", value: "expanded: false", row: 0, col: 2 }
  - { id: state, label: "state", emoji: "🧠", value: "open: false", row: 1, col: 2 }
  - { id: drawer, label: Drawer, emoji: "🗄️", value: hidden, row: 0, col: 4 }
  - { id: lines, label: "#drawer-lines", emoji: "📋", value: "Golu × 2", row: 1, col: 4 }
steps:
  - say: The shopper taps the cart button. Its click handler only asks for a change to the state.
    send: { from: shopper, to: toggle, label: click }
  - say: update makes a new state with open set to true, then calls render.
    send: { from: toggle, to: state, label: "update({ open: true })" }
    set: { state: "open: true" }
  - say: render draws everything from the state. The drawer shows, and aria-expanded becomes true.
    send: { from: state, to: drawer, label: render }
    set: { drawer: shown, toggle: "expanded: true" }
  - say: Focus moves to the Close button, so keyboard users land inside the drawer.
    focus: [drawer]
  - say: The shopper taps + on Golu's line. The click bubbles up to the one listener on the list.
    send: { from: shopper, to: lines, label: "click +" }
  - say: The listener finds the button's data-action and line, and asks for new lines with one more Golu.
    send: { from: lines, to: state, label: "update({ lines })" }
  - say: render draws the lines, the total and the button text again, all from the same state.
    send: { from: state, to: lines, label: render }
    set: { lines: "Golu × 3", toggle: "Cart (3), expanded: true" }
```

## What you build

Everything goes in `src/drawer.js`. A cart line is `{ id: "DC-1042", qty: 2 }`, and a product is `{ id, name, paise }`.

1. `countItems(lines)` returns the number of pieces: the sum of every `qty`.
2. `cartTotal(lines, catalogue)` returns the total in paise: each line's price times its `qty`, added up.
3. `changeQty(lines, id, delta)` returns a **new** array: the line's `qty` changed by `delta` (1 or −1). A new id with `delta` 1 is added at the end. A line that reaches 0 is removed.
4. `createDrawer(root, catalogue, startLines)` wires the page, keeps `{ open, lines }` as its state, and returns `{ getLines }`.

The page it wires:

```html
<button id="cart-toggle" type="button" aria-controls="cart-drawer" aria-expanded="false">Cart (0)</button>
<aside id="cart-drawer" hidden>
  <h2>Your cart</h2>
  <button id="cart-close" type="button">Close</button>
  <ul id="drawer-lines"></ul>
  <p id="drawer-empty">Your cart is empty.</p>
  <p>Total: <strong id="drawer-total">₹0.00</strong></p>
</aside>
```

Each line in `#drawer-lines` looks like this:

```html
<li class="line" data-id="DC-1042">
  <span class="name">Golu the elephant</span>
  <button type="button" data-action="dec" aria-label="One less Golu the elephant">−</button>
  <span class="qty">2</span>
  <button type="button" data-action="inc" aria-label="One more Golu the elephant">+</button>
  <span class="amount">₹900.00</span>
</li>
```

### Syntax: aria-expanded and aria-controls

```html
<button id="cart-toggle" aria-controls="cart-drawer" aria-expanded="false">Cart (0)</button>
```

- `aria-controls` names the id of the element the button opens.
- `aria-expanded` says whether it is open now: the **string** `"true"` or `"false"`. A screen reader reads it as "Cart, collapsed" or "Cart, expanded".
- Set it in `render`, from the state: `toggle.setAttribute("aria-expanded", String(state.open))`.

```callout jargon
**Focus** is where keyboard input goes. One element at a time has it, and `document.activeElement` is that element. Tab moves focus, and `element.focus()` moves it from code.
```

### Syntax: moving focus

```js
closeButton.focus();     // after the drawer opens
toggleButton.focus();    // after it closes
```

- Call `focus()` after `render`, once the element is no longer hidden. A hidden element can't take focus in a real browser.
- Moving focus into a panel when it opens, and back when it closes, means keyboard users never get lost.

```callout tip
Each redraw makes new + and − buttons, so the button you just pressed is replaced. Keyboard users lose their place. React, in module 8, solves this by keeping elements that didn't change. For now, the drawer is fine as it is.
```

## Recap

- One state object, `{ open, lines }`, and one `render` that draws the button, the drawer, the lines, the empty message and the total.
- Small plain functions do the maths: count, total and a new array of lines for each change.
- Two delegated listeners: one on the product list for Add, one on the drawer's list for + and −.
- The cart button carries `aria-expanded`. Focus moves into the drawer when it opens and back when it closes. Escape closes it.

## Common mistakes

**Changing the lines array in place.** `line.qty += 1` changes the old array, and `update` gets the same array back. Make a new array with `map`, `filter` and spread, as `changeQty` should.

**Setting aria-expanded only when opening.** If only the open path sets `"true"`, the button still says "expanded" after the drawer closes. Set it in `render`, from the state, so both directions are covered.

**Forgetting to remove empty lines.** − on a line with one piece must remove it. A line showing `0` with a total of `₹0.00` looks broken.

**Opening the drawer on every Add.** The design says Add only updates the count. A drawer that jumps out on every tap gets in the way of browsing.

**Focusing before the drawer is shown.** If `close.focus()` runs while the drawer is still `hidden`, a real browser ignores it. Call `update` first, then `focus`.
