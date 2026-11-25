## Hook

The custom-order form has boxes everyone fills in, like the item and how many. It also has boxes most people leave empty: gift wrap, a name to stitch on, a delivery address for people who don't collect from the stall.

Code has to deal with those empty boxes every day. This ticket gives types short names, marks which properties may be missing, and shows two small operators that handle "missing" without a crash.

## Concept

### Syntax: type aliases

```ts
type ItemCode = "CST" | "KEY" | "AMI" | "BAG" | "BTY";
```

`type` gives any type a name, called a **type alias**. Writing `"CST" | "KEY" | "AMI" | "BAG" | "BTY"` in ten places is long and easy to get wrong. With an alias, you write it once and use `ItemCode` everywhere:

```ts
type ItemCode = "CST" | "KEY" | "AMI" | "BAG" | "BTY";
type PaymentMode = "CASH" | "UPI" | "CARD";
type Line = [code: ItemCode, qty: number];
type OrderLine = {
  code: ItemCode;
  qty: number;
};
```

An alias doesn't make a new type. It's only a name for one, the way "Golu" is a name for one particular elephant.

```callout jargon
A **type alias** is a name for a type, made with `type Name = ...;`. It can name any type: a union, a tuple, an object type, even `number`.
```

### type or interface?

For object shapes, both work, and they mean almost the same thing:

```ts
interface ItemA {
  id: string;
  paise: number;
}
type ItemB = {
  id: string;
  paise: number;
};
```

The differences that matter today:

- Only `type` can name a union, a tuple or a primitive, like `type ItemCode = "CST" | "KEY"`.
- Only `interface` can use `extends`. A `type` can combine shapes with `&` instead: `type Amigurumi = Item & { animal: string };`.

Most teams pick one style for object shapes and stick to it. Dhaaga's code uses `type` for orders and `interface` for catalogue items, and that's fine.

### Syntax: optional properties

```ts
type OrderLine = {
  code: string;
  qty: number;
  giftWrap?: boolean;
};
```

A `?` after a property name makes it **optional**: an object may leave it out. Both of these are `OrderLine`s:

```ts
type OrderLine = {
  code: string;
  qty: number;
  giftWrap?: boolean;
};
const plain: OrderLine = { code: "KEY", qty: 1 };
const wrapped: OrderLine = { code: "BTY", qty: 2, giftWrap: true };
```

When you read an optional property, its type includes `undefined`: `plain.giftWrap` is `boolean | undefined`, and here it's `undefined`. `undefined` is falsy, so `line.giftWrap ? 3000 : 0` works for `true`, `false` and missing.

```callout jargon
An **optional property** is one an object may leave out, marked with `?`. Reading a missing one gives `undefined`. `null` means "no value, on purpose", and `undefined` usually means "never given".
```

### Syntax: optional chaining

```ts
const city = customer.address?.city;
```

When the address itself is optional, `customer.address.city` would crash for a customer with no address. TypeScript doesn't let you try:

```text
error TS18048: 'customer.address' is possibly 'undefined'.
```

`?.` reads the next property only if the value before it exists. If it's `undefined` or `null`, the whole expression stops and gives `undefined`, with no crash.

```callout jargon
**Optional chaining**, `?.`, reads a property only when the value before it isn't `null` or `undefined`. Otherwise it gives `undefined`.
```

### Syntax: nullish coalescing

```ts
const qty = typedQty ?? 1;
```

`a ?? b` gives `a`, unless `a` is `null` or `undefined`. Then it gives `b`. It's the safe way to fill in a default. The older `||` also gives `b` for **every** falsy value, including `0` and `""`:

```ts run
const cancelled: number | undefined = 0;
const missing: number | undefined = undefined;
console.log(cancelled || 1, cancelled ?? 1);
console.log(missing || 1, missing ?? 1);
```

It prints `1 0`, then `1 1`. With `||`, the cancelled line's `0` became `1`. With `??`, only the missing value did.

```callout jargon
**Nullish** means `null` or `undefined`. The **nullish coalescing** operator, `??`, swaps in a default only for nullish values.
```

Watch both operators look up a yarn supplier's phone, when some suppliers and phones are missing:

