## Hook

A customer at the Aundh shop says, "I'm looking for a... um... a pink... no, a Rani pink... keychain." A good shopkeeper doesn't run to the shelf at "a pink". They wait until the customer stops talking, then fetch one thing.

A search box that asks the server on every key press is the shopkeeper who runs at every word. It wastes trips, and it brings back answers to questions nobody meant to ask.

The fix is to wait for a short pause in the typing, then search once. That trick is called **debouncing**.

## Concept

### One request per key press is too many

Typing `golu` fires four `input` events: `g`, `go`, `gol`, `golu`. If each one fetches results, that is four requests for one search. A longer phrase fires dozens.

That costs the customer's data, loads the server for nothing, and invites the race from the last ticket: the answer for `go` can land after the answer for `golu`.

### What debouncing is

```callout jargon
To **debounce** a function is to delay it until calls have stopped for a set time. Each new call cancels the one waiting and starts the wait again. Only the last call in a burst actually runs.
```

The name comes from electronics. A pressed switch "bounces" and sends many signals; debouncing keeps only one.

### Syntax: a debounce function

```js
function debounce(fn, ms) {
  let timer = null;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => fn(...args), ms);
  };
}
```

- `debounce` takes the real function `fn` and a wait `ms`, and returns a **new** function.
- `timer` lives in a closure, so every call of the new function shares it.
- `(...args)` collects whatever arguments the call got. `fn(...args)` passes them all on.
- Each call cancels the timer that was waiting, then starts a fresh one.

```callout jargon
`...args` in a parameter list is a **rest parameter**: it gathers all the arguments into one array. `fn(...args)` **spreads** that array back out into separate arguments.
```

### Watch the timer reset

```animate
title: Three key presses, one search
kind: code
language: js
code: |
  function debounce(fn, ms) {
    let timer = null;
    return (...args) => {
      clearTimeout(timer);
      timer = setTimeout(() => fn(...args), ms);
    };
  }
  const search = debounce((text) => console.log(`Search: ${text}`), 20);
  search("g");
  search("go");
  search("golu");
steps:
  - line: 8
    vars: { timer: "null" }
    say: debounce makes search, a new function with its own timer box, empty for now.
  - line: 9
    vars: { timer: '"wait for g"' }
    say: The first key press. Nothing is waiting to cancel, so a 20 ms timer starts for "g".
  - line: 10
    vars: { timer: '"wait for go"' }
    say: The next key press comes before 20 ms. clearTimeout cancels the "g" timer and starts one for "go".
  - line: 11
    vars: { timer: '"wait for golu"' }
    say: Same again. The "go" timer is cancelled, and a fresh 20 ms wait starts for "golu".
  - line: 0
    say: Now the typing stops. Nothing cancels the last timer, so the 20 ms run out.
  - line: 5
    out: "Search: golu"
    say: Only the last call runs. Three key presses, one search.
```

Run it with two bursts of typing, separated by a pause:

```js run
function debounce(fn, ms) {
  let timer = null;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => fn(...args), ms);
  };
}

const search = debounce((text) => console.log(`Search: ${text}`), 20);
search("g");
search("go");
search("golu");
setTimeout(() => {
  search("b");
  search("bag");
}, 50);
```

Each burst gives exactly one search: the last word typed in it.

### Wiring it to the search box

Make the debounced function **once**, outside the event listener. Then every `input` event calls the same one, so they share one timer.

```js
const box = document.querySelector("#search");
const runSearch = debounce((text) => console.log("Searching for", text), 300);

box.addEventListener("input", () => runSearch(box.value));
```

300 ms is a common choice. It is short enough to feel instant, and long enough to skip the letters in the middle of a word.

### Debounce plus the newest-answer rule

Debouncing cuts the number of requests, but it doesn't stop a race. A customer can type `golu`, pause, then type `bag`. Two requests go out, and the `golu` answer can still land last.

So keep the rule from the last ticket too: number each search, and let only the newest one change the results.

```js
let latest = 0;

async function searchNow(text, show) {
  latest = latest + 1;
  const mine = latest;
  const response = await fetch(`/api/catalogue?${new URLSearchParams({ q: text })}`);
  const items = await response.json();
  if (mine !== latest) return;
  show(items.map((item) => item.name));
}
```

### Throttle, debounce's cousin

```callout jargon
To **throttle** a function is to let it run at most once every so often, such as once every 200 ms, however often it is called. Debounce waits for a pause; throttle keeps a steady beat.
```

Use debounce for things that should happen after the user stops, like searching or saving a draft. Use throttle for things that should keep happening during a long action, like updating a progress bar while the page scrolls.

## Worked example

The custom-order form saves a draft while the customer types the message to be stitched on. Saving on every key press would be wasteful, so it saves after a one-second pause:

```js
function startDraftSaving(textarea, save) {
  const saveSoon = debounce(() => save(textarea.value), 1000);
  textarea.addEventListener("input", saveSoon);
}
```

Line by line:

1. `debounce(..., 1000)` makes `saveSoon` once, when the form starts. It owns one timer.
2. Every `input` event calls `saveSoon`, which cancels the waiting save and starts a new one-second wait.
3. When the customer pauses for a full second, the timer runs out and `save` gets the textarea's current text.
4. The callback reads `textarea.value` when it runs, not when the key was pressed, so it always saves the latest text.

## Try it

In the runnable example, change the delay inside `setTimeout` from 50 to 10, so the second burst starts before the first has waited 20 ms. Predict the output, then run it. Now the two bursts merge into one, and only `bag` is searched.

## Recap

- Searching on every key press floods the server and invites races.
- Debouncing delays a function until calls stop for a set time. Each call cancels the waiting timer and starts a new one.
- Write it with a `timer` in a closure, `clearTimeout`, then `setTimeout(() => fn(...args), ms)`.
- Make the debounced function once, outside the event listener, so every call shares one timer.
- Debounce doesn't stop out-of-order answers. Keep the newest-answer rule as well. Throttle is different: it runs at a steady rate.

## Common mistakes

**1. Making a new debounced function on every event.**

```js
box.addEventListener("input", () => {
  const runSearch = debounce(searchNow, 300);
  runSearch(box.value);
});
```

Each event makes a brand-new function with its own empty timer, so nothing is ever cancelled. Every key press still searches, just 300 ms late. Fix: create `runSearch` once, outside the listener.

**2. Keeping the timer inside the returned function.**

```js
function debounce(fn, ms) {
  return (...args) => {
    let timer = null;
    clearTimeout(timer);
    timer = setTimeout(() => fn(...args), ms);
  };
}
```

`timer` is new and empty on every call, so `clearTimeout` never cancels the old wait. Fix: declare `let timer = null;` in `debounce`, above the `return`.

**3. Forgetting to pass the arguments on.**

```js run
function debounce(fn, ms) {
  let timer = null;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(fn, ms);
  };
}

const search = debounce((text) => console.log(`Search: ${text}`), 10);
search("golu");
```

This prints `Search: undefined`. The timer calls `fn` with no arguments. Fix: `setTimeout(() => fn(...args), ms)`.

**4. Expecting debounce to fix races.**

```js
const runSearch = debounce(async (text) => {
  const response = await fetch(`/api/catalogue?${new URLSearchParams({ q: text })}`);
  showResults(await response.json());
}, 300);
```

Two searches a second apart are both sent, and the slower answer still wins the screen. Fix: count the searches and ignore any answer that isn't from the newest one.
