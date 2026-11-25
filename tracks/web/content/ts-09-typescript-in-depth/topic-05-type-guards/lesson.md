## Hook

At the Aundh shop, a parcel arrives with no label. Raju doesn't guess what's inside. He gives it a shake, checks the weight, peeks under the flap. Only then does he put it on the right shelf.

A **type guard** is that check, written in code. Before you use a value whose type could be several things, you check it, and TypeScript remembers the answer.

## Concept

### Narrowing, one more time

You met narrowing in the last project: after `if (typeof price === "number")`, TypeScript treats `price` as a number inside the `if`.

```callout jargon
A **type guard** is any check that narrows a type: TypeScript understands it and treats the value as the narrower type wherever the check passed.
```

This ticket collects every kind of guard, and shows you how to write your own.

### Syntax: typeof

```ts
if (typeof value === "string") {
  // value is a string here
}
```

`typeof` works for the simple types: `"string"`, `"number"`, `"boolean"`, `"undefined"`, `"object"` and `"function"`.

```ts run
function qtyFrom(input: string | number): number {
  if (typeof input === "string") {
    return Number(input.trim());
  }
  return input;
}
console.log(qtyFrom(" 3 "), qtyFrom(2));
```

It prints `3 2`. Inside the `if`, `input.trim()` is allowed, because `input` is a string there.

### Syntax: instanceof

```ts
if (when instanceof Date) {
  // when is a Date here
}
```

`instanceof` checks whether an object was made by a class, like `Date`, `Error` or one of your own. `typeof` can't tell these apart, because it says `"object"` for all of them.

```ts run
function dayOf(when: Date | string): string {
  if (when instanceof Date) {
    return when.toISOString().slice(0, 10);
  }
  return when.slice(0, 10);
}
console.log(dayOf(new Date("2026-11-14T10:00:00Z")));
console.log(dayOf("2026-11-15 18:30"));
```

It prints `2026-11-14`, then `2026-11-15`.

### Syntax: in

```ts
if ("trackingId" in delivery) {
  // delivery is the member that has trackingId
}
```

`"field" in object` is true when the object has that field. When the members of a union have different fields, `in` narrows to the members that have it.

```ts run
type Courier = { courier: string; trackingId: string };
type Pickup = { pickupAt: string };

function deliveryNote(delivery: Courier | Pickup): string {
  if ("trackingId" in delivery) {
    return `${delivery.courier}, tracking ${delivery.trackingId}`;
  }
  return `Collect from ${delivery.pickupAt}`;
}
console.log(deliveryNote({ courier: "Swift Couriers", trackingId: "SC-88" }));
console.log(deliveryNote({ pickupAt: "the Aundh shop" }));
```

It prints `Swift Couriers, tracking SC-88`, then `Collect from the Aundh shop`. Use `in` when there's no shared tag field to switch on.

### Syntax: your own type guard

```ts
function isPickup(delivery: Courier | Pickup): delivery is Pickup {
  return "pickupAt" in delivery;
}
```

A function that returns a boolean can promise what `true` means. The return type `delivery is Pickup` is a **type predicate**. Wherever `isPickup(d)` is true, TypeScript treats `d` as a `Pickup`.

```callout jargon
A **type predicate** is a return type of the form `param is Type`. It turns a boolean function into a type guard you can reuse, give a name to, and test.
```

### Guards and filter

`filter` understands type predicates. Give it a guard, and the array that comes out has the narrower type.

```animate
title: A guard turns unknown[] into string[]
kind: code
language: ts
code: |
  function isText(value: unknown): value is string {
    return typeof value === "string";
  }
  const scanned: unknown[] = ["DC-1042", 42, "DC-1044", null];
  const ids = scanned.filter(isText);
  console.log(ids.length, ids[1].toLowerCase());
steps:
  - line: 4
    vars: { scanned: '["DC-1042", 42, "DC-1044", null]' }
    say: A barcode scanner gave us a mixed list. Its type is unknown[], so nothing can be used safely yet.
  - line: 5
    say: filter calls isText on each element. Because isText is a type guard, filter's result has the type string[].
  - line: 2
    vars: { value: '"DC-1042"' }
    say: For "DC-1042", typeof says "string", so it is kept.
  - line: 2
    vars: { value: "42" }
    say: 42 is a number, and null is not a string either. Both are dropped.
  - line: 5
    vars: { value: ~, ids: '["DC-1042", "DC-1044"]' }
    say: ids holds the two strings. TypeScript knows they're strings, so toLowerCase is allowed.
  - line: 6
    out: "2 dc-1044"
    say: Two ids, and the second one lower-cased.
```

