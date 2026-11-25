## Hook

A tailor's measuring tape tells you the cloth is the right size. It can't tell you the shirt looks good. For that, someone has to try it on.

TypeScript is the measuring tape. It proves every value has the right shape before the code runs. Tests are the trying on. They run the code and check what the customer really sees. A professional team uses both, and makes them help each other.

## Concept

### What types prove, and what tests prove

You met component tests with React Testing Library in [Writing tests with Vitest](/t/web-07-tooling/topic-07-unit-tests) and the React modules before this one. TypeScript doesn't replace them. Each one catches a different kind of mistake:

| Mistake | Caught by |
|---|---|
| Passing `"450"` where a number of paise belongs | the type checker |
| Forgetting a required prop | the type checker |
| Showing ₹45000.00 instead of ₹450.00 | a test |
| A Cancel button that shows for a delivered order | a test |

A component can type-check perfectly and still show the wrong price. Types check **shapes**. Tests check **behaviour**.

```callout jargon
A **type error** is the compiler refusing code before it runs, because a value has the wrong shape. A **failing test** is code that ran and did the wrong thing.
```

### Test files in TypeScript

A test for a `.tsx` component is a `.test.tsx` file. It imports the component with its extension, and it renders it with typed props:

```tsx
import { render, screen } from "@testing-library/react";
import { OrderCard } from "../src/OrderCard.tsx";

render(<OrderCard order={order} onCancel={vi.fn()} />);
```

Here the test forgot `onTrack`, a required prop. In a project where `tsc` checks the tests too, this line is an error:

```text
error TS2741: Property 'onTrack' is missing in type '{ order: Order; onCancel: Mock<Procedure>; }' but required in type 'OrderCardProps'.
```

That's a gift. When a teammate adds a required prop, every test that renders the component shows up in the error list.

```callout tip
Most teams type-check their tests along with the app, with one tsconfig that includes the test folder. In these tickets, only src/ is type-checked, so keep your helpers in src/.
```

### Syntax: a fixture builder

```ts
function makeOrder(overrides: Partial<Order> = {}): Order {
  return { id: "order-17", customer: "Meera Kulkarni", status: "placed", lines: [{ ...GOLU_LINE }], ...overrides };
}
```

`Partial<Order>` makes every field optional, so a test passes only what it cares about: `makeOrder({ status: "shipped" })`. The spread puts `overrides` last, so the test's fields win.

```callout jargon
A **fixture** is the test data a check starts from: an order, a cart, a customer. A **fixture builder** is a function that makes a fresh, complete one each time.
```

The return type `Order` is the clever part. If someone adds a field to `Order`, the builder stops compiling until it gets a default. Every test keeps working, and none of them builds a half-empty order.

### Fresh data for every test

A builder must make **new** objects every time. If two tests share one object, the first test can change it and spoil the second.

```animate
title: A shared line spoils the next test
kind: code
language: ts
code: |
  type Line = { id: string; qty: number };
  const GOLU: Line = { id: "DC-1042", qty: 1 };
  const sharedOrder = () => ({ lines: [GOLU] });
  const freshOrder = () => ({ lines: [{ ...GOLU }] });
  sharedOrder().lines[0].qty = 5;
  console.log(sharedOrder().lines[0].qty);
  GOLU.qty = 1;
  freshOrder().lines[0].qty = 5;
  console.log(freshOrder().lines[0].qty);
steps:
  - line: 2
    vars: { GOLU: '{ id: "DC-1042", qty: 1 }' }
    say: GOLU is one line object, kept in a constant for every test to use.
  - line: 3
    say: sharedOrder builds a new array, but puts the same GOLU object inside it every time.
  - line: 4
    say: freshOrder copies GOLU with a spread, so each order gets its own line object.
  - line: 5
    vars: { GOLU: '{ id: "DC-1042", qty: 5 }' }
    say: One test sets its line's qty to 5. That line is GOLU itself, so GOLU changes.
  - line: 6
    out: "5"
    say: The next test builds a "new" order and finds 5 Golus in it. The first test spoiled it.
  - line: 7
    vars: { GOLU: '{ id: "DC-1042", qty: 1 }' }
    say: Put GOLU back to 1, and try the fresh builder.
  - line: 8
    say: This test changes its own copy. GOLU is untouched.
  - line: 9
    out: "1"
    say: The next fresh order still has 1 Golu. Every test starts clean.
```

