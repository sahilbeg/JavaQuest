## What happened

Arjun runs your mock loop today, and he plays the interviewer. The rule for every round: plain JavaScript and the DOM, no framework. He hands you a previous candidate's answers. Each worked once, when the candidate tried it. Each still failed the round.

1. **The JavaScript round.** "Write your own `Promise.all`." The answer hangs on an empty list, mixes up the order, crashes on plain values and ignores errors.
2. **The DOM round.** "Build a star rating for Golu's review form." It works with a mouse. A keyboard can't reach it, and a screen reader hears five stars with no meaning.
3. **The design round.** "Product pages ask for the same stock again and again. Put a small cache in front of the API." It shares requests nicely, but it remembers a failure forever, and it never gets fresh stock.

Your job: fix each answer the way an interviewer hopes to see it fixed.

### Why the first answer gets the order wrong

The candidate collected results with `push`, so they land in the order the **answers arrive**. On a fast network that happened to match. On a real one, Golu's stock comes back last.

```animate
title: Push versus storing by index
kind: code
language: js
code: |
  const results = [];
  const byIndex = [];
  const arrivals = [[2, 12], [0, 7], [1, 0]];
  for (const [index, stock] of arrivals) {
    results.push(stock);
    byIndex[index] = stock;
  }
  console.log(results.join(" "));
  console.log(byIndex.join(" "));
steps:
  - line: 3
    say: Three stock answers arrive. The keychain (index 2) answers first, then Golu (0), then the tote bag (1).
  - line: 5
    say: push puts each answer at the end of the list, in the order it arrived.
  - line: 6
    say: byIndex stores each answer at its own position, whenever it arrives.
  - line: 8
    out: "12 7 0"
    say: With push, the first number belongs to the keychain, not Golu. Every caller gets the wrong stock.
  - line: 9
    out: "7 0 12"
    say: By index, Golu is first, as asked. That's what Promise.all promises.
```

The other three bugs only show on inputs the candidate never tried. An empty list never reaches "all answers are in", so it waits forever. `45000.then` doesn't exist, so a plain value crashes. And nothing listens for a rejection, so one error leaves the caller waiting forever.

### Why the third answer keeps failing

The candidate cached the **promise**, which is a smart way to share one request between callers. But a rejected promise stays rejected. Once the server has one bad moment, every later visitor gets the old error, without the cache even asking again.

```animate
title: A cached failure that never goes away
kind: scene
nodes:
  - { id: page, label: Golu's page, emoji: "📱", row: 0, col: 0 }
  - { id: cache, label: Stock cache, emoji: "🗃️", value: empty, row: 0, col: 2 }
  - { id: api, label: Stock API, emoji: "🗄️", value: busy, row: 0, col: 4 }
steps:
  - say: At 10:00 on sale day the API is overloaded. The first visitor's request fails with HTTP 503.
    send: [{ from: page, to: cache, label: "get DC-1042" }, { from: cache, to: api, label: "fetch" }, { from: api, to: cache, label: "503", back: true }]
    set: { cache: "rejected promise" }
  - say: A minute later the API is fine again.
    set: { api: healthy }
  - say: The next visitor asks. The cache hands back the same rejected promise, and never calls the API.
    send: { from: page, to: cache, label: "get DC-1042" }
    focus: [cache]
  - say: Golu's page shows an error to everyone, until someone reloads the whole app.
    send: { from: cache, to: page, label: "503 again", back: true }
  - say: The fix is to delete the entry when its promise rejects. The next visitor gets a fresh request.
    set: { cache: empty }
```

## What you need

You learned each idea in an earlier ticket. Here they meet in one incident.

- The interview method, testing by hand, the event loop: [Frontend interviews without a framework](/t/vanilla-10-professional-vanilla/topic-08-interviews).
- Promises, `then`, rejections and `Promise.all` itself: [Promises](/t/web-06-async-javascript/topic-03-promises) and [Many requests at once](/t/web-06-async-javascript/topic-08-parallel-requests).
- Roles, `aria-*`, focus and keys: [Accessibility in depth](/t/vanilla-10-professional-vanilla/topic-02-accessibility-in-depth) and [Accessible widgets](/t/vanilla-08-components-without-a-framework/topic-10-accessible-widgets).
- Closures that keep state, and `Map`: [Scope and closures](/t/web-04-javascript-in-depth/topic-01-scope-and-closures) and [Map and Set](/t/web-04-javascript-in-depth/topic-11-maps-and-sets).

