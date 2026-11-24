## Hook

At a busy food stall, you pay and get a token with a number on it. The token isn't food. It is a promise of food.

You can sit down and chat while you wait. Later, one of two things happens. Your number is called and you collect your dosa. Or someone comes over and says, "Sorry, the batter ran out."

A JavaScript **promise** is that token. It stands for a result that isn't ready yet, and it tells you when the result arrives, or when it never will.

## Concept

### What a promise is

```callout jargon
A **promise** is an object that stands for a result you will get later. Slow jobs, like asking a server for stock, give you a promise straight away instead of making you wait.
```

A promise is always in one of three states:

```callout jargon
**Pending**: still waiting. **Fulfilled**: it worked, and it holds a value. **Rejected**: it failed, and it holds a reason, usually an `Error`. A promise that is fulfilled or rejected is called **settled**.
```

A promise settles only once. After that, its state and value never change.

```animate
title: The life of a payment promise
kind: scene
nodes:
  - { id: page, label: Checkout page, emoji: "🛒", value: "showing QR" }
  - { id: promise, label: Payment promise, emoji: "🎟️", value: pending }
  - { id: bank, label: Bank, emoji: "🏦", value: idle }
steps:
  - say: The customer scans the QR. Checkout asks the bank to confirm and gets a promise back at once.
    send: { from: page, to: bank, label: "confirm ₹450?" }
    set: { bank: checking }
    focus: [promise]
  - say: The promise is pending. The page is free, so the customer can still scroll and tap.
    focus: [page]
  - say: The bank answers yes. The promise becomes fulfilled, holding the payment's reference.
    send: { from: bank, to: promise, label: "ref UPI-5521", back: true }
    set: { promise: "fulfilled", bank: idle }
  - say: The page's then callback runs with that value and shows a thank-you message.
    send: { from: promise, to: page, label: "then" }
    set: { page: "Payment received" }
  - say: Had the bank failed, the promise would be rejected instead, and the catch callback would run. Either way, it settles only once.
    focus: [promise]
```

### Syntax: then

```js
promise.then((value) => {
  // runs once, when the promise is fulfilled
});
```

`then` hands the promise a callback. When the promise is fulfilled, the callback runs with the value. If the promise is already fulfilled, the callback still runs later, never straight away.

```js run
const payment = new Promise((resolve) => {
  setTimeout(() => resolve({ status: "SUCCESS", ref: "UPI-5521" }), 10);
});

console.log("Waiting for UPI...");
payment.then((result) => console.log(`Paid! Ref ${result.ref}`));
```

### Syntax: catch

```js
promise.catch((error) => {
  // runs once, when the promise is rejected
});
```

`catch` handles a failure. The error is usually an `Error` object, so `error.message` holds the text.

```js run
const payment = Promise.reject(new Error("Bank app closed"));

payment
  .then(() => console.log("Paid!"))
  .catch((error) => console.log(`Payment failed: ${error.message}`));
```

The `then` callback is skipped, because the promise failed. The failure falls through to `catch`.

### Syntax: finally

```js
promise.finally(() => {
  // runs once the promise settles, whether it worked or not
});
```

`finally` is for clean-up that must happen either way, like closing the QR popup. Its callback gets no value. It also doesn't change the result: a success stays a success, and a failure stays a failure.

### Chaining: every then makes a new promise

`then`, `catch` and `finally` each return a **new** promise. So you can line them up, one step per line.

```callout jargon
**Chaining** means calling `.then(...)` on the promise that the last `.then(...)` returned. Whatever a callback returns becomes the value for the next step.
```

```js run
Promise.resolve(45000)
  .then((paise) => paise * 2)
  .then((paise) => `₹${paise / 100}`)
  .then((text) => console.log(`Two Golus cost ${text}`));
```

Compare that with the pile of callbacks from the last ticket. Each step sits on its own line, and nothing drifts right. If any step throws, the chain skips ahead to the next `catch`.

### Syntax: new Promise

```js
const promise = new Promise((resolve, reject) => {
  // start the slow job; call resolve(value) or reject(error) when it ends
});
```

Most promises come from tools like `fetch`. But you can make your own, to wrap something callback-based like a timer.

- The function you pass runs straight away.
- Call `resolve(value)` to fulfil the promise.
- Call `reject(error)` to reject it.