### Syntax: a typed spy

```tsx
const onCancel = vi.fn<(orderId: string) => void>();
```

You met `vi.fn()` spies in the React modules. Give the spy the function type of the prop, inside `< >`. Then the spy fits the prop exactly, and `onCancel.mock.calls` is typed as a list of `[orderId: string]` entries.

An untyped `vi.fn()` accepts any arguments and returns `any`. It hides mistakes the typed one would show.

```callout jargon
A **spy** (or mock function) is a stand-in function that remembers every call and its arguments. `mock.calls` holds one entry per call.
```

### One check per state: case tables

Order status is a union: `"placed" | "packed" | "shipped" | "delivered" | "cancelled"`. A bug often hides in the one state nobody tried. So write the expected result for **every** state as a table:

```ts
const STATUS_CASES: Record<OrderStatus, StatusCase> = {
  placed: { label: "Order placed", canCancel: true, canTrack: false },
  packed: { label: "Packed, waiting for the courier", canCancel: true, canTrack: false },
  shipped: { label: "On its way", canCancel: false, canTrack: true },
  delivered: { label: "Delivered", canCancel: false, canTrack: false },
  cancelled: { label: "Cancelled", canCancel: false, canTrack: false },
};
```

`Record<OrderStatus, StatusCase>` needs one key for each member of the union. When someone adds `"returned"`, the table stops compiling until it has a `returned` row. The compiler finds the test you forgot to write.

```text
error TS2741: Property 'returned' is missing in type '{ placed: ...; packed: ...; shipped: ...; delivered: ...; cancelled: ...; }' but required in type 'Record<OrderStatus, StatusCase>'.
```

Then one loop makes a check per row, each with its own name:

```tsx
for (const status of STATUSES) {
  it(`shows the right label for a ${status} order`, () => {
    render(<OrderCard order={makeOrder({ status })} onCancel={vi.fn()} onTrack={vi.fn()} />);
    expect(screen.queryByRole("status")?.textContent, `label for ${status}`).toBe(STATUS_CASES[status].label);
  });
}
```

### Testing the types themselves

Sometimes the type **is** the feature. Say `OrderCard` must refuse a status that doesn't exist. A **type test** proves the compiler says no:

```tsx
// @ts-expect-error: "lost" is not an OrderStatus
makeOrder({ status: "lost" });
```

```callout jargon
`// @ts-expect-error` tells the compiler: the next line **should** be an error. If it isn't, that is itself an error. `// @ts-ignore` just hides any error, and never complains.
```

So `@ts-expect-error` is a check: if someone widens `status` to any string, the type test fails. Vitest also has `expectTypeOf(value).toEqualTypeOf<Order>()` for checks like this.

### Don't silence the compiler in tests

The quickest way to write a test is to lie to the compiler:

```ts
const order = { status: "packed" } as Order;
const props = { order } as any;
```

Both lines compile. Both build an order with no id, no customer and no lines. The test either crashes in a strange place or passes for the wrong reason. A builder gives you a complete order in the same number of keystrokes.

## Worked example

Here is a test file for `OrderCard`, using a builder, typed spies and a case table.

```tsx
import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { OrderCard } from "../src/OrderCard.tsx";
import { makeOrder, STATUS_CASES } from "../src/fixtures.ts";
import type { OrderStatus } from "../src/types.ts";

afterEach(cleanup);

const STATUSES: OrderStatus[] = ["placed", "packed", "shipped", "delivered", "cancelled"];

