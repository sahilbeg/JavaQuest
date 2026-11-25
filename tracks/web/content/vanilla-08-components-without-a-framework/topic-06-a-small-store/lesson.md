## Hook

The Dhaaga team has a WhatsApp group for stock. When Arjun counts the shelf, he posts once: "Golu: 3 left." Kavya, Raju and Sana all see the same message at the same moment. Nobody keeps their own count and hopes it's right.

A **store** does that for the page. It holds one copy of the cart. When the cart changes, the store "posts" the news to every part of the page that joined, and each part redraws itself.

## Concept

### Three copies drift apart

The header badge, the cart drawer and the checkout button each kept their own count. Each was updated by different code, at different times. When one handler forgot one copy, the header said `Cart (2)` while the drawer showed three coaster sets.

You met the fix in the DOM module: one **state** object, the single source of truth. A store is that idea for many components: one state, shared by all of them.

```callout jargon
A **store** is one object that holds the shared state, lets code change it in one way only, and tells everyone who asked whenever it changes.
```

### Syntax: createStore

```js
function createStore(initialState) {
  let state = initialState;
  const listeners = new Set();
  return {
    getState() {
      return state;
    },
    setState(changes) {
      state = { ...state, ...changes };
      for (const listener of listeners) {
        listener(state);
      }
    },
    subscribe(listener) {
      listeners.add(listener);
      return () => listeners.delete(listener);
    },
  };
}
```

- `state` and `listeners` live inside the function. Only the three returned methods can reach them: a closure, from the JavaScript module.
- `getState()` reads the state.
- `setState(changes)` makes a **new** state object, then tells every listener.
- `subscribe(listener)` adds a function to call on every change, and returns a way to stop.

```callout jargon
A **subscriber** (or listener) is a function that asked to be told about changes. The store keeps a list of them and calls each one after every change. This is called the **observer pattern**.
```

### One change, every subscriber

```animate
title: One setState, two subscribers
kind: code
language: js
code: |
  const listeners = new Set();
  let state = { count: 0 };
  const subscribe = (fn) => listeners.add(fn);
  function setState(changes) {
    state = { ...state, ...changes };
    for (const fn of listeners) fn(state);
  }
  subscribe((s) => console.log(`badge: Cart (${s.count})`));
  subscribe((s) => console.log(`drawer: ${s.count} items`));
  setState({ count: 2 });
steps:
  - line: 1
    say: The store keeps its subscribers in a Set, so the same function can't join twice.
  - line: 2
    vars: { state: "{ count: 0 }" }
    say: One state object for the whole page. The cart is empty.
  - line: 8
    say: The header badge subscribes. Nothing runs yet; it only joins the list.
  - line: 9
    say: The drawer subscribes too. Two functions are waiting.
  - line: 10
    say: A customer adds two coaster sets. The code asks the store for a change. It never touches the badge or the drawer.
  - line: 5
    vars: { state: "{ count: 2 }" }
    say: The store makes a new state object, with the change on top of the old fields.
  - line: 6
    out: "badge: Cart (2)"
    say: Then it calls every subscriber with the new state. The badge redraws first.
  - line: 6
    out: "drawer: 2 items"
    say: Then the drawer. Both read the same state, so they can never disagree.
```

### Syntax: setState with a function

```js
cart.setState((state) => ({ count: state.count + 1 }));
```

When the new value depends on the old one, pass a function. The store calls it with the current state and uses what it returns as the changes. Your code never holds an old copy of the state by mistake.

Inside `setState`, one line tells the two forms apart:

```js
const changes = typeof change === "function" ? change(state) : change;
```

### Syntax: unsubscribe

```js
const stop = cart.subscribe(drawBadge);
stop();
```

- `subscribe` returns a function. Calling it removes the subscriber again.
- After that, the store no longer calls `drawBadge`.
- Keep the returned function somewhere, because it's the only way to stop.

### Components subscribe while they are on the page

A custom element fits the store neatly:

```js
class CartCount extends HTMLElement {
  #unsubscribe = null;

  connectedCallback() {
    this.#unsubscribe = this.store.subscribe((state) => this.render(state));
    this.render(this.store.getState());
  }

  disconnectedCallback() {
    this.#unsubscribe();
  }
}
```

- On joining the page, it subscribes, and draws once straight away from the current state. Otherwise it would show nothing until the next change.
- On leaving the page, it unsubscribes.
- The store comes in as a property, `element.store = cart`: data down, as in the events ticket.

