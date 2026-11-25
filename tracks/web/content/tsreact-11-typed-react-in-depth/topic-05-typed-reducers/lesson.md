## Hook

At the counter, Raju keeps a rule card for orders: "Pack before you ship. Never cancel what has left the shop." Anyone can ask for a change, but the card decides what actually happens.

When state can change in many ways, React code needs a rule card too. It's called a reducer. With TypeScript, every request to change the state is a typed **action**, and the compiler makes sure the card has a rule for each one.

## Concept

### When state changes in many ways

Raju's order board changes in four ways: an order is added, packed, shipped or cancelled. With `useState`, each button would call `setOrders` with its own copy of the logic. The rules ("only packed orders ship") end up spread across many click handlers.

```callout jargon
A **reducer** is a function that takes the current state and an **action** (a plain object saying what happened) and returns the next state. All the rules for changing the state live in that one function.
```

```ts
function reducer(state: State, action: Action): State {
  // look at action.type, return the next state
}
```

The name comes from the array method `reduce`, which also turns "current value plus one more thing" into a new value.

### Syntax: an action union

```ts
type OrderAction =
  | { type: "added"; id: string; customer: string }
  | { type: "packed"; id: string }
  | { type: "shipped"; id: string }
  | { type: "cancelled"; id: string };
```

Each action is an object with a `type`. The union lists every action the board understands, and what data each one carries. `added` needs a customer; the others only need an id.

This is a discriminated union, like `Load<T>` two tickets ago. `type` is the discriminant.

```tsx
dispatch({ type: "packed", id: "order-17" });            // fine
dispatch({ type: "packd", id: "order-17" });             // error: not one of the four types
dispatch({ type: "added", id: "order-18" });             // error: customer is missing
```

### Narrowing in a switch

Inside `switch (action.type)`, each `case` narrows `action` to one shape. In `case "added":`, TypeScript knows `action.customer` exists. In `case "packed":`, writing `action.customer` is an error.

Watch a small reducer apply Raju's rule card to one order's status:

```animate
title: A reducer is a rule card for every action
kind: code
language: ts
code: |
  type Status = "new" | "packed" | "shipped" | "cancelled";
  type Action = { type: "packed" } | { type: "shipped" } | { type: "cancelled" };
  function reduce(status: Status, action: Action): Status {
    switch (action.type) {
      case "packed": return status === "new" ? "packed" : status;
      case "shipped": return status === "packed" ? "shipped" : status;
      case "cancelled": return status === "shipped" ? status : "cancelled";
    }
  }
  let status: Status = "new";
  status = reduce(status, { type: "shipped" });
  console.log(status);
  status = reduce(status, { type: "packed" });
  status = reduce(status, { type: "shipped" });
  console.log(status);
  status = reduce(status, { type: "cancelled" });
  console.log(status);
steps:
  - line: 10
    vars: { status: '"new"' }
    say: A new order comes in from WhatsApp.
  - line: 11
    vars: { status: '"new"' }
    say: Someone presses Ship too early. The shipped rule only moves packed orders, so the status stays new.
  - line: 12
    out: "new"
    say: The rule card refused a jump it doesn't allow.
  - line: 13
    vars: { status: '"packed"' }
    say: Raju packs it. new becomes packed.
  - line: 14
    vars: { status: '"shipped"' }
    say: Now Ship works, because the order is packed.
  - line: 15
    out: "shipped"
    say: The courier has it.
  - line: 16
    vars: { status: '"shipped"' }
    say: A late Cancel arrives. Shipped orders can't be cancelled, so nothing changes.
  - line: 17
    out: "shipped"
    say: Every change went through the same rules, in one place.
```

### Syntax: useReducer

```tsx
const [state, dispatch] = useReducer(reducer, initialState);
```

`useReducer` is like `useState`, but instead of a setter you get `dispatch`. You call `dispatch(action)`, React calls your reducer with the current state and that action, and the result becomes the new state.

```callout jargon
To **dispatch** an action means to send it to the reducer. The component says *what happened* ("packed order-17"); the reducer decides *what changes*.
```

TypeScript reads the types from your reducer. `state` gets the reducer's state type, and `dispatch` only accepts its action type. You don't need to write `<...>` at all.

```tsx
const [orders, dispatch] = useReducer(ordersReducer, start);
// orders: Order[]
// dispatch: (action: OrderAction) => void
```

### Exhaustive checks with never

Months later, someone adds `{ type: "returned"; id: string }` to the union and forgets the reducer. Nothing would handle it. The `never` trick turns that into a type error.

```ts
function assertNever(action: never): never {
  throw new Error(`Unknown action: ${(action as { type: string }).type}`);
}
```

```tsx
switch (action.type) {
  case "added": /* ... */
  case "packed": /* ... */
  case "shipped": /* ... */
  case "cancelled": /* ... */
  default:
    return assertNever(action);
}
```

```callout jargon
**never** is the type with no values. After every case is handled, nothing is left for `action` to be, so in `default` its type is `never`.
```

