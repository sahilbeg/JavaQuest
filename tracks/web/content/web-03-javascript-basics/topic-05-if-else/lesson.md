## Hook

A sign hangs on the FC Road stall: "Open Saturday and Sunday, 10:00 to 20:00". A customer reads it, checks the day and the time, and decides whether to go.

The website has to make choices like that all the time. Is this a pickup? Is the order big enough for free delivery? Is there a gift note to print? This ticket teaches code to choose.

## Concept

A choice needs two things: a question with a yes-or-no answer, and different code for each answer.

### Syntax: if

```js
if (condition) {
  // runs only when the condition is truthy
}
```

The condition goes inside round brackets. The code inside the curly braces runs only when the condition is truthy. Otherwise JavaScript skips the whole block.

```js
const subtotal = 120000;
if (subtotal > 99900) {
  console.log("Free delivery!");
}
```

```callout jargon
A **block** is the code between `{` and `}`. It runs as one group: all of it, or none of it.
```

### Syntax: if and else

```js
if (condition) {
  // runs when the condition is truthy
} else {
  // runs when it isn't
}
```

`else` gives the program a second path. Exactly one of the two blocks runs, never both and never neither.

```js run
const paid = false;
if (paid) {
  console.log("Pack the order");
} else {
  console.log("Send a UPI reminder");
}
console.log("Order checked");
```

This prints `Send a UPI reminder`, then `Order checked`. After the if-else, JavaScript carries on with the next line, whichever block ran.

### Syntax: else if

```js
if (first) {
  // first is truthy
} else if (second) {
  // first isn't, but second is
} else {
  // neither is
}
```

JavaScript checks each condition from top to bottom. It runs the first block whose condition is truthy, then skips the rest of the chain. Watch it check the Indigo yarn:

```animate
title: The first true condition wins
kind: code
language: js
code: |
  const grams = 120;
  if (grams < 40) {
    console.log("Too little for a toy");
  } else if (grams < 200) {
    console.log("Running low");
  } else {
    console.log("Plenty");
  }
  console.log("Yarn checked");
steps:
  - line: 1
    vars: { grams: 120 }
    say: There are 120 grams of Indigo yarn left in the box.
  - line: 2
    say: Is 120 less than 40? No, so JavaScript skips this block without running it.
  - line: 4
    say: Is 120 less than 200? Yes. This is the first true condition in the chain.
  - line: 5
    out: Running low
    say: JavaScript runs this block, and only this block.
  - line: 9
    out: Yarn checked
    say: It jumps past the rest of the chain, else included, and carries on.
```

The order matters. If you checked `grams < 200` first, 30 grams would say "Running low" instead of "Too little for a toy".

### Syntax: &&, || and !

```js
const open = weekend && hour >= 10;
```

**Logical operators** join booleans together:

- `&&` is AND. It's truthy only when both sides are, as in `paid && inStock`.
- `||` is OR. It's truthy when at least one side is, as in `day === "Sat" || day === "Sun"`.
- `!` is NOT. It flips a value, so `!paid` is true when `paid` is false.

```js run
const day = "Sun";
const hour = 21;
console.log(day === "Sat" || day === "Sun");
console.log(hour >= 10 && hour < 20);
console.log(!(hour >= 10 && hour < 20));
```

This prints `true`, `false`, `true`. It's Sunday, but 21:00 is after closing time.

```callout analogy
`&&` is a door with two locks: you need both keys. `||` is a house with two doors: either one lets you in.
```

### && and || stop early

JavaScript reads `&&` and `||` from left to right, and stops as soon as it knows the answer. This is called **short-circuiting**.

- `a && b`: if `a` is falsy, the answer is already falsy, so `b` never runs.
- `a || b`: if `a` is truthy, the answer is already truthy, so `b` never runs.

That lets the left side act as a **guard** for the right side:

```animate
title: The guard on the left
kind: code
language: js
code: |
  const note = null;
  if (note && note.length > 20) {
    console.log("Long note");
  } else {
    console.log("Short or no note");
  }
steps:
  - line: 1
    vars: { note: "null" }
    say: The customer wrote no gift note, so note holds null.
  - line: 2
    say: JavaScript checks the left side first. null is falsy, so the whole && is already false.
  - line: 2
    say: It never runs note.length. Reading length from null would have crashed the page.
  - line: 4
    say: The condition was falsy, so the else block runs.
  - line: 5
    out: Short or no note
    say: A polite answer instead of a crash. Always put the safety check on the left.
```

