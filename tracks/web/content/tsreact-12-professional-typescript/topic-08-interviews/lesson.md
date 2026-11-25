## Hook

A cricket trial isn't one long match. A coach watches you bat for ten minutes, bowl for ten, and field for ten. Each round tests one skill, and in each one they watch how you think, not only whether you hit the ball.

A TypeScript interview loop works the same way. Each round asks something different: explain a type, type a function, build a typed component. In every round the interviewer watches how you get there.

## Concept

### The TypeScript interview loop

```callout jargon
An **interview loop** is the set of interview rounds a company runs for one job, often on a single day. Each round has its own interviewer and its own task.
```

For a frontend role that says "TypeScript", the loop usually has these rounds:

| Round | What happens | What they look for |
|---|---|---|
| Type questions | "What's the difference between `unknown` and `any`?" | Clear words, a small example |
| Typing a function | Type and write a helper, such as grouping orders | Types first, edge cases, no `any` |
| Typed React | A component or hook with typed props, state and events | Honest types for loading and errors |
| Type design | Model a feature's data, such as order states | Unions over flags, readable types |
| Review a diff | Find the problems in typed code | `as`, `!`, `any`, missing cases |

You'll be asked to talk while you work. Silence is the most common reason good candidates fail a round.

### Question: type or interface?

Both name an object's shape, and for plain objects either is fine:

```ts
interface Item { id: string; paise: number }
type ItemAlias = { id: string; paise: number };
```

The differences worth saying in an interview:

- Only `type` can name a union, a tuple, or any other non-object type: `type Payment = "CASH" | "UPI" | "CARD"`.
- Only `interface` can be **merged**: two `interface Window` declarations join into one. Libraries use this to let you add fields.
- `interface ... extends` and `type ... &` both combine shapes. `extends` gives clearer errors when the two conflict.

A good answer ends with the team rule: "Many teams use `interface` for object shapes and `type` for everything else. What matters most is being consistent."

### Question: any, unknown and never

These three come up in almost every loop:

| Type | Means | You can... |
|---|---|---|
| `any` | "don't check this" | do anything with it; checking is off |
| `unknown` | "could be anything, check before use" | only use it after narrowing it |
| `never` | "this can't happen" | nothing; no value has this type |

```callout jargon
**Narrowing** means turning a wide type into a narrower one with a check, like `typeof value === "string"`. Inside the check, TypeScript treats the value as the narrower type.
```

Give one use for each: `unknown` for JSON and caught errors, `never` for the exhaustive check in a `switch`, and `any` only as a marked, temporary step in a migration.

### Question: why does this fit?

Interviewers love this one. Why does `golu` fit a parameter whose type doesn't mention `name`?

```animate
title: An object with an extra field still fits
kind: code
language: ts
code: |
  type Priced = { id: string; paise: number };
  const golu = { id: "DC-1042", paise: 45000, name: "Golu the elephant" };
  function rupees(item: Priced): number {
    return item.paise / 100;
  }
  console.log(rupees(golu));
steps:
  - line: 1
    say: Priced asks for two fields, id and paise.
  - line: 2
    vars: { golu: '{ id: "DC-1042", paise: 45000, name: "Golu the elephant" }' }
    say: golu has those two fields, and a name as well.
  - line: 3
    say: rupees takes a Priced. It only ever reads paise.
  - line: 6
    out: "450"
    say: golu fits, because it has everything Priced needs. TypeScript compares shapes, not names. That's structural typing.
```

```callout jargon
**Structural typing** means a value fits a type if it has the right shape, whatever it was called. Java, by contrast, uses **nominal typing**: a class fits only if it is declared to.
```

The follow-up: "Then why is `rupees({ id: "DC-1042", paise: 45000, name: "Golu" })` an error?" Because an object literal written straight into the call gets an **excess property check**. TypeScript guesses an extra field there is a typo. A variable that already exists doesn't get that check.

### Question: generics

```ts
function first<T>(items: readonly T[]): T | undefined {
  return items[0];
}
```

The interviewer wants to hear why `<T>` beats `any`: it keeps the link between what goes in and what comes out. `first(orders)` returns an `Order | undefined`, not an `any`.

You met generics and constraints in [Generics](/t/ts-09-typescript-in-depth/topic-01-generics) and [Generic constraints](/t/ts-09-typescript-in-depth/topic-02-generic-constraints). Common follow-ups:

- "Add a constraint": `function byId<T extends { id: string }>(items: readonly T[], id: string): T | undefined`.
- "Use `keyof`": `function pluck<T, K extends keyof T>(items: readonly T[], key: K): T[K][]`.
- "Write `Partial` yourself": `type MyPartial<T> = { [K in keyof T]?: T[K] }`.

Notice `readonly T[]` in the parameters. It promises the caller you won't change their array, and lets them pass a read-only one. Interviewers notice it.

### The method for a live typing round

When you're asked to type and write a function:

