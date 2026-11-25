## Hook

Every morning, Arjun prints the stock list and pins it to the Aundh shop's counter. When a customer asks "Do you have Golu?", Raju reads the list. He doesn't phone the warehouse each time.

If he did, every customer would wait on hold, and the warehouse would hear the same question fifty times a day. Your SPA does exactly that when every screen fetches its own data. This ticket pins the list to the counter.

## Concept

### Where fetched data should live

In web-06 a screen fetched its own data and drew it. That works on a multi-page site, where every page starts fresh anyway.

In an SPA, screens are thrown away when the customer moves on. Data fetched inside a screen goes with it. Press Back, and the new copy of the screen fetches it all again.

So put fetched data in the **store**. The store outlives every screen, and any screen can read it.

```callout jargon
A **cache** is a saved copy of data you fetched, kept so the next request can use it instead of asking the server again.
```

### A slice with a status

Storing only the list isn't enough. A screen also needs to know whether the list is loading, ready, or failed. Keep all three together in one slice of the state:

```js
const items = { status: "loading", data: [], error: null };
```

```callout jargon
A **slice** is one part of the store's state that belongs to one job, such as `items` or `cart`. A **status** field says which stage that job is in.
```

- `"idle"`: nobody has asked yet.
- `"loading"`: a request is on its way.
- `"ready"`: `data` holds the answer.
- `"error"`: the last request failed, and `error` holds the friendly message.

### Syntax: updating one slice

```js
store.setState((state) => ({ ...state, items: { ...state.items, status: "loading" } }));
```

Two spreads, one inside the other. The outer one keeps the rest of the state, such as the cart and the route. The inner one keeps the rest of the slice, such as the data already loaded.

### Fetch once, use many times

Before asking the server, look in the store. If `status` is already `"ready"`, the data is there, so return at once.

```animate
title: Back to the catalogue, without the spinner
kind: scene
nodes:
  - { id: screen, label: Catalogue screen, emoji: "🖼️", value: empty }
  - { id: store, label: Store, emoji: "🗃️", value: "status: idle" }
  - { id: api, label: dhaaga.in API, emoji: "🖥️", value: idle }
steps:
  - say: Mrs. Deshpande opens the catalogue. The screen asks the store to load the items.
    send: { from: screen, to: store, label: "load()" }
  - say: The store has nothing yet, so it marks the slice loading and asks the server.
    send: { from: store, to: api, label: "GET /api/items" }
    set: { store: "status: loading", screen: "Loading..." }
  - say: The items arrive. The slice becomes ready, and the screen draws them.
    send: { from: api, to: store, label: "3 items", back: true }
    set: { store: "status: ready", screen: "3 items" }
  - say: She opens Golu, then presses Back. A brand-new catalogue screen asks to load again.
    send: { from: screen, to: store, label: "load()" }
  - say: The status is already ready. The store answers at once, and the server hears nothing.
    set: { screen: "3 items" }
    focus: [store, api]
```

### Two screens asking at once

On the item page, the header's search box and the item screen might both need the catalogue, in the same moment. If each one starts a request, the server answers the same question twice.

The fix is to keep the promise of the running request. Anyone who asks while it is running gets **that same promise**. When it settles, forget it.

```callout jargon
A request is **in flight** while it has been sent but has not been answered yet. Sharing one in-flight request between callers is called **deduplicating** it.
```

```js run
let calls = 0;
function fakeFetch() {
  calls += 1;
  return new Promise((resolve) => setTimeout(() => resolve(["DC-1042", "DC-2001"]), 5));
}

let inFlight = null;
function load() {
  if (inFlight === null) {
    inFlight = fakeFetch().finally(() => {
      inFlight = null;
    });
  }
  return inFlight;
}

async function main() {
  const [a, b] = await Promise.all([load(), load()]);
  console.log(a.length, b.length, `fetches: ${calls}`);
  await load();
  console.log(`fetches: ${calls}`);
}

main();
```

