## Hook

When a delivery of yarn reaches the Aundh shop, Raju doesn't put the box straight on the shelf. He opens it, counts the balls, checks the colours against the order, and weighs one. Only then does it go into stock.

The label on the box said "200 g of Mint". But labels can be wrong, and a wrong box on the shelf causes trouble for weeks.

Data from outside your code is that box. Types are only the label. This ticket is about opening the box and checking.

## Concept

### Types vanish, data doesn't obey them

TypeScript checks your code before it runs. It can't check what a server, a file or a person sends while it runs. `JSON.parse` returns `any`, so whatever arrives is believed.

```ts run
interface Item {
  id: string;
  paise: number;
}
const item: Item = JSON.parse('{ "id": "DC-1042", "paise": "45000" }');
const withGiftWrap: number = item.paise + 3000;
console.log(withGiftWrap);
```

It prints `450003000`. The type says `paise` is a number, but the file held text, so `+` joined two strings. TypeScript never saw a problem.

### Syntax: unknown

```ts
const data: unknown = JSON.parse(text);
```

`unknown` is the safe opposite of `any`. A value of type `unknown` can be anything, so TypeScript lets you do nothing with it until you've checked what it is.

```callout jargon
**unknown** is the type of a value you know nothing about yet. You can store it and pass it around, but you must narrow it with checks before you read fields or do maths with it.
```

```ts
const data: unknown = JSON.parse('{ "paise": 45000 }');
console.log(data.paise);
```

```text
error TS18046: 'data' is of type 'unknown'.
```

That error is the point. It forces the check you'd otherwise forget.

### Syntax: checking for an object

```ts
function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}
```

To read fields, first check you have a plain object. `typeof` says `"object"` for `null` and for arrays too, so both need ruling out.

`Record<string, unknown>` means "an object with any keys, whose values are still unknown". After `isRecord(data)`, you may read `data.paise`, but its type is `unknown`, so you check it next.

### Checking each field

Narrow each field with the guards you already know. Once a check passes, TypeScript knows the field's real type.

```ts run
function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function priceFrom(data: unknown): number | null {
  if (!isRecord(data)) {
    return null;
  }
  const paise = data.paise;
  if (typeof paise !== "number" || !Number.isInteger(paise) || paise < 0) {
    return null;
  }
  return paise;
}

console.log(priceFrom(JSON.parse('{ "paise": 45000 }')));
console.log(priceFrom(JSON.parse('{ "paise": "45000" }')));
console.log(priceFrom(JSON.parse("[45000]")));
```

It prints `45000`, `null` and `null`. Text and arrays are turned away at the door.

```callout jargon
**Validating** data means checking, while the code runs, that it really has the shape your types describe. Code that does it is a **validator**.
```

### Syntax: a result type

```ts
type ParseResult<T> = { ok: true; value: T } | { ok: false; error: string };
```

A validator should say *what* was wrong, not just give up. A result type is a discriminated union: either the checked value, or a message. It's generic, so one type works for items, orders and anything else.

Checking `result.ok` narrows it. Only after `if (result.ok)` can you read `result.value`.

### Check at the edge, trust inside

Check data once, where it enters your app: the `fetch` reply, the uploaded file, the form. After that, the rest of the code can trust the types, because nothing unchecked got in.

```animate
title: Bad data is stopped at the door
kind: scene
nodes:
  - { id: file, label: WhatsApp import, emoji: "📄" }
  - { id: check, label: parseItems, emoji: "🛂", value: waiting }
  - { id: store, label: Store code, emoji: "🛒", value: "0 items" }
  - { id: owner, label: Raju, emoji: "🙋" }
steps:
  - say: The import file arrives as text. Its type, after JSON.parse, is unknown.
    send: { from: file, to: check, label: "JSON text" }
    set: { check: checking }
  - say: Item 1, Golu, has a string id like DC-1042, a name and whole-number paise. It passes.
    set: { check: "item 1 ok" }
  - say: Item 2 has "paise" as the text "45000". The validator stops here instead of guessing.
    set: { check: "item 2 bad" }
    focus: [check]
  - say: The store gets nothing from this file, so no cart can show a ₹45,00,030.00 elephant.
    focus: [store]
  - say: Raju gets a message that says exactly what to fix.
    send: { from: check, to: owner, label: "item 2: paise must be a whole number" }
  - say: The fixed file passes every check. Now the store code can trust every field's type.
    send: { from: check, to: store, label: "Item[]" }
    set: { check: "all ok", store: "12 items" }
```

