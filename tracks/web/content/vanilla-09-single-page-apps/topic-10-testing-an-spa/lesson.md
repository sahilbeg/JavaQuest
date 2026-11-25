## Hook

Before the Diwali pop-up opens, Meera Aunty walks the stall like a customer. She picks up a Golu, checks the price tag, asks for gift wrap, and pays by UPI. She doesn't inspect each stitch. She checks that the whole thing works together, the way a customer will use it.

Your SPA needs the same walk-through. Its bugs hide in the joins: the router, the store and the API working together. This ticket builds the tools for walking the app like a customer, in a test.

## Concept

### Tests for the joins

You wrote unit tests with Vitest in web-07, one function at a time. They are still the base. But in an SPA, many bugs only appear when parts meet: a route that fetches the wrong path, a Back that doesn't redraw, a store that a screen forgets to read.

```callout jargon
An **integration test** checks several parts working together, such as the router, a screen and a pretend API, through the page itself.
```

Integration tests are slower and fewer. A handful of them over the main journeys (open the catalogue, open an item, go back) catches the bugs unit tests miss.

### Test like a customer

A customer can't see your store's state or your functions. They see text, links and buttons. Write the test the same way:

- Start the app, and go to an address.
- Wait until the screen shows what a person would see.
- Tap links and buttons with `click()`.
- Check the **text and the address**, not internal variables.

A test written like this survives a rewrite of the inside. A test that peeks at internals breaks every time you tidy the code, even when the app still works.

### Driving the URL

In jsdom, no one presses Back. The test does what the browser would: change the address, then fire `popstate`.

### Syntax: visiting an address in a test

```js
history.replaceState(null, "", "/items/DC-1042");
window.dispatchEvent(new PopStateEvent("popstate"));
```

- `replaceState` sets the address without adding a history entry, so tests don't pile up entries.
- Dispatching `popstate` is what the browser does after Back or Forward, so the router redraws.
- Opening the app at a deep link is the same: set the address before starting the app.

### A pretend server

```callout jargon
A **fake** is a simple, working stand-in for something real, used in tests. A fake server answers requests from a small table in memory, with no network.
```

```callout jargon
**Dependency injection** means passing a part, such as `fetch`, into the code that uses it, instead of the code reaching for the global one. Then a test can pass in a fake.
```

`startApp(root, { fetch })` takes its `fetch` as an argument. The test passes a fake that also records every path asked for, so the test can check that the right requests were made, and only once.

### Waiting for the screen

SPA screens appear a moment later: after a fetch, after a `.json()`, after a re-render. A check made too early sees "Loading...".

```callout jargon
A **flaky test** passes sometimes and fails other times, with no change to the code, usually because it depends on timing.
```

The tempting fix is to sleep for half a second. It makes every test slow, and on a busy computer half a second is sometimes not enough. Instead, check again and again, a few milliseconds apart, until the screen is right or time runs out:

```animate
title: Sleeping versus waiting for the screen
kind: scene
nodes:
  - { id: test, label: The test, emoji: "🧪", value: started }
  - { id: app, label: The app, emoji: "🛒", value: "Loading..." }
  - { id: clock, label: Clock, emoji: "⏱️", value: "0 ms" }
steps:
  - say: The test visits /items. The app shows Loading and asks the fake server.
    send: { from: test, to: app, label: "visit(/items)" }
  - say: A test that sleeps for 500 ms just stops here, whatever happens.
    set: { test: "sleeping..." }
    focus: [clock]
  - say: The catalogue was ready after 4 ms. The sleeping test wastes the other 496 ms, in every test.
    set: { app: Catalogue, clock: "500 ms" }
  - say: Now the waiting test. It checks the screen every 10 ms instead.
    set: { test: "checking", app: "Loading...", clock: "0 ms" }
  - say: The first check sees Loading, so it tries again 10 ms later.
    send: { from: test, to: app, label: "Catalogue yet?" }
  - say: The second check sees the catalogue, and the test carries on at once.
    send: { from: app, to: test, label: "yes", back: true }
    set: { test: "passed", app: Catalogue, clock: "10 ms" }
  - say: If the screen never appears, the wait gives up after a timeout with a clear error, instead of hanging.
    focus: [test]
```

### Syntax: waiting with waitFor

```js
async function seeCatalogue() {
  await waitFor(() => heading() === "Catalogue");
  const link = await waitFor(() => document.querySelector('a[href="/items/DC-1042"]'));
  link.click();
}
```

- The check is a function, so it can be run again and again.
- `waitFor` resolves with whatever the check gave, which is handy for finding an element.
- Always `await` it. Without `await`, the test moves on before the screen is ready.

### Each test starts clean

Tests in one file share one jsdom page: the same `document`, the same `localStorage` and the same address. A test that leaves something behind changes the next test's result:

