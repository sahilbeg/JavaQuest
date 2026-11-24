## Hook

A customer at the stall asks for three Golu elephants. There is one on the table. A good assistant says, "Sorry, only one left. Would you like it?" A bad one takes the money for three and hopes nobody notices.

Code faces the same choice. When a function is asked to do something impossible, it should say so loudly, not carry on with nonsense like a stock of -2. In JavaScript, saying so loudly is called throwing an error.

## Concept

### Errors stop the program

You have already seen errors: `TypeError`, `ReferenceError`. When one happens and nothing handles it, JavaScript stops and prints it. That is annoying, but it is much better than quietly giving a wrong answer.

```callout jargon
An **error** (also called an **exception**) is an object that describes something that went wrong. When it is **thrown**, normal running stops until some code **catches** it.
```

### Syntax: throw

```js
throw new Error("message");
```

`throw` stops the current function at once. Nothing after it runs. The error travels back to whoever called the function, then to their caller, and so on.

```js
function reserve(stock, qty) {
  if (qty > stock) {
    throw new Error("Only " + stock + " left");
  }
  return stock - qty;
}
```

If `qty` is too big, `reserve` never reaches `return`. There is no way for a wrong stock number to come out.

### Syntax: try and catch

```js
try {
  // code that might throw
} catch (error) {
  // what to do if it did
}
```

`try` runs its block. If anything inside throws, JavaScript jumps straight to `catch`, handing it the error. If nothing throws, `catch` is skipped. Watch both paths.

```animate
title: An error jumps to catch
kind: code
language: js
code: |
  function reserve(stock, qty) {
    if (qty > stock) {
      throw new Error("Only " + stock + " left");
    }
    return stock - qty;
  }
  try {
    console.log(reserve(3, 2));
    console.log(reserve(1, 3));
    console.log("never printed");
  } catch (error) {
    console.log("Sorry: " + error.message);
  }
  console.log("The shop stays open");
steps:
  - line: 8
    say: try runs its block. The first call asks for 2 when 3 are in stock.
  - line: 2
    vars: { stock: 3, qty: 2 }
    say: 2 is not more than 3, so there is no problem.
  - line: 5
    say: reserve returns 3 - 2, which is 1.
  - line: 8
    vars: { stock: ~, qty: ~ }
    out: "1"
    say: The first line prints 1.
  - line: 3
    vars: { stock: 1, qty: 3 }
    say: The second call asks for 3 when only 1 is left. throw stops reserve right here.
  - line: 11
    vars: { stock: ~, qty: ~, error: '"Error: Only 1 left"' }
    say: JavaScript skips the rest of try, including line 10, and jumps to catch with the error.
  - line: 12
    out: "Sorry: Only 1 left"
    say: catch reads the error's message and answers kindly.
  - line: 14
    vars: { error: ~ }
    out: "The shop stays open"
    say: After catch, the program carries on as normal. It didn't crash.
```

### Error objects: name and message

Every error has a `name`, which says what kind it is, and a `message`, which says what happened. JavaScript throws a few kinds by itself.

```js run
const errors = [];
try {
  null.length;
} catch (error) {
  errors.push(error.name);
}
try {
  (12000).toFixed(200);
} catch (error) {
  errors.push(error.name);
}
try {
  missingVariable + 1;
} catch (error) {
  errors.push(error.name);
}
console.log(errors.join(", "));
```

It prints `TypeError, RangeError, ReferenceError`. A `TypeError` means a value was the wrong kind, like `null`. A `RangeError` means a number was out of the allowed range. A `ReferenceError` means a name doesn't exist.

```callout tip
Throw the built-in kinds yourself when they fit. A quantity of -1 is out of range, so `throw new RangeError("qty must be a whole number above 0")` tells the reader exactly what went wrong.
```

### Syntax: your own error class

```js
class OutOfStockError extends Error {
  constructor(id) {
    super(id + " is sold out");
    this.name = "OutOfStockError";
  }
}
```

`extends Error` makes a new class that is a kind of `Error`. It gets `message` and everything else an error has. In its constructor, `super(...)` runs `Error`'s own constructor with the message. It must come before any use of `this`.

```js run
class OutOfStockError extends Error {
  constructor(id) {
    super(id + " is sold out");
    this.name = "OutOfStockError";
    this.id = id;
  }
}
try {
  throw new OutOfStockError("DC-1047");
} catch (error) {
  console.log(error.name);
  console.log(error.message);
  console.log(error instanceof OutOfStockError, error instanceof Error);
}
```

It prints `OutOfStockError`, `DC-1047 is sold out`, then `true true`.

```callout jargon
**extends** makes one class build on another. **super(...)** calls the constructor of the class you built on. A **custom error** is your own class that extends `Error`.
```

### Catch what you can handle, throw the rest

A `catch` receives every kind of error. Usually you only know how to handle some of them. Check the kind with `instanceof`, and throw the others again so they aren't hidden.