### Conditions use truthy and falsy

A condition doesn't have to be a real boolean. JavaScript uses the truthy and falsy rules from the last topic:

```js run
const phone = "";
if (phone) {
  console.log("We'll text " + phone);
} else {
  console.log("Please add a phone number");
}
```

An empty string is falsy, so this prints `Please add a phone number`. The same `if` treats `undefined` and `null` the same way.

### Syntax: a default with ??

```js
const shown = value ?? "fallback";
```

`??` gives the right side only when the left side is `null` or `undefined`. `||` gives the right side for any falsy value, which includes `0` and `""`.

```js run
const qty = 0;
console.log(qty || 1);
console.log(qty ?? 1);
```

This prints `1`, then `0`. A quantity of 0 is a real answer, so `??` keeps it, while `||` throws it away.

```callout jargon
**Nullish** means "null or undefined". `??` is called the nullish coalescing operator: it falls back only for those two.
```

### Returning early

Inside a function, `return` ends the function at once. So an `if` that returns can stand without an `else`:

```js
function wrapFee(giftWrap) {
  if (!giftWrap) {
    return 0;
  }
  return 3000;
}
```

If `giftWrap` is falsy, the function returns `0` and stops. Only otherwise does it reach `return 3000;`.

## Worked example

The Aundh shop opens Tuesday to Sunday, 11:00 to 21:00. The stall opens Saturday and Sunday, 10:00 to 20:00. Where can a customer go?

```js run
const day = "Mon";
const hour = 12;
if (day !== "Mon" && hour >= 11 && hour < 21) {
  console.log("The Aundh shop is open");
} else if ((day === "Sat" || day === "Sun") && hour >= 10 && hour < 20) {
  console.log("Try the FC Road stall");
} else {
  console.log("Closed right now. Order on WhatsApp!");
}
```

1. Line 3 asks three things joined with `&&`. `day !== "Mon"` is false, so JavaScript stops there.
2. Line 5 asks about the stall. The brackets make the `||` run before the `&&`s. It's not a weekend, so this is false too.
3. No condition matched, so the `else` runs and prints `Closed right now. Order on WhatsApp!`

## Try it

Change `day` to `"Sun"` and `hour` to `10`, then run it again. Then try `"Sun"` at `20`.

```js run
const day = "Sat";
const hour = 9;
const weekend = day === "Sat" || day === "Sun";
if (weekend && hour >= 10 && hour < 20) {
  console.log("The stall is open");
} else if (weekend) {
  console.log("The stall is closed at this hour");
} else {
  console.log("The stall only opens at weekends");
}
```

## Recap

- `if` runs a block only when its condition is truthy, and `else` runs when it isn't.
- `else if` checks more conditions in order, and the first truthy one wins.
- `&&` needs both, `||` needs either, and `!` flips a value.
- `&&` and `||` stop as soon as they know the answer, so put guards like `note &&` on the left.
- `??` falls back only for `null` and `undefined`, while `||` falls back for every falsy value.

## Common mistakes

**1. Writing day === "Sat" || "Sun".**

```js run
const day = "Mon";
console.log(day === "Sat" || "Sun");
```

This prints `Sun`. `day === "Sat"` is false, so `||` hands back the right side, `"Sun"`, which is truthy. Inside an `if`, the stall would be open every day. Write the comparison twice: `day === "Sat" || day === "Sun"`.

**2. Chaining two comparisons like in maths.**

```js run
const hour = 25;
console.log(10 <= hour < 20);
```

This prints `true` for 25:00! JavaScript works out `10 <= 25` first, which is `true`. Then `true < 20` turns `true` into `1`, and `1 < 20` is true. Join the two comparisons with `&&`: `hour >= 10 && hour < 20`.

**3. Using = instead of === in a condition.**

```js
let qty = 3;
if (qty = 0) {
  console.log("Cart is empty");
}
console.log(qty);
```

This prints `0`. The `=` stored 0 in `qty`, and 0 is falsy, so the block was skipped. Your cart lost its items too. Compare with `qty === 0`.

**4. A semicolon right after the condition.**

```js
const paid = false;
if (paid); {
  console.log("Pack the order");
}
```

This prints `Pack the order` even though nothing was paid. The `;` ends the `if` with an empty step, so the braces always run. Remove the semicolon after `)`.