```animate
title: One test's leftovers break the next
kind: code
language: js
code: |
  const storage = new Map();
  function addToCart(id) {
    const cart = storage.get("cart") ?? [];
    storage.set("cart", [...cart, id]);
    return storage.get("cart").length;
  }
  console.log(addToCart("DC-1042"));
  console.log(addToCart("DC-1042"));
  storage.clear();
  console.log(addToCart("DC-1042"));
steps:
  - line: 1
    say: The Map stands in for localStorage, which every test in a file shares.
  - line: 7
    out: "1"
    say: Test one adds Golu to an empty cart and expects 1. It passes.
  - line: 8
    out: "2"
    say: Test two does the same and also expects 1. But test one's Golu is still saved, so it gets 2 and fails.
  - line: 9
    say: The fix is to clear everything before each test, in beforeEach.
  - line: 10
    out: "1"
    say: Now each test starts with an empty cart, and passes on its own or in any order.
```

```callout jargon
**Test isolation** means each test starts from the same clean state and leaves nothing behind, so tests pass alone, together, and in any order.
```

Before each test: empty the body, clear `localStorage`, set the address back to `/`. After each test: stop the app, so its listeners don't hear the next test's events.

### Reading text like a person

`textContent` keeps every space and new line from the HTML, so `"\n  Golu   the\n elephant "` is hard to compare. Squeeze each run of spaces into one, and trim the ends. Then the test compares what a person would read: `"Golu the elephant"`.

```callout tip
Testing Library offers these same tools with more polish: `findByText`, `waitFor` and queries by role. Playwright and Cypress run tests in a real browser, with layout, for the few journeys that need it. Your small kit shows what they do inside.
```

## Worked example

A test for the cart badge journey, using the kit:

```js
import { it, expect, beforeEach, afterEach } from "vitest";
import { startApp } from "../src/app.js";
import { visit, fakeServer, waitFor, textOf, reset } from "../src/testkit.js";

let stop;
beforeEach(() => {
  reset();
  document.body.innerHTML = `<span id="badge"></span><main id="app"></main>`;
});
afterEach(() => stop());

it("counts Golu in the badge after Add to cart", async () => {
  const server = fakeServer({ "/api/items/DC-1042": { id: "DC-1042", name: "Golu the elephant", paise: 45000 } });
  stop = startApp(document.querySelector("#app"), { fetch: server.fetch });
  visit("/items/DC-1042");
  const add = await waitFor(() => document.querySelector("button.add"));
  add.click();
  await waitFor(() => textOf(document.querySelector("#badge")) === "1");
  expect(server.calls, "Golu's page should be fetched once").toEqual(["/api/items/DC-1042"]);
});
```

1. `beforeEach` resets everything, then builds a fresh page. `afterEach` stops the app.
2. The fake server knows one path. Anything else gets a 404.
3. `visit` opens Golu's page like a deep link would.
4. Each step waits for what a customer would see, then acts.
5. The last line checks the requests the app made.

## Try it

Here is a fake server with a table of answers. Run it, then add `"/api/items/DC-1042"` to `answers` and fetch it too.

```js run
const answers = { "/api/items": ["DC-1042", "DC-2001"] };
const calls = [];

async function pretendFetch(path) {
  calls.push(path);
  const found = path in answers;
  return { ok: found, status: found ? 200 : 404, json: async () => answers[path] ?? { error: "Not found" } };
}

async function main() {
  const good = await pretendFetch("/api/items");
  console.log(good.status, (await good.json()).length);
  const bad = await pretendFetch("/api/hats");
  console.log(bad.status, (await bad.json()).error);
  console.log(calls.join(" then "));
}

main();
```

## Recap

- Integration tests check the router, screens and API together, through the page, like a customer.
- Drive the URL with `replaceState` plus a `popstate` event, and pass a fake server in as `fetch`.
- Wait for the screen by checking again every few milliseconds, never with a fixed sleep. Always `await` the wait.
- Keep tests isolated: reset the body, `localStorage` and the address before each test, and stop the app after it.
- Compare text the way a person reads it, with spaces squeezed and ends trimmed.

## Common mistakes

**1. Sleeping instead of waiting.**

```js
async function slowTest() {
  visit("/items");
  await new Promise((resolve) => setTimeout(resolve, 500));
  expect(heading()).toBe("Catalogue");
}
```

Every test takes half a second, and on a slow day the screen still isn't ready, so it fails at random. Fix: `await waitFor(() => heading() === "Catalogue")`.

**2. Forgetting await.**

```js
async function earlyTest() {
  visit("/items");
  waitFor(() => heading() === "Catalogue");
  expect(heading()).toBe("Catalogue");
}
```

The check runs at once, sees "Loading...", and fails, even though the wait would have worked. Fix: put `await` in front of `waitFor`.

**3. Leaving state behind.**

```js
beforeEach(() => {
  document.body.innerHTML = `<main id="app"></main>`;
});
```

The body is fresh, but `localStorage` and the address still hold the last test's cart and path. Tests pass alone and fail together. Fix: reset `localStorage` and the address too, and stop the app in `afterEach`.

**4. Testing the inside instead of the screen.**

```js
expect(store.getState().items.status).toBe("ready");
```

The status can say ready while the screen shows nothing, and the test still passes. Fix: check what the customer sees: the heading, the links and the address.
