## Hook

At the FC Road stall there is one price board. Raju reads it, Sana reads it, and customers read it. When the coaster price changes, someone changes the board once, and everyone sees the new price.

Imagine if each of them kept their own notebook of prices instead. Someone would always be reading an old one. In an SPA, screens are like those people. They should all read one board.

## Concept

### One store for the whole app

```callout jargon
A **store** is one object that holds the app's **state**: the data that decides what the screens show, such as the cart lines and the current route. Screens read from it and ask it to change.
```

You built a small store with subscribers in the components module. In an SPA it matters even more. Screens are created and thrown away as the customer moves around, but the store lives as long as the page does. A screen that keeps its own copy of the cart loses it, or shows an old one.

### Syntax: a store with update functions

```js
const store = createStore({ lines: [], route: "/" });
store.setState((state) => ({ ...state, route: "/cart" }));
const stop = store.subscribe((state) => console.log(state.route));
```

- `getState()` gives the current state.
- `setState(update)` calls `update` with the old state. Whatever it returns becomes the new state, and every subscriber is told.
- `subscribe(listener)` adds a listener, and returns a function that removes it again.

### Never change the old state

```callout jargon
An **immutable update** builds a new object with the change in it, and leaves the old object exactly as it was.
```

Why bother? Because the store and its views compare states with `===`. If you `push` into the old array, the new state and the old one are the **same** object, so nothing looks changed. Old snapshots, such as the one an undo button keeps, also change behind your back.

```js run
const before = { lines: [{ id: "DC-1042", qty: 1 }] };

const pushed = before;
pushed.lines.push({ id: "DC-2001", qty: 1 });
console.log(pushed === before, before.lines.length);

const copied = { ...before, lines: [...before.lines, { id: "DC-3001", qty: 1 }] };
console.log(copied === before, before.lines.length, copied.lines.length);
```

It prints `true 2`, then `false 2 3`. Pushing changed `before` itself. Copying made a new object and left `before` alone.

### Syntax: copying with spread, map and filter

```js
const added = { ...state, lines: [...state.lines, { id: "DC-1042", qty: 1 }] };
const raised = { ...state, lines: state.lines.map((l) => (l.id === "DC-1042" ? { ...l, qty: l.qty + 1 } : l)) };
const removed = { ...state, lines: state.lines.filter((l) => l.id !== "DC-1042") };
```

- `{ ...state, lines }` copies every key of the state, then replaces `lines`.
- `[...lines, newLine]` is a new array with one more line at the end.
- `map` gives a new array. The changed line is a new object, `{ ...l, qty: l.qty + 1 }`. Unchanged lines are reused.
- `filter` gives a new array without the removed line.

### Selectors: asking the store one question

```callout jargon
A **selector** is a small function that takes the whole state and returns one piece of it, or something worked out from it, such as the number of items in the cart.
```

```js
function lineCount(state) {
  return state.lines.length;
}
```

Views use selectors so each one asks only the question it cares about. The badge asks "how many items?" The cart screen asks "which lines?"

### Redraw only when your answer changes

Every `setState` tells every subscriber. A route change tells the badge too, though the count didn't move. A view should keep its last answer and touch the page only when the new answer is different:

```animate
title: The badge ignores news it doesn't need
kind: code
language: js
code: |
  let last = 0;
  function onState(count) {
    if (count === last) {
      return;
    }
    last = count;
    console.log(`badge: ${count}`);
  }
  onState(1);
  onState(1);
  onState(2);
steps:
  - line: 1
    vars: { last: 0 }
    say: The badge remembers the last count it drew. The cart starts empty.
  - line: 9
    vars: { count: 1 }
    say: The customer adds Golu. The store tells the badge, and the selector says 1.
  - line: 3
    say: 1 is not the same as 0, so this is real news.
  - line: 6
    vars: { last: 1 }
    say: The badge remembers 1 for next time.
  - line: 7
    out: "badge: 1"
    say: Only now does it touch the page.
  - line: 10
    vars: { count: 1 }
    say: The customer opens the cart screen. The route changed, but the count is still 1.
  - line: 4
    say: Same answer as last time, so the badge returns and leaves the page alone.
  - line: 11
    vars: { count: 2 }
    say: Another Golu. The count is 2, which is new.
  - line: 6
    vars: { last: 2 }
    say: The badge remembers 2.
  - line: 7
    out: "badge: 2"
    say: And it draws once more. Two changes to the count, two redraws.
```

Comparing with `!==` works because of immutable updates: a changed piece is always a new value, and an unchanged one is the very same value.

### Mounting and unmounting views

```callout jargon
To **mount** a view is to draw it and subscribe it to the store. To **unmount** it is to remove it and unsubscribe. A **memory leak** is memory a program keeps holding after it stops needing it.
```

