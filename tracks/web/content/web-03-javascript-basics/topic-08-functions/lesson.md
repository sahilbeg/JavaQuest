## Hook

Meera Aunty has a recipe card for every pattern: "Coaster: chain 6, join, 12 double crochet in the ring..." She wrote each card once. Now she just says "make a coaster" and follows it, every time.

A **function** is a recipe card for code. You write the steps once, give them a name, and run them whenever you need them, with different ingredients each time.

## Concept

```callout jargon
A **function** is a named block of code that does one job. **Calling** a function means running it: you write its name followed by round brackets.
```

### Syntax: a function declaration

```js
function name(parameter) {
  return parameter;
}
```

`function`, then a name, then round brackets, then a block. Writing this **declares** the function: JavaScript remembers the recipe, but doesn't run it yet. It runs only when something calls it.

```js run
function wrapFee(items) {
  return items * 3000;
}
console.log(wrapFee(1));
console.log(wrapFee(3));
```

This prints `3000`, then `9000`. One recipe, two calls, two different answers.

### Syntax: parameters and arguments

```js
function lineCost(paise, qty) {
  return paise * qty;
}
lineCost(15000, 2);
```

The names in the declaration's brackets, `paise` and `qty`, are **parameters**: empty boxes waiting for values. The values in the call, `15000` and `2`, are **arguments**. They fill the boxes in order: the first argument goes into the first parameter.

```callout jargon
A **parameter** is a box named in the function's declaration. An **argument** is the value you pass in when you call it.
```

### Syntax: return

```js
function check(qty) {
  return qty > 0;
}
```

`return` hands a value back to whoever called the function, and stops the function on the spot. Any lines after it don't run.

```js run
function check(qty) {
  if (qty === 0) {
    return "Empty";
  }
  return "Has items";
}
console.log(check(0));
console.log(check(2));
```

This prints `Empty`, then `Has items`. For `check(0)`, the first `return` ends the function, so it never reaches the second one. A function with no `return` at all hands back `undefined`.

### Missing arguments are undefined

If you call a function with fewer arguments than it has parameters, the extra parameters hold `undefined`:

```js run
function lineTotal(paise, qty) {
  return paise * qty;
}
console.log(lineTotal(15000, 2));
console.log(lineTotal(15000));
```

This prints `30000`, then `NaN`. In the second call, `qty` is `undefined`, and `15000 * undefined` is `NaN`.

### Syntax: default parameters

```js
function greet(name = "friend") {
  return "Hi " + name + "!";
}
```

A **default parameter** gives a box a value to use when its argument is left out, or is `undefined`.

```js run
function greet(name = "friend") {
  return "Hi " + name + "!";
}
console.log(greet("Asha"));
console.log(greet());
```

This prints `Hi Asha!`, then `Hi friend!`.

### Syntax: arrow functions

```js
const toRupees = (paise) => paise / 100;
```

An **arrow function** is a shorter way to write a function, stored in a `const`. With a single expression after `=>`, that expression's value is returned for you. With curly braces, you write `return` yourself.

```js run
const toRupees = (paise) => paise / 100;
const label = (code, qty) => {
  const word = qty === 1 ? "item" : "items";
  return `${qty} ${word} of ${code}`;
};
console.log(toRupees(89900));
console.log(label("KEY", 2));
```

This prints `899`, then `2 items of KEY`. You call an arrow function exactly like any other function.

### Functions calling functions

A function can call another function and use its answer. Watch JavaScript jump in and out:

