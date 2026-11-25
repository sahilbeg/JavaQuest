## Hook

Meera Aunty's recipe book has little paper tabs stuck to the pages: "Golu", "coasters", "booties". Flip to a tab, and you're on that page. It is still the same book.

The `#` part of a URL works like those tabs. It points somewhere inside the page you already have, and changing it never fetches a new page. That makes it the simplest way to give an SPA real addresses.

## Concept

### The hash: the part after #

```callout jargon
The **hash** (also called the **fragment**) is the part of a URL from the `#` sign onwards. In `dhaaga.in/#/items/DC-1042`, the hash is `#/items/DC-1042`.
```

You met it in web-01, where `<a href="#contact">` jumped to the element with `id="contact"`. Two rules make it perfect for a first router:

- Changing the hash **never reloads** the page.
- The browser **never sends** the hash to the server. It is only for the page itself.

Back and Forward still step through hash changes, so the visited list from topic 01 works for free.

### Syntax: location.hash

```js
const hash = location.hash;
location.hash = "#/cart";
```

- Reading `location.hash` gives the hash with its `#`, such as `"#/cart"`, or `""` when there is none.
- Setting it changes the address bar and adds an entry to the visited list, without a reload.
- A link like `<a href="#/cart">` does the same when tapped, with no JavaScript at all.

To get a path without the `#`, drop the first character: `"#/cart".slice(1)` gives `"/cart"`.

### Syntax: the hashchange event

```js
window.addEventListener("hashchange", () => {
  console.log(location.hash);
});
```

The browser fires `hashchange` on `window` whenever the hash changes: a tapped link, a hash typed into the address bar, or Back and Forward. Your router listens for it and redraws.

```animate
title: A tap on a hash link
kind: scene
nodes:
  - { id: bar, label: Address bar, emoji: "🔗", value: "dhaaga.in/#/items" }
  - { id: router, label: Your router, emoji: "🧭", value: listening }
  - { id: outlet, label: Outlet, emoji: "🖼️", value: Catalogue }
  - { id: server, label: Server, emoji: "🖥️", value: idle }
steps:
  - say: 'The customer taps Golu. The link''s href is "#/items/DC-1042".'
    set: { bar: "dhaaga.in/#/items/DC-1042" }
  - say: Only the hash changed, so the browser loads nothing. The server never hears about it.
    focus: [server]
  - say: The browser fires a hashchange event on window. Your router is listening.
    send: { from: bar, to: router, label: hashchange }
  - say: The router reads location.hash, finds the item route, and asks that view for a screen.
    set: { router: "item, id DC-1042" }
  - say: It swaps the outlet. The header and cart badge never moved.
    send: { from: router, to: outlet, label: "replaceChildren" }
    set: { outlet: "Golu the elephant" }
  - say: "The customer presses Back. The hash goes back to #/items, hashchange fires again, and the catalogue returns."
    set: { bar: "dhaaga.in/#/items", outlet: Catalogue, router: listening }
```

### Draw once at the start, too

`hashchange` fires only when the hash **changes**. When a customer opens a deep link, the page loads with the hash already set, and no event fires.

So a router does two things: it draws the screen for the current hash straight away, then redraws on every `hashchange`.

### Route patterns and params

A route table with one entry per item would need hundreds of lines. Instead, one route matches every item:

```js
const routes = [
  { path: "/", view: homeView },
  { path: "/items", view: catalogueView },
  { path: "/items/:id", view: itemView },
  { path: "/cart", view: cartView },
];
```

```callout jargon
A **route pattern** is a path with blanks in it, such as `/items/:id`. A part starting with `:` is a **route param**: it matches any value, and the router saves that value under the name, here `id`.
```

To compare, split both on `/` and look at the parts side by side. Watch the router pull the id out of Golu's path:

```animate
title: Matching /items/:id
kind: code
language: js
code: |
  const pattern = "/items/:id".split("/");
  const parts = "/items/DC-1042".split("/");
  const params = {};
  for (let i = 1; i < pattern.length; i++) {
    if (pattern[i].startsWith(":")) {
      params[pattern[i].slice(1)] = parts[i];
    }
  }
  console.log(params.id);
steps:
  - line: 1
    objects: { pat: { type: Array, fields: { "0": '""', "1": '"items"', "2": '":id"' } } }
    vars: { pattern: "@pat" }
    say: Splitting the pattern on "/" gives three parts. The first is empty, because the path starts with a slash.
  - line: 2
    objects: { got: { type: Array, fields: { "0": '""', "1": '"items"', "2": '"DC-1042"' } } }
    vars: { parts: "@got" }
    say: The real path splits into three parts too. Same length, so they might match.
  - line: 3
    objects: { par: { type: Object, fields: {} } }
    vars: { params: "@par" }
    say: An empty object will collect the params.
  - line: 4
    vars: { i: 1 }
    say: Start at part 1. Part 0 is the empty one, which is always the same.
  - line: 5
    say: '"items" is a fixed part, not a param. A full router would check that it equals "items" in the path.'
  - line: 4
    vars: { i: 2 }
    say: On to part 2.
  - line: 6
    objects: { par: { type: Object, fields: { id: '"DC-1042"' } } }
    say: '":id" starts with a colon. slice(1) gives the name "id", and the path''s part is saved under it.'
  - line: 9
    out: "DC-1042"
    say: The item view gets params.id and knows which item to draw.
```

