## Hook

A shop sign that only says OPEN is wrong half the time. A good sign has several sides: OPEN, CLOSED, BACK IN 10 MINUTES. Each side carries its own details, like a time, and you flip to the right one.

A component often has a few different states too. In stock, sold out, made to order. A union type lists the sides, and TypeScript makes sure you draw every one.

## Concept

### Syntax: choosing what to show

React has no special `if` for JSX. You use plain JavaScript, in three ways:

```tsx
// 1. An if before the return
if (items.length === 0) {
  return <p>Your bag is empty</p>;
}

// 2. A ternary inside JSX: one thing or another
<p>{soldOut ? "Sold out" : "In stock"}</p>

// 3. && inside JSX: something or nothing
{soldOut && <button type="button">Tell me when it's back</button>}
```

```callout jargon
**Conditional rendering** means showing different JSX depending on props or state, using `if`, the ternary `? :` or `&&`.
```

`a && b` gives `b` when `a` is true, and `a` when it is false. React draws nothing for `false`, so the button simply doesn't appear. A component can also `return null` to draw nothing at all.

### The trouble with flags

```tsx
interface StockProps {
  soldOut: boolean;
  count?: number;
  backOn?: string;
  madeToOrder?: boolean;
  days?: number;
}
```

Props like these allow nonsense. What does `soldOut` with `count={5}` and `madeToOrder` mean? Every optional field must be checked everywhere, and nothing says which fields belong together.

### Syntax: a union for the states

```tsx
type Stock =
  | { kind: "in-stock"; count: number }
  | { kind: "sold-out"; backOn: string | null }
  | { kind: "made-to-order"; days: number };
```

This is a discriminated union, as in ts-09. Each state has a `kind` and only the fields that make sense for it. A sold-out item has no count. A made-to-order item has days instead.

```tsx
<StockLine stock={{ kind: "sold-out", backOn: "14 Dec" }} />
<StockLine stock={{ kind: "sold-out", count: 5 }} />
```

The second line fails to type-check, because `count` doesn't belong to the sold-out state.

### Syntax: a switch that returns JSX

```tsx
function StockLine({ stock }: { stock: Stock }) {
  switch (stock.kind) {
    case "in-stock":
      return <p>{stock.count} in stock</p>;
    case "sold-out":
      return <p>Sold out</p>;
    case "made-to-order":
      return <p>Ready in {stock.days} days</p>;
  }
}
```

Inside each `case`, TypeScript narrows `stock` to that one state. `stock.days` is allowed only in the made-to-order case. Elsewhere it is a type error.

### Narrowing, step by step

This is the same switch without JSX, so you can watch it run.

```animate
title: One switch, three kinds of stock
kind: code
language: ts
code: |
  type Stock =
    | { kind: "in-stock"; count: number }
    | { kind: "made-to-order"; days: number };

  function label(stock: Stock): string {
    switch (stock.kind) {
      case "in-stock":
        return `${stock.count} left`;
      case "made-to-order":
        return `Ready in ${stock.days} days`;
    }
  }

  console.log(label({ kind: "in-stock", count: 2 }));
  console.log(label({ kind: "made-to-order", days: 5 }));
steps:
  - line: 1
    say: Stock has two states. Each one has a kind and its own fields.
  - line: 14
    vars: { stock: '{ kind: "in-stock", count: 2 }' }
    say: We ask for the label of two Golus in stock.
  - line: 6
    say: The switch looks at kind, the field every state shares.
  - line: 8
    say: In this case TypeScript knows stock has a count, so stock.count is allowed.
    out: "2 left"
  - line: 15
    vars: { stock: '{ kind: "made-to-order", days: 5 }' }
    say: Now booties that are crocheted after you order.
  - line: 10
    say: In this case stock has days, not count. Using count here would be a type error.
    out: "Ready in 5 days"
```

### Syntax: the exhaustive check

```tsx
default: {
  const unhandled: never = stock;
  return unhandled;
}
```

What if someone adds `{ kind: "pre-order" }` to `Stock` next month? Without a check, the switch quietly returns nothing for it, and the badge disappears.

