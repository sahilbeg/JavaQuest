## Hook

You post two letters to a friend. On Monday: "Let's meet at FC Road." On Tuesday: "Change of plan, meet at Aundh." Tuesday's letter goes by speed post and arrives first. Monday's arrives a day later.

Your friend reads them in the order they arrive, and goes to FC Road. The newest plan lost, just because the older letter was slower.

Requests do this too. On a patchy connection, the answer to an old question can arrive after the answer to a new one, and the page shows the wrong thing.

## Concept

### Answers can arrive out of order

Each request travels on its own. A slow tower, a busy server or a lost packet can make any one of them late. Nothing promises that answers come back in the order you asked.

That's fine when each answer goes to its own place on the page. It is a problem when two answers fight over the **same** place, like one line under the PIN box.

```animate
title: The old answer wins
kind: scene
nodes:
  - { id: page, label: PIN box, emoji: "📮", value: empty }
  - { id: server, label: dhaaga.in API, emoji: "🖥️", value: idle }
steps:
  - say: The customer types 560001 by mistake and presses Check. That request takes the slow road.
    send: { from: page, to: server, label: "pin=560001" }
    set: { page: "560001", server: "1 waiting" }
  - say: She fixes it to 411045 and presses Check again. A second request goes out.
    send: { from: page, to: server, label: "pin=411045" }
    set: { page: "411045", server: "2 waiting" }
  - say: The new request is quick. Its answer arrives first, and the page shows it.
    send: { from: server, to: page, label: "deliverable: true", back: true }
    set: { page: "Delivery to 411045 in 2 days", server: "1 waiting" }
  - say: Then the old, slow answer finally lands. The page shows that too, on top of the right one.
    send: { from: server, to: page, label: "deliverable: false", back: true }
    set: { page: "We don't deliver to 560001", server: idle }
  - say: The box says 411045, but the message is about 560001. The customer sees a wrong answer.
    focus: [page]
```

### What a race condition is

```callout jargon
A **race condition** is a bug where the result depends on which of two things finishes first. It works when the timing is lucky, and breaks when it isn't. On your fast laptop it may never show up; on 3G it shows up every day.
```

You can see the race with timers standing in for slow requests:

```js run
const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function check(pin, ms) {
  await wait(ms);
  console.log(`Screen shows the answer for ${pin}`);
}

check("560001", 30);
check("411045", 10);
```

The last line printed wins the screen, and it is the old PIN.

### Fix 1: only the newest request may change the screen

Give every request a number, counting up. Remember the newest number in a variable outside the function. When an answer arrives, compare: if a newer request has started since, throw this answer away.

```js run
const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
let latest = 0;

async function check(pin, ms) {
  latest = latest + 1;
  const myNumber = latest;
  await wait(ms);
  if (myNumber !== latest) {
    console.log(`Ignoring the old answer for ${pin}`);
    return;
  }
  console.log(`Screen shows the answer for ${pin}`);
}

check("560001", 30);
check("411045", 10);
```

```callout tip
Check **after** the `await`, right before you touch the page. That is the moment a newer request may have started.
```

This fix is simple, and it works with any async job. But the old request still runs to the end and uses the customer's data plan for nothing.

### Syntax: AbortController

```js
const controller = new AbortController();
fetch(url, { signal: controller.signal });
controller.abort();
```

```callout jargon
An **AbortController** lets you cancel a request. Its `signal` is passed to `fetch`. Calling `controller.abort()` tells everything holding that signal to stop. **Abort** means stop before finishing.
```

The signal also remembers whether it was aborted, and fires an `abort` event when it is:

```js run
const controller = new AbortController();
controller.signal.addEventListener("abort", () => console.log("The request was cancelled"));

console.log(controller.signal.aborted);
controller.abort();
console.log(controller.signal.aborted);
```

A controller can abort only once. For every new request, make a new controller.

### What an aborted fetch does

When you abort, `fetch` stops waiting and its promise **rejects** with an error whose `name` is `"AbortError"`. That isn't a real failure: you asked for it. So check the name in `catch`, and quietly return.

This pretend `fetch` behaves like the real one when aborted:

```js run
function pretendFetch(url, { signal } = {}) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => resolve(`answer for ${url}`), 30);
    signal?.addEventListener("abort", () => {
      clearTimeout(timer);
      const error = new Error("This operation was aborted");
      error.name = "AbortError";
      reject(error);
    });
  });
}

async function main() {
  const controller = new AbortController();
  const request = pretendFetch("/api/delivery?pin=560001", { signal: controller.signal });
  controller.abort();
  try {
    await request;
  } catch (error) {
    console.log(error.name);
  }
}

main();
```

