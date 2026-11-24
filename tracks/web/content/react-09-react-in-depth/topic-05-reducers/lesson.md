## Hook

At a busy chai stall, the person at the counter doesn't make the tea. They shout a short order, "two cutting, one less sugar", and the person at the stove decides how to make it. The counter only says *what happened*. The stove knows *how* to handle it.

A reducer splits your state the same way. Buttons say what happened, in a small message. One function, in one place, decides how the state changes.

## Concept

### Many ways to change one thing

The cart can now be changed in four ways: add an item, remove a line, change a quantity, and empty it. With `useState`, each button does its own `setLines(...)` with its own logic. The rules for the cart end up spread across many files, and two of them can disagree.

`useReducer` moves every rule into one function, outside your components.

### Syntax: an action

```jsx
{ type: "added", item: KEY }
```

```callout jargon
An **action** is a plain object that describes something that happened. It has a `type`, a short name in the past tense, and any extra data the change needs.
```

The cart's actions:

```jsx
{ type: "added", item: { code: "KEY", name: "Keychain", paise: 12000 } }
{ type: "removed", code: "KEY" }
{ type: "quantityChanged", code: "KEY", qty: 3 }
{ type: "cleared" }
```

Name actions after what the customer did ("removed"), not after how the state changes ("filterLines"). That keeps the rules inside the reducer.

### Syntax: a reducer

```jsx
function reducer(state, action) {
  switch (action.type) {
    case "something":
      return newState;
    default:
      throw new Error(`Unknown action: ${action.type}`);
  }
}
```

```callout jargon
A **reducer** is a function that takes the current state and an action, and returns the next state. It's named after the array method `reduce`, which works the same way: one step at a time, from a start value.
```

Watch a tiny counter reducer handle three actions:

```animate
title: A reducer, one action at a time
kind: code
language: js
code: |
  function cart(state, action) {
    switch (action.type) {
      case "added":
        return { count: state.count + 1 };
      case "cleared":
        return { count: 0 };
      default:
        throw new Error("Unknown action: " + action.type);
    }
  }
  let state = { count: 0 };
  state = cart(state, { type: "added" });
  state = cart(state, { type: "added" });
  console.log(state.count);
  state = cart(state, { type: "cleared" });
  console.log(state.count);
steps:
  - line: 11
    vars: { state: "{ count: 0 }" }
    say: The cart starts empty. This is the starting state.
  - line: 12
    vars: { state: "{ count: 1 }" }
    say: An "added" action goes in. The reducer returns a new object with count 1.
  - line: 13
    vars: { state: "{ count: 2 }" }
    say: Another "added". The reducer starts from the state it is given, so count becomes 2.
  - line: 14
    out: "2"
    say: Two adds, so the count is 2.
  - line: 15
    vars: { state: "{ count: 0 }" }
    say: A "cleared" action returns a fresh empty cart.
  - line: 16
    out: "0"
    say: The reducer never changed an old object. Each action produced a new one.
```

### A reducer must be pure

A reducer gets called by React, sometimes more than once for the same action in development. So it must follow the same rules as rendering:

- Return a **new** state. Never change the old one with `push`, `splice` or `line.qty += 1`.
- No side effects: no `fetch`, no `localStorage`, no timers. Just work out the next state.
- Same state and action in, same state out.

```jsx
case "removed":
  return { ...state, lines: state.lines.filter((line) => line.code !== action.code) };
```

`{ ...state, lines: ... }` copies everything in the old state, then replaces `lines`. If you later add a `coupon` field to the state, this line keeps it.

### Syntax: useReducer

```jsx
const [state, dispatch] = useReducer(reducer, initialState);
```

- `state` is the current state, like the first value from `useState`.
- `dispatch` sends an action to the reducer: `dispatch({ type: "removed", code: "KEY" })`.

```callout jargon
To **dispatch** an action means to send it off to be handled. React passes it to your reducer with the current state, keeps what comes back, and renders again.
```

```animate
title: A click becomes an action
kind: scene
nodes:
  - { id: button, label: "Remove Keychain", emoji: "🔘" }
  - { id: react, label: React, emoji: "⚛️", value: "2 lines" }
  - { id: reducer, label: cartReducer, emoji: "🧮" }
  - { id: page, label: The page, emoji: "📄", value: "Cart (3)" }
steps:
  - say: The customer clicks Remove on the keychain line.
    focus: [button]
  - say: The click handler only describes what happened. It dispatches an action.
    send: { from: button, to: react, label: "{ type: removed, code: KEY }" }
  - say: React calls the reducer with the current state and the action.
    send: { from: react, to: reducer, label: "state + action" }
  - say: The reducer returns a new state without the keychain line.
    send: { from: reducer, to: react, label: "new state", back: true }
    set: { react: "1 line" }
  - say: The state changed, so React renders the cart again.
    send: { from: react, to: page, label: render }
    set: { page: "Cart (1)" }
```

### Unknown actions should throw

If a button dispatches `{ type: "remove" }` instead of `"removed"`, a reducer with `default: return state;` does nothing, silently. You click and nothing happens, with no clue why.

Throwing in `default` turns a typo into a loud error that names the bad type: `Unknown action: remove`.

### useState or useReducer?

Both are fine. Pick by how the state changes:

- One or two simple values that change in simple ways: `useState`.
- One piece of state with many kinds of change, or rules you want to test on their own: `useReducer`.

