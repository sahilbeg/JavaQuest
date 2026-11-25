## Hook

A parcel arrives at the counter. Before Raju opens it, he reads the label. Is it a return from a customer, or yarn from the supplier? What he does next depends on the answer.

A union is a parcel with two possible labels. TypeScript won't let you open it until you've checked which one it is. The good news is that the check is ordinary JavaScript you already know: `typeof`, `===`, `in`, `instanceof`.

## Concept

```callout jargon
**Narrowing** means checking a value so TypeScript can tell which member of a union it is. After the check, TypeScript treats the value as that narrower type.
```

### Syntax: typeof checks

```ts
function show(id: string | number) {
  if (typeof id === "number") {
    return id.toFixed(0);
  }
  return id.toUpperCase();
}
```

`typeof` from web-03 gives `"string"`, `"number"`, `"boolean"` and a few more. TypeScript reads your `if` and narrows the type inside it. Watch the type of `id` change as the code flows:

```animate
title: typeof narrows a union, path by path
kind: code
language: ts
code: |
  function orderLabel(id: string | number): string {
    if (typeof id === "number") {
      return "order-" + id.toFixed(0);
    }
    return id.toLowerCase();
  }
  console.log(orderLabel(17));
  console.log(orderLabel("ORDER-18"));
steps:
  - line: 7
    say: The first call passes a number, an old WhatsApp order id.
  - line: 1
    vars: { id: 17 }
    say: At the top, id is string | number. You may only use what both have.
  - line: 2
    say: typeof id is "number", so the check is true.
  - line: 3
    say: Inside this if, TypeScript knows id is a number. toFixed is allowed here, and toLowerCase isn't.
  - line: 7
    vars: { id: ~ }
    out: "order-17"
    say: The number path returns its label.
  - line: 1
    vars: { id: '"ORDER-18"' }
    say: The second call passes text from the website.
  - line: 5
    say: The check was false, so the if's return didn't run. Any number would have left already, so here id must be a string.
  - line: 8
    vars: { id: ~ }
    out: "order-18"
    say: The string path lower-cases it. Each path only used what its type has.
```

The `return` inside the `if` matters. Because numbers leave the function there, everything after the `if` only ever sees strings. TypeScript follows these paths for you.

```callout jargon
**Control flow analysis** is TypeScript following each path through your code, through every `if`, `return` and `else`, to know a value's type at each line.
```

### Checking for null

`null` means "no value, on purpose". In a union like `string | null`, compare with `===` to narrow:

```ts run
function stitchLabel(name: string | null): string {
  if (name === null) {
    return "No name";
  }
  return `Stitch: ${name.toUpperCase()}`;
}
console.log(stitchLabel("Riya"));
console.log(stitchLabel(null));
```

It prints `Stitch: RIYA`, then `No name`. After the `if`, `name` can only be a `string`.

You can also narrow with a **truthiness** check, `if (name)`. It removes `null` too, but it also removes the empty string `""`, because `""` is falsy. Sometimes that's what you want, and sometimes it isn't. With numbers, it treats `0` as "missing", which is usually a bug.

```callout jargon
A **truthy** value counts as true in an `if`. The **falsy** values are `false`, `0`, `""`, `null`, `undefined` and `NaN`. Everything else is truthy.
```

### Syntax: the in operator

```ts
if ("stall" in order) {
}
```

`"stall" in order` is `true` when the object has a `stall` property. It tells two object shapes apart:

```ts run
function where(order: { stall: string } | { address: string }): string {
  if ("stall" in order) {
    return `Pick up at ${order.stall}`;
  }
  return `Deliver to ${order.address}`;
}
console.log(where({ stall: "FC Road" }));
console.log(where({ address: "Aundh, Pune" }));
```

It prints `Pick up at FC Road`, then `Deliver to Aundh, Pune`. Inside the `if`, `order` is the `{ stall: string }` shape. After it, `order` is the other one.

### Syntax: instanceof and Array.isArray

```ts
if (when instanceof Date) {
}
```

`instanceof` checks whether an object was made by a class, like `Date`. It narrows to that class:

```ts run
function yearOf(when: Date | number): number {
  if (when instanceof Date) {
    return when.getUTCFullYear();
  }
  return new Date(when).getUTCFullYear();
}
console.log(yearOf(new Date("2026-11-08T00:00:00Z")), yearOf(0));
```