If a case is missing, `action` in `default` still has that shape, and it can't be passed where `never` is expected:

```text
error TS2345: Argument of type '{ type: "returned"; id: string; }' is not assignable to parameter of type 'never'.
```

And if plain JavaScript somewhere dispatches a made-up action, `assertNever` throws `Unknown action: ...` instead of silently doing nothing.

### Typing dispatch for child components

A row component that sends actions needs `dispatch` as a prop. React's types name it for you:

```tsx
import type { Dispatch } from "react";

function OrderRow({ order, dispatch }: { order: Order; dispatch: Dispatch<OrderAction> }) {
  return <button onClick={() => dispatch({ type: "packed", id: order.id })}>Pack {order.id}</button>;
}
```

`Dispatch<OrderAction>` means "a function that takes an `OrderAction` and returns nothing". The row can't send an action the reducer doesn't know.

### Reducers must be pure

A reducer only works out the next state. It never changes the old one, and it never reaches outside: no `fetch`, no `localStorage`, no timers.

Return new objects and arrays with `map`, `filter` and spread (`...`) instead of changing the old ones. React compares old and new state with `Object.is`. If you change the array in place and return it, React sees the same array and doesn't re-render.

```callout tip
In development, React may call your reducer twice for the same action, to catch impure reducers. A pure reducer gives the same answer both times, so nobody notices.
```

## Worked example

Here's a reducer for a different screen: Arjun's yarn box, counted in grams per colour.

```tsx
import { useReducer } from "react";

type Colour = "Marigold" | "Indigo" | "Mint";
type Stock = Record<Colour, number>;

type YarnAction =
  | { type: "received"; colour: Colour; grams: number }
  | { type: "used"; colour: Colour; grams: number }
  | { type: "counted"; stock: Stock };

function assertNever(action: never): never {
  throw new Error(`Unknown action: ${(action as { type: string }).type}`);
}

function yarnReducer(stock: Stock, action: YarnAction): Stock {
  switch (action.type) {
    case "received":
      return { ...stock, [action.colour]: stock[action.colour] + action.grams };
    case "used":
      return { ...stock, [action.colour]: Math.max(stock[action.colour] - action.grams, 0) };
    case "counted":
      return action.stock;
    default:
      return assertNever(action);
  }
}

export function YarnBox() {
  const [stock, dispatch] = useReducer(yarnReducer, { Marigold: 1200, Indigo: 600, Mint: 300 });
  return (
    <button onClick={() => dispatch({ type: "used", colour: "Marigold", grams: 40 })}>
      Make one Marigold amigurumi ({stock.Marigold} g left)
    </button>
  );
}
```

- `Record<Colour, number>` means the stock has exactly one number per colour.
- `{ ...stock, [action.colour]: ... }` copies the object and changes one colour. The old object is untouched.
- `Math.max(..., 0)` keeps the stock from going below zero.
- `counted` carries a whole `Stock`, so `case "counted"` can read `action.stock`, and the other cases can't.

## Try it

Open the preview. Press **Ship order-17** before it's packed: nothing should happen. Pack it, then ship it. Then try **Cancel** on a shipped order: the button isn't even there, and the reducer would refuse anyway.

Then add `| { type: "returned"; id: string }` to `OrderAction` in `src/orders.ts`. Look at the error on `assertNever(action)`. Remove it again.

## Recap

- A reducer takes the current state and an action, and returns the next state. All the rules live in one place.
- Type the actions as a union with a `type` field. `dispatch` then only accepts actions that exist, with the right data.
- `useReducer(reducer, initial)` gives `[state, dispatch]`, with both types read from the reducer.
- `default: return assertNever(action);` turns a forgotten case into a type error, and an unknown action into a clear runtime error.
- Reducers are pure: return new arrays and objects, and never fetch or save inside them.

## Common mistakes

**1. Changing the state in place.**

```tsx
case "packed": {
  const order = orders.find((o) => o.id === action.id);
  if (order) order.status = "packed";
  return orders;
}
```

The status changes inside the array, but it's the same array, so React doesn't re-render. The board still says `new`.

Fix: return a new array, `orders.map((o) => (o.id === action.id ? { ...o, status: "packed" } : o))`.

**2. A default that hides missing cases.**

```tsx
default:
  return orders;
```

It compiles even when a new action type has no case. That action silently does nothing.

Fix: `default: return assertNever(action);`.

**3. Reading a field the action doesn't have.**

```tsx
case "packed":
  return [...orders, { id: action.id, customer: action.customer, status: "packed" }];
```

You see `Property 'customer' does not exist on type '{ type: "packed"; id: string; }'`. In this case, `action` is only the packed shape.

Fix: only `added` carries a customer. Packing changes an existing order.

**4. Doing side effects in the reducer.**

```tsx
case "shipped":
  fetch(`/api/orders/${action.id}/ship`, { method: "POST" });
  return move(orders, action.id, ["packed"], "shipped");
```

In development React may call the reducer twice, so the courier is booked twice.

Fix: call the API in the click handler, then dispatch the action.
