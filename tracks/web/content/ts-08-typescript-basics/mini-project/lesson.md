## The design

Meera Aunty's quote now says how the customer will pay, and whether they collect from the stall. For Mrs. Joshi, who wants two tote bags and gift-wrapped baby booties delivered in Pune, paid by UPI:

```text
Dhaaga & Co. price quote
BAG x 2: ₹1,798.00
BTY x 1: ₹350.00
Extras: ₹30.00
Subtotal: ₹2,178.00
Diwali offer: -₹217.80
Delivery: FREE
Total: ₹1,960.20
Payment: UPI
```

The rules are the ones from the web-03 calculator, all in whole paise:

- **Items:** each line costs its price times its quantity.
- **Extras:** per item, gift wrap adds ₹30 and a stitched name adds ₹50.
- **Subtotal:** items plus extras.
- **Diwali offer:** 10% off the subtotal, rounded down, only when it's above ₹1,500.
- **Delivery:** a pickup from the stall is free. In Pune, it's ₹60, and free when the subtotal is above ₹999.
- **Total:** subtotal, minus the offer, plus delivery.

What's new is that the types now describe all of it, across four modules:

| File | Holds |
|---|---|
| `prices.ts` | `ITEM_CODES` and `PRICES`, written once `as const`, and `ItemCode` taken from them. The fees as constants. |
| `types.ts` | `PaymentMode` and `Delivery` as literal unions. `CartLine` with optional `giftWrap` and `name`. `Order` and `Quote`. |
| `money.ts` | `formatRupees`, already done. |
| `calculator.ts` | The seven functions you write. |

Watch how the types stop a bad quote long before Meera Aunty sees it:

```animate
title: One typo, caught across four files
kind: scene
nodes:
  - { id: prices, label: prices.ts, emoji: "💰", value: "5 codes" }
  - { id: types, label: types.ts, emoji: "🏷️" }
  - { id: calc, label: calculator.ts, emoji: "🧮" }
  - { id: tsc, label: tsc, emoji: "🔍", value: idle }
  - { id: meera, label: Meera Aunty's quote, emoji: "📱" }
steps:
  - say: ItemCode is worked out from ITEM_CODES in prices.ts. CartLine in types.ts uses it for its code.
    send: { from: prices, to: types, label: "import type { ItemCode }" }
  - say: calculator.ts imports the types and the prices. A test order has a line with the code "BGA".
    send: [{ from: types, to: calc, label: "CartLine, Order, Quote" }, { from: prices, to: calc, label: "PRICES" }]
    set: { calc: "code: \"BGA\"" }
  - say: tsc reads all four files together. "BGA" isn't an ItemCode, so it reports the line before anything runs.
    send: { from: calc, to: tsc, label: "npm run typecheck" }
    set: { tsc: "1 error" }
  - say: The typo is fixed to "BAG". PRICES has a key for every ItemCode, so the lookup can never give undefined.
    send: { from: tsc, to: calc, label: "fix", back: true }
    set: { calc: "code: \"BAG\"", tsc: "0 errors" }
  - say: The quote goes out with the right price. A delivery of "Pune" with a capital P, or a payment of "GPay", would have been stopped the same way.
    send: { from: calc, to: meera, label: receipt(order) }
    set: { meera: "₹1,960.20" }
```

A smaller order, a coaster set and two keychains with "Riya" stitched on, delivered and paid in cash, has no offer row:

```text
Dhaaga & Co. price quote
CST x 1: ₹150.00
KEY x 2: ₹240.00
Extras: ₹100.00
Subtotal: ₹490.00
Delivery: ₹60.00
Total: ₹550.00
Payment: CASH
```

The receipt's rules:

- The first row is always `Dhaaga & Co. price quote`.
- One row per cart line: the code, ` x `, the quantity, `: `, and that line's item cost, without extras.
- `Extras:` and `Subtotal:` always appear, even at ₹0.00.
- `Diwali offer:` appears only when there's a discount, with a `-` before the ₹.
- `Delivery:` says `Pickup at the stall` for a pickup, `FREE` for free delivery in Pune, or `₹60.00`.
- The last row is `Payment:` and the mode. Rows are joined with `"\n"`, with no newline at the end.

## What you build

In `src/calculator.ts`, every import is already written. Fill in:

1. `parseCode(text)` turns WhatsApp text like `" bag "` into an `ItemCode`, or `null`.
2. `itemsTotal(lines)` adds price times quantity.
3. `extrasTotal(lines)` adds the gift wrap and name fees, per item.
4. `diwaliDiscount(subtotal)` is 10% rounded down, above `150000` only.
5. `deliveryFee(subtotal, delivery)` handles `"pickup"` and `"pune"`.
6. `quote(order)` returns a `Quote`.
7. `receipt(order)` returns the text above.

### Syntax: a typed object return

```ts
interface Quote {
  subtotal: number;
  total: number;
}
function smallQuote(subtotal: number): Quote {
  return { subtotal, total: subtotal + 6000 };
}
```

With `Quote` as the return type, TypeScript checks the object you return. Leave out `total`, or misspell `subtotal`, and `tsc` points at the `return` line, so the receipt can never read a missing number.

### Syntax: building text from typed rows

```ts
const rows: string[] = ["Dhaaga & Co. price quote"];
rows.push("Payment: UPI");
const text = rows.join("\n");
```

An empty array needs its type, so start with `const rows: string[]`. Push each row, then join once at the end. `join` puts `"\n"` only between rows, never after the last one.

## Recap

- Shared types live in their own module and are imported with `import type`.
- `as const` lists give literal unions like `ItemCode`, so a typo in a code is a type error.
- Optional properties (`giftWrap?`, `name?`) read as `undefined` when they're missing, which is falsy.
- Literal unions like `"pickup" | "pune"` and `"CASH" | "UPI" | "CARD"` keep every choice spelt right.
- A declared return type, like `Quote`, makes TypeScript check every property you return.

## Common mistakes

**1. Charging extras once per line.** Two wrapped booties need two wraps. Multiply each item's extras by the line's `qty`.

**2. Free delivery at exactly ₹999.** It's free only **above** ₹999, so a subtotal of `99900` still pays `6000`. The same goes for the offer: exactly `150000` gets no discount.

**3. Printing the offer row every time.** A `Diwali offer: -₹0.00` row confuses customers. Push it only when the discount is above 0.

**4. Treating a pickup like a free delivery.** A pickup's row says `Pickup at the stall`, not `FREE`. Check `order.delivery` first, then look at the fee.
