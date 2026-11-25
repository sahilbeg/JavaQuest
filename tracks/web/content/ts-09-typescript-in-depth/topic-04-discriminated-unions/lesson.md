## Hook

At the Aundh shop counter there are three trays: one for cash slips, one for UPI slips and one for card slips. Each slip has its payment mode printed in big letters at the top.

Raju never has to guess. He reads the top line, and then he knows what else is on the slip. A cash slip has the notes handed over. A UPI slip has a reference. A card slip has the last four digits.

A **discriminated union** is that tray system, for types.

## Concept

### The problem: a type full of maybes

Here is the old payment type. Every mode's fields are squeezed into one shape, so they're all optional.

```ts
interface Payment {
  mode: string;
  paise: number;
  tenderedPaise?: number;
  upiRef?: string;
  last4?: string;
}
```

Nothing stops a UPI payment with no reference, or a cash payment with a card number. The receipt code has to guess which fields are there. When it guesses wrong, Mr. Deshpande's UPI receipt says "Change ₹NaN".

### Syntax: a discriminated union

```ts
type Payment =
  | { mode: "CASH"; paise: number; tenderedPaise: number }
  | { mode: "UPI"; paise: number; upiRef: string }
  | { mode: "CARD"; paise: number; last4: string };
```

Each member is one kind of payment, with exactly the fields it needs, none optional. They share one field, `mode`, and each member gives it a different literal value.

```callout jargon
A **discriminated union** is a union of object types that share one field with a different literal type in each member. That field is the **discriminant**, or tag. Checking it tells you which member you have.
```

Now a wrong payment can't even be built:

```ts
const bad: Payment = { mode: "UPI", paise: 45000, tenderedPaise: 50000 };
```

TypeScript refuses it. The tag says `"UPI"`, so it checks the object against the UPI member, and `tenderedPaise` doesn't belong there. Take that line out, and it complains next that `upiRef` is missing.

### Checking the tag narrows the type

Before you check `mode`, only the shared fields are safe. After you check it, TypeScript knows which member it is.

```ts
function refOf(payment: Payment): string {
  if (payment.mode === "UPI") {
    return payment.upiRef;
  }
  return "no ref";
}
```

Inside the `if`, `payment` is the UPI member, so `payment.upiRef` is allowed. Outside it, `payment.upiRef` is an error:

```text
error TS2339: Property 'upiRef' does not exist on type 'Payment'.
```

### Syntax: switch on the tag

```ts
switch (payment.mode) {
  case "CASH":
    // payment is the CASH member here
    break;
  case "UPI":
    // payment is the UPI member here
    break;
}
```

With more than two members, a `switch` reads best. Each `case` narrows to one member. Watch it happen.

```animate
title: Each case knows its own payment
kind: code
language: ts
code: |
  type Payment =
    | { mode: "CASH"; paise: number; tenderedPaise: number }
    | { mode: "UPI"; paise: number; upiRef: string };
  function slip(p: Payment): string {
    switch (p.mode) {
      case "CASH":
        return `Cash, change ${p.tenderedPaise - p.paise}`;
      case "UPI":
        return `UPI ref ${p.upiRef}`;
    }
  }
  console.log(slip({ mode: "UPI", paise: 45000, upiRef: "UPI-5521" }));
  console.log(slip({ mode: "CASH", paise: 45000, tenderedPaise: 50000 }));
steps:
  - line: 12
    say: The first call passes a UPI payment.
  - line: 5
    vars: { p: '{ mode: "UPI", paise: 45000, upiRef: "UPI-5521" }' }
    say: The switch reads the tag, p.mode. It is "UPI".
  - line: 8
    say: In this case, TypeScript knows p is the UPI member. So p.upiRef is allowed, and p.tenderedPaise would be an error.
  - line: 9
    out: "UPI ref UPI-5521"
    say: The slip shows the reference, and the first line is printed.
  - line: 6
    vars: { p: '{ mode: "CASH", paise: 45000, tenderedPaise: 50000 }' }
    say: The second call passes cash. The switch jumps to the CASH case.
  - line: 7
    out: "Cash, change 5000"
    say: Here p is the CASH member, so tenderedPaise is there. 50000 minus 45000 is 5000 paise of change.
```

### Syntax: never and the exhaustive check

```ts
function assertNever(value: never): never {
  throw new Error(`Unexpected value: ${JSON.stringify(value)}`);
}
```

After every case has been handled, there is nothing left that `payment` could be. TypeScript calls that type `never`.

```callout jargon
**never** is the type with no values at all. A variable has the type `never` when every possibility has already been ruled out. Only a `never` can be passed where a `never` is expected.
```

