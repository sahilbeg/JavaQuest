## Hook

Picture the FC Road stall on a busy Sunday. One person runs the counter. A customer pays by UPI, and the bank takes ten seconds to confirm.

The counter could stare at the phone for those ten seconds while the queue grows. Or it could say "I'll check in a moment", serve the next customer, and look at the phone when it buzzes.

The second way is how JavaScript waits. In this ticket you see why a web page must never stare at the phone, and how the browser lets it get on with other work instead.

## Concept

### One thing at a time

JavaScript in a web page runs on one thread.

```callout jargon
A **thread** is one line of work that a computer follows, step after step. JavaScript in a browser tab gets **one** thread, called the **main thread**. It runs your code, handles clicks and redraws the page.
```

Because there is one thread, only one piece of your code runs at any moment. Line 1 finishes, then line 2 starts.

```callout jargon
Code is **synchronous** when each line waits for the line before it to finish completely. Most code you have written so far is synchronous.
```

That is simple to follow, and it works well for quick jobs like adding up a cart.

### Blocking: when waiting freezes the page

Some jobs are slow. Asking the server for stock can take a second, or ten on slow 3G.

Suppose your code sat in a loop until the answer came. The one thread would be busy looping. It could not handle a tap, run an animation or redraw the page. The whole tab would freeze.

```js
function waitBadly(ms) {
  const end = Date.now() + ms;
  while (Date.now() < end) {
    // spinning: nothing else can run, not even a click
  }
}
```

```callout jargon
Code is **blocking** when it keeps the thread busy while it waits, so nothing else can run. A frozen page is the sign of blocking code.
```

Chrome even shows a "Page unresponsive" box when a tab blocks for too long. Customers simply leave.

### Asynchronous: start now, finish later

The fix is to start the slow job, then let your code carry on. When the job is done, the browser runs a function you gave it.

```callout jargon
Code is **asynchronous** ("async") when it starts a job and does not wait for it. The result arrives later, and a function you handed over runs then. That function is called a **callback**.
```

The simplest async job is a timer. `setTimeout(callback, ms)` asks the browser to run `callback` after at least `ms` milliseconds.

```js run
console.log("1. Take Mrs. Joshi's order");
setTimeout(() => console.log("3. UPI payment confirmed"), 10);
console.log("2. Greet the next customer");
```

Line 2 does not wait 10 milliseconds. It hands the timer to the browser and returns at once. So line 3 prints second, and the callback prints last.

### The call stack

To see where a callback goes, you need two more ideas. The first is the call stack.

```callout jargon
The **call stack** is JavaScript's list of functions that are running right now. Calling a function puts it on top. Returning takes it off. When the stack is empty, your code has finished for now.
```

The thread only ever works on the top of the stack. A callback can only run when the stack is empty, because the thread is free then.

### The task queue and the event loop

Who holds a callback while it waits? The browser does, in a line called the task queue.

```callout jargon
The **task queue** is a waiting line of callbacks that are ready to run: a timer that has finished, a click that happened, an answer that arrived. First in, first out.
```

```callout jargon
The **event loop** is the browser's endless check: "Is the call stack empty? Then take the first callback from the task queue and run it." It repeats this forever while the page is open.
```

Watch the stall's counter work through it.

```animate
title: A timer goes round the event loop
kind: scene
nodes:
  - { id: stack, label: Call stack, emoji: "🧵", value: empty, row: 0, col: 0 }
  - { id: browser, label: Browser timers, emoji: "⏱️", value: none, row: 0, col: 2 }
  - { id: queue, label: Task queue, emoji: "📥", value: empty, row: 1, col: 2 }
  - { id: loop, label: Event loop, emoji: "🔁", value: watching, row: 1, col: 0 }
steps:
  - say: Your script starts. It goes on the call stack and runs line by line.
    focus: [stack]
    set: { stack: "script" }
  - say: setTimeout hands the callback and 10 ms to the browser. Then your script carries on at once.
    send: { from: stack, to: browser, label: "callback, 10 ms" }
    set: { browser: "10 ms left" }
  - say: The script prints its next line and finishes. The stack is empty now.
    set: { stack: empty }
  - say: Ten milliseconds pass. The browser puts the callback at the back of the task queue.
    send: { from: browser, to: queue, label: callback }
    set: { browser: none, queue: "1 callback" }
  - say: The event loop sees an empty stack and a waiting callback, so it moves the callback onto the stack.
    send: { from: queue, to: loop, label: next }
    set: { queue: empty }
  - say: The callback runs and prints. When it returns, the stack is empty again, and the loop keeps watching.
    send: { from: loop, to: stack, label: run it }
    set: { stack: "callback", loop: watching }
```

