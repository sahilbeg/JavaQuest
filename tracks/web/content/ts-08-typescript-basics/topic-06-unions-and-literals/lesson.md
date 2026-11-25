## Hook

The payment box at the counter has three slots: cash, UPI and card. There's no fourth slot for "GPay" or "upi" or "online". A payment goes into one of the three, or it isn't a payment Dhaaga knows how to count.

Some values in code are like that. They aren't "any text": they're one of a short list of choices. TypeScript can write that list down, and then it refuses everything else.

## Concept

### Syntax: union types

```ts
let orderId: string | number;
```

The `|` means "or". `string | number` is a **union type**: the box may hold text or a number. Old WhatsApp orders are numbered `17`, and the website says `"order-17"`, so an order id can be either:

```ts
let orderId: string | number = 17;
orderId = "order-17";
orderId = true;
```

```text
error TS2322: Type 'boolean' is not assignable to type 'string | number'.
```

Lines 1 and 2 are fine. Line 3 is neither text nor a number.

```callout jargon
A **union type** like `string | number` allows a value of any one of its members. Each type in the list is a **member** of the union.
```

### You can only use what every member has

When a value might be text or a number, TypeScript only lets you do what works for **both**:

```ts
function shout(id: string | number): string {
  return id.toUpperCase();
}
```

```text
error TS2339: Property 'toUpperCase' does not exist on type 'string | number'.
  Property 'toUpperCase' does not exist on type 'number'.
```

Numbers have no `toUpperCase`, and `id` might be a number. Something both members can do is fine, like `String(id)`, which turns either one into text. To use what only one member has, check which one you've got first. That's the next ticket.

### Syntax: literal types

```ts
let mode: "UPI";
```

A string in quotes can be a type of its own. `"UPI"` as a type allows exactly one value: the text `"UPI"`. Numbers work too: `let qty: 1;` allows only `1`.

On its own, a literal type isn't very useful. Its power shows up in a union.

```callout jargon
A **literal type** is a type that allows exactly one value, like `"UPI"` or `1`.
```

### Syntax: a union of literals

```ts
let mode: "CASH" | "UPI" | "CARD";
```

A union of literal types is a fixed list of choices. Now only three values fit:

```ts
function modeLabel(mode: "CASH" | "UPI" | "CARD"): string {
  return mode === "CASH" ? "Cash" : mode;
}
modeLabel("UPI");
modeLabel("GPAY");
modeLabel("upi");
```

```text
error TS2345: Argument of type '"GPAY"' is not assignable to parameter of type '"CARD" | "CASH" | "UPI"'.
error TS2345: Argument of type '"upi"' is not assignable to parameter of type '"CARD" | "CASH" | "UPI"'.
```

Line 4 is fine. Lines 5 and 6 are refused: an unknown mode, and a known mode with the wrong case. In VS Code, typing `modeLabel("` also lists the three choices for you.

### Literal unions as keys

A value from a literal union can pick a property, as long as the object has a property for every member. Watch yarn being counted by colour:

```animate
title: Counting grams by colour
kind: code
language: ts
code: |
  const used: { colour: "Marigold" | "Indigo"; grams: number }[] = [
    { colour: "Indigo", grams: 40 },
    { colour: "Marigold", grams: 25 },
    { colour: "Indigo", grams: 40 },
  ];
  const grams = { Marigold: 0, Indigo: 0 };
  for (const u of used) {
    grams[u.colour] += u.grams;
  }
  console.log(grams.Marigold, grams.Indigo);
steps:
  - line: 1
    say: Each entry's colour can only be Marigold or Indigo. Any other colour would be a type error.
  - line: 6
    objects: { g1: { type: Object, fields: { Marigold: 0, Indigo: 0 } } }
    vars: { grams: "@g1" }
    say: One counter per colour. The object has a key for every member of the union.
  - line: 7
    vars: { u: '{ colour: "Indigo", grams: 40 }' }
    say: The first entry is Indigo.
  - line: 8
    objects: { g1: { type: Object, fields: { Marigold: 0, Indigo: 40 } } }
    say: u.colour picks the Indigo key. TypeScript knows it exists, because both colours have a key.
  - line: 7
    vars: { u: '{ colour: "Marigold", grams: 25 }' }
    say: The next entry is Marigold.
  - line: 8
    objects: { g1: { type: Object, fields: { Marigold: 25, Indigo: 40 } } }
    say: This time u.colour picks Marigold.
  - line: 8
    vars: { u: '{ colour: "Indigo", grams: 40 }' }
    objects: { g1: { type: Object, fields: { Marigold: 25, Indigo: 80 } } }
    say: The last entry adds another 40 g of Indigo.
  - line: 10
    vars: { u: ~ }
    out: "25 80"
    say: Every gram went to the right colour, and no colour can be misspelt.
```

