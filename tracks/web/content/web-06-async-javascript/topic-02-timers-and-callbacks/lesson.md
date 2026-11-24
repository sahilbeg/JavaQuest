## Hook

Think of a kitchen timer. You twist it to three minutes, put it down and go back to chopping onions. You do not stand and stare at it. When it rings, you take the chai off the stove.

You can also change your mind. If the chai is ready early, you turn the timer off so it doesn't ring for nothing.

JavaScript timers work the same way. You set one, carry on, and say what should happen when it rings. You can also cancel it, as long as you kept hold of it.

## Concept

### Syntax: setTimeout

```js
const id = setTimeout(callback, ms);
```

- `callback` is the function to run later.
- `ms` is the wait in milliseconds. 1000 ms is one second.
- The return value is a **timer id**. Keep it if you might want to cancel.

```callout jargon
A **timer id** is a ticket for one timer. The browser gives it to you when you set the timer. You hand it back to cancel that exact timer. In a browser it is a number; in Node it is an object. Either way, you just store it.
```

```js run
setTimeout(() => console.log("Hide the toast"), 10);
console.log("Toast shown: Added Golu to your cart");
```

The toast line prints first. The callback runs about 10 ms later, once the script has finished.

### Pass the function, don't call it

`setTimeout` needs a function it can call later. Give it the function's name, or an arrow function. Don't put `()` after the name.

```js
function hideToast() {
  console.log("Hide the toast");
}

setTimeout(hideToast, 3000); // right: the browser calls it in 3 seconds
setTimeout(() => hideToast(), 3000); // also right: an arrow function that calls it
```

Writing `setTimeout(hideToast(), 3000)` calls `hideToast` straight away, and hands `setTimeout` its return value, `undefined`. The toast hides at once, and nothing happens three seconds later.

### Syntax: clearTimeout

```js
clearTimeout(id);
```

`clearTimeout` cancels a timer that has not run yet. The callback will never run. Cancelling a timer that already ran, or an id of `null`, does nothing and is safe.

```js run
const id = setTimeout(() => console.log("Hide the toast"), 10);
clearTimeout(id);
console.log("The toast stays up");
```

Only one line prints. The timer was cancelled before it could ring.

### One timer for one toast

Here is the bug in the cart. Every toast starts its own hide timer. If you add two things quickly, the **first** timer still rings and hides the **second** message early.

The fix: keep the timer id in a variable outside the function. Before you start a new timer, cancel the old one.

```animate
title: Two toasts, one timer
kind: scene
nodes:
  - { id: page, label: Cart toast, emoji: "💬", value: hidden }
  - { id: timer, label: Hide timer, emoji: "⏱️", value: none }
  - { id: memory, label: hideTimer variable, emoji: "📌", value: "null" }
steps:
  - say: The customer adds Golu. The toast shows "Added Golu" and a 3 second hide timer starts.
    set: { page: "Added Golu", timer: "3 s left", memory: "timer A" }
  - say: Two seconds later they add a coaster set. The toast text changes.
    set: { page: "Added coaster set", timer: "1 s left" }
  - say: Before starting a new timer, the code cancels timer A with clearTimeout(hideTimer).
    focus: [memory]
    send: { from: memory, to: timer, label: "clearTimeout" }
    set: { timer: none }
  - say: Now it starts timer B for a full 3 seconds and keeps its id.
    set: { timer: "3 s left", memory: "timer B" }
  - say: Three seconds after the second click, timer B rings and the toast hides. The new message got its full time.
    send: { from: timer, to: page, label: "hide" }
    set: { page: hidden, timer: none }
```

### Syntax: setInterval and clearInterval

```js
const id = setInterval(callback, ms);
clearInterval(id);
```

`setInterval` runs the callback again and again, every `ms` milliseconds, until you stop it. `clearInterval(id)` stops it. An interval never stops by itself.

```js run
let left = 3;
const id = setInterval(() => {
  left = left - 1;
  console.log(`QR expires in ${left}`);
  if (left === 0) {
    clearInterval(id);
  }
}, 5);
```

This prints 2, then 1, then 0, and stops. Without the `clearInterval` line, it would count down into minus numbers forever.