### Why a delay of 0 still waits

`setTimeout(callback, 0)` does not mean "now". It means "put this in the queue as soon as you can". The callback still waits for the stack to be empty.

```animate
title: setTimeout with 0 still goes last
kind: code
language: js
code: |
  console.log("Order taken");
  setTimeout(() => console.log("UPI confirmed"), 0);
  console.log("Next customer");
steps:
  - line: 1
    out: "Order taken"
    say: The first line runs and prints straight away.
  - line: 2
    say: setTimeout hands the callback to the browser. The delay is 0, but nothing runs yet.
  - line: 3
    out: "Next customer"
    say: The script carries on. It is still on the stack, so the callback must wait.
  - line: 0
    say: The script has finished. The stack is empty, and the event loop takes the callback from the queue.
  - line: 2
    out: "UPI confirmed"
    say: Only now does the callback run. A 0 ms timer always runs after the code that is running now.
```

### A timer's delay is a minimum

The number you give `setTimeout` is the shortest wait, not an exact one. If the thread is busy for 2 seconds, a 10 ms timer runs after 2 seconds.

That is why blocking code is so harmful. It delays every timer, every click and every answer waiting in the queue.

## Worked example

The shop's home page must draw the catalogue, show a banner, and fill in stock when the server answers. Here is a pretend version, with timers standing in for the slow parts:

```js run
console.log("1. Open dhaaga.in");
setTimeout(() => console.log("4. Stock answer arrives"), 20);
setTimeout(() => console.log("3. Show the Diwali banner"), 0);
console.log("2. Draw the catalogue");
```

Trace it line by line:

1. Line 1 prints straight away.
2. Line 2 gives the browser a 20 ms timer. Nothing prints.
3. Line 3 gives the browser a 0 ms timer. Nothing prints.
4. Line 4 prints. The script ends, and the stack is empty.
5. The 0 ms timer is ready first, so its callback runs and prints the banner line.
6. About 20 ms later, the stock callback runs and prints last.

The rule of thumb: **every plain line runs first, top to bottom. Then the callbacks run, in the order their timers finish.**

## Try it

Change the 20 to 0 in the worked example and run it. Now both timers have the same delay, so they run in the order they were set: the stock line prints before the banner. Then change the banner's delay to 30 and predict the order before you press Run.

## Recap

- JavaScript in a page runs on one thread, so only one piece of your code runs at a time.
- Blocking code keeps that thread busy while it waits, and the whole page freezes.
- Asynchronous code starts a job, carries on, and runs a callback when the job is done.
- Finished callbacks wait in the task queue. The event loop runs them only when the call stack is empty.
- `setTimeout(callback, 0)` still runs after all the code that is running now. A timer's delay is a minimum, not an exact time.

## Common mistakes

**1. Thinking a 0 ms timer runs straight away.**

```js run
setTimeout(() => console.log("Stock: 3"), 0);
console.log("Drawing the page");
```

This prints `Drawing the page` first, then `Stock: 3`. The callback waits in the queue until the script finishes. Fix: put code that needs the result inside the callback, not after `setTimeout`.

**2. Reading a value before the callback has set it.**

```js run
let stock = "unknown";
setTimeout(() => {
  stock = 3;
}, 0);
console.log(stock);
```

This prints `unknown`. The last line runs before the callback does. Fix: use `stock` inside the callback, or in a function the callback calls.

**3. Waiting with a busy loop.**

```js
const end = Date.now() + 3000;
while (Date.now() < end) {}
console.log("Three seconds later");
```

It does print after three seconds, but the page is frozen the whole time: no taps, no scrolling, no animation. Fix: use `setTimeout` (or the tools in the next tickets) so the thread stays free while you wait.

**4. Expecting a timer to fire at exactly its delay.**

A 1000 ms timer fires after **at least** 1000 ms. If other code keeps the thread busy, it fires later. Fix: never use timers to line up steps that depend on each other. Start the next step from inside the callback instead.