### The radio group pattern

A star rating is really a set of five radio buttons: exactly one can be chosen. The WAI-ARIA Authoring Practices describe how a **radio group** behaves:

| Key | What it does |
|---|---|
| Tab | Lands on the chosen star, or the first star when none is chosen. The next Tab leaves the group |
| Right / Down | Chooses and focuses the next star; 5 wraps to 1 |
| Left / Up | Chooses and focuses the star before; 1 wraps to 5 |
| Space | Chooses the focused star |

```callout jargon
A **roving tabindex** gives a group of controls one Tab stop between them. The active control has `tabindex="0"` and the rest `tabindex="-1"`. When the arrows move, the `0` moves too, and focus follows.
```

Moving with an arrow both **chooses** the star and **focuses** it, so the screen reader reads "3 stars, radio button, checked, 3 of 5".

### What the gold checks do

The checks give the cache a pretend `fetchJson` whose answers they control, and a pretend clock, `now`. They ask twice at once, wait for answers, make the server fail and then recover, and move the clock to exactly 59,999 ms and 60,000 ms. Nothing touches the network, and nothing really waits a minute.

## The tiers

1. **Bronze** resolves the incident. `promiseAll` keeps the input's order, resolves `[]` at once, accepts plain values, and rejects on the first error.
2. **Silver**: the DOM round. A labelled `radiogroup` of five named `radio` stars, `aria-checked` on the chosen one, a roving tabindex, and arrow keys and Space that choose and focus.
3. **Gold**: the design round. Failed requests leave the cache at once, without removing a newer request, and answers older than `maxAgeMs` are fetched again.

The exact rules are in the README.

## Recap

- Before saying "done", try the edges out loud: empty input, answers in a different order, plain values, a failure, a failure followed by a recovery, and the exact boundary of a time limit.
- `Promise.all` keeps the input's order: store each result at its index and count how many are left. Wrap values with `Promise.resolve`, and pass `reject` along.
- A star rating is a radio group: `role="radiogroup"` with a label, `role="radio"` stars with names and `aria-checked`, a roving tabindex, and arrows that choose and focus.
- Caching the promise shares one request between callers, but a rejected promise must be deleted from the cache so the next caller tries again.
- Store when each entry was made, and treat it as stale at `maxAgeMs`. Pass the clock in, so tests can move time.

## Common mistakes

**1. Collecting results with push.**

```js
const results = [];
Promise.resolve(7).then((stock) => results.push(stock));
```

Symptom: the bronze check says the results should be `[7, 0, 12]`, but got `[12, 7, 0]`. Fix: `results[index] = stock`, with a separate counter of answers still to come.

**2. Checking results.length to know when you're done.**

```js
const results = new Array(3);
console.log(results.length === 3);
```

Symptom: with `new Array(values.length)`, the length is already full before any answer, so it resolves at once with empty slots. Fix: count down a `left` variable, and resolve when it reaches 0.

**3. Arrow keys that choose but don't focus.**

```js
function onArrow(choose, next) {
  choose(next);
}
```

Symptom: the silver check says focus should move to the third star. Pressing the arrow again starts from the old star, and the screen reader says nothing. Fix: after `choose(next)`, call `stars[next - 1].focus()`.

**4. Deleting the cache entry without checking which one it is.**

```js
const cache = new Map();
const promise = Promise.reject(new Error("HTTP 504"));
promise.catch(() => cache.delete("/api/stock/DC-1042"));
```

Symptom: the gold check says no third request should be made. An old, slow failure wiped out the newer request that replaced it. Fix: keep the entry object, and delete only if `cache.get(url) === fresh`.

**5. Stale at the wrong moment.**

```js
const fresh = (now, at, maxAgeMs) => now - at <= maxAgeMs;
```

Symptom: the gold check says at 60,000 ms the answer is stale, but your cache served the old stock. Fix: fresh means `now - at < maxAgeMs`, strictly less than.
