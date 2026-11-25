## What happened

Rakhi week is Dhaaga's busiest week after Diwali. Half the orders are presents for brothers and sisters. Last Monday, a new **gift** step went into checkout, between the address and payment: gift wrap for ₹30, and a name stitched on for ₹50.

By Thursday, not one order had gift wrap. Arjun walked through checkout himself. Cart, address, then straight to "Pay by UPI". The gift step was written, tested on its own, and shipped. It just could never be reached.

```animate
title: The step that was never on the route
kind: scene
nodes:
  - { id: cart, label: Cart, emoji: "🛒" }
  - { id: address, label: Address, emoji: "🏠" }
  - { id: gift, label: Gift, emoji: "🎁", value: "never shown" }
  - { id: upi, label: Pay by UPI, emoji: "📲" }
  - { id: done, label: Order placed, emoji: "🎉" }
links:
  - { from: cart, to: address }
  - { from: address, to: upi }
  - { from: upi, to: done }
steps:
  - say: The Step type has five steps, including gift. TypeScript knows gift exists.
    focus: [cart, address, gift, upi, done]
  - say: Arjun presses Continue on his cart. The address step appears, as it should.
    send: { from: cart, to: address, label: Continue }
  - say: He presses Continue again. Checkout jumps straight to Pay by UPI.
    send: { from: address, to: upi, label: Continue }
  - say: The gift step sits there, finished and tested, with no way in.
    focus: [gift]
  - say: Nothing failed to compile. Somewhere, the code quietly accepted a step it doesn't know about.
    focus: [gift]
```

## What you need

This incident uses topic 05 (reducers, action unions and `never`) and topic 06 (exhaustive switches). One more tool makes the fix last:

### Syntax: a Record that must cover every step

```ts
const NEXT: Record<Step, Step | null> = {
  cart: "address",
  address: "gift",
  gift: "upi",
  upi: "done",
  done: null,
};
```

`Record<Step, ...>` must have a key for **every** member of `Step`. Leave out `gift`, and TypeScript refuses:

```text
error TS2741: Property 'gift' is missing in type '{ cart: "address"; address: "upi"; upi: "done"; done: null; }' but required in type 'Record<Step, Step | null>'.
```

A plain array, `const steps: Step[] = [...]`, only checks that each entry *is* a step. It can't tell that one is missing.

The code lives in `src/checkout.ts`. `src/Checkout.tsx` draws each step and is fine. The state is `{ step, giftWrap, stitchedName }`, and the actions are:

| Action | What it does |
|---|---|
| `{ type: "next" }` | `cart` → `address` → `gift` → `upi` → `done`. On `done`, nothing changes. |
| `{ type: "back" }` | One step back: `upi` → `gift` → `address` → `cart`. On `cart` and `done`, nothing changes. |
| `{ type: "giftChosen", giftWrap, stitchedName }` | Keeps the customer's gift choices. |

Step titles: `Your cart`, `Delivery address`, `Make it a gift`, `Pay by UPI`, `Order placed`. `extrasPaise(state)` adds 3000 paise for gift wrap and 5000 paise for a stitched name. Delivery isn't part of this incident.

## The tiers

1. **Bronze** resolves the incident: Continue from the address reaches the gift step, its heading says `Make it a gift`, and Continue from there goes on to UPI.
2. **Silver:** Back from UPI returns to the gift step, and Back from the gift step returns to the address. Gift wrap and a stitched name on Golu make the UPI step say `Pay ₹530.00 to dhaaga@upi`.
3. **Gold:** every step has its own title. `next` on `done`, and `back` on `cart` or `done`, change nothing. An unknown action throws `Unknown action: <type>`. A stitched name of only spaces costs nothing.

## Recap

- A step list typed `Step[]` checks each entry, but not that every step is there. A `Record<Step, ...>` does.
- A `default` that returns something "safe" hides a missing case. `const unknownStep: never = step;` turns it into a type error.
- An unknown action should throw a clear error, not quietly return the state.
- Trim text before deciding whether the customer really typed something.

## Common mistakes

**1. Adding gift to the list and stopping there.**

```ts
const ORDER: Step[] = ["cart", "address", "gift", "upi", "done"];
```

Continue now reaches the gift step, but its heading still says `Your cart`, because `stepTitle`'s default swallows gift. Back has its own table, so it still skips the gift step. And the next new step will be forgotten in the same way.

Fix: fix every place that lists the steps, and make each one exhaustive so TypeScript checks it.

**2. A default that guesses.**

```tsx
default:
  return "Your cart";
```

Any step without a case gets the cart's title, silently. That is how a whole step went unseen for a week.

Fix: `default: { const unknownStep: never = step; return unknownStep; }`.

**3. Counting spaces as a name.**

```ts
(state.stitchedName !== "" ? 5000 : 0)
```

A customer who taps the space bar by mistake pays ₹50 for an empty name.

Fix: `state.stitchedName.trim() !== ""`.