### Fix 2: cancel the old request

Keep the current controller outside the function. Before each new request, abort the old one:

```js
let controller = null;

async function checkPin(pin, show) {
  controller?.abort();
  controller = new AbortController();
  try {
    const response = await fetch(`/api/delivery?pin=${pin}`, { signal: controller.signal });
    const data = await response.json();
    show(data.deliverable ? "We deliver here" : "Not yet");
  } catch (error) {
    if (error.name === "AbortError") return;
    show("Couldn't check your PIN");
  }
}
```

`controller?.abort()` uses `?.`, so the very first call, when `controller` is `null`, does nothing. The old request stops, its answer never arrives, and the customer's data isn't wasted.

### Giving up after a time limit

A request on a dead connection can hang for a long time. A timer plus `abort()` puts a limit on it:

```js
async function fetchWithLimit(url, ms) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), ms);
  try {
    return await fetch(url, { signal: controller.signal });
  } finally {
    clearTimeout(timer);
  }
}
```

If the answer comes in time, `finally` clears the timer, so it never fires. If not, the timer aborts, and `fetch` rejects with an `AbortError` that the caller can turn into "Taking too long, please try again".

```callout tip
Modern browsers also have `AbortSignal.timeout(5000)`, a ready-made signal that aborts itself after 5 seconds. Writing it by hand first shows you how it works.
```

## Worked example

The colour picker on Golu's page loads a photo for each colour. Customers tap quickly through Marigold, Indigo and Mint, and only the last tap matters:

```js
function createPhotoLoader(img) {
  let latest = 0;
  return async function load(colour) {
    latest = latest + 1;
    const myNumber = latest;
    const response = await fetch(`/api/photos/DC-1042?colour=${colour}`);
    const data = await response.json();
    if (myNumber !== latest) return;
    img.src = data.url;
  };
}
```

Line by line:

1. `createPhotoLoader` keeps `latest` in a closure, so every call to `load` shares it.
2. Each call takes the next number and keeps its own copy in `myNumber`.
3. After both `await`s, it checks whether a newer call has started. If so, it stops without touching the picture.
4. Only the newest tap sets `img.src`. Slow answers from older taps are thrown away.

## Try it

In the "Fix 1" example, add a third call, `check("411001", 20);`, at the end. Predict what prints before you run it. Then swap the delays so the oldest call is the fastest, and run it again.

## Recap

- Answers can arrive in a different order from the questions. When two answers fight over one spot on the page, that is a race condition.
- Fix 1: number each request, and let only the newest one change the page. Check right after the `await`.
- Fix 2: cancel the old request with an `AbortController`: pass `controller.signal` to `fetch`, then call `controller.abort()`.
- An aborted `fetch` rejects with an error named `"AbortError"`. Ignore it quietly in `catch`.
- A timer that calls `abort()` gives a request a time limit. Clear the timer in `finally`.

## Common mistakes

**1. Showing every answer that arrives.**

```js
async function check(pin, show) {
  const response = await fetch(`/api/delivery?pin=${pin}`);
  const data = await response.json();
  show(data.deliverable ? "We deliver here" : "Not yet");
}
```

It works on fast Wi-Fi. On 3G, an old answer lands last and overwrites the new one. Fix: number the requests and ignore stale answers, or abort the old request.

**2. Treating an abort as a real error.**

```js
async function check(pin, signal, show) {
  try {
    await fetch(`/api/delivery?pin=${pin}`, { signal });
  } catch (error) {
    show("Couldn't check your PIN");
  }
}
```

Every time the customer types again, the old request is aborted and the page flashes an error. Fix: `if (error.name === "AbortError") return;` at the top of `catch`.

**3. Reusing one controller for every request.**

```js
const controller = new AbortController();

async function check(pin) {
  controller.abort();
  return fetch(`/api/delivery?pin=${pin}`, { signal: controller.signal });
}
```

After the first `abort()`, the signal stays aborted forever, so every new request fails at once. Fix: make a `new AbortController()` for each request, and keep it in a `let`.

**4. Forgetting to clear the time limit.**

```js
async function fetchWithLimit(url, ms) {
  const controller = new AbortController();
  setTimeout(() => controller.abort(), ms);
  return fetch(url, { signal: controller.signal });
}
```

The timer keeps running after the answer arrives. If the body is still being read when it fires, reading it fails. Fix: keep the timer id and `clearTimeout` it in `finally`.