### Why unsubscribing matters

If a component never unsubscribes, the store keeps calling it after it has left the page. It redraws an element nobody can see, every time. Worse, the store's list holds on to the element, so the browser can never free its memory. Open and close the cart drawer fifty times, and fifty dead drawers are still listening.

```callout jargon
A **memory leak** is memory a program keeps although it no longer needs it, usually because something still points to it. Forgotten subscribers are a common cause.
```

## Worked example

The stall tablet shows yarn stock. A banner warns when any colour is under 100 g. Both read one store.

```js
const yarn = createStore({ grams: { Marigold: 420, Mint: 135 } });

function drawShelf(state) {
  const text = Object.entries(state.grams).map(([colour, g]) => `${colour}: ${g} g`).join(", ");
  document.querySelector("#shelf").textContent = text;
}

function drawBanner(state) {
  const low = Object.keys(state.grams).filter((colour) => state.grams[colour] < 100);
  const banner = document.querySelector("#low-stock");
  banner.hidden = low.length === 0;
  banner.textContent = `Order more: ${low.join(", ")}`;
}

yarn.subscribe(drawShelf);
yarn.subscribe(drawBanner);
drawShelf(yarn.getState());
drawBanner(yarn.getState());

function useYarn(colour, grams) {
  yarn.setState((state) => ({ grams: { ...state.grams, [colour]: state.grams[colour] - grams } }));
}

useYarn("Mint", 40);
```

1. One store holds the grams. Two drawing functions subscribe to it, and both draw once at the start.
2. `useYarn` changes the state only through `setState`, with a function, because the new grams depend on the old.
3. It copies `grams` with spread, so the old state object is never changed.
4. After 40 g of Mint is used, Mint has 95 g. Both subscribers run, and the banner appears.

## Try it

Run this store. Then subscribe a third function that prints `checkout: 4 items` style lines, and see when it runs.

```js run
function createStore(initialState) {
  let state = initialState;
  const listeners = new Set();
  return {
    getState: () => state,
    setState(change) {
      const changes = typeof change === "function" ? change(state) : change;
      state = { ...state, ...changes };
      for (const listener of listeners) {
        listener(state);
      }
    },
    subscribe(listener) {
      listeners.add(listener);
      return () => listeners.delete(listener);
    },
  };
}

const cart = createStore({ count: 0 });
const stopBadge = cart.subscribe((s) => console.log(`badge: Cart (${s.count})`));
cart.subscribe((s) => console.log(`drawer: ${s.count} items`));

cart.setState({ count: 3 });
stopBadge();
cart.setState((s) => ({ count: s.count + 1 }));
```

It prints the badge and the drawer for 3, then only `drawer: 4 items`. The badge stopped listening before the second change.

## Recap

- A **store** holds one shared state. Every part of the page reads it, so no copies drift.
- `setState` takes changes or a function of the state, makes a **new** state, then calls every **subscriber**.
- `subscribe(listener)` returns a function that **unsubscribes**.
- A component subscribes in `connectedCallback`, draws once straight away, and unsubscribes in `disconnectedCallback`.
- A subscriber that never leaves is a **memory leak**: it keeps dead elements alive and busy.

## Common mistakes

**1. Changing the state in place.**

```js
cart.getState().count += 1;
```

The number changes, but no subscriber is told, so the badge still says the old count. Change the state only through `setState`.

**2. Forgetting to draw on connect.**

```js
class CartCount extends HTMLElement {
  connectedCallback() {
    this.store.subscribe((state) => this.render(state));
  }
}
```

The badge is empty until the first change, so a customer who arrives with three items sees nothing. Call `this.render(this.store.getState())` straight after subscribing.

**3. Throwing the unsubscribe away.**

```js
class CartCount extends HTMLElement {
  connectedCallback() {
    this.store.subscribe((state) => this.render(state));
    this.render(this.store.getState());
  }

  disconnectedCallback() {}
}
```

Every removed badge stays subscribed for ever. Keep what `subscribe` returns, and call it in `disconnectedCallback`.

**4. Telling subscribers before the state changes.**

```js
const store = {
  setState(changes) {
    for (const listener of listeners) {
      listener(state);
    }
    state = { ...state, ...changes };
  },
};
```

Every subscriber draws the old state, so the page is always one step behind. Change the state first, then tell them.