### Callbacks you write yourself

You have handed callbacks to `setTimeout`. Your own functions can take callbacks too. That lets the caller decide what happens, while your function decides when.

```callout jargon
A function that takes callbacks often names them `onSomething`: `onTick`, `onDone`, `onError`. The name says **when** the callback is called.
```

```js run
function afterPacking(orderId, onDone) {
  setTimeout(() => onDone(`${orderId} is packed`), 5);
}

afterPacking("order-17", (message) => console.log(message));
console.log("Packing started");
```

`afterPacking` doesn't print anything itself. It calls `onDone` with a message, and the caller chose to print it.

### When callbacks pile up

Callbacks are fine for one step. Real work has several steps, each waiting for the last: check stock, then take payment, then send the receipt. With callbacks, each step goes inside the one before.

```js
checkStock("DC-1042", (stock) => {
  takePayment(45000, (receipt) => {
    sendReceipt(receipt, () => {
      console.log("All done");
    });
  });
});
```

The code drifts to the right and gets hard to read. Error handling makes it worse. The next ticket shows **promises**, which keep the steps flat.

## Worked example

When an order arrives, the stall's tablet rings a bell three times, then goes quiet. Here is that as a function with two callbacks:

```js run
function ringBell(times, onRing, onDone) {
  let rung = 0;
  const id = setInterval(() => {
    rung = rung + 1;
    onRing(rung);
    if (rung === times) {
      clearInterval(id);
      onDone();
    }
  }, 5);
}

ringBell(
  3,
  (n) => console.log(`Ring ${n}`),
  () => console.log("Bell is quiet"),
);
console.log("Order screen open");
```

Line by line:

1. `rung` counts the rings. It lives outside the interval's callback, so it keeps its value between runs.
2. `setInterval` starts the repeating timer, and `id` keeps its ticket.
3. Each run adds one to `rung` and calls `onRing` with the count.
4. On the last ring, `clearInterval(id)` stops the repeating, and `onDone()` says it has finished.
5. `Order screen open` prints first, because the interval's first run waits for the script to end.

## Try it

Change `3` to `5` in the worked example and run it. Then delete the `clearInterval(id);` line and think about what would happen. (The ringing would never stop, and `onDone` would run once and then never again.) Put the line back before you press Run.

## Recap

- `setTimeout(callback, ms)` runs a callback once, later. `setInterval(callback, ms)` runs it again and again.
- Both return a timer id. Keep it, so you can cancel with `clearTimeout(id)` or `clearInterval(id)`.
- Pass the function itself to a timer. Don't call it with `()`.
- An interval never stops by itself. Stop it with `clearInterval`.
- Your own functions can take callbacks such as `onTick` and `onDone`, so the caller decides what happens.

## Common mistakes

**1. Calling the function instead of passing it.**

```js
setTimeout(hideToast(), 3000);
```

The toast hides straight away, and nothing happens after three seconds. The `()` ran `hideToast` at once. Fix: `setTimeout(hideToast, 3000)` or `setTimeout(() => hideToast(), 3000)`.

**2. Forgetting to stop an interval.**

```js
let left = 300;
setInterval(() => {
  left = left - 1;
  console.log(left);
}, 1000);
```

The countdown goes past 0 into -1, -2, -3 and keeps going while the page is open. Fix: keep the id, and call `clearInterval(id)` when `left` reaches 0.

**3. Not keeping the timer id.**

```js
function showToast(el, text) {
  el.textContent = text;
  setTimeout(() => {
    el.hidden = true;
  }, 3000);
}
```

Each call starts a new timer, and no one can cancel the old ones. An old timer hides a newer message early. Fix: store the id in a variable outside the function, and call `clearTimeout` on it before starting the next timer.

**4. Keeping the id in a variable inside the function.**

```js
function showToast(el, text) {
  let hideTimer = null;
  clearTimeout(hideTimer);
  hideTimer = setTimeout(() => {
    el.hidden = true;
  }, 3000);
}
```

Every call makes a fresh `hideTimer` that starts as `null`, so `clearTimeout` never cancels the old timer. Fix: declare `let hideTimer = null;` once, outside the function, so every call shares it.