```animate
title: One function calls another
kind: code
language: js
code: |
  function wrapFee(items) {
    return items * 3000;
  }
  function bill(paise, items) {
    const wrap = wrapFee(items);
    return paise + wrap;
  }
  const total = bill(45000, 2);
  console.log(total);
steps:
  - line: 1
    say: JavaScript reads both function declarations and remembers them. Nothing runs yet.
  - line: 8
    say: Line 8 calls bill with the arguments 45000 and 2. JavaScript jumps into bill.
  - line: 4
    vars: { paise: 45000, items: 2 }
    say: Inside bill, the parameters paise and items now hold the two arguments.
  - line: 5
    say: bill calls wrapFee(items), passing 2, and waits for the answer.
  - line: 2
    say: wrapFee works out 2 * 3000 and returns 6000 to bill.
  - line: 5
    vars: { wrap: 6000 }
    say: Back in bill, the answer 6000 goes into wrap.
  - line: 6
    say: bill returns 45000 + 6000 to line 8.
  - line: 8
    vars: { paise: ~, items: ~, wrap: ~, total: 51000 }
    say: bill is finished, so its boxes disappear. total gets 51000.
  - line: 9
    out: "51000"
    say: The total for a gift-wrapped amigurumi pair is printed.
```

### Scope: boxes live inside their function

A box made inside a function exists only inside that function. Outside, it's gone:

```js run crash
function bill() {
  const total = 45000;
  return total;
}
bill();
console.log(total);
```

JavaScript says `ReferenceError: total is not defined`. To use a function's answer outside, `return` it and store it: `const total = bill();`.

```callout jargon
**Scope** is the part of the code where a box can be used. A box made inside a function, or inside any `{ }` block, can only be used in there.
```

### Why the practice files say export

In this course's practice files, functions start with `export`, as in `export function lineTotal(paise, qty)`. `export` lets other files, like the checks, use the function. Web-04 covers it properly. For now, leave it in front of each function.

## Worked example

Three small functions, each doing one job, working together:

```js run
function itemPrice(code) {
  if (code === "CST") return 15000;
  if (code === "KEY") return 12000;
  return 0;
}

function lineCost(code, qty = 1) {
  return itemPrice(code) * qty;
}

const toRupees = (paise) => paise / 100;

const total = lineCost("CST", 2) + lineCost("KEY");
console.log(total);
console.log(toRupees(total) + " rupees");
```

1. `itemPrice` turns a code into paise. An `if` with one statement can skip the braces, as here.
2. `lineCost` calls `itemPrice`, then multiplies. `qty` defaults to 1.
3. `lineCost("CST", 2)` is 30000. `lineCost("KEY")` leaves out `qty`, so it's 1, giving 12000.
4. The program prints `42000`, then `420 rupees`.

## Try it

Run it. The second line says `NaN`. Give `paise` a default of `12000` so it works. Then rewrite `toRupees` as an arrow function.

```js run
function toRupees(paise) {
  return paise / 100;
}
function priceLine(name, paise) {
  return name + ": " + toRupees(paise) + " rupees";
}
console.log(priceLine("Tote bag", 89900));
console.log(priceLine("Keychain"));
```

## Recap

- `function name(parameters) { ... }` declares a function, and `name(arguments)` calls it.
- Arguments fill the parameters in order. A missing one is `undefined`, unless it has a default like `qty = 1`.
- `return` hands a value back and stops the function. Without it, the call gives `undefined`.
- `const f = (x) => x * 2;` is an arrow function, which returns its single expression for you.
- Boxes made inside a function only exist inside it.

## Common mistakes

**1. Forgetting return.**

```js run
function wrapFee(items) {
  items * 3000;
}
console.log(wrapFee(2));
```

This prints `undefined`. The sum is worked out and thrown away. Write `return items * 3000;`

**2. Leaving out the brackets when you call.**

```js run
function shopName() {
  return "Dhaaga & Co.";
}
console.log(shopName);
```

This prints `[Function: shopName]`, the recipe card itself instead of its result. Call it: `shopName()`.

**3. Printing instead of returning.**

```js run
function wrapFee(items) {
  console.log(items * 3000);
}
const fee = wrapFee(2);
console.log(fee + 6000);
```

This prints `6000`, then `NaN`. The function shows the number but hands back `undefined`, so the sum fails. A function that works something out should `return` it.

**4. Braces on an arrow function, but no return.**

```js run
const toRupees = (paise) => { paise / 100; };
console.log(toRupees(89900));
```

This prints `undefined`. With braces, an arrow function needs its own `return`. Drop the braces, `(paise) => paise / 100`, or add `return`.