```animate
title: ?. stops early, ?? fills the gap
kind: code
language: ts
code: |
  type Yarn = { colour: string; supplier?: { name: string; phone?: string } };
  const indigo: Yarn = { colour: "Indigo", supplier: { name: "Lakshmi Mills", phone: "98765 43210" } };
  const mint: Yarn = { colour: "Mint", supplier: { name: "Pune Wool" } };
  const marigold: Yarn = { colour: "Marigold" };
  console.log(indigo.supplier?.phone ?? "no phone");
  console.log(mint.supplier?.phone ?? "no phone");
  console.log(marigold.supplier?.phone ?? "no phone");
steps:
  - line: 1
    say: A yarn may have a supplier, and a supplier may have a phone. Both are optional.
  - line: 4
    vars: { indigo: '{ supplier: { phone: "98765 43210" } }', mint: '{ supplier: { name: "Pune Wool" } }', marigold: '{ colour: "Marigold" }' }
    say: Indigo has both. Mint's supplier has no phone. Marigold has no supplier at all.
  - line: 5
    out: "98765 43210"
    say: The supplier exists, so ?. reads the phone. It isn't nullish, so ?? keeps it.
  - line: 6
    out: "no phone"
    say: The supplier exists, but the phone is undefined. ?? swaps in the default.
  - line: 7
    out: "no phone"
    say: There's no supplier, so ?. stops at once and gives undefined. Without ?., this line would crash.
```

## Worked example

The top line of a receipt, built from a customer who may or may not have given a phone and an address:

```ts run
type Address = { area: string; city: string; pin?: string };
type Customer = { name: string; phone?: string; address?: Address };

function header(customer: Customer, orderNo?: number): string {
  const no = orderNo ?? 0;
  const phone = customer.phone ?? "no phone";
  const area = customer.address?.area ?? "stall pickup";
  return `#${no} ${customer.name} (${phone}), ${area}`;
}

console.log(header({ name: "Asha", phone: "98765 43210", address: { area: "Aundh", city: "Pune" } }, 17));
console.log(header({ name: "Ravi" }));
```

1. Two aliases name the shapes. `Customer` uses `Address` for its optional `address`.
2. `orderNo` is an optional parameter, so `?? 0` gives a number for the receipt.
3. `customer.phone ?? "no phone"` fills in only a missing phone.
4. `customer.address?.area` stops safely when there's no address, and `??` fills in `"stall pickup"`.
5. It prints `#17 Asha (98765 43210), Aundh`, then `#0 Ravi (no phone), stall pickup`.

## Try it

Run this. Then change `||` to `??` and run it again. Which line changes, and why?

```ts run
type OrderLine = { code: string; qty?: number };
const lines: OrderLine[] = [{ code: "KEY", qty: 0 }, { code: "CST" }, { code: "BAG", qty: 2 }];
for (const line of lines) {
  console.log(line.code, line.qty || 1);
}
```

## Recap

- `type Name = ...` gives any type a name: a union, a tuple or an object shape.
- For object shapes, `type` and `interface` are nearly the same. Only `type` can name unions and tuples.
- `name?: string` is an optional property. Reading a missing one gives `undefined`.
- `a?.b` reads `b` only when `a` exists, and gives `undefined` instead of crashing.
- `a ?? b` uses `b` only when `a` is `null` or `undefined`, so `0` and `""` are kept.

## Common mistakes

**1. Reading through an optional object.**

```ts
type Customer = { name: string; address?: { city: string } };
function cityOf(customer: Customer): string {
  return customer.address.city;
}
```

```text
error TS18048: 'customer.address' is possibly 'undefined'.
```

A customer who collects from the stall has no address. Use `customer.address?.city`, and give a fallback with `??`.

**2. Filling in a default with ||.**

```ts
function qtyOrOne(qty?: number): number {
  return qty || 1;
}
```

TypeScript is happy, but `qtyOrOne(0)` gives `1`. That's how a cancelled keychain gets charged. Use `qty ?? 1`, which only replaces a missing quantity.

**3. Forgetting the ? on a property that can be missing.**

```ts
type OrderLine = { code: string; qty: number; giftWrap: boolean };
const line: OrderLine = { code: "KEY", qty: 1 };
```

```text
error TS2741: Property 'giftWrap' is missing in type '{ code: string; qty: number; }' but required in type 'OrderLine'.
```

If most lines have no gift wrap, the property should be optional: `giftWrap?: boolean`.

**4. Silencing the check with !.**

```ts run crash
type Customer = { name: string; address?: { city: string } };
const ravi: Customer = { name: "Ravi" };
console.log(ravi.address!.city);
```

Node stops with `TypeError: Cannot read properties of undefined (reading 'city')`. A `!` after a value tells TypeScript "trust me, it's there", and the checker stops warning you. Ravi has no address, so it crashes. Use `?.` and `??` instead.
