## Hook

The yarn winder at the shop has two slots. The left one takes a cone of yarn, the right one an empty ball. Load them the wrong way round, and you get a tangle, not a ball.

A function is a machine with slots too. In JavaScript, nothing stops you loading them the wrong way round. In TypeScript, every slot has a label, and the checker reads the labels on every call.

## Concept

### Syntax: typed parameters

```ts
function lineTotal(paise: number, qty: number) {
  return paise * qty;
}
```

Each parameter gets a type, just like a variable. Now every call is checked against it:

```ts
function lineTotal(paise: number, qty: number) {
  return paise * qty;
}
lineTotal("15000", 2);
lineTotal(15000);
```

```text
error TS2345: Argument of type 'string' is not assignable to parameter of type 'number'.
error TS2554: Expected 2 arguments, but got 1.
```

The first error is about line 4, which passes text. The second is about line 5, which forgets the quantity.

```callout jargon
A **parameter** is a name in the function's brackets, like `paise`. An **argument** is the value a call passes in, like `15000`. TypeScript checks every argument against its parameter's type.
```

### Syntax: return types

```ts
function lineTotal(paise: number, qty: number): number {
  return paise * qty;
}
```

The `: number` after the brackets is the **return type**: what the function promises to give back. TypeScript could infer it, but writing it states your plan. Then a mistake inside the function is reported inside the function, where it is:

```ts
function deliveryFee(subtotal: number): number {
  if (subtotal > 99900) {
    return "FREE";
  }
  return 6000;
}
```

```text
error TS2322: Type 'string' is not assignable to type 'number'.
```

"FREE" is for the receipt. The fee itself must stay a number, so the maths keeps working.

### Syntax: void

```ts
function logTotal(paise: number): void {
  console.log(paise);
}
```

`void` means "this function gives nothing back". Use it for functions that only do something, like printing or saving.

```callout jargon
A **return type** is the type written after a function's brackets, saying what it gives back. **void** is the return type of a function that gives back nothing useful.
```

### Syntax: optional parameters

```ts
function withWrap(paise: number, giftWrap?: boolean): number {
  return giftWrap ? paise + 3000 : paise;
}
```

A `?` after the name makes a parameter **optional**: callers may leave it out. When they do, it is `undefined`, which is falsy.

```ts run
function withWrap(paise: number, giftWrap?: boolean): number {
  return giftWrap ? paise + 3000 : paise;
}
console.log(withWrap(35000));
console.log(withWrap(35000, true));
```

It prints `35000`, then `38000`. Optional parameters must come after the required ones.

```callout jargon
An **optional parameter** is one a caller may leave out, marked with `?`. Inside the function, it might be `undefined`, and TypeScript makes you allow for that.
```

### Syntax: default values

```ts
function splitBill(totalPaise: number, people = 2): number {
  return Math.floor(totalPaise / people);
}
```

A default value works as in JavaScript: it's used when the argument is left out. TypeScript also infers the type from it, so `people = 2` makes `people` a `number`. You could write `people: number = 2`, but you don't need to.

```ts run
function splitBill(totalPaise: number, people = 2): number {
  return Math.floor(totalPaise / people);
}
console.log(splitBill(89900));
console.log(splitBill(100000, 3));
```

It prints `44950`, then `33333`.

Watch a default and an optional parameter fill in, call by call:

```animate
title: Defaults and optional parameters, call by call
kind: code
language: ts
code: |
  function quoteLine(code: string, paise: number, qty = 1, wrap?: boolean): string {
    const total = paise * qty + (wrap ? 3000 * qty : 0);
    return `${code} x ${qty}: ${total}`;
  }
  console.log(quoteLine("BTY", 35000));
  console.log(quoteLine("KEY", 12000, 2, true));
steps:
  - line: 5
    say: The first call passes only two arguments, a code and a price.
  - line: 1
    vars: { code: '"BTY"', paise: 35000, qty: 1, wrap: undefined }
    say: qty was left out, so its default 1 is used. wrap was left out too, so it's undefined.
  - line: 2
    vars: { total: 35000 }
    say: undefined is falsy, so no gift wrap is added. The total is just 35000.
  - line: 5
    vars: { code: ~, paise: ~, qty: ~, wrap: ~, total: ~ }
    out: "BTY x 1: 35000"
    say: The function returns its line, and console.log prints it.
  - line: 1
    vars: { code: '"KEY"', paise: 12000, qty: 2, wrap: true }
    say: The second call passes all four. The default isn't needed this time.
  - line: 2
    vars: { total: 30000 }
    say: Two keychains are 24000, plus two wraps at 3000 each. That makes 30000.
  - line: 6
    vars: { code: ~, paise: ~, qty: ~, wrap: ~, total: ~ }
    out: "KEY x 2: 30000"
    say: TypeScript checked both calls before they ran. Every argument fitted its slot.
```

