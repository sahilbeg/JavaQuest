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
  - say: Continue moves to the address step. The cart can't be empty.
    send: { from: cart, to: address, label: cartConfirmed }
  - say: Only a valid address moves on to payment. The address is kept in the reducer's state.
    send: { from: address, to: upi, label: addressSaved }
    set: { address: "Asha Rao, 411007" }
  - say: Back on the payment step returns to the address, still filled in.
    send: { from: upi, to: address, label: wentBack, back: true }
  - say: After paying by UPI, the customer taps I've paid. The click handler places the order.
    send: { from: upi, to: done, label: paid }
    set: { done: order-17 }
  - say: The last step shows the order id, so the customer can quote it when they call Raju.
    focus: [done]
```

The step lives in a reducer. The state is `{ step, address, orderId }`, starting as `{ step: "cart", address: null, orderId: null }`. Every move is an action:

| Action | Allowed on | Goes to | Also |
|---|---|---|---|
| `{ type: "cartConfirmed" }` | `cart` | `address` | |
| `{ type: "addressSaved", address }` | `address` | `upi` | keeps `address` |
| `{ type: "paid", orderId }` | `upi` | `done` | keeps `orderId` |
| `{ type: "wentBack" }` | `address`, `upi` | one step back | keeps `address` |

An action on the wrong step changes nothing: the reducer returns the state it was given. An unknown action type throws `Unknown action: <type>`, as in ticket 5.

```callout jargon
A reducer like this is a small **state machine**: a fixed set of states (the steps), and rules for which actions move from one state to another. It makes "paying with no address" impossible, not just unlikely.
```

Placing the order is a side effect: it talks to the outside world and creates an order id. So it happens in the "I've paid" click handler, and the reducer only records the id it's given.

## What you build

Money is in paise everywhere, shown with `rupees()` from `src/money.js`.

1. **`checkoutTotals(lines)`** in `src/totals.js` returns `{ items, delivery, total }`. `items` adds up `paise * qty`. Delivery inside Pune is ₹60 (6000 paise), free when `items` is above ₹999 (99900 paise). An empty cart has no delivery charge.
2. **`validateAddress({ name, phone, pincode })`** in `src/address.js`, with the rules and messages from ticket 7: `Please enter your name.`, `Phone needs 10 digits, like 98765 43210.` and `PIN code needs 6 digits.`
3. **`checkoutReducer`** in `src/checkoutReducer.js`, following the table above.
4. **`Checkout({ lines, placeOrder })`** in `src/Checkout.jsx`, one screen per step:
   - **Cart**: heading `Your cart`. Each line as `Coaster set × 2: ₹300.00`, then `Items: ₹750.00`, `Delivery: ₹60.00` (or `Delivery: Free`) and `To pay: ₹810.00`. A **Continue to address** button, disabled with `Your cart is empty.` when there are no lines.
   - **Address**: heading `Delivery address`, fields labelled `Name`, `Phone` and `PIN code`, a **Back** button and a **Continue to payment** button. Messages show after the first try. The saved address is cleaned: names trimmed, the phone without spaces.
   - **UPI**: heading `Pay by UPI`, `Pay ₹810.00 to dhaaga@upi`, `Delivering to Asha Rao, 411007`, and **Back** and **I've paid** buttons. I've paid calls `placeOrder({ lines, address, total })` once, which returns an id like `order-17`.
   - **Done**: heading `Order placed` and `Your order id is order-17.`

### Syntax: choosing a screen from the step

```jsx
if (state.step === "address") return <AddressStep onSave={...} onBack={...} />;
if (state.step === "upi") return <UpiStep onPaid={...} onBack={...} />;
if (state.step === "done") return <DoneStep orderId={state.orderId} />;
return <CartStep onContinue={...} />;
```

Each step is its own small component. `Checkout` owns the reducer and passes each step only what it needs.

## Recap

- A reducer with a `step` field is a small state machine: each action is allowed only on the right step.
- Totals are worked out from the lines on every render, never kept in state.
- The address form validates with the same rules as ticket 7 and keeps what was typed when the customer goes back.
- Side effects like placing an order go in event handlers. The reducer only records the result.

## Common mistakes

**1. Placing the order inside the reducer.**

```jsx
case "paid":
  return { ...state, step: "done", orderId: placeOrder(state) };
```

It looks tidy, but the reducer is no longer pure. In development React may call it twice, and the customer gets two orders.

Fix: call `placeOrder` in the click handler, then `dispatch({ type: "paid", orderId })`.

**2. Letting any action jump anywhere.**

```jsx
case "paid":
  return { ...state, step: "done", orderId: action.orderId };
```

A stray `paid` action on the cart step would show "Order placed" for an order with no address.

Fix: check the step first, `if (state.step !== "upi") return state;`.

**3. Free delivery at exactly ₹999.**

```jsx
const delivery = items >= 99900 ? 0 : 6000;
```

A ₹999 order gets free delivery, but the rule says free only *above* ₹999.

Fix: `items > 99900`.
