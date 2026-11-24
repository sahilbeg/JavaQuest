## Hook

All day at the counter, Raju answers yes-or-no questions. Is this order above ₹999? Has she paid? Is "bag" the same item as "BAG"? Did he write a gift note, or leave it blank?

Code answers questions like these with `true` and `false`. JavaScript also has a famous trap in how it checks "is equal", and it has fooled many programmers. This ticket shows you the trap, and the safe way round it.

## Concept

### Syntax: true and false

```js
const paid = true;
```

A **boolean** has only two possible values: `true` or `false`. Notice there are no quotes. `"true"` in quotes is a string, not a boolean.

```callout jargon
A **boolean** is a value that is either `true` or `false`. It's named after George Boole, a mathematician who studied yes-or-no logic.
```

### Syntax: comparison operators

```js
const free = subtotal > 99900;
```

A **comparison** asks a question about two values and answers with a boolean.

| Operator | Meaning | Example (subtotal is 99900) | Result |
|---|---|---|---|
| `===` | equal to | `subtotal === 99900` | `true` |
| `!==` | not equal to | `subtotal !== 99900` | `false` |
| `>` | greater than | `subtotal > 99900` | `false` |
| `<` | less than | `subtotal < 99900` | `false` |
| `>=` | greater than or equal to | `subtotal >= 99900` | `true` |
| `<=` | less than or equal to | `subtotal <= 99900` | `true` |

"Free above ₹999" means `>`, so an order of exactly ₹999 still pays for delivery.

```callout warning
One `=` stores a value in a box. Three `===` ask "are these equal?" Mixing them up is the most common slip in JavaScript.
```

Watch the answers being stored like any other value:

```animate
title: Questions with true-or-false answers
kind: code
language: js
code: |
  const subtotal = 99900;
  const free = subtotal > 99900;
  const typed = "2";
  const same = Number(typed) === 2;
  console.log(free);
  console.log(same);
steps:
  - line: 1
    vars: { subtotal: 99900 }
    say: An order of exactly ₹999, stored as 99900 paise.
  - line: 2
    vars: { free: false }
    say: Is 99900 greater than 99900? No. The answer false goes into a box, just like a number would.
  - line: 3
    vars: { typed: '"2"' }
    say: The quantity a customer typed. From a form it always arrives as text, so it's "2", not 2.
  - line: 4
    vars: { same: true }
    say: Number turns "2" into 2. Now === compares two numbers, and they match.
  - line: 5
    out: "false"
    say: console.log prints the boolean in free.
  - line: 6
    out: "true"
    say: And the one in same.
```

### Syntax: === and !==

```js
const same = a === b;
```

`===` is **strict equality**. It's `true` only when both sides are the same type and the same value. `!==` is its opposite: "not strictly equal".

### The == trap

JavaScript also has `==`, with two equals signs. It's called **loose equality**, and it quietly converts types before comparing:

```js run
console.log("5" === 5);
console.log("5" == 5);
console.log(0 == "");
console.log(0 === "");
```

This prints `false`, `true`, `true`, `false`. With `==`, the text `"5"` equals the number 5, and an empty string equals 0. Those surprises cause real bugs.

```callout tip
Always use `===` and `!==`. When two values have different types, convert one yourself first, like `Number(typed) === 2`. Then you know exactly what's being compared.
```

### Comparing text

`===` compares strings letter by letter, and capitals count:

```js run
console.log("AMI" === "AMI");
console.log("ami" === "AMI");
console.log("ami".toUpperCase() === "AMI");
```

This prints `true`, `false`, `true`. That's why typed text gets cleaned up before you compare it.

### Syntax: typeof

```js
const kind = typeof value;
```

`typeof` tells you the type of a value, as a string:

```js run
console.log(typeof 15000);
console.log(typeof "AMI");
console.log(typeof true);
console.log(typeof undefined);
console.log(typeof null);
```

This prints `number`, `string`, `boolean`, `undefined` and `object`. That last one is a famous old mistake in JavaScript. `null` isn't really an object, but fixing it now would break old websites.

### undefined and null

JavaScript has two ways to say "no value":

- `undefined` means nothing has been put here yet. An empty `let` holds it, and so does a missing argument.
- `null` means "empty, on purpose". A programmer writes it to say "there is no gift note".

```js run
let note;
console.log(note);
note = null;
console.log(note);
console.log(null === undefined);
```

