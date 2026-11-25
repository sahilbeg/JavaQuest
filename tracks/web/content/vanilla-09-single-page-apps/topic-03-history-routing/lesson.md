## Hook

At the Diwali pop-up, Sana hangs a sign over the counter that says "Coasters". When the customer moves on to tote bags, she swaps the sign for "Tote bags". The counter never moves. Only the sign changes.

The **History API** lets your JavaScript swap the sign in the address bar, `dhaaga.in/items` to `dhaaga.in/items/DC-1042`, without loading anything. No `#`, and the URLs look like a normal website's.

## Concept

### The History API

```callout jargon
The **History API** is a set of browser tools on the `history` object. They let a page change the path in the address bar, and add to the visited list, without loading a new page.
```

With the hash, the browser did two jobs for you: it changed the URL when a link was tapped, and it fired an event. With the History API, the path is a real path, so the browser would happily load a new page. Your code has to step in.

### Syntax: history.pushState

```js
history.pushState(null, "", "/items/DC-1042");
```

- The first argument is a small piece of data stored with the entry. `null` is fine.
- The second is unused by browsers. Always pass `""`.
- The third is the new URL. It must be on the same website.

`pushState` changes the address bar and **adds** a new entry to the visited list. Nothing loads, and **no event fires**. Your code must draw the new screen itself, right after.

### Syntax: history.replaceState

```js
history.replaceState(null, "", "/items?colour=Indigo");
```

`replaceState` changes the address of the **current** entry, without adding a new one. Use it when the address should change but Back shouldn't stop there, such as a search box that updates `?q=` on every letter typed.

### Syntax: the popstate event

```js
window.addEventListener("popstate", () => {
  console.log(location.pathname);
});
```

When the customer presses Back or Forward between entries your app made, the browser changes the address and fires `popstate` on `window`. By then `location.pathname` already holds the new path, so your router just reads it and draws.

```callout warning
`popstate` fires for Back and Forward only. It never fires for your own `pushState` or `replaceState` calls.
```

```animate
title: pushState, then Back
kind: scene
nodes:
  - { id: list, label: Visited list, emoji: "📜", value: "/items" }
  - { id: bar, label: Address bar, emoji: "🔗", value: "/items" }
  - { id: router, label: Your router, emoji: "🧭", value: idle }
  - { id: outlet, label: Outlet, emoji: "🖼️", value: Catalogue }
steps:
  - say: The customer taps Golu. Your click listener stops the browser loading a page, and calls navigate.
    focus: [router]
  - say: navigate calls pushState. The address changes and a new entry joins the list. No event fires.
    send: { from: router, to: list, label: "pushState" }
    set: { list: "/items, /items/DC-1042", bar: "/items/DC-1042" }
  - say: So the router draws Golu's screen itself, straight after.
    send: { from: router, to: outlet, label: render }
    set: { outlet: "Golu the elephant" }
  - say: She presses Back. The browser steps back along the list and changes the address.
    set: { bar: "/items" }
    focus: [list, bar]
  - say: This time the browser fires popstate. The router reads location.pathname, which is /items.
    send: { from: bar, to: router, label: popstate }
    set: { router: "path /items" }
  - say: It draws the catalogue. It doesn't call pushState, because the list is already right.
    send: { from: router, to: outlet, label: render }
    set: { outlet: Catalogue, router: idle }
```

### The visited list in code

Here is the visited list as a plain array, so you can see what `pushState` and Back do to it:

```js run
const entries = ["/"];
let current = 0;

function pushState(path) {
  entries.length = current + 1;
  entries.push(path);
  current = entries.length - 1;
}

function back() {
  if (current > 0) {
    current -= 1;
  }
  return entries[current];
}

pushState("/items");
pushState("/items/DC-1042");
console.log(back());
pushState("/cart");
console.log(entries.join(" > "));
```

It prints `/items`, then `/ > /items > /cart`. A push after Back cuts off the entries ahead, which is why Forward greys out in your browser.

### Catching link taps

Your app's links are normal `<a href="/cart">` links, so they work even before JavaScript loads. Mark them with a `data-link` attribute, and catch taps on them with one listener on `document`:

```js
document.addEventListener("click", (event) => {
  const link = event.target.closest("a[data-link]");
  if (link === null) {
    return;
  }
  event.preventDefault();
  navigate(link.getAttribute("href"));
});
```