A reducer is a plain function, so you can test it with no React at all: call it with a state and an action, and check what comes back.

```callout tip
Reducers pair well with context. Put `dispatch` in a context, and any component deep in the page can send actions to the one cart. Redux, a popular state library, is built on this exact reducer-and-action idea.
```

## Worked example

Here is the cart reducer, and a panel that uses it.

```jsx
export const emptyCart = { lines: [] };

export function cartReducer(state, action) {
  switch (action.type) {
    case "added": {
      const found = state.lines.some((line) => line.code === action.item.code);
      if (found) {
        return {
          ...state,
          lines: state.lines.map((line) => (line.code === action.item.code ? { ...line, qty: line.qty + 1 } : line)),
        };
      }
      return { ...state, lines: [...state.lines, { ...action.item, qty: 1 }] };
    }
    case "removed":
      return { ...state, lines: state.lines.filter((line) => line.code !== action.code) };
    case "quantityChanged":
      if (action.qty <= 0) {
        return { ...state, lines: state.lines.filter((line) => line.code !== action.code) };
      }
      return {
        ...state,
        lines: state.lines.map((line) => (line.code === action.code ? { ...line, qty: action.qty } : line)),
      };
    case "cleared":
      return emptyCart;
    default:
      throw new Error(`Unknown action: ${action.type}`);
  }
}
```

```jsx
export function CartPanel({ items }) {
  const [cart, dispatch] = useReducer(cartReducer, emptyCart);
  return (
    <ul>
      {cart.lines.map((line) => (
        <li key={line.code}>
          {line.name} × {line.qty}
          <button onClick={() => dispatch({ type: "removed", code: line.code })}>Remove {line.name}</button>
        </li>
      ))}
    </ul>
  );
}
```

- The `"added"` case has braces, `case "added": { ... }`, because it declares a `const`. The braces keep `found` inside that case.
- `quantityChanged` with 0 or less removes the line, which fixes Kavya's qty 0 bug in one place.
- `"cleared"` can return the shared `emptyCart`, because nothing ever changes a state object.
- The panel's buttons only say what happened. The rules all live in `cartReducer`.

## Try it

Because a reducer is just a function, `Array.prototype.reduce` can play a whole shopping trip through it. Run this, then add a `{ type: "quantityChanged", code: "KEY", qty: 0 }` action and run it again.

```js run
const emptyCart = { lines: [] };

function cartReducer(state, action) {
  switch (action.type) {
    case "added": {
      const found = state.lines.some((line) => line.code === action.item.code);
      if (found) {
        return {
          ...state,
          lines: state.lines.map((line) => (line.code === action.item.code ? { ...line, qty: line.qty + 1 } : line)),
        };
      }
      return { ...state, lines: [...state.lines, { ...action.item, qty: 1 }] };
    }
    case "removed":
      return { ...state, lines: state.lines.filter((line) => line.code !== action.code) };
    default:
      throw new Error(`Unknown action: ${action.type}`);
  }
}

const KEY = { code: "KEY", name: "Keychain", paise: 12000 };
const CST = { code: "CST", name: "Coaster set", paise: 15000 };
const actions = [
  { type: "added", item: KEY },
  { type: "added", item: CST },
  { type: "added", item: KEY },
  { type: "removed", code: "CST" },
];

const final = actions.reduce(cartReducer, emptyCart);
for (const line of final.lines) {
  console.log(`${line.name} x ${line.qty}`);
}
console.log(emptyCart.lines.length);
```

The last line prints `0`: the starting cart was never changed. The `quantityChanged` action will crash with `Unknown action`, because this short reducer doesn't handle it yet. That's the loud error doing its job.

## Recap

- An action is a plain object, `{ type: "removed", code: "KEY" }`, that says what happened.
- A reducer, `(state, action) => nextState`, holds every rule for changing the state in one place.
- A reducer must be pure: return new objects, never change the old state, and make no requests.
- `const [state, dispatch] = useReducer(reducer, initialState)`. Components call `dispatch(action)`.
- Throw on an unknown action type, so a typo fails loudly instead of silently.

## Common mistakes

**1. Changing the old state.**

```jsx
case "added":
  state.lines.push({ ...action.item, qty: 1 });
  return state;
```

The page doesn't update after Add. The reducer returned the same object, so React sees no change and skips the render.

Fix: return a new object, `{ ...state, lines: [...state.lines, { ...action.item, qty: 1 }] }`.

**2. Forgetting to return.**

```jsx
case "cleared":
  emptyCart;
```

There's no `return`, so the code falls through into the next case. Here that's `default`, so pressing "Empty the cart" throws `Unknown action: cleared`, even though `cleared` has a case. At the end of a switch with no `default`, the reducer would return `undefined` instead.

Fix: every case must `return` the next state.

**3. Doing side effects in the reducer.**

```jsx
case "added":
  localStorage.setItem("cart", JSON.stringify(state));
  return { ...state, lines: [...state.lines, { ...action.item, qty: 1 }] };
```

It seems to work, but it saves the old state, and in development it can run twice. The reducer is no longer pure.

Fix: keep the reducer pure. Save in a `useEffect` that depends on the state.

**4. Ignoring unknown actions.**

```jsx
default:
  return state;
```

A button that dispatches `{ type: "remove" }` (missing the d) does nothing, with no error anywhere.

Fix: throw, so the typo shows up straight away.

```jsx
default:
  throw new Error(`Unknown action: ${action.type}`);
```