It prints `2 2 fetches: 1`, then `fetches: 2`. The two loads at once shared one request. The later load started a new one, because `finally` had cleared `inFlight`.

### Syntax: finally on a promise

```js
const shared = request().finally(() => {
  inFlight = null;
});
```

`promise.finally(callback)` runs the callback when the promise settles, whether it worked or failed. It gives back a promise that settles the same way as the original. That makes it the place to clean up.

### When it fails: keep the data, allow a retry

- **Keep the old data.** If the store already had items, a failed refresh shouldn't wipe them. Set `status` and `error`, and leave `data` alone.
- **Don't cache errors.** Only `"ready"` means "already done". After an error, the next `load()` must try the server again.
- **Never throw out of load.** The error belongs in the store, where the screen can show it with a Try again button.

### Screens read with selectors

The item screen doesn't fetch Golu separately when the list is already loaded. It asks the store with a selector:

```js
function selectItem(state, id) {
  return state.items.data.find((item) => item.id === id) ?? null;
}
```

```callout tip
Libraries such as TanStack Query do this job in big apps: caching, sharing in-flight requests, retrying and refreshing old data. Building a small one yourself shows you what they do.
```

## Worked example

Each item can have reviews, loaded only when someone opens that item. Here the reviews are cached per item id:

```js
function createReviewLoader(store) {
  const inFlight = new Map();

  async function request(id) {
    const response = await fetch(`/api/items/${id}/reviews`);
    const reviews = response.ok ? await response.json() : [];
    store.setState((state) => ({ ...state, reviews: { ...state.reviews, [id]: reviews } }));
  }

  return function load(id) {
    if (store.getState().reviews[id] !== undefined) {
      return Promise.resolve();
    }
    if (!inFlight.has(id)) {
      inFlight.set(id, request(id).finally(() => inFlight.delete(id)));
    }
    return inFlight.get(id);
  };
}
```

1. The store's `reviews` slice is an object keyed by item id, so Golu's reviews and the tote bag's live side by side.
2. `[id]: reviews` is a computed key: it adds or replaces the entry for this id only.
3. A `Map` holds one in-flight promise per id, so two quick taps on Golu share one request.
4. Reviews already in the store are used straight away.

## Try it

In the deduplicating example above, change `main` so it waits for the first two loads, and then starts two more at the same time. How many fetches should it print? Run it and check.

## Recap

- In an SPA, keep fetched data in the store, not in the screen, so it survives moving between screens.
- Give each slice a status: idle, loading, ready or error, next to its `data` and `error`.
- If the slice is ready, use it. If a request is already in flight, share its promise. Clear it in `finally`.
- On failure, keep the old data, store a friendly error, and let the next load try again. Never throw out of `load`.
- Screens read what they need with selectors.

## Common mistakes

**1. Fetching inside the screen every time it opens.**

```js
async function catalogueScreen(outlet) {
  outlet.textContent = "Loading...";
  const items = await (await fetch("/api/items")).json();
  drawItems(outlet, items);
}
```

Every Back shows the spinner and asks the server again. Fix: ask the store's loader, which fetches only when the items aren't ready.

**2. Replacing the slice with just the data.**

```js
store.setState((state) => ({ ...state, items: data }));
```

The slice loses its `status` and `error`, so screens can't tell loading from ready, and `items.data` is `undefined`. Fix: set `items: { status: "ready", data, error: null }`.

**3. Treating an error as done.**

```js
function load() {
  if (store.getState().items.status !== "idle") {
    return Promise.resolve();
  }
  return request();
}
```

After one failure, the status is `"error"`, so the app never asks again, even when the customer taps Try again. Fix: skip the fetch only when the status is `"ready"`.

**4. Never clearing the in-flight promise.**

```js
function load() {
  if (inFlight === null) {
    inFlight = request();
  }
  return inFlight;
}
```

After a failure, every later load gets the same old, finished promise, so nothing is ever fetched again. Fix: `inFlight = request().finally(() => { inFlight = null; });`.