1. **Ask.** "Can the list be empty? Must I keep the input unchanged? What should a missing key give?"
2. **Types first.** Write the signature before the body. Say it out loud: "it takes read-only orders and returns a record of counts".
3. **An example by hand.** Two or three inputs, and the output you expect, written as a comment.
4. **Code.** Simple and clear. No `any`, no `as`.
5. **Test out loud.** Empty input, one item, two with the same key, the input unchanged afterwards.
6. **Talk about trade-offs.** "A `Map` would keep insertion order for any key type. A `Record` is easier to turn into JSON."

```callout tip
If you're stuck on a type, say so and write the plainest type that's true, like string instead of a clever template type. You can tighten it once the code works.
```

### Reviewing typed code on the spot

Some loops hand you a diff and ask, "What would you comment on?" Use the order from [Code reviews for TypeScript](/t/tsreact-12-professional-typescript/topic-06-code-reviews): behaviour first, then escape hatches, honest types, unions that grew, and simple types. Label what you'd block, and say why.

## Worked example

The question: "Type a function `totalByMode` that adds up order totals per payment mode."

**Ask.** "Should a mode with no orders appear as 0, or be missing?" The interviewer says: as 0.

**Types first.**

```ts
type Payment = "CASH" | "UPI" | "CARD";
type PaidOrder = { id: string; payment: Payment; totalPaise: number };

function totalByMode(orders: readonly PaidOrder[]): Record<Payment, number> {
  const totals: Record<Payment, number> = { CASH: 0, UPI: 0, CARD: 0 };
  for (const order of orders) {
    totals[order.payment] += order.totalPaise;
  }
  return totals;
}
```

**Talk through it.**

- `Record<Payment, number>` says every mode has a number. Starting with all three at 0 answers the "missing mode" question, and the compiler checks that none is forgotten.
- `readonly PaidOrder[]` promises not to change the orders.
- **Edge cases, out loud:** no orders gives all zeros. Two UPI orders add up. A new mode, like `"WALLET"`, makes the starting object a type error until it gets a 0, which is exactly what we want.

That last sentence is what moves a "pass" to a "strong pass": you used the types to prove the code handles the future.

## Try it

This is a generic `countBy`, a classic warm-up question. Run it.

```ts run
function countBy<T>(items: readonly T[], keyOf: (item: T) => string): Record<string, number> {
  const counts: Record<string, number> = {};
  for (const item of items) {
    const key = keyOf(item);
    counts[key] = (counts[key] ?? 0) + 1;
  }
  return counts;
}

const orders = [
  { id: "order-17", mode: "UPI" },
  { id: "order-18", mode: "CASH" },
  { id: "order-19", mode: "UPI" },
];
console.log(JSON.stringify(countBy(orders, (order) => order.mode)));
```

It prints:

```text
{"UPI":2,"CASH":1}
```

1. Call it with an empty list. What does it print, and is that the answer you'd want in an interview?
2. Change the return type so that only real payment modes can be keys. What else has to change?

## Recap

- A TypeScript loop adds type questions, typing a function, typed React, type design and reviewing typed code. Talk while you work.
- `interface` and `type` both describe objects; only `type` names unions, and only `interface` merges. Be consistent.
- `any` switches checks off, `unknown` must be narrowed before use, and `never` means "can't happen".
- TypeScript is structural: extra fields fit, except in an object literal written straight into a typed spot.
- Generics keep the link between input and output. In a live round: ask, write the types first, try examples, code, then test the edges out loud.

## Common mistakes

**1. Reaching for any when the type gets hard.**

```ts
function groupOrders(orders: any): any {
  return orders.reduce(byDay, {});
}
```

Symptom: the interviewer asks "what does this return?", and you can't say. That round is lost. Fix: write the plainest true type, such as `Record<string, Order[]>`, and say you'd tighten it later.

**2. Saying "unknown and any are the same".**

```ts
const data: unknown = JSON.parse(text);
data.items.length;
```

Symptom: this is a type error, `'data' is of type 'unknown'`, and the interviewer knows you haven't used it. Fix: explain that `unknown` must be narrowed first, and show a check.

**3. Coding before typing the signature.**

```text
(types in silence for 12 minutes, then adds types at the end)
```

Symptom: the body fights its own types, and the interviewer saw no plan. Fix: write and say the signature first. It's half the answer.

**4. Forgetting the empty case.**

```ts
function cheapest(items: readonly Item[]): Item {
  return items.reduce((a, b) => (a.paise <= b.paise ? a : b));
}
```

Symptom: the interviewer passes `[]`, and `reduce` throws "Reduce of empty array with no initial value". Fix: return `Item | undefined`, and handle the empty list first.

**5. Answering type or interface with a rule and no reason.**

```text
"Always use interface."
```

Symptom: the follow-up "how would you type the payment modes, then?" has no answer. Fix: give the real difference (unions need `type`, merging needs `interface`), then the team rule.