- `closest` finds the link even when the tap lands on a `<span>` or image inside it.
- `preventDefault()` stops the browser loading a new page.
- `getAttribute("href")` gives the path exactly as written, `/cart`. The `link.href` property would give the full URL.

### Leave some taps to the browser

Not every tap on a link is yours to catch:

- **Links to other sites**, like WhatsApp, have no `data-link`, so they load normally.
- **Ctrl-click, Cmd-click, Shift-click and Alt-click** mean "open in a new tab" or "download". Check `event.ctrlKey`, `event.metaKey`, `event.shiftKey` and `event.altKey`, and return early if any is true.

Customers notice at once when a site breaks "open in new tab".

### The server must answer every path

```callout jargon
A **fallback** route on the server sends the same `index.html` for any path it has no file for. Then a refreshed `/items/DC-1042` loads the app, and the router draws Golu.
```

With the hash, the server only ever saw `/`. Now it sees `/items/DC-1042` on every refresh or shared link. Static hosts call this a "rewrite" or "SPA fallback" setting. Without it, refresh gives a 404.

### Jobs the browser no longer does

A full page load gave you these for free. In an SPA, each screen change should:

- set `document.title`, so the tab and the history list say where the customer is;
- scroll to the top, with `window.scrollTo(0, 0)`, so a new screen doesn't open halfway down;
- move focus to the new screen's heading, so a screen reader announces it.

## Worked example

The stall's info pages again, now with clean paths:

```js
const pages = {
  "/": () => "Welcome to the Dhaaga stall",
  "/hours": () => "Sat–Sun 10:00–20:00",
  "/pay": () => "We take CASH, UPI and CARD",
};
const box = document.querySelector("#info");

function render() {
  const page = pages[location.pathname];
  box.textContent = page ? page() : "Page not found";
}

function navigate(path) {
  history.pushState(null, "", path);
  render();
}

document.addEventListener("click", (event) => {
  const link = event.target.closest("a[data-link]");
  if (link === null || event.ctrlKey || event.metaKey) {
    return;
  }
  event.preventDefault();
  navigate(link.getAttribute("href"));
});
window.addEventListener("popstate", render);
render();
```

1. `render` reads the path from the address bar, so it works for every way of arriving.
2. `navigate` pushes an entry and draws, because `pushState` fires nothing.
3. The click listener catches only app links, and leaves new-tab clicks alone.
4. `popstate` just calls `render`. The browser already moved the address.
5. The last line draws whatever address the page opened with.

## Try it

In the visited list example above, add a `forward()` function that moves `current` one place ahead, if there is an entry there. Call `back()` twice and `forward()` once, and print the path each time.

## Recap

- `history.pushState(null, "", path)` changes the URL and adds an entry, with no reload and no event. Draw the screen yourself right after.
- `history.replaceState` changes the current entry without adding one.
- Back and Forward fire `popstate` on `window`. Read `location.pathname` and draw. Never push from there.
- Catch taps on `a[data-link]` with one listener, call `preventDefault()`, and leave external links and Ctrl or Cmd clicks alone.
- The server must answer every app path with `index.html`. Set the title, scroll and focus for each new screen.

## Common mistakes

**1. Waiting for an event after pushState.**

```js
function navigate(path) {
  history.pushState(null, "", path);
}
window.addEventListener("popstate", render);
```

The address bar says `/cart`, but the screen still shows the catalogue. `pushState` never fires `popstate`. Fix: call `render()` right after `pushState`.

**2. Forgetting preventDefault.**

```js
document.addEventListener("click", (event) => {
  const link = event.target.closest("a[data-link]");
  if (link !== null) {
    navigate(link.getAttribute("href"));
  }
});
```

The screen swaps, and then the browser loads the whole page from the server anyway. The cart and everything else reset. Fix: call `event.preventDefault()` before `navigate`.

**3. Pushing from popstate.**

```js
window.addEventListener("popstate", () => {
  navigate(location.pathname);
});
```

Every Back adds a fresh entry, so Forward never works, and Back seems stuck between two screens. Fix: in `popstate`, only call `render()`.

**4. Catching every link.**

```js
const link = event.target.closest("a");
```

The WhatsApp link and Ctrl-clicks stop working: the router tries to draw `https://wa.me/...` as a screen. Fix: catch only `a[data-link]`, and return early when a modifier key is held.