A real matcher also says **no** when the lengths differ, when a fixed part is different, or when a param's part is empty (`/items/` has no id).

### First match wins

The router tries the routes in order and stops at the first one that matches. Order matters when two patterns could match one path:

```js
const routes = [
  { path: "/items/new", view: newItemView },
  { path: "/items/:id", view: itemView },
];
```

`/items/new` matches both. Because the fixed route comes first, it wins. Put specific routes before general ones.

### The not found screen

Some addresses match nothing: an old link, a typo, `#/hats`. Never leave the outlet blank. Show a friendly screen that says nothing lives there, with a link home. This is your app's own 404 page.

### Syntax: replaceChildren for the outlet

```js
outlet.replaceChildren(screen);
```

`replaceChildren` removes everything inside the outlet and puts the new screen in, in one step. Called with nothing, it just empties the element. `append` would pile each new screen under the old ones.

### Why teams moved on from the hash

Hash routing works everywhere, needs no server setup, and is fine for small apps. It has costs:

- URLs look odd: `dhaaga.in/#/cart`.
- The server never sees the path, so it can't send a ready page for search engines.
- It clashes with normal `#` jumps to a heading.

The next ticket uses the History API to get clean URLs like `dhaaga.in/cart`.

## Worked example

Here is a tiny router for the stall's info pages, with an exact-match table and no params:

```js
const pages = {
  "/": () => "Welcome to the Dhaaga stall",
  "/hours": () => "Sat–Sun 10:00–20:00",
  "/pay": () => "We take CASH, UPI and CARD",
};

function showPage(box) {
  const path = location.hash.slice(1) || "/";
  const page = pages[path];
  box.textContent = page ? page() : "Page not found";
}

const box = document.querySelector("#info");
showPage(box);
window.addEventListener("hashchange", () => showPage(box));
```

1. `slice(1)` drops the `#`. `|| "/"` turns an empty result into the home path.
2. The table lookup finds the page, or `undefined`, which shows the not found text.
3. `showPage(box)` runs once at the start, for deep links.
4. The listener redraws every time the hash changes, including Back and Forward.

Your ticket does the same, with patterns like `/items/:id` and real elements.

## Try it

This matcher only knows fixed parts. Run it, then teach it params: when a pattern part starts with `:`, save the path's part in a `params` object and return that object instead of `true`.

```js run
function matches(pattern, path) {
  const wanted = pattern.split("/");
  const parts = path.split("/");
  if (wanted.length !== parts.length) {
    return false;
  }
  return wanted.every((part, i) => part === parts[i]);
}

console.log(matches("/cart", "/cart"));
console.log(matches("/items/:id", "/items/DC-1042"));
console.log(matches("/items", "/items/DC-1042"));
```

## Recap

- The hash is the part of the URL from `#` on. Changing it never reloads the page, and the server never sees it.
- `location.hash` reads it. The `hashchange` event on `window` fires whenever it changes, including on Back and Forward.
- A router draws the screen for the current hash at the start, then again on every `hashchange`.
- Route patterns like `/items/:id` match many paths and save params. The first matching route wins, and no match shows a not found screen.
- `outlet.replaceChildren(screen)` swaps the screen in one step.

## Common mistakes

**1. Only listening for hashchange.**

```js
window.addEventListener("hashchange", render);
```

Tapping around works, but a deep link from WhatsApp opens on a blank outlet, because no hash changed. Fix: call `render()` once when the router starts, then listen.

**2. Forgetting the # when comparing.**

```js
if (location.hash === "/cart") {
  showCart();
}
```

`location.hash` is `"#/cart"`, with the `#`, so the cart never shows. Fix: drop the first character with `slice(1)` before comparing.

**3. Adding screens instead of swapping them.**

```js
outlet.append(view(params));
```

Every tap adds another screen under the old one, and the catalogue ends up below the cart. Fix: use `outlet.replaceChildren(view(params))`.

**4. Putting a general route first.**

```js
const routes = [
  { path: "/items/:id", view: itemView },
  { path: "/items/new", view: newItemView },
];
```

`/items/new` matches `:id` first, so the item view looks for an item called "new" and shows not found. Fix: list specific routes before general ones.