### Syntax: rest parameters

```ts
function sumAll(...amounts: number[]): number {
  return 0;
}
```

`...amounts` collects every argument into one array, as in JavaScript. Its type is `number[]`, which means "an array of numbers". You'll meet array types properly in the next ticket. Now `sumAll(15000, 12000)` is fine, and `sumAll(15000, "12000")` is a type error.

### Syntax: function types

```ts
let offer: (paise: number) => number;
```

Functions are values, so they have types too. `(paise: number) => number` means "a function that takes a number and gives back a number". Use it to type a parameter that is itself a function:

```ts run
function applyOffer(paise: number, offer: (paise: number) => number): number {
  return paise - offer(paise);
}
const diwali = (paise: number): number => Math.floor(paise / 10);
console.log(applyOffer(200000, diwali));
console.log(applyOffer(15000, (p) => 5000));
```

It prints `180000`, then `10000`. In the second call, `p` has no annotation, yet TypeScript knows it is a `number`. It reads that from `offer`'s type. Passing a function with the wrong shape, like one that takes text, is a type error.

```callout jargon
A **function type** describes a function's parameters and what it gives back, written like an arrow function: `(paise: number) => number`.
```

## Worked example

Mrs. Joshi orders two tote bags and gift-wrapped baby booties. Here is her total, with every function typed:

```ts run
const GIFT_WRAP = 3000;

function lineTotal(paise: number, qty: number, giftWrap = false): number {
  const wrap = giftWrap ? GIFT_WRAP * qty : 0;
  return paise * qty + wrap;
}

function sumAll(...amounts: number[]): number {
  let sum = 0;
  for (const amount of amounts) {
    sum += amount;
  }
  return sum;
}

function logTotal(label: string, paise: number): void {
  console.log(`${label}: ₹${(paise / 100).toFixed(2)}`);
}

const total = sumAll(lineTotal(89900, 2), lineTotal(35000, 1, true));
logTotal("Mrs. Joshi", total);
```

1. `lineTotal` has a default: `giftWrap = false`, so its type is `boolean`, inferred.
2. The tote bags are `lineTotal(89900, 2)`: `179800`. The booties are `lineTotal(35000, 1, true)`: `38000`.
3. `sumAll` collects both numbers into `amounts` and adds them: `217800`.
4. `logTotal` returns `void`. It only prints `Mrs. Joshi: ₹2178.00`.

Swap any two arguments, and `tsc` reports it before Mrs. Joshi ever sees a wrong bill.

## Try it

Run this. Then add a third parameter, `pickup?: boolean`, so a pickup from the stall pays no delivery at all. Call it with `true` and check that it prints `0`.

```ts run
function deliveryFee(subtotal: number, free = 99900): number {
  return subtotal > free ? 0 : 6000;
}
console.log(deliveryFee(50000));
console.log(deliveryFee(120000));
```

## Recap

- Annotate every parameter: `paise: number`. TypeScript then checks every call's arguments, and how many there are.
- A return type after the brackets, like `): number`, is the function's promise. `void` means it gives back nothing.
- `name?: string` is an optional parameter: it may be left out, and is then `undefined`.
- A default value like `people = 2` is used when the argument is left out, and gives the parameter its type.
- `...amounts: number[]` collects the arguments, and `(paise: number) => number` is the type of a function.

## Common mistakes

**1. Swapping the arguments.**

```ts
function withExtras(paise: number, giftWrap?: boolean): number {
  return giftWrap ? paise + 3000 : paise;
}
withExtras(true, 35000);
```

```text
error TS2345: Argument of type 'boolean' is not assignable to parameter of type 'number'.
```

This is the bug from the ticket. Pass the arguments in the order of the parameters: `withExtras(35000, true)`.

**2. Putting an optional parameter first.**

```ts
function stitch(name?: string, paise: number): number {
  return paise + 5000;
}
```

```text
error TS1016: A required parameter cannot follow an optional parameter.
```

Callers can only leave out arguments at the end. Put the optional ones last: `stitch(paise: number, name?: string)`.

**3. Forgetting to return on every path.**

```ts
function deliveryFee(subtotal: number): number {
  if (subtotal > 99900) {
    return 0;
  }
}
```

```text
error TS2366: Function lacks ending return statement and return type does not include 'undefined'.
```

A subtotal of ₹500 would get `undefined` back. Add the missing `return 6000;` at the end.

**4. Using an optional parameter as if it were always there.**

```ts
function stitchName(name?: string): string {
  return name.toUpperCase();
}
```

```text
error TS18048: 'name' is possibly 'undefined'.
```

When the caller leaves `name` out, there's nothing to upper-case. Check first: `return name ? name.toUpperCase() : "";`.