`never` is the type with no values. In the `default` branch, every known kind has been handled, so `stock` is `never`. Add a new kind, and it reaches `default` with a real type. Then `tsc` stops with an error like `Type '{ kind: "pre-order"; }' is not assignable to type 'never'.`

```callout jargon
An **exhaustive check** makes `tsc` fail when a switch misses a case of a union. Assigning the value to a `never` variable in `default` does it.
```

### When && shows a 0

```tsx
{count && <p>{count} left</p>}
```

When `count` is `0`, `0 && ...` gives `0`. React draws numbers, so the page shows a stray `0`. Compare instead: `{count > 0 && <p>{count} left</p>}`.

## Worked example

A delivery tracker for an order. Each state of the delivery has its own details.

```tsx
type Delivery =
  | { kind: "packing" }
  | { kind: "on-the-way"; rider: string }
  | { kind: "delivered"; at: string }
  | { kind: "failed"; reason: string };

export function DeliveryStatus({ delivery }: { delivery: Delivery }) {
  switch (delivery.kind) {
    case "packing":
      return <p className="status">Packing your order</p>;
    case "on-the-way":
      return <p className="status">On the way with {delivery.rider}</p>;
    case "delivered":
      return <p className="status">Delivered at {delivery.at}</p>;
    case "failed":
      return (
        <p className="status status--failed">
          We couldn't deliver: {delivery.reason}
        </p>
      );
    default: {
      const unhandled: never = delivery;
      return unhandled;
    }
  }
}
```

Line by line:

- `packing` carries no details, so its object holds only the `kind`.
- Each `case` uses only the fields of its own state. `delivery.rider` is allowed only for `on-the-way`.
- The `default` branch is the exhaustive check. It never runs while every kind has a case.

## Try it

Run this, then add a fourth state `{ kind: "pre-order"; month: string }` with its own case.

```ts run
type Stock =
  | { kind: "in-stock"; count: number }
  | { kind: "sold-out"; backOn: string | null }
  | { kind: "made-to-order"; days: number };

function badge(stock: Stock): string {
  switch (stock.kind) {
    case "in-stock":
      return stock.count <= 3 ? `Only ${stock.count} left` : "In stock";
    case "sold-out":
      return stock.backOn === null ? "Sold out" : `Sold out, back on ${stock.backOn}`;
    case "made-to-order":
      return `Made to order: ready in ${stock.days} days`;
  }
}

console.log(badge({ kind: "in-stock", count: 2 }));
console.log(badge({ kind: "sold-out", backOn: "14 Dec" }));
console.log(badge({ kind: "made-to-order", days: 5 }));
```

It prints `Only 2 left`, `Sold out, back on 14 Dec` and `Made to order: ready in 5 days`.

## Recap

- Choose what to show with an `if` before the return, a ternary for one thing or another, or `&&` for something or nothing.
- Model a component's states as a discriminated union, with only the fields each state needs.
- A `switch` on `kind` narrows the type, so each case can use its own fields.
- `const unhandled: never = value;` in `default` makes `tsc` fail when a new state is not handled.
- `0 && ...` shows `0`. Compare numbers before using `&&`.

## Common mistakes

**1. A pile of optional flags.**

```tsx
interface StockProps {
  soldOut: boolean;
  count?: number;
}
```

Nothing stops `soldOut` with a count, and every use must check `count !== undefined`. Use a union with a `kind` for each state.

**2. Using a field from another state.**

```tsx
case "sold-out":
  return <p>Only {stock.count} left</p>;
```

`tsc` says `Property 'count' does not exist` on the sold-out state. That is the point: a sold-out item has no count. Use only the fields of the case you are in.

**3. && with a number.**

```tsx
{stock.count && <p>{stock.count} left</p>}
```

At zero, a lonely `0` appears on the page. Write `{stock.count > 0 && <p>{stock.count} left</p>}`.

**4. No exhaustive check.**

```tsx
switch (stock.kind) {
  case "in-stock":
    return <p>In stock</p>;
}
```

Other kinds return `undefined`, and the badge silently vanishes. Handle every case, and add the `never` check in `default` so `tsc` reminds you next time.