So put `assertNever(payment)` in the `default` case. Today it compiles. But when someone adds a fourth mode and forgets its case, `payment` in `default` is no longer `never`, and TypeScript points at the line:

```text
error TS2345: Argument of type '{ mode: "CARD"; paise: number; last4: string; }' is not assignable to parameter of type 'never'.
```

```callout jargon
A check is **exhaustive** when it handles every member of a union. The `never` trick turns a forgotten member into a type error instead of a wrong receipt.
```

If bad data still sneaks in when the code runs, for example from an old server, `assertNever` throws a clear error instead of carrying on with nonsense.

### Modelling states, not flags

Discriminated unions fit anything that is in one state at a time. A stock check is loading, ready or failed, never two at once.

```ts
type StockCheck =
  | { status: "loading" }
  | { status: "ready"; stock: number }
  | { status: "failed"; error: string };
```

Compare that with `{ loading: boolean; stock?: number; error?: string }`, which allows nonsense like "loading, with an error, and a stock of 3". With the union, only real states exist, and each one carries exactly its own data.

## Worked example

Sana's stock badge has three looks. Each state carries only the data it needs.

```ts run
type Badge =
  | { kind: "inStock"; count: number }
  | { kind: "low"; count: number }
  | { kind: "soldOut"; restockOn: string };

function assertNever(value: never): never {
  throw new Error(`Unexpected badge: ${JSON.stringify(value)}`);
}

function badgeText(badge: Badge): string {
  switch (badge.kind) {
    case "inStock":
      return `${badge.count} in stock`;
    case "low":
      return `Only ${badge.count} left!`;
    case "soldOut":
      return `Back on ${badge.restockOn}`;
    default:
      return assertNever(badge);
  }
}

console.log(badgeText({ kind: "inStock", count: 12 }));
console.log(badgeText({ kind: "low", count: 2 }));
console.log(badgeText({ kind: "soldOut", restockOn: "Saturday" }));
```

Line by line:

1. `Badge` has three members, tagged by `kind`. Only `soldOut` has `restockOn`.
2. `switch (badge.kind)` narrows `badge` in each case, so `badge.count` and `badge.restockOn` are only used where they exist.
3. `default` can only be reached by a badge that isn't one of the three. Its type there is `never`, so `assertNever(badge)` compiles.
4. It prints `12 in stock`, `Only 2 left!` and `Back on Saturday`.

## Try it

Add a fourth member, `{ kind: "preorder"; shipsOn: string }`, to `Badge` in your editor. Look at the red line TypeScript draws under `assertNever(badge)`. Then add the missing case, `Ships on Monday`, and watch the error go away.

```ts run
type Badge =
  | { kind: "inStock"; count: number }
  | { kind: "soldOut"; restockOn: string };

function badgeText(badge: Badge): string {
  if (badge.kind === "inStock") {
    return `${badge.count} in stock`;
  }
  return `Back on ${badge.restockOn}`;
}
console.log(badgeText({ kind: "soldOut", restockOn: "Sunday" }));
```

## Recap

- A discriminated union is a union of object types that share a tag field, like `mode` or `kind`, with a different literal value in each member.
- Each member holds exactly its own fields, none optional, so a wrong combination can't be built.
- Checking the tag, with `if` or `switch`, narrows the value to one member.
- `assertNever(value)` in the `default` case makes the check exhaustive: a forgotten member becomes a type error.
- Model "one state at a time" with a union, not with booleans and optional fields.

## Common mistakes

**1. Reading a member's field before checking the tag.**

```ts
function refOf(payment: Payment): string {
  return payment.upiRef;
}
```

```text
error TS2339: Property 'upiRef' does not exist on type 'Payment'.
```

A cash payment has no `upiRef`. Fix: check `payment.mode === "UPI"` first.

**2. Using a plain string for the tag.**

```ts
type Payment =
  | { mode: string; paise: number; upiRef: string }
  | { mode: string; paise: number; last4: string };
```

Both members have `mode: string`, so checking `mode` tells TypeScript nothing, and it can't narrow. Fix: give each member its own literal, like `mode: "UPI"` and `mode: "CARD"`.

**3. A default case that hides a missing member.**

```ts
function slip(payment: Payment): string {
  switch (payment.mode) {
    case "CASH":
      return "Cash";
    case "UPI":
      return "UPI";
    default:
      return "Card";
  }
}
```

It compiles. But when a new mode, like `"WALLET"`, is added, it silently prints `Card`. Fix: handle `"CARD"` in its own case and make `default` call `assertNever(payment)`.
