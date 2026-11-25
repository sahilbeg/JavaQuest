## The design

Checkout is four steps, always in the same order. A customer can go back one step, but never skip ahead.

```animate
title: The four steps of checkout
kind: scene
nodes:
  - { id: cart, label: Cart, emoji: "🛒", value: "To pay ₹810.00" }
  - { id: address, label: Address, emoji: "🏠" }
  - { id: upi, label: Pay by UPI, emoji: "📲" }
  - { id: done, label: Order placed, emoji: "🎉" }
links:
  - { from: cart, to: address }
  - { from: address, to: upi }
  - { from: upi, to: done }
steps:
  - say: Checkout starts on the cart, with the items, the delivery charge and the amount to pay.
    focus: [cart]
  - say: Continue moves to the address step. An empty cart can't continue.
    send: { from: cart, to: address, label: cartConfirmed }
  - say: Only a valid address moves on to payment. The upi state carries that address with it.
    send: { from: address, to: upi, label: addressSaved }
    set: { upi: "Asha Rao, 411007" }
  - say: Back returns to the address step, still filled in, because the state going back carries the saved address.
    send: { from: upi, to: address, label: wentBack, back: true }
  - say: After paying by UPI, the customer taps I've paid. The click handler places the order.
    send: { from: upi, to: done, label: paid }
    set: { done: order-17 }
  - say: The last step shows the order id, so the customer can quote it when they call Raju.
    focus: [done]
```

### Syntax: a state union

```ts
type CheckoutState =
  | { step: "cart" }
  | { step: "address"; saved: Address | null }
  | { step: "upi"; address: Address }
  | { step: "done"; address: Address; orderId: string };
```

Each step is its own shape, and carries exactly what that step needs. The `upi` shape *must* have an `address`. So a state "on the payment step with no address" can't even be written:

```tsx
const broken: CheckoutState = { step: "upi" };
```

```text
error TS2322: Type '{ step: "upi"; }' is not assignable to type 'CheckoutState'.
  Property 'address' is missing in type '{ step: "upi"; }' but required in type '{ step: "upi"; address: Address; }'.
```

```callout jargon
A **state machine** is a fixed set of states, plus rules for which actions move from one state to another. Here the union is the states, and the reducer is the rules.
```

| Action | Allowed on | Next state |
|---|---|---|
| `{ type: "cartConfirmed" }` | `cart` | `{ step: "address", saved: null }` |
| `{ type: "addressSaved", address }` | `address` | `{ step: "upi", address }` |
| `{ type: "wentBack" }` | `address` | `{ step: "cart" }` |
| `{ type: "wentBack" }` | `upi` | `{ step: "address", saved: <the upi state's address> }` |
| `{ type: "paid", orderId }` | `upi` | `{ step: "done", address, orderId }` |

An action on the wrong step returns the state it was given. An unknown action type throws `Unknown action: <type>`.

Inside the reducer, checking the step narrows the state. After `if (state.step !== "upi") return state;`, TypeScript knows `state.address` is there.

```callout tip
Placing the order is a side effect: it talks to the outside world and creates an order id. It belongs in the "I've paid" click handler. The reducer only records the id it is given.
```

## What you build

Money is in paise everywhere, shown with `rupees()` from `src/money.ts`.

1. **`checkoutTotals(lines)`** in `src/totals.ts` returns `{ items, delivery, total }`. `items` adds up `paise * qty`. Delivery inside Pune is ₹60 (6000 paise), free when `items` is above ₹999 (99900 paise). An empty cart has no delivery charge.
2. **`validateAddress(form)`** in `src/address.ts`, with one message per bad field: `Please enter your name.`, `Phone needs 10 digits, like 98765 43210.` and `PIN code needs 6 digits.` (`cleanAddress` is done.)
3. **`checkoutReducer`** in `src/checkoutMachine.ts`, following the table above.
4. **`Checkout({ lines, placeOrder })`** in `src/Checkout.tsx`: use the reducer, show one step component per state, and handle I've paid. The step components in `src/steps.tsx` are done:
   - **Cart**: heading `Your cart`, lines like `Coaster set × 2: ₹300.00`, then `Items`, `Delivery` (or `Free`) and `To pay`, and a **Continue to address** button, disabled with `Your cart is empty.` when there are no lines.
   - **Address**: heading `Delivery address`, fields `Name`, `Phone` and `PIN code`, **Back** and **Continue to payment**.
   - **UPI**: heading `Pay by UPI`, `Pay ₹810.00 to dhaaga@upi`, `Delivering to Asha Rao, 411007`, **Back** and **I've paid**. I've paid calls `placeOrder({ lines, address, total })` once; it returns an id like `order-17`.
   - **Done**: heading `Order placed` and `Your order id is order-17.`

### Syntax: one screen per state

```tsx
switch (state.step) {
  case "cart": return <CartStep ... />;
  case "address": return <AddressStep initial={state.saved} ... />;
  case "upi": return <UpiStep address={state.address} ... />;
  case "done": return <DoneStep orderId={state.orderId} />;
  default: { const unknownStep: never = state; return unknownStep; }
}
```

Each case narrows `state`, so each step component gets exactly the data its shape carries, already typed.

## Recap

- A union of state shapes, one per step, makes impossible states impossible to write: no payment without an address.
- The reducer checks the step before every move, and returns the state unchanged on the wrong step.
- Totals are worked out from the lines on every render, never kept in state.
- Side effects like placing an order go in event handlers. The reducer only records the result.
- A `switch` on `state.step` with a `never` check picks the screen and makes sure every step has one.

## Common mistakes

**1. Placing the order inside the reducer.**

```tsx
case "paid":
  return { step: "done", address: state.address, orderId: placeOrder(state) };
```

The reducer is no longer pure. In development React may call it twice, and the customer gets two orders.

Fix: call `placeOrder` in the click handler, then `dispatch({ type: "paid", orderId })`.

**2. One flat state with optional fields.**

```ts
interface CheckoutState { step: string; address?: Address; orderId?: string }
```

Now `{ step: "upi" }` type-checks, and the UPI screen must guess what to do with no address. Every screen fills up with `?.` and `!`.

Fix: one shape per step, in a union, as in the design.

**3. Free delivery at exactly ₹999.**

```ts
const delivery = items >= 99900 ? 0 : 6000;
```

A ₹999 order gets free delivery, but the rule says free only *above* ₹999.

Fix: `items > 99900`.