When the router swaps out the cart screen, the screen's subscription is still in the store's list. It keeps redrawing a screen nobody can see, and the store can't free it. Every visit to the cart adds another one.

So a mount function returns its unsubscribe function, and the router calls it when the screen leaves.

```animate
title: A screen that forgets to leave
kind: scene
nodes:
  - { id: store, label: Store, emoji: "🗃️", value: "2 listeners" }
  - { id: badge, label: Header badge, emoji: "🔢", value: mounted }
  - { id: cart, label: Cart screen, emoji: "🛒", value: mounted }
  - { id: router, label: Router, emoji: "🧭" }
steps:
  - say: The header badge and the cart screen both subscribe to the store.
    send: [{ from: badge, to: store, label: subscribe }, { from: cart, to: store, label: subscribe }]
  - say: The customer goes to the catalogue. The router takes the cart screen off the page.
    send: { from: router, to: cart, label: remove }
    set: { cart: "off the page" }
  - say: If nobody unsubscribes, the store keeps calling the hidden screen on every change.
    send: { from: store, to: cart, label: "new state" }
    set: { store: "2 listeners (1 wasted)" }
  - say: The fix is for the router to call the screen's unmount function as it leaves.
    send: { from: router, to: store, label: unsubscribe }
    set: { store: "1 listener", cart: unmounted }
  - say: Now only the badge hears about changes, because only the badge is on screen.
    send: { from: store, to: badge, label: "new state" }
```

## Worked example

The header also shows the cart's total. Here is a view for it that follows the store:

```js
function cartTotal(state, prices) {
  return state.lines.reduce((sum, line) => sum + prices[line.id] * line.qty, 0);
}

function mountTotal(el, store, prices) {
  let last = null;
  function draw(state) {
    const total = cartTotal(state, prices);
    if (total === last) {
      return;
    }
    last = total;
    el.textContent = `₹${(total / 100).toFixed(2)}`;
  }
  draw(store.getState());
  return store.subscribe(draw);
}
```

1. `cartTotal` is a selector. Prices are in paise, so the sum is whole paise.
2. `draw` works out the total and returns early if it hasn't changed.
3. It draws once straight away, so the total shows before anything changes.
4. It returns the unsubscribe function from `subscribe`, which is the view's unmount.

## Try it

This is the store from the ticket. Run it, then add a subscriber that prints only the number of lines, and only when that number changes.

```js run
function createStore(initial) {
  let state = initial;
  const listeners = new Set();
  return {
    getState: () => state,
    setState(update) {
      state = update(state);
      for (const listener of [...listeners]) listener(state);
    },
    subscribe(listener) {
      listeners.add(listener);
      return () => listeners.delete(listener);
    },
  };
}

const store = createStore({ lines: [], route: "/" });
const stop = store.subscribe((state) => console.log(`lines: ${state.lines.length}, route: ${state.route}`));
store.setState((state) => ({ ...state, lines: [...state.lines, { id: "DC-1042", qty: 1 }] }));
store.setState((state) => ({ ...state, route: "/cart" }));
stop();
store.setState((state) => ({ ...state, route: "/" }));
console.log(store.getState().route);
```

## Recap

- Keep the app's state in one store that outlives every screen. Screens read it and ask it to change.
- Update immutably: build new objects with `{ ...state }`, `[...lines, line]`, `map` and `filter`. Never `push`, `splice` or `qty++`.
- A selector picks one answer from the state. A view redraws only when its answer is `!==` the last one.
- Mounting a view subscribes it. Unmounting must unsubscribe, or hidden screens keep running and leak memory.

## Common mistakes

**1. Pushing into the old state.**

```js
store.setState((state) => {
  state.lines.push({ id: "DC-1042", qty: 1 });
  return state;
});
```

The new state is the same object as the old one, so every `!==` check says nothing changed, and the badge stays at 0. Fix: return `{ ...state, lines: [...state.lines, { id: "DC-1042", qty: 1 }] }`.

**2. Never unsubscribing.**

```js
function mountCart(outlet, store) {
  store.subscribe((state) => drawCart(outlet, state));
}
```

Every visit to the cart adds one more listener. After ten visits, each change draws the cart ten times, nine of them off screen. Fix: return the function `subscribe` gives you, and call it when the screen leaves.

**3. A selector that builds a new array every time.**

```js
const giftLines = (state) => state.lines.filter((line) => line.gift);
```

`filter` always returns a new array, so `!==` is always true and the view redraws on every change. Fix: select something that stays the same when nothing changed, such as a count, or compare the arrays' contents.

**4. Redrawing everything on every change.**

```js
store.subscribe(() => {
  outlet.replaceChildren(currentScreen(store.getState()));
});
```

Typing in a search box changes the state, so the whole screen is rebuilt on each letter and the box loses its focus. Fix: let each view watch only its own slice.
