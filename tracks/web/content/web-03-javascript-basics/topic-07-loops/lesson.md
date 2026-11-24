## Hook

A WhatsApp order comes in: a coaster set, two keychains and an amigurumi. Raju opens the calculator on his phone and types 150, plus 120, plus 120, plus 450. One small step, repeated once for each item.

A big Diwali order might have twenty items. Code does that kind of repeating with a **loop**, and it never gets bored or loses count.

## Concept

```callout jargon
A **loop** runs the same block of code again and again. Each run is called an **iteration**, or a round.
```

### A first look at arrays

```js
const prices = [15000, 12000, 45000];
```

Loops usually walk through a list. In JavaScript, a list of values in square brackets is called an **array**. `prices[0]` is the first value, and `prices.length` is how many values there are. Topic 9 covers arrays fully.

```callout jargon
An **array** is a list of values in one box, written in square brackets and separated by commas.
```

### Syntax: for...of

```js
for (const item of list) {
  // runs once for each item in the list
}
```

`for...of` takes the values of a list one at a time. Each round, the next value goes into the box named before `of`, and the block runs.

```js run
const prices = [15000, 12000, 45000];
for (const price of prices) {
  console.log(price);
}
```

This prints `15000`, `12000` and `45000`, one per line. The box `price` is a fresh `const` in every round, so it never needs reassigning.

### Syntax: an accumulator

```js
let total = 0;
```

To add up a list, make a box **before** the loop, starting at 0. Then each round adds one value to it. A box that collects a result like this is called an **accumulator**.

Watch the total grow, round by round:

```animate
title: Adding up a WhatsApp order
kind: code
language: js
code: |
  const prices = [15000, 12000, 45000];
  let total = 0;
  for (const price of prices) {
    total = total + price;
  }
  console.log(total);
steps:
  - line: 1
    objects: { pr: { type: Array, fields: { "0": 15000, "1": 12000, "2": 45000 } } }
    vars: { prices: "@pr" }
    say: An array of three prices in paise. The box prices points to the list.
  - line: 2
    vars: { total: 0 }
    say: The accumulator starts at 0, before the loop begins.
  - line: 3
    vars: { price: 15000 }
    say: Round 1. for...of takes the first value and puts it in price.
  - line: 4
    vars: { total: 15000 }
    say: 0 + 15000 is 15000.
  - line: 3
    vars: { price: 12000 }
    say: Round 2. The next value goes into price.
  - line: 4
    vars: { total: 27000 }
    say: 15000 + 12000 is 27000. The total keeps what earlier rounds added.
  - line: 3
    vars: { price: 45000 }
    say: Round 3, the last value.
  - line: 4
    vars: { total: 72000 }
    say: 27000 + 45000 is 72000.
  - line: 6
    vars: { price: ~ }
    out: "72000"
    say: No values are left, so the loop ends. price is gone, but total was made outside the loop, so it stays.
```

### Syntax: the counting for loop

```js
for (let i = 0; i < 3; i++) {
  // i is 0, then 1, then 2
}
```

The classic `for` loop counts. Its brackets hold three parts, split by semicolons:

1. `let i = 0` runs once, at the start.
2. `i < 3` is checked before every round. The loop stops when it's false.
3. `i++` runs after every round. It adds 1 to `i`, just like `i = i + 1`.

Use it when you need each value's **position** as well as the value:

```js run
const codes = ["CST", "KEY", "AMI"];
for (let i = 0; i < codes.length; i++) {
  console.log(i + ": " + codes[i]);
}
```

This prints `0: CST`, `1: KEY` and `2: AMI`. Positions start at 0, so the last one is `codes.length - 1`. That's why the check is `<`, not `<=`.

### Syntax: while

```js
while (condition) {
  // repeats while the condition is truthy
}
```

`while` repeats as long as its condition stays truthy. Use it when you don't know in advance how many rounds you need. Meera Aunty saves ₹250 a week for a ₹1,200 stall umbrella:

