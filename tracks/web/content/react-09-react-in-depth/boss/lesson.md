## What happened

The Diwali sale opened at 10:00. Within ten minutes, the stock API had taken 40,000 requests and stopped answering. Without it, no page could show stock, and checkout refused every order.

Arjun traced every request to one place: `SaleStock`, the small badge on each item page that says `DC-1042: 7 left`. A customer with Golu's page open wasn't sending one request. Their page sent request after request, as fast as the API could answer, for as long as the tab stayed open.

```callout jargon
A **render loop** is when a component's own work makes it render again, and that render does the same work again, without end. Here, the work is a request to the server, so the loop floods the API too.
```

During the sale, stock changed with every order. So almost every answer had a new number, and every new number kept the loop going. In the checks, the stock stays still, so React stops after an identical second answer. The bug shows up there as a second request that should never have been sent.

```animate
title: How one open page floods the API
kind: scene
nodes:
  - { id: badge, label: SaleStock, emoji: "🏷️", value: "renders: 1" }
  - { id: effect, label: The effect, emoji: "⚡" }
  - { id: api, label: Stock API, emoji: "🖥️", value: "requests: 0" }
steps:
  - say: The badge renders for the first time. After that render, its effect runs.
    focus: [badge, effect]
  - say: The effect asks the API for DC-1042's stock.
    send: { from: effect, to: api, label: "GET /api/stock/DC-1042" }
    set: { api: "requests: 1" }
  - say: The answer says 7 left. setLeft(7) is new state, so the badge renders again.
    send: { from: api, to: badge, label: "left: 7", back: true }
    set: { badge: "renders: 2" }
  - say: Ask yourself when an effect runs. This one has no dependency array.
    focus: [effect]
  - say: So the effect runs after this render too, and asks again. Mid-sale, someone has just bought one.
    send: { from: effect, to: api, label: "GET /api/stock/DC-1042" }
    set: { api: "requests: 2" }
  - say: The answer is 6, new state, another render, another request. Thousands of open pages each do this, nonstop.
    send: { from: api, to: badge, label: "left: 6", back: true }
    set: { badge: "renders: 3", api: "requests: 40,000" }
```

## What you need

This incident uses topic 01 (effects and their dependency arrays) and topic 02 (fetching in an effect, loading states and ignoring stale answers). There's no new syntax.

The component is `src/SaleStock.jsx`. It gets an `itemId` prop and asks `GET /api/stock/<itemId>`, which answers with JSON like `{ "id": "DC-1042", "left": 7 }`. It shows:

- `Checking stock for DC-1042...` while it waits,
- `DC-1042: 7 left` once it knows,
- `DC-1042 is sold out` at 0.

## The tiers

1. **Bronze** resolves the incident: one page view sends exactly one request, and the badge shows the stock it got back.
2. **Silver**: when the page renders again for the same item, no request is sent. When `itemId` changes to another item, exactly one new request is sent, and the badge shows the new item's stock.
3. **Gold**: right after switching items, the badge says `Checking stock for DC-2001...`, never the old item's number under the new id. A slow answer for the old item, arriving after the new one, is ignored.

## Recap

- An effect with no dependency array runs after every render. If it sets state, that state causes the next render, and the effect runs again.
- List what the effect reads, `[itemId]`, so it runs once per item, not once per render.
- An empty array stops the flood, but then the badge never asks about a new item.
- Reset to the loading state when the id changes, and ignore answers from an effect that has been cleaned up.

## Common mistakes

**1. Stopping the loop with an empty array.**

```jsx
useEffect(() => {
  fetch(`/api/stock/${itemId}`)
    .then((res) => res.json())
    .then((data) => setLeft(data.left));
}, []);
```

The flood ends, and bronze passes. But a customer who taps from Golu to the coaster set sees Golu's stock forever, because the effect never runs again.

Fix: depend on what the effect reads, `[itemId]`.

**2. Guarding with a flag outside React.**

```jsx
let asked = false;
// inside the effect:
if (asked) return;
asked = true;
```

The second request stops, but so does every other badge on the page, and every later item. The flag is shared by all of them and never resets.

Fix: let the dependency array decide when the effect runs.

**3. Keeping the whole answer in state.**

```jsx
.then((data) => setStock(data));
```

With no dependency array, this loops even when the stock never changes. Every answer is a new object, and two objects are never the same object, so React always renders again.

Fix: the dependency array, not a cleverer `setState`.