It prints `2026 1970`. A number here is a timestamp in milliseconds, and `0` is the very start of 1970.

Arrays need their own check. `typeof` says `"object"` for an array, which doesn't tell it apart from other objects. Use `Array.isArray`:

```ts run
function colourList(colours: string | string[]): string {
  return Array.isArray(colours) ? colours.join(" + ") : colours;
}
console.log(colourList(["Indigo", "Mint"]));
console.log(colourList("Marigold"));
```

It prints `Indigo + Mint`, then `Marigold`. Narrowing works in a ternary too.

## Worked example

WhatsApp orders arrive messy: the id might be a number or text, there might be one item or a list, the note might be `null`, and the order might be a pickup or a delivery.

```ts run
interface WhatsAppOrder {
  id: string | number;
  items: string | string[];
  note: string | null;
  to: { stall: string } | { address: string };
}

function summary(order: WhatsAppOrder): string {
  const id = typeof order.id === "number" ? `order-${order.id}` : order.id;
  const count = Array.isArray(order.items) ? order.items.length : 1;
  const place = "stall" in order.to ? order.to.stall : order.to.address;
  const note = order.note === null ? "" : ` (${order.note})`;
  return `${id} to ${place}, items: ${count}${note}`;
}

console.log(summary({ id: 17, items: ["AMI", "KEY"], note: null, to: { stall: "FC Road" } }));
console.log(summary({ id: "order-18", items: "BAG", note: "Ring twice", to: { address: "Aundh" } }));
```

1. `typeof` turns a number id into `order-17` and keeps text ids as they are.
2. `Array.isArray` counts a list by its length, and a single code as one item.
3. `in` picks the stall or the address. Narrowing works on `order.to`, a property, as well as on plain variables.
4. `=== null` leaves out a missing note, so no slip ever says `(null)`.
5. It prints `order-17 to FC Road, items: 2`, then `order-18 to Aundh, items: 1 (Ring twice)`.

## Try it

Run this. Then add a third member to the union, `boolean`, where `true` means "the customer asked for gift wrap". Which line does TypeScript need you to change first?

```ts run
function wrapText(wrap: string | null): string {
  if (wrap === null) {
    return "No wrap";
  }
  return `Wrap in ${wrap}`;
}
console.log(wrapText("Marigold paper"));
console.log(wrapText(null));
```

## Recap

- Narrowing is checking which member of a union a value is. After the check, TypeScript uses the narrower type.
- `typeof x === "number"` narrows to a primitive, and `x === null` removes `null`.
- `"stall" in order` tells object shapes apart, and `instanceof Date` checks for a class.
- `Array.isArray` is the way to tell an array from other values.
- TypeScript follows `if`, `return` and `else`, so after an early return, the rest of the function has the narrower type.

## Common mistakes

**1. Using a member before checking which one you have.**

```ts
function colourList(colours: string | string[]): string {
  return colours.join(" + ");
}
```

```text
error TS2339: Property 'join' does not exist on type 'string | string[]'.
  Property 'join' does not exist on type 'string'.
```

`colours` might be a single string. Check first: `Array.isArray(colours) ? colours.join(" + ") : colours`.

**2. Forgetting that a value might be null.**

```ts
function noteText(message: string | null): string {
  return message.trim();
}
```

```text
error TS18047: 'message' is possibly 'null'.
```

This is the "Note: null" bug, caught before it prints. Handle `null` first: `if (message === null) return "No note";`.

**3. Checking for an array with typeof.**

```ts
function count(items: string | string[]): number {
  if (typeof items === "array") {
    return items.length;
  }
  return 1;
}
```

```text
error TS2367: This comparison appears to be unintentional because the types '"bigint" | "boolean" | "function" | "number" | "object" | "string" | "symbol" | "undefined"' and '"array"' have no overlap.
```

`typeof` never gives `"array"`. Use `Array.isArray(items)`.

**4. Using truthiness on a number that can be 0.**

```ts
function qtyText(qty: number | null): string {
  return qty ? `${qty} items` : "No quantity";
}
```

TypeScript is happy, but `qtyText(0)` says `No quantity`, because `0` is falsy. Check exactly what you mean, `qty === null`, so that `0` still prints `0 items`.