```js run
class OutOfStockError extends Error {
  constructor(id) {
    super(id + " is sold out");
    this.name = "OutOfStockError";
  }
}
function placeOrder(id, qty) {
  if (!Number.isInteger(qty) || qty < 1) {
    throw new RangeError("qty must be a whole number above 0");
  }
  throw new OutOfStockError(id);
}
function safeOrder(id, qty) {
  try {
    placeOrder(id, qty);
    return "placed";
  } catch (error) {
    if (error instanceof OutOfStockError) {
      return "sold out, sorry";
    }
    throw error;
  }
}
console.log(safeOrder("DC-1047", 1));
try {
  safeOrder("DC-1047", 0);
} catch (error) {
  console.log("A bug to fix: " + error.message);
}
```

Sold out is normal on a busy day, so `safeOrder` turns it into a polite answer. A quantity of 0 is a bug in the page, so it is thrown again, and the outer `catch` sees it. `Number.isInteger(qty)` is `true` only for whole numbers.

```callout jargon
To **rethrow** is to `throw` an error again from inside `catch`, because this code can't fix it.
```

### Syntax: finally

```js
try {
  // work
} catch (error) {
  // handle
} finally {
  // always runs
}
```

A `finally` block runs at the end whatever happened: success, a caught error, or even a `return`. Use it to tidy up.

```js run
function packOrder(id, fail) {
  console.log("Open the packing table for " + id);
  try {
    if (fail) {
      throw new Error("tape ran out");
    }
    console.log("Packed " + id);
  } catch (error) {
    console.log("Problem: " + error.message);
  } finally {
    console.log("Close the packing table");
  }
}
packOrder("order-17", false);
packOrder("order-18", true);
```

The table is closed after both orders, the good one and the failed one.

## Worked example

Customers type a quantity into the order form. This checks it, with a custom error for bad input.

```js run
class BadQuantityError extends Error {
  constructor(text) {
    super('"' + text + '" is not a quantity');
    this.name = "BadQuantityError";
  }
}

function parseQty(text) {
  const qty = Number(text);
  if (!Number.isInteger(qty) || qty < 1) {
    throw new BadQuantityError(text);
  }
  return qty;
}

for (const typed of ["2", "two", "0", "1.5"]) {
  try {
    const qty = parseQty(typed);
    console.log("OK: " + qty);
  } catch (error) {
    if (error instanceof BadQuantityError) {
      console.log(error.name + ": " + error.message);
    } else {
      throw error;
    }
  }
}
```

Line by line:

1. `BadQuantityError` extends `Error`. `super` sets the message, then the constructor sets the name.
2. `parseQty` turns the text into a number. `Number("two")` is `NaN`, which is not a whole number.
3. Anything that isn't a whole number of at least 1 is thrown as a `BadQuantityError`.
4. The loop tries each typed value. `"2"` prints `OK: 2`.
5. `"two"`, `"0"` and `"1.5"` each land in `catch` and print a clear message. Any other kind of error would be rethrown.

## Try it

Add `"-3"` and `"10"` to the list. Then add a `finally` block that prints `checked` after every value, and run it.

```js run
function parseQty(text) {
  const qty = Number(text);
  if (!Number.isInteger(qty) || qty < 1) {
    throw new RangeError('"' + text + '" is not a quantity');
  }
  return qty;
}
for (const typed of ["2", "two"]) {
  try {
    console.log("OK: " + parseQty(typed));
  } catch (error) {
    console.log(error.message);
  }
}
```

## Recap

- `throw new Error("...")` stops a function at once and sends the error up to its callers.
- `try { } catch (error) { }` catches a thrown error so the program can answer kindly instead of crashing.
- Every error has a `name` and a `message`. JavaScript throws `TypeError`, `RangeError` and `ReferenceError` itself.
- `class OutOfStockError extends Error` makes your own kind of error. Call `super(message)` first in its constructor.
- Handle the kinds you expect, checked with `instanceof`, and rethrow the rest. `finally` always runs.

## Common mistakes

**1. Throwing a plain string.**

```js run
try {
  throw "Out of stock";
} catch (error) {
  console.log(error.message);
}
```

It prints `undefined`. A string has no `message` and no `name`, and `instanceof` checks can't tell kinds apart. Fix: always throw an error object, `throw new Error("Out of stock");`.

**2. An empty catch that hides the problem.**

```js run
function reserve(stock, qty) {
  if (qty > stock) {
    throw new Error("Only " + stock + " left");
  }
  return stock - qty;
}
let left = 3;
try {
  left = reserve(left, 5);
} catch (error) {}
console.log("Order placed! Left: " + left);
```

It prints `Order placed! Left: 3`, which is a lie. The error was caught and thrown away. Fix: tell the customer in `catch`, or rethrow the error.

**3. Using this before super.**

```js run crash
class OutOfStockError extends Error {
  constructor(id) {
    this.name = "OutOfStockError";
  }
}
throw new OutOfStockError("DC-1042");
```

Node stops with `ReferenceError: Must call super constructor in derived class before accessing 'this' or returning from derived constructor`. Fix: make `super(id + " is sold out");` the first line of the constructor.