### const, let and literal types

TypeScript picks a literal type when a value can never change, and a wider type when it can:

```ts
const fixed = "UPI";
let changing = "UPI";
```

`fixed` gets the type `"UPI"`, because a `const` can never hold anything else. `changing` gets the type `string`, because a `let` could later hold any text. This is called **widening**, and it matters when you pass the box on:

```ts
function modeLabel(mode: "CASH" | "UPI" | "CARD"): string {
  return mode;
}
let changing = "UPI";
modeLabel(changing);
```

```text
error TS2345: Argument of type 'string' is not assignable to parameter of type '"CARD" | "CASH" | "UPI"'.
```

Use `const`, or give the `let` the union type: `let changing: "CASH" | "UPI" | "CARD" = "UPI";`.

```callout jargon
**Widening** is TypeScript choosing a wider type, like `string`, instead of a literal type, like `"UPI"`, for a box that might change later.
```

## Worked example

Delivery and order references, with unions doing the checking:

```ts run
function deliveryFee(method: "pickup" | "pune", subtotal: number): number {
  if (method === "pickup") {
    return 0;
  }
  return subtotal > 99900 ? 0 : 6000;
}

function orderRef(id: string | number): string {
  return `#${String(id).padStart(4, "0")}`;
}

const method = "pune";
console.log(orderRef(17), deliveryFee(method, 50000));
console.log(orderRef("230"), deliveryFee("pickup", 50000));
```

1. `method` can only be `"pickup"` or `"pune"`. A typo like `"Pune"` is refused before it runs.
2. `orderRef` takes a number or text. `String(id)` works on both, so it's allowed. Then `padStart` fills it to four characters.
3. `const method = "pune"` has the literal type `"pune"`, so it fits the parameter.
4. It prints `#0017 6000`, then `#0230 0`. A pickup never pays delivery.

## Try it

Run this. Then change `const` to `let` on the first line. The run still works, because Node strips the types. Which line would `tsc` now refuse, and why?

```ts run
const mode = "UPI";
function modeLabel(m: "CASH" | "UPI" | "CARD"): string {
  return m === "CASH" ? "Cash" : m === "UPI" ? "UPI" : "Card";
}
console.log(modeLabel(mode));
```

## Recap

- A union type like `string | number` allows a value of any one of its members.
- With a union, you can only use what every member has, like `String(id)`.
- A literal type like `"UPI"` allows exactly that one value.
- A union of literals, like `"CASH" | "UPI" | "CARD"`, is a fixed list of choices, and TypeScript refuses anything else.
- `const` keeps a literal type, while `let` widens it to `string`.

## Common mistakes

**1. Using a method only one member has.**

```ts
function shout(id: string | number): string {
  return id.toUpperCase();
}
```

```text
error TS2339: Property 'toUpperCase' does not exist on type 'string | number'.
  Property 'toUpperCase' does not exist on type 'number'.
```

Use something both members can do, like `String(id).toUpperCase()`, or check the type first, as in the next ticket.

**2. Passing a value that isn't one of the choices.**

```ts
function modeLabel(mode: "CASH" | "UPI" | "CARD"): string {
  return mode;
}
modeLabel("upi");
```

```text
error TS2345: Argument of type '"upi"' is not assignable to parameter of type '"CARD" | "CASH" | "UPI"'.
```

Literal types match exactly, case included. Clean up typed text before it becomes a mode, for example with `trim().toUpperCase()` and a check.

**3. Losing the literal type with let.**

```ts
function modeLabel(mode: "CASH" | "UPI" | "CARD"): string {
  return mode;
}
let mode = "CARD";
modeLabel(mode);
```

```text
error TS2345: Argument of type 'string' is not assignable to parameter of type '"CARD" | "CASH" | "UPI"'.
```

`let` widens `"CARD"` to `string`. Use `const mode = "CARD";`, or write the union on the `let`.

**4. Comparing with a value that can never match.**

```ts
function isUpi(mode: "CASH" | "UPI" | "CARD"): boolean {
  return mode === "Upi";
}
```

```text
error TS2367: This comparison appears to be unintentional because the types '"CARD" | "CASH" | "UPI"' and '"Upi"' have no overlap.
```

In JavaScript, this would quietly return `false` forever. TypeScript knows `mode` can never be `"Upi"`. Fix the spelling: `mode === "UPI"`.