describe("OrderCard", () => {
  for (const status of STATUSES) {
    it(`shows the right buttons for a ${status} order`, () => {
      render(<OrderCard order={makeOrder({ status })} onCancel={vi.fn()} onTrack={vi.fn()} />);
      const hasCancel = screen.queryByRole("button", { name: "Cancel order" }) !== null;
      expect(hasCancel, `Cancel order for ${status}`).toBe(STATUS_CASES[status].canCancel);
    });
  }

  it("cancels once, with the order's id", async () => {
    const user = userEvent.setup();
    const onCancel = vi.fn<(orderId: string) => void>();
    render(<OrderCard order={makeOrder({ status: "packed" })} onCancel={onCancel} onTrack={vi.fn()} />);
    await user.click(screen.getByRole("button", { name: "Cancel order" }));
    expect(onCancel.mock.calls, "one cancel for order-17").toEqual([["order-17"]]);
  });
});
```

Line by line:

- `STATUSES` is typed `OrderStatus[]`, so a misspelt status like `"shiped"` is a type error.
- The loop makes five checks. A failure names the status, such as "shows the right buttons for a packed order".
- `makeOrder({ status })` builds a complete order and changes one field.
- The typed spy fits `onCancel` exactly, and the last check compares its whole `mock.calls` list.

## Try it

This checks a label function against a case table, row by row. One row has a bug. Run it.

```ts run
type OrderStatus = "placed" | "packed" | "shipped" | "delivered" | "cancelled";

function label(status: OrderStatus): string {
  switch (status) {
    case "placed": return "Order placed";
    case "packed": return "Packed";
    case "shipped": return "On its way";
    case "delivered": return "Delivered";
    case "cancelled": return "Cancelled";
  }
}

const CASES: Record<OrderStatus, string> = {
  placed: "Order placed",
  packed: "Packed, waiting for the courier",
  shipped: "On its way",
  delivered: "Delivered",
  cancelled: "Cancelled",
};

const STATUSES: OrderStatus[] = ["placed", "packed", "shipped", "delivered", "cancelled"];
for (const status of STATUSES) {
  const actual = label(status);
  console.log(status + ": " + (actual === CASES[status] ? "pass" : "FAIL, got " + actual));
}
```

It prints:

```text
placed: pass
packed: FAIL, got Packed
shipped: pass
delivered: pass
cancelled: pass
```

1. Fix the `packed` label and run it again.
2. Add `"returned"` to the `OrderStatus` union. In an editor, which two places turn red?

## Recap

- Types prove shapes before the code runs. Tests prove behaviour when it runs. You need both.
- Write component tests as `.test.tsx` files with typed props, so a missing or wrong prop is a compile error.
- Build test data with a typed fixture builder, `makeOrder(overrides: Partial<Order>)`, that makes fresh objects on every call.
- Type your spies, like `vi.fn<(orderId: string) => void>()`, and keep a `Record<Union, Expected>` case table so every state gets a check.
- Prove type rules with `// @ts-expect-error`, and never silence the compiler with `as any` in tests.

## Common mistakes

**1. Sharing one object between tests.**

```ts
const ORDER: Order = { id: "order-17", customer: "Meera Kulkarni", status: "placed", lines: [GOLU_LINE] };
```

Symptom: a test passes alone but fails when the whole file runs, because an earlier test changed `ORDER.lines`. Fix: a builder that returns a new order, with copied lines, on every call.

**2. Building half an object with `as`.**

```ts
const order = { status: "shipped" } as Order;
```

Symptom: `OrderCard` crashes with "Cannot read properties of undefined (reading 'reduce')", because the order has no lines. Fix: `makeOrder({ status: "shipped" })`, which fills in every field.

**3. Testing only the first state of a union.**

```tsx
it("shows the label", () => { render(<OrderCard order={makeOrder()} onCancel={vi.fn()} onTrack={vi.fn()} />); });
```

Symptom: every check is green, and packed orders show the wrong buttons in production. Fix: a `Record<OrderStatus, ...>` case table and one check per row.

**4. An untyped spy for a typed prop.**

```tsx
const onCancel = vi.fn();
```

Symptom: you change `onCancel` to take an order object instead of an id, and every test still compiles. They fail later with confusing messages. Fix: `vi.fn<(orderId: string) => void>()`, so the spy breaks at compile time with the prop.

**5. Using @ts-ignore in a type test.**

```ts
// @ts-ignore
makeOrder({ status: "lost" });
```

Symptom: someone widens `status` to `string`, and nothing complains. The type test can never fail. Fix: `// @ts-expect-error`, which errors when the expected error goes away.