## Worked example

The cart is saved in the browser as JSON, and a customer's old cart may be broken. Here is a validator for one cart line.

```ts run
type ParseResult<T> = { ok: true; value: T } | { ok: false; error: string };
type CartLine = { itemId: string; qty: number };

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function parseLine(value: unknown): ParseResult<CartLine> {
  if (!isRecord(value)) {
    return { ok: false, error: "not an object" };
  }
  const { itemId, qty } = value;
  if (typeof itemId !== "string") {
    return { ok: false, error: "itemId must be text" };
  }
  if (typeof qty !== "number" || !Number.isInteger(qty) || qty < 1) {
    return { ok: false, error: "qty must be a whole number of 1 or more" };
  }
  return { ok: true, value: { itemId, qty } };
}

for (const text of ['{ "itemId": "DC-1042", "qty": 2 }', '{ "itemId": "DC-1042", "qty": 0 }', "null"]) {
  const result = parseLine(JSON.parse(text));
  console.log(result.ok ? `${result.value.qty} x ${result.value.itemId}` : `Broken line: ${result.error}`);
}
```

Line by line:

1. `parseLine` takes `unknown`, so callers can pass anything, and must get a `ParseResult<CartLine>` back.
2. `isRecord` rules out `null`, arrays and plain values. After it, the fields can be read, as `unknown`.
3. Each field is narrowed with `typeof`, then checked for real-world rules: a quantity is a whole number of at least 1.
4. The final object is built from the checked variables, so any extra fields in the JSON are left behind.
5. It prints `2 x DC-1042`, then `Broken line: qty must be a whole number of 1 or more`, then `Broken line: not an object`.

## Try it

Add a check that `itemId` looks like `DC-` followed by digits, with `/^DC-\d+$/.test(itemId)`. Then try the line `{ "itemId": "1042", "qty": 1 }`.

```ts run
const data: unknown = JSON.parse('{ "itemId": "DC-1042", "qty": 2 }');
if (typeof data === "object" && data !== null && "qty" in data) {
  console.log(data.qty);
}
```

## Recap

- Types are checked before the code runs. Data from outside isn't, so it can break the promises your types make.
- Give outside data the type `unknown`, not `any`, so TypeScript makes you check it before you use it.
- Rule out `null` and arrays before reading fields, then narrow each field with `typeof`, `Number.isInteger` and your own rules.
- Return a result type, `{ ok: true; value } | { ok: false; error }`, so the caller learns what was wrong.
- Validate once, at the edge where data enters. Inside, the types can then be trusted.

## Common mistakes

**1. Writing as Item instead of checking.**

```ts
const item = JSON.parse(text) as Item;
```

It compiles, and nothing is checked. A price that arrives as text still breaks the total. Fix: give the parsed value the type `unknown`, and pass it to a validator.

**2. Forgetting that typeof null is "object".**

```ts run crash
const data: unknown = JSON.parse("null");
if (typeof data === "object") {
  console.log((data as { paise: number }).paise);
}
```

Node stops with `TypeError: Cannot read properties of null (reading 'paise')`. Fix: check `data !== null` too, as `isRecord` does.

**3. Trusting typeof number for prices.**

```ts run
const paise: unknown = 450.5;
console.log(typeof paise === "number");
```

It prints `true`, but a price in paise must be a whole number. `NaN` and `Infinity` are numbers too. Fix: `typeof paise === "number" && Number.isInteger(paise) && paise >= 0`. Keep the `typeof` check: it is what tells TypeScript that `paise` is a number.