### A guard is a promise TypeScript believes

TypeScript doesn't check that a guard's body matches its predicate. If the body is wrong, TypeScript still believes it.

```ts
function isPickup(delivery: Courier | Pickup): delivery is Pickup {
  return true;
}
```

This compiles, and now every courier delivery is treated as a pickup. So keep guard bodies short and obvious, and test them like any other function.

## Worked example

The stall's order list mixes saved orders and drafts. Drafts may have no customer yet, and the server sometimes sends `null` for a missing order.

```ts run
type Order = { id: string; customer: string; paise: number };
type Draft = { draftOf: string; paise: number };

function isOrder(entry: Order | Draft): entry is Order {
  return "customer" in entry;
}
function isPresent<T>(value: T | null): value is T {
  return value !== null;
}

const fromServer: (Order | Draft | null)[] = [
  { id: "order-17", customer: "Mrs. Rao", paise: 194800 },
  null,
  { draftOf: "Mr. Deshpande", paise: 45000 },
  { id: "order-18", customer: "Asha", paise: 15000 },
];

const orders = fromServer.filter(isPresent).filter(isOrder);
const total = orders.reduce((sum, order) => sum + order.paise, 0);
console.log(orders.map((order) => order.customer).join(", "));
console.log(total);
```

Line by line:

1. `isOrder` uses `in` to tell an order from a draft, and promises `entry is Order`.
2. `isPresent<T>` is a generic guard: it removes `null` from any type.
3. The first `filter` gives `(Order | Draft)[]`, with no `null`. The second gives `Order[]`.
4. So `order.customer` is allowed in the `map`, with no extra checks.
5. It prints `Mrs. Rao, Asha`, then `209800`.

## Try it

Write `isCourier(delivery: Courier | Pickup): delivery is Courier`, and use it with `filter` to list only the tracking ids. Then change its body to `return true;` and see that TypeScript doesn't complain, but the output is wrong.

```ts run
type Courier = { courier: string; trackingId: string };
type Pickup = { pickupAt: string };
const deliveries: (Courier | Pickup)[] = [
  { courier: "Swift Couriers", trackingId: "SC-88" },
  { pickupAt: "the FC Road stall" },
];
console.log(deliveries.length);
```

## Recap

- A type guard is a check that narrows a type: `typeof`, `instanceof`, `in`, or your own function.
- `typeof` is for strings, numbers and booleans; `instanceof` is for objects made by a class, like `Error` and `Date`.
- `"field" in value` narrows a union to the members that have that field.
- A return type like `value is Bundle` makes your own function a guard. `filter` uses it to narrow the array it returns.
- TypeScript trusts a guard's body, so keep it simple and test it.

## Common mistakes

**1. Using typeof for a class.**

```ts
function messageOf(error: Error | string): string {
  if (typeof error === "Error") {
    return error.message;
  }
  return error;
}
```

TypeScript flags the comparison (error TS2367) as one that can never be true. `typeof` never says `"Error"`: for any object it says `"object"`. Fix: `error instanceof Error`.

**2. A guard that returns the wrong answer.**

```ts
function isPresent<T>(value: T | null | undefined): value is T {
  return Boolean(value);
}
```

It compiles, but `Boolean(0)` and `Boolean("")` are false. A stock count of 0 is treated as missing. Fix: `return value !== null && value !== undefined;`.

**3. Expecting filter(Boolean) to remove null from the type.**

```ts
const ids = ["DC-1042", null, "DC-1044"].filter(Boolean);
```

The nulls are gone when the code runs, but the type of `ids` is still `(string | null)[]`, so TypeScript keeps asking you to check for `null`. Fix: filter with a real guard, like `isPresent`.
