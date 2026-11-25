## The design

The checkout becomes four small screens, one per step, each with its own address:

| Step | Address | Title in the tab |
|---|---|---|
| The cart, with the total | `/checkout/cart` | `Checkout: Your cart \| Dhaaga & Co.` |
| Name, phone and PIN code | `/checkout/details` | `Checkout: Your details \| Dhaaga & Co.` |
| Pay by UPI | `/checkout/pay` | `Checkout: Pay by UPI \| Dhaaga & Co.` |
| Thank you | `/checkout/done` | `Checkout: Order placed \| Dhaaga & Co.` |

Because every step has an address, Back goes to the previous step, and what the customer typed stays in the store.

The total follows the story's rules, in paise: one Golu (₹450) and two coaster sets (₹300) make ₹750, plus ₹60 delivery, so `81000` paise, shown as ₹810.00. Delivery is free above ₹999, and an empty cart pays none.

### Route guards

```callout jargon
A **route guard** is a check the router makes before showing a screen. If the customer isn't allowed there yet, the guard sends them somewhere they are allowed instead.
```

The steps open in order. The **furthest step** is the last one the customer may reach right now:

- once paid: `done`, and nothing else, so no one can pay twice;
- with an empty cart: `cart`;
- while the details have errors: `details`;
- otherwise: `pay`.

Any step up to the furthest one is open. A closed step, or an address that isn't a step at all, is swapped for the furthest step with `replaceState`, so Back can never return to it.

```animate
title: A guard at the payment step
kind: scene
nodes:
  - { id: bar, label: Address bar, emoji: "🔗", value: "/checkout/pay" }
  - { id: guard, label: Route guard, emoji: "🛡️", value: checking }
  - { id: store, label: Store, emoji: "🗃️", value: "2 items, no PIN code" }
  - { id: outlet, label: Outlet, emoji: "🖼️", value: empty }
steps:
  - say: A customer opens a saved link to the payment step, but her details have no PIN code yet.
    focus: [bar]
  - say: Before drawing anything, the guard asks the store how far she may go.
    send: { from: guard, to: store, label: "furthest step?" }
  - say: The cart has items, but the details have an error, so the furthest open step is details.
    send: { from: store, to: guard, label: details, back: true }
  - say: Pay is past details, so it is closed. The guard swaps the address with replaceState.
    set: { bar: "/checkout/details", guard: "sent to details" }
  - say: The details step is drawn, and the title follows. Back won't lead to the payment page she couldn't use.
    set: { outlet: "Your details" }
  - say: After she pays, the furthest step is done. Back to pay is guarded too, so she can never pay twice.
    set: { store: "paid", bar: "/checkout/done", outlet: "Order placed" }
```

## What you build

Everything goes in `checkout.js`. Build it bottom-up, as in the README:

1. The money: `subtotal`, `deliveryFee` and `orderTotal`, all in whole paise.
2. `detailsErrors(details)`, with the name, phone and PIN code rules from topic 06. Here an empty phone or PIN code gets the shape message, such as "A PIN code has 6 digits, like 411004."
3. The guard: `furthestStep(state)` and `canVisit(state, step)`.
4. The router: `createCheckout(outlet, store)` returns `start`, `stop` and `go`. One `show()` function reads the step from the address, guards it, draws it and sets the title. Start, `go` and `popstate` all call it.

The screens in `steps.js` are finished. Each takes `{ state, store, go, total, errors }` and returns a `<section data-step="...">`. The checks read `data-step` to see which screen is showing.

## Recap

- Give every step of a flow its own address, so Back moves between steps and nothing typed is lost.
- Keep the rules in small pure functions: the money, the details errors and the furthest step. They are easy to test on their own.
- A route guard checks before drawing. A closed step is swapped for the furthest open one with `replaceState`.
- After paying, only the thank-you step is open, so no one can pay twice.
- One `show()` draws and sets the title for every way of arriving: start, `go`, Back and Forward.

## Common mistakes

**1. Guarding only the buttons.** Hiding the Continue button doesn't stop a saved link or Back from opening `/checkout/pay`. Guard in the router, where every way of arriving passes.

**2. Redirecting with pushState.** The blocked address stays in the history, so Back returns to it and the guard bounces the customer forward again. Use `replaceState`.

**3. Leaving pay open after paying.** Back to the payment step shows the UPI request again, and some customers pay twice. Once `paid` is true, only `done` is open.

**4. Free delivery at exactly ₹999.** The rule is free *above* ₹999, so `99900` paise still pays ₹60. Compare with `>`, not `>=`.