```js run
function fakeUpi(ms, result) {
  return new Promise((resolve) => {
    setTimeout(() => resolve(result), ms);
  });
}

fakeUpi(10, "UPI-7730").then((ref) => console.log(`Bank says: ${ref}`));
console.log("QR is showing");
```

`Promise.resolve(value)` and `Promise.reject(error)` are shortcuts for a promise that is already settled.

### Promise callbacks jump the queue

Promise callbacks don't wait in the same queue as timers. They have their own, faster one.

```callout jargon
The **microtask queue** holds promise callbacks. After each piece of code finishes, the event loop empties the whole microtask queue **before** it takes the next timer or click from the task queue.
```

```animate
title: A promise beats a 0 ms timer
kind: code
language: js
code: |
  console.log("A");
  setTimeout(() => console.log("D"), 0);
  Promise.resolve().then(() => console.log("C"));
  console.log("B");
steps:
  - line: 1
    out: "A"
    say: Plain line, so it prints straight away.
  - line: 2
    say: The timer's callback is handed off. It will wait in the task queue.
  - line: 3
    say: The promise is already fulfilled, so its then callback goes into the microtask queue.
  - line: 4
    out: "B"
    say: The script carries on and prints B. Now it has finished.
  - line: 3
    out: "C"
    say: Before any timer, the event loop empties the microtask queue. The promise callback prints C.
  - line: 2
    out: "D"
    say: Only then does the timer's callback get its turn and print D.
```

## Worked example

Here is a checkout step that turns a payment promise into a message for the screen.

```js run
function receiptLine(payment) {
  return payment
    .then((result) => `Thank you! Ref ${result.ref}`)
    .catch((error) => `Sorry: ${error.message}`)
    .finally(() => console.log("QR closed"));
}

receiptLine(Promise.resolve({ ref: "UPI-5521" })).then((text) => console.log(text));
```

Line by line:

1. `receiptLine` returns the end of the chain, so the caller gets a promise of the message.
2. If the payment works, `then` returns the thank-you text.
3. If it fails, `then` is skipped and `catch` turns the error into a message instead. The chain is now fulfilled again, with that message.
4. `finally` prints `QR closed` either way, and passes the message on unchanged.
5. The caller's `then` prints the message. So the output is `QR closed`, then `Thank you! Ref UPI-5521`.

## Try it

In the worked example, replace `Promise.resolve({ ref: "UPI-5521" })` with `Promise.reject(new Error("Bank app closed"))` and run it. The second line becomes `Sorry: Bank app closed`. Then delete the `.catch(...)` line and run again. What happens to the failure now?

## Recap

- A promise stands for a result that comes later. It is pending, then fulfilled with a value or rejected with an error, and it settles only once.
- `then` handles the value, `catch` handles the failure, and `finally` runs after either without changing the result.
- Each of them returns a new promise, so you chain steps one per line. A callback's return value goes to the next step.
- `new Promise((resolve, reject) => ...)` wraps a callback-based job, like a timer, in a promise.
- Promise callbacks run from the microtask queue, before the next timer or click.

## Common mistakes

**1. Forgetting to return inside then.**

```js run
Promise.resolve(45000)
  .then((paise) => {
    paise * 2;
  })
  .then((total) => console.log(total));
```

This prints `undefined`. With curly braces, an arrow function returns nothing unless you write `return`. Fix: `return paise * 2;`, or drop the braces: `.then((paise) => paise * 2)`.

**2. Leaving out catch.**

```js run crash
Promise.reject(new Error("Bank app closed"));
```

Nobody handles the failure. The browser console shows `Uncaught (in promise) Error: Bank app closed`, and Node stops the program. The customer sees nothing at all. Fix: end every chain with `.catch(...)` that shows a message.

**3. Using the value outside the then callback.**

```js run
let ref = "none";
Promise.resolve({ ref: "UPI-5521" }).then((result) => {
  ref = result.ref;
});
console.log(ref);
```

This prints `none`. The last line runs before the callback does, as with timers. Fix: use the value inside `then`, or in a function that `then` calls.

**4. Expecting finally to change the result.**

```js run
Promise.resolve("Paid")
  .finally(() => "Closed")
  .then((value) => console.log(value));
```

This prints `Paid`. Whatever `finally` returns is thrown away. Fix: use `finally` only for clean-up, and `then` or `catch` to produce values.