This prints `undefined`, `null`, `false`. They're two different values, so `===` keeps them apart.

```callout jargon
**null** is a value that means "no value here, on purpose". You write it yourself. JavaScript never puts it anywhere by surprise.
```

### Truthy and falsy

Any value can stand in for a boolean. `Boolean(value)` shows which way it goes:

```js run
console.log(Boolean(0));
console.log(Boolean(""));
console.log(Boolean("0"));
console.log(Boolean(" "));
console.log(Boolean(null));
console.log(Boolean(undefined));
console.log(Boolean(NaN));
console.log(Boolean(15000));
console.log(Boolean([]));
```

This prints `false false true true false false false true true`, one per line. Only six values count as false: `false`, `0`, `""`, `null`, `undefined` and `NaN`. Everything else counts as true, even `"0"`, a single space, and an empty list `[]`.

```callout jargon
A **falsy** value acts like `false` when JavaScript needs a yes or no. A **truthy** value acts like `true`. The next topic uses this inside `if`.
```

### NaN is never equal

`NaN` is so strange that it isn't even equal to itself. Check for it with `Number.isNaN`:

```js run
const qty = Number("two");
console.log(qty);
console.log(qty === NaN);
console.log(Number.isNaN(qty));
```

This prints `NaN`, `false`, `true`.

### Syntax: Number.isInteger

```js
const whole = Number.isInteger(value);
```

`Number.isInteger` is `true` only for a whole number. It never converts text first.

```js run
console.log(Number.isInteger(15000));
console.log(Number.isInteger(150.5));
console.log(Number.isInteger("15000"));
```

This prints `true`, `false`, `false`. It's a handy check that an amount really is whole paise.

## Worked example

A few checks the stall's code makes before pricing an order:

```js run
const subtotal = 150000;
const typedCode = " bag ";
const phone = null;
const diwaliOffer = subtotal > 150000;
const isBag = typedCode.trim().toUpperCase() === "BAG";
const noPhone = phone === null;
console.log("Diwali offer: " + diwaliOffer);
console.log("Tote bag: " + isBag);
console.log("Phone missing: " + noPhone);
console.log(typeof subtotal + " " + typeof typedCode);
```

1. Line 4: the offer is for orders above ₹1,500. This order is exactly ₹1,500, so `diwaliOffer` is `false`.
2. Line 5 cleans the typed code to `"BAG"` before comparing, so `isBag` is `true`.
3. Line 6: the customer gave no phone number, stored as `null`, so `noPhone` is `true`.
4. Lines 7 to 9 glue each boolean into text: `Diwali offer: false`, `Tote bag: true`, `Phone missing: true`.
5. Line 10 prints `number string`.

## Try it

Run this, then change `typed` to `"0"`, and then to `""`. Before each run, guess every line.

```js run
const typed = "2";
console.log(typed == 2);
console.log(typed === 2);
console.log(Number(typed) === 2);
console.log(Boolean(typed));
```

## Recap

- A boolean is `true` or `false`, and comparisons like `subtotal > 99900` produce one.
- Use `===` and `!==`. Loose `==` converts types first and gives surprises like `0 == ""`.
- `typeof` names a value's type. `undefined` means "not set yet", and `null` means "empty on purpose".
- Six values are falsy: `false`, `0`, `""`, `null`, `undefined` and `NaN`. Everything else is truthy.
- `NaN` never equals anything, so check it with `Number.isNaN`. `Number.isInteger` checks for whole numbers.

## Common mistakes

**1. Comparing with == and getting a surprise.**

```js
const typed = "";
console.log(typed == 0);
```

This prints `true`, so an empty box looks like a quantity of 0. Use `===`, which says `false` here, and convert text with `Number` when you mean a number.

**2. Comparing text from a form with a number.**

```js
const typed = "2";
console.log(typed === 2);
```

This prints `false`, because a string is never strictly equal to a number. Convert first: `Number(typed) === 2`.

**3. Checking for NaN with ===.**

```js
const qty = Number("two");
console.log(qty === NaN);
```

This prints `false`, even though `qty` is `NaN`. Use `Number.isNaN(qty)`.

**4. Treating 0 as "no answer".**

```js
const qty = 0;
console.log(Boolean(qty));
```

This prints `false`, but 0 is a real quantity. Falsy isn't the same as missing. To check for missing, compare with `undefined` or `null` directly.
