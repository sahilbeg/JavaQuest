## Hook

Raju packs orders from three slips of paper. One says "count the Indigo items", one says "count the items in stock", one says "count items under ₹350". Each slip repeats the same five steps. Only one line differs.

Wouldn't it be easier to keep one slip that says "count the items that pass this test", and hand over the test separately? In JavaScript you can do exactly that, because a function is a value you can hand over.

## Concept

### A function is a value

You already store numbers and text in variables. You can store a function the same way, copy it to another variable, and call it through either name.

```js run
const shout = function (text) {
  return text.toUpperCase() + "!";
};
const alsoShout = shout;
console.log(typeof shout);
console.log(alsoShout("shabaash"));
```

It prints `function`, then `SHABAASH!`. `alsoShout` isn't a copy of the result. It is the same function under a second name.

```callout jargon
A **first-class value** is anything you can store in a variable, pass to a function or return from one. In JavaScript, functions are first-class values.
```

### Syntax: arrow functions

```js
(a, b) => a + b
```

An arrow function is a shorter way to write a function. Parameters go in brackets, then `=>`, then the body. When the body is one expression, its value is returned without writing `return`.

```js run
const lineTotal = (paise, qty) => paise * qty;
const rupees = (paise) => "₹" + (paise / 100).toFixed(2);
const hello = () => "Namaste";
console.log(lineTotal(15000, 2));
console.log(rupees(lineTotal(15000, 2)));
console.log(hello());
```

It prints `30000`, `₹300.00` and `Namaste`. With exactly one parameter, the brackets are optional: `paise => paise * 2` also works. With none, write empty brackets `()`.

```callout jargon
An **arrow function** is a function written as `(parameters) => body`. It is the usual way to write a small function that is passed around.
```

### Syntax: arrow bodies with braces

```js
const lineTotal = (paise, qty) => {
  const total = paise * qty;
  return total;
};
```

When you need more than one statement, put the body in braces. Now it is a normal block, so you must write `return`.

One catch: to return an object straight away, wrap it in round brackets. Without them, JavaScript reads the `{` as the start of a block.

```js run
const makeLine = (code, qty) => ({ code: code, qty: qty });
const line = makeLine("KEY", 3);
console.log(line.code, line.qty);
```

### Callbacks: handing a function over

A callback is a function you pass to another function, so it can call it for you. The other function decides when to call it and with what.

```js run
function forEachItem(items, action) {
  for (const item of items) {
    action(item);
  }
}
const cart = ["CST", "KEY", "AMI"];
forEachItem(cart, (code) => console.log("Packing " + code));
```

It prints `Packing CST`, `Packing KEY` and `Packing AMI`. `forEachItem` knows how to loop. The callback knows what to do with each item.

```callout jargon
A **callback** is a function given to another function as an argument, to be called back later. "Later" may mean once per item, or once when something finishes.
```

### Higher-order functions

A function that takes a function, or returns one, is called higher-order. Here is one that adds up any value you pick from each item. Watch how the callback runs once per item.

```animate
title: sumBy calls the callback for every item
kind: code
language: js
code: |
  function sumBy(items, valueOf) {
    let total = 0;
    for (const item of items) {
      total = total + valueOf(item);
    }
    return total;
  }
  const cart = [{ code: "CST", paise: 15000 }, { code: "KEY", paise: 12000 }];
  console.log(sumBy(cart, (item) => item.paise));
steps:
  - line: 9
    say: We call sumBy with the cart and an arrow function. The arrow isn't run yet. It travels in as the value of valueOf.
  - line: 2
    vars: { total: 0, valueOf: "(item) => item.paise" }
    say: Inside sumBy, valueOf holds our arrow function, and total starts at 0.
  - line: 3
    vars: { item: '{ code: "CST", paise: 15000 }' }
    say: The loop takes the first item, the coaster set.
  - line: 4
    vars: { total: 15000 }
    say: valueOf(item) calls our arrow, which returns 15000. total becomes 15000.
  - line: 3
    vars: { item: '{ code: "KEY", paise: 12000 }' }
    say: Next comes the keychain.
  - line: 4
    vars: { total: 27000 }
    say: The arrow runs again and returns 12000. total is now 27000.
  - line: 6
    say: There are no more items, so sumBy returns 27000.
  - line: 9
    vars: { total: ~, item: ~, valueOf: ~ }
    out: "27000"
    say: console.log prints the result. Pass (item) => 1 instead, and the same sumBy would count the items.
```

