## The design

Meera Aunty wants a quote she can paste into a WhatsApp chat. For a customer who wants two tote bags, plus baby booties in gift wrap, it looks like this:

```text
Dhaaga & Co. price quote
BAG x 2: ₹1,798.00
BTY x 1: ₹350.00
Extras: ₹30.00
Subtotal: ₹2,178.00
Diwali offer: -₹217.80
Delivery: FREE
Total: ₹1,960.20
```

A cart is an array of lines. Each line is an object like `{ code: "BTY", qty: 1, giftWrap: true }`. A line may also have a `name` to stitch on. A line without `giftWrap` or `name` has no extras.

The rules, all in whole paise:

- **Items:** each line costs its price times its quantity.
- **Extras:** for each item, gift wrap adds ₹30, and a name stitched on adds ₹50. Two wrapped booties pay for two wraps.
- **Subtotal:** items plus extras.
- **Diwali offer:** 10% off the subtotal, only when it's above ₹1,500, rounded down to whole paise.
- **Delivery:** ₹60, but free when the subtotal is above ₹999.
- **Total:** subtotal, minus the offer, plus delivery.

A smaller order, a coaster set and two keychains with "Riya" stitched on, gets no offer and pays for delivery:

```text
Dhaaga & Co. price quote
CST x 1: ₹150.00
KEY x 2: ₹240.00
Extras: ₹100.00
Subtotal: ₹490.00
Delivery: ₹60.00
Total: ₹550.00
```

The receipt's rules:

- The first row is always `Dhaaga & Co. price quote`.
- One row per cart line: the code, ` x `, the quantity, `: `, and that line's item cost without extras.
- `Extras:` and `Subtotal:` rows always appear, even when the extras are ₹0.00.
- The `Diwali offer:` row appears only when there's a discount, with a `-` before the ₹.
- `Delivery:` shows `FREE` or `₹60.00`.
- Rows are joined with `"\n"`, with no newline after the `Total:` row.

```callout tip
This feature uses nearly every tool from the project: objects, arrays, loops, if, the ternary, functions, template literals and the rupee formatter.
```

## What you build

`src/calculator.js` already has the price list, the fees and a rupee formatter called `inr`. Fill in seven functions:

1. `formatRupees(paise)` shows paise as rupees, like `"₹1,299.00"`.
2. `itemsTotal(lines)` adds up price times quantity for every line.
3. `extrasTotal(lines)` adds up the gift wrap and name fees, for every item.
4. `diwaliDiscount(subtotal)` is 10% rounded down, above `150000` paise only.
5. `deliveryFee(subtotal)` is `0` above `99900` paise, and `6000` otherwise.
6. `quote(lines)` returns `{ items, extras, subtotal, discount, delivery, total }`.
7. `receipt(lines)` returns the quote text shown above.

### Syntax: an optional property

```js
const wrap = line.giftWrap ? GIFT_WRAP : 0;
```

When a line has no `giftWrap` property, reading it gives `undefined`, which is falsy. So the same check works for `true`, `false` and a missing property. The same goes for `name`: `""` and `undefined` are both falsy.

### Syntax: building text row by row

```js
const rows = [];
rows.push("Dhaaga & Co. price quote");
const text = rows.join("\n");
```

Push each row into an array, then join them with `"\n"` at the end. `join` only puts the newline **between** rows, so there's never one after the last row. Watch it:

```animate
title: Rows into one message
kind: code
language: js
code: |
  const rows = [];
  rows.push("Dhaaga & Co. price quote");
  rows.push("Total: ₹550.00");
  const text = rows.join("\n");
  console.log(text);
  console.log(rows.length);
steps:
  - line: 1
    objects: { r1: { type: Array, fields: {} } }
    vars: { rows: "@r1" }
    say: An empty array to collect the rows of the quote.
  - line: 2
    objects: { r1: { type: Array, fields: { "0": '"Dhaaga & Co. price quote"' } } }
    say: The heading row goes in first.
  - line: 3
    objects: { r1: { type: Array, fields: { "0": '"Dhaaga & Co. price quote"', "1": '"Total: ₹550.00"' } } }
    say: In the real receipt, many rows go in between. Here the total row comes next.
  - line: 4
    vars: { text: '"Dhaaga & Co. price quote\nTotal: ₹550.00"' }
    say: join glues the rows into one string, with a newline between each pair and none at the end.
  - line: 5
    out: "Dhaaga & Co. price quote\nTotal: ₹550.00"
    say: One string, printed as two lines, ready to paste into WhatsApp.
  - line: 6
    out: "2"
    say: The array still holds its two rows. join made a new string and changed nothing.
```

### Syntax: returning an object

```js
function quote(lines) {
  const items = 0;
  const extras = 0;
  return { items, extras };
}
```

A function can hand back several answers at once inside an object. With shorthand properties, each box's name becomes its key.

## Recap

- Split the work into small functions that each do one job, and build `quote` from them.
- Every sum is in whole paise, and the formatter is used only for the receipt text.
- A missing optional property is `undefined`, which is falsy.
- Collect rows in an array, then `join("\n")` once at the end.

## Common mistakes

**1. Charging extras once per line.** Two wrapped booties need two wraps. Multiply the extras for one item by the line's `qty`.

**2. A newline after the last row.** Adding `"\n"` after every row leaves one dangling at the end, and the checks compare the text exactly. Let `join("\n")` place the newlines.

**3. Free delivery at exactly ₹999.** Delivery is free only **above** ₹999, so a subtotal of `99900` still pays `6000`.

**4. Always printing the offer row.** A `Diwali offer: -₹0.00` row confuses customers. Push it only when the discount is above 0.