```js run
const goal = 120000;
let saved = 0;
let weeks = 0;
while (saved < goal) {
  saved = saved + 25000;
  weeks = weeks + 1;
}
console.log(weeks + " weeks");
console.log(saved);
```

This prints `5 weeks`, then `125000`. After 4 weeks she has ₹1,000, which isn't enough, so the loop runs a fifth time.

```callout warning
Something inside a `while` loop must change its condition. If nothing does, the loop never ends and the page freezes. That's called an **infinite loop**.
```

### Syntax: break and continue

```js
for (const price of prices) {
  if (price === 0) {
    continue;
  }
  if (price > 50000) {
    break;
  }
}
```

- `continue` skips the rest of this round and jumps to the next one.
- `break` leaves the loop completely.

```js run
const prices = [15000, 0, 12000, 89900, 35000];
let total = 0;
for (const price of prices) {
  if (price === 0) {
    continue;
  }
  if (price > 50000) {
    console.log("Big item found, stopping");
    break;
  }
  total = total + price;
}
console.log(total);
```

The `0` is skipped. The loop stops at `89900`, so `35000` is never reached. It prints `Big item found, stopping`, then `27000`.

Inside a function, `return` also leaves the loop, and the whole function with it.

### Which loop to use

- Every value in a list, one by one: `for...of`.
- You need each value's position: the counting `for`.
- You don't know how many rounds it takes: `while`.

## Worked example

How many keychains are in this WhatsApp order, and what do they cost?

```js run
const order = ["KEY", "CST", "KEY", "AMI", "KEY"];
let keychains = 0;
for (const code of order) {
  if (code === "KEY") {
    keychains = keychains + 1;
  }
}
console.log("Keychains: " + keychains);
console.log("Cost: " + keychains * 12000 + " paise");
```

1. Line 2 makes a counter, a kind of accumulator, starting at 0.
2. The loop looks at each code. Only `"KEY"` adds 1 to the counter.
3. After 5 rounds, `keychains` is 3. Line 8 prints `Keychains: 3`.
4. In line 9, `*` runs before `+`, so it prints `Cost: 36000 paise`.

## Try it

Run it. Then add the tote bag, `89900`, to the list. Finally, make the loop skip any price under 13000 with `continue`.

```js run
const order = [15000, 12000, 12000, 45000];
let total = 0;
let count = 0;
for (const price of order) {
  total = total + price;
  count = count + 1;
}
console.log(count + " items, " + total + " paise");
```

## Recap

- `for (const x of list)` runs once for each value in a list.
- An accumulator is a box made before the loop, like `let total = 0;`, that each round adds to.
- `for (let i = 0; i < list.length; i++)` counts positions, from 0 up to `length - 1`.
- `while (condition)` repeats until the condition is false, so something inside must change it.
- `continue` skips to the next round, and `break` leaves the loop.

## Common mistakes

**1. Going one step too far.**

```js run
const prices = [15000, 12000];
let total = 0;
for (let i = 0; i <= prices.length; i++) {
  total = total + prices[i];
}
console.log(total);
```

This prints `NaN`. With `<=`, `i` reaches 2, but the last position is 1. `prices[2]` is `undefined`, and a number plus `undefined` is `NaN`. Use `i < prices.length`.

**2. Making the total inside the loop.**

```js run
const prices = [15000, 12000];
for (const price of prices) {
  let total = 0;
  total = total + price;
  console.log(total);
}
```

This prints `15000`, then `12000`, with no running total. A new `total` starts at 0 in every round. Make the accumulator once, before the loop.

**3. A while loop that never ends.**

```js
let grams = 100;
while (grams > 0) {
  console.log("Crocheting...");
}
```

Nothing changes `grams`, so the condition is true forever and the page freezes. Use up some yarn each round: `grams = grams - 40;`
