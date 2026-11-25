## What happened

dhaaga.in's router lives in `router.js`. `createRouter(routes, outlet, notFound)` returns `start`, `stop` and `navigate`. On the first Saturday of the Diwali sale, three kinds of complaint came in:

1. A customer opens the catalogue, taps Golu, and presses Back. The address bar says `/items`, but the screen still shows Golu.
2. Old addresses are still around. A banner on the home page links to `dhaaga.in/shop`, and the price tags print `dhaaga.in/golu`. Tapping the banner lands on the catalogue, but the first Back stops at `/shop` and still shows the catalogue, so it takes two Backs to leave. Scanning a `/golu` tag opens a not found page. Tapping Cart twice also needs two Backs to leave.
3. The browser tab shows the wrong title after Back, and a customer who filtered the catalogue to Indigo loses the filter when she comes Back to it.

```animate
title: The address moves, the screen doesn't
kind: scene
nodes:
  - { id: bar, label: Address bar, emoji: "🔗", value: "/items" }
  - { id: router, label: Router, emoji: "🧭", value: "remembers /items" }
  - { id: outlet, label: Outlet, emoji: "🖼️", value: Catalogue }
steps:
  - say: The customer taps Golu. navigate pushes /items/DC-1042, and the router remembers it.
    set: { bar: "/items/DC-1042", router: "remembers /items/DC-1042", outlet: "Golu the elephant" }
  - say: She presses Back. The browser changes the address to /items first, then fires popstate.
    set: { bar: "/items" }
    send: { from: bar, to: router, label: popstate }
  - say: The router draws the path it remembered, which is the page she is leaving.
    send: { from: router, to: outlet, label: "draw /items/DC-1042" }
  - say: The address says /items, but the screen still shows Golu. To her, Back is broken.
    focus: [bar, outlet]
```

## What you need

This builds on the History API router from topic 03 and the tests from topic 10. A route now has three parts:

| Part | Example | What it is for |
|---|---|---|
| `path` | `/items/:id` | the pattern, as before |
| `title(params)` | `Item DC-1042` | the screen's name, for `document.title` |
| `view(params, query)` | an element | `query` is a `URLSearchParams` for the part after `?` |

The title in the tab is the route's title, a space, a `|`, a space, and `SITE`: `Catalogue | Dhaaga & Co.`. Unknown addresses are titled `Page not found | Dhaaga & Co.`.

### Syntax: URLSearchParams

```js
const query = new URLSearchParams("?colour=Indigo");
const colour = query.get("colour");
```

`location.search` is the part of the address from `?` on, such as `"?colour=Indigo"`, or `""`. `new URLSearchParams(location.search)` reads it, and `query.get("colour")` gives `"Indigo"`, or `null` when there is no such key.

```callout tip
Keep one function that draws the screen and sets the title from the address bar. Then start, navigate and popstate can all call it, and none of them can drift out of step.
```

## The tiers

1. **Bronze** resolves the incident: Back and Forward show the page the address bar now shows, including after a deep link.
2. **Silver**: no extra history entries. Navigating to the address you are already on adds none. An old address in `REDIRECTS` leaves exactly one entry, for the new address. If the app starts on an old address, it swaps it with `replaceState`.
3. **Gold**: `document.title` is right after start, navigate, Back and Forward, and for the not found page. The `?colour=` filter comes back with Back, and a different filter counts as a different address.

## Recap

- When `popstate` fires, the address bar is already right. Draw from `location`, never from a path the router remembered.
- Push an entry only when the address really changes, path and query together.
- Redirect old addresses with one entry: push only the new address, and use `replaceState` when the app opens on an old one.
- Set the title in the same function that draws, so every way of arriving gets it.
- The query string is part of the address. Read it from `location.search`, so filters survive Back and Forward.

## Common mistakes

**1. Drawing what the router remembered.** A variable like `currentPath` is the page you are leaving when `popstate` fires. Read `location.pathname` and `location.search` instead.

**2. Fixing Back with pathname only.** The catalogue comes back, but without its `?colour=Indigo` filter. Include `location.search`.

**3. Pushing both the old and the new address.** Back lands on `/shop`, an address that only exists to redirect, so the customer needs two Backs to leave the catalogue. Push only the new address.

**4. Setting the title only in navigate.** Taps get the right title, but Back and deep links keep the old one. Set it where the screen is drawn.