```callout jargon
A **higher-order function** takes a function as a parameter, returns a function, or both.
```

### Returning a function

A higher-order function can also build and return a new function. Combined with a closure, it makes ready-to-use tools.

```js run
function makeDiscount(percent) {
  return (paise) => paise - Math.floor((paise * percent) / 100);
}
const tenOff = makeDiscount(10);
const twentyOff = makeDiscount(20);
console.log(tenOff(89900));
console.log(twentyOff(89900));
```

It prints `80910` and `71920`. Each returned arrow remembers its own `percent`.

### Passing a function, not calling it

`getPaise` is the function. `getPaise(item)` runs it and gives back a number. To hand a function over, write its name with no brackets.

```js
const getPaise = (item) => item.paise;
const item = { paise: 15000 };
console.log(typeof getPaise);   // function: the function itself
console.log(getPaise(item));    // 15000: what it returns
```

## Worked example

Three totals from one `sumBy`, each with a different callback.

```js run
function sumBy(items, valueOf) {
  let total = 0;
  for (const item of items) {
    total = total + valueOf(item);
  }
  return total;
}

function withWrap(valueOf) {
  return (line) => valueOf(line) + 3000 * line.qty;
}

const cart = [
  { code: "BAG", paise: 89900, qty: 1 },
  { code: "CST", paise: 15000, qty: 2 },
];

const lineTotal = (line) => line.paise * line.qty;
const countItems = (line) => line.qty;

console.log(sumBy(cart, lineTotal));
console.log(sumBy(cart, countItems));
console.log(sumBy(cart, withWrap(lineTotal)));
```

Line by line:

1. `sumBy` loops and adds whatever `valueOf` returns for each line.
2. `lineTotal` returns paise times quantity, so the first total is 89900 + 30000 = `119900`.
3. `countItems` returns the quantity, so the second total is 1 + 2 = `3`.
4. `withWrap(lineTotal)` is called first. It returns a new arrow that adds ₹30 gift wrap per item.
5. That arrow goes into `sumBy`, giving 92900 + 36000 = `128900`.

## Try it

Make a `fiveOff` from `makeDiscount` and print the price of a ₹450 amigurumi with it. Then write `makeFlatOff(paise)` that returns an arrow taking a fixed amount off any price.

```js run
function makeDiscount(percent) {
  return (paise) => paise - Math.floor((paise * percent) / 100);
}
const tenOff = makeDiscount(10);
console.log(tenOff(45000));
```

## Recap

- Functions are values: you can store them in variables, pass them in and return them.
- An arrow function is `(parameters) => body`. One expression is returned for you; a body in braces needs `return`.
- A callback is a function you hand to another function, which calls it for you.
- A higher-order function takes or returns a function, like `sumBy` or `makeDiscount`.
- Pass a function by its name, without brackets. Brackets call it.

## Common mistakes

**1. Calling the callback instead of passing it.**

```js run crash
function getPaise(item) {
  return item.paise;
}
function sumBy(items, valueOf) {
  let total = 0;
  for (const item of items) {
    total = total + valueOf(item);
  }
  return total;
}
console.log(sumBy([{ paise: 15000 }], getPaise()));
```

Node stops with `TypeError: Cannot read properties of undefined (reading 'paise')`. `getPaise()` ran straight away, with no item. Fix: pass `getPaise`, without brackets.

**2. Forgetting return in a body with braces.**

```js run
const double = (x) => { x * 2 };
console.log(double(15000));
```

It prints `undefined`. A body in braces returns nothing unless you say `return`. Fix: `(x) => x * 2`, or `(x) => { return x * 2; }`.

**3. Returning an object without round brackets.**

```js run
const makeLine = (code) => { code: code };
console.log(makeLine("KEY"));
```

It prints `undefined`. JavaScript read the braces as a block, not an object. Fix: `(code) => ({ code: code })`.
