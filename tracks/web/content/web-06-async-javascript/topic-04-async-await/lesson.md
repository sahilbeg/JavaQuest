## Hook

A recipe reads top to bottom. "Boil the milk. Then add the tea leaves. Then add sugar." Each step waits for the one before, and you can read it in one go.

Promise chains work, but they don't read like a recipe. Every step hides inside a `.then(...)` callback.

`async` and `await` let you write slow, step-by-step work like a recipe again. The code reads top to bottom, and the page still never freezes.

## Concept

### Syntax: async function

```js
async function name(parameters) {
  // body
}
```

Put `async` before `function`, and the function **always returns a promise**.

- If the body returns a value, the promise is fulfilled with that value.
- If the body throws, the promise is rejected with that error.

```js run
async function getStockCount() {
  return 3;
}

const result = getStockCount();
console.log(result instanceof Promise);
result.then((count) => console.log(`Stock: ${count}`));
```

The function returned 3, but the caller got a promise of 3. Arrow functions can be async too: `const check = async (id) => { ... };`.

### Syntax: await

```js
const value = await promise;
```

`await` can only be used inside an `async` function. It pauses **that function** until the promise settles.

- If the promise is fulfilled, `await` gives you its value.
- If the promise is rejected, `await` throws its error, right on that line.

```callout jargon
**Await** means "wait here, in this function only, until this promise settles". The rest of the page keeps running while the function is paused.
```

### await pauses the function, not the page

When a function hits `await`, it steps off the call stack. Other code runs. When the promise settles, the function is put back on the stack and carries on from the same line.

```animate
title: await steps aside
kind: code
language: js
code: |
  async function checkStock() {
    console.log("Asking for stock");
    const stock = await Promise.resolve(3);
    console.log(`Stock: ${stock}`);
  }
  checkStock();
  console.log("Page still works");
steps:
  - line: 6
    say: The script calls checkStock. An async function starts running straight away, like any function.
  - line: 2
    out: "Asking for stock"
    say: The first line inside prints at once.
  - line: 3
    say: await meets a promise. checkStock pauses here and steps off the call stack.
  - line: 7
    out: "Page still works"
    say: The script carries on. The page is not frozen while checkStock waits.
  - line: 3
    vars: { stock: 3 }
    say: The promise is fulfilled. checkStock comes back, and await hands over the value 3.
  - line: 4
    out: "Stock: 3"
    say: The function carries on from where it paused and prints the stock.
```

### Syntax: try and catch with await

```js
try {
  const value = await promise;
} catch (error) {
  // the promise was rejected
}
```

Because a rejected promise makes `await` throw, you handle failures with the same `try...catch` you already know. `finally` works too.

```js run
async function pay(paise) {
  throw new Error("Bank server is busy");
}

async function checkout() {
  try {
    const receipt = await pay(45000);
    console.log(`Paid, ref ${receipt.ref}`);
  } catch (error) {
    console.log(`Could not pay: ${error.message}`);
  } finally {
    console.log("QR closed");
  }
}

checkout();
```

The line after `await pay(...)` never runs. The error jumps straight to `catch`, then `finally` runs.

### Steps that depend on each other

`await` shines when each step needs the one before. Reserve the item, then take the money, then confirm.

```js
async function buy(api, itemId, paise) {
  await api.reserve(itemId);
  const receipt = await api.pay(paise);
  return `Confirmed, ref ${receipt.ref}`;
}
```

Read it like a recipe. Payment never starts until the reservation is done. If either step throws, the function stops, and its promise is rejected.

### await inside a loop

`await` works inside `for...of`. Each round waits for its answer before the next round starts.

```js run
const shelf = { "DC-1042": 3, "DC-2001": 12 };

async function getStock(id) {
  return shelf[id];
}

async function printStock(ids) {
  for (const id of ids) {
    const stock = await getStock(id);
    console.log(`${id}: ${stock}`);
  }
  console.log("All checked");
}

printStock(["DC-1042", "DC-2001"]);
```

This is simple and keeps the order. It is also slow when each answer takes a second, because the requests queue up one behind another. A later ticket shows how to ask for them all at once.

```callout warning
`forEach` doesn't wait for an async callback. `ids.forEach(async (id) => { await ... })` starts every round at once and finishes before any of them. Use `for...of` when you need each round to wait.
```

### Forgetting await

Without `await`, you get the promise itself, not its value. A promise has no `ref` property, so you read `undefined`.

```js run
async function pay(paise) {
  return { ref: "UPI-5521" };
}

async function checkout() {
  const receipt = pay(45000);
  console.log(`UPI ref ${receipt.ref}`);
}

checkout();
```

That is exactly the "UPI ref undefined" on the receipt. One missing word.

## Worked example

Packing an order has two slow steps. Here they are, with `wait` from the last ticket standing in for the slow parts:

```js run
function wait(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function packOrder(orderId) {
  console.log("Picking Golu from the shelf");
  await wait(10);
  console.log("Wrapping in tissue paper");
  await wait(10);
  return `${orderId} packed`;
}

packOrder("order-17").then((message) => console.log(message));
console.log("Meanwhile, the counter serves the next customer");
```

Line by line:

1. `packOrder("order-17")` starts at once and prints the picking line.
2. `await wait(10)` pauses `packOrder`. Control goes back to the script.
3. The script prints the "Meanwhile" line. It never waited for the packing.
4. 10 ms later, `packOrder` carries on and prints the wrapping line, then pauses again.
5. After another 10 ms, it returns `"order-17 packed"`. That fulfils its promise, and the `then` callback prints it.

## Try it

In the worked example, remove both `await` words and run it. The two packing lines print together, and the message prints before the waits are over. Put them back. Then add a third step, `console.log("Sticking on the label")`, with its own `await wait(10)`.

## Recap

- An `async` function always returns a promise: a returned value fulfils it, and a thrown error rejects it.
- `await promise` pauses only that function until the promise settles, then gives you the value. The page keeps running.
- A rejected promise makes `await` throw, so use `try...catch...finally` around it.
- Steps that need each other read top to bottom, one `await` per step. Inside a loop, use `for...of`, not `forEach`.
- Forgetting `await` hands you a promise instead of its value, which shows up as `undefined` properties.

## Common mistakes

**1. Forgetting await.**

```js
const receipt = pay(45000);
console.log(receipt.ref);
```

This prints `undefined`. `receipt` is a promise, not the receipt. Fix: `const receipt = await pay(45000);` inside an async function.

**2. Using await in a function that isn't async.**

```js nocompile
function checkout() {
  const receipt = await pay(45000);
}
```

The file won't even load: `SyntaxError: await is only valid in async functions and the top level bodies of modules`. Fix: write `async function checkout()`.

**3. Leaving out try...catch.**

```js
async function checkout() {
  const receipt = await pay(45000);
  showMessage(`Paid, ref ${receipt.ref}`);
}
```

When the bank declines, `await` throws, `checkout`'s promise is rejected, and nothing tells the customer. The console shows `Uncaught (in promise)`. Fix: wrap the steps in `try...catch` and show a message in `catch`.

**4. Calling a cleanup step without await.**

```js
async function buyGolu(api) {
  try {
    await api.pay(45000);
    return "Paid";
  } catch (error) {
    api.release("DC-1042");
    return "Payment failed";
  }
}
```

The message comes back before the item is released, and if releasing fails, no one hears about it. Fix: `await api.release("DC-1042");` so the function only finishes once the clean-up has.
