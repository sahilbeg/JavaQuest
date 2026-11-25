## Hook

Picture the Aundh shop with a strange rule. Every time a customer wants to see a different shelf, Raju locks the door, empties the shop, and builds it again around that one shelf. The counter, the price board and the customer's basket all start from nothing.

That is how dhaaga.in works today. Every tap asks the server for a whole new page, and the browser throws the old one away.

A **single-page app** keeps the shop standing. Only the shelf in the middle changes. In this ticket you learn what that means, and what it costs.

## Concept

### A multi-page site: a new page for every tap

```callout jargon
A **multi-page app** (MPA) is a website where every link loads a brand-new HTML page from the server. Most websites you met in web-01 work this way.
```

When a customer taps **Golu the elephant**, the browser asks the server for `/items/DC-1042`. The server sends a whole HTML page. The browser throws away the old page, with all its JavaScript variables, and draws the new one from the top.

That is simple and sturdy. But the header, the menu and the cart badge are sent and drawn again on every tap, and anything kept in a variable is lost.

### A single-page app: one page, many screens

```callout jargon
A **single-page app** (SPA) loads one HTML page and its JavaScript once. After that, JavaScript swaps what is on the screen itself, and asks the server only for data, usually as JSON.
```

The browser never loads a new page. The same `document` stays alive the whole time, so variables, the cart and the open menu all survive a move to another screen.

```animate
title: One tap, two kinds of site
kind: scene
nodes:
  - { id: phone, label: Customer's phone, emoji: "📱", value: "Catalogue" }
  - { id: server, label: dhaaga.in server, emoji: "🖥️", value: idle }
steps:
  - say: On a multi-page site, the customer taps Golu. The browser asks for a whole new page.
    send: { from: phone, to: server, label: "GET /items/DC-1042" }
    set: { server: busy }
  - say: The server sends back a full HTML page, header, menu, footer and all.
    send: { from: server, to: phone, label: "whole HTML page", back: true }
    set: { server: idle, phone: "(blank, redrawing)" }
  - say: The browser throws the old page away and draws the new one from the top. Every variable starts again.
    set: { phone: "Golu's page" }
  - say: Now the single-page app. The customer taps Golu, and JavaScript catches the tap itself.
    set: { phone: "Catalogue" }
    focus: [phone]
  - say: It asks the server only for Golu's data, which is a few hundred bytes of JSON.
    send: { from: phone, to: server, label: "GET /api/items/DC-1042" }
  - say: The JSON arrives. JavaScript swaps the middle of the screen. The header and the cart never moved.
    send: { from: server, to: phone, label: "{ name, paise }", back: true }
    set: { phone: "Golu's screen" }
```

### The app shell

```callout jargon
The **app shell** is the part of an SPA that stays on screen all the time: the header, the menu, the cart badge and the footer. Inside it sits one empty element, the **outlet**, where the screens are swapped.
```

```html
<header>
  <a href="/">Dhaaga &amp; Co.</a>
  <span id="cart-badge">0</span>
</header>
<main id="app"></main>
<footer>FC Road stall, Sat–Sun 10:00–20:00</footer>
```

Here `<main id="app">` is the outlet. The header and footer are drawn once and never again.

### Routing: the URL still matters

```callout jargon
A **route** is one address in your app, such as `/cart`, paired with the screen it shows. **Client-side routing** means your JavaScript, not the server, reads the address and picks the screen.
```

An SPA must still change the address bar for every screen. Customers rely on it more than you think:

- **Back** should go to the screen they saw before.
- **Refresh** should keep them on the same screen.
- A link pasted into WhatsApp should open that exact screen.

```callout jargon
A **deep link** is a URL that opens one particular screen inside an app, such as `dhaaga.in/items/DC-1042`, rather than the home page.
```

### A route table in plain JavaScript

At heart, a router is a table from addresses to screens. Here is the idea with text instead of real screens:

```js run
const routes = {
  "/": () => "Home: Dhaaga & Co.",
  "/items": () => "Catalogue: 5 items",
  "/cart": () => "Cart: 2 items",
};

function show(path) {
  const view = routes[path];
  console.log(view ? view() : `Not found: ${path}`);
}

show("/");
show("/items");
show("/hats");
```

An address the table doesn't know gets a **not found** screen, just like a 404 page.

### What the back button remembers

The browser keeps a list of the addresses you visited in this tab. Back steps one place along that list. Watch a customer browse and then press Back:

```animate
title: The visited list and the Back button
kind: code
language: js
code: |
  const visited = ["/"];
  visited.push("/items");
  visited.push("/items/DC-1042");
  visited.pop();
  console.log(visited.at(-1));
steps:
  - line: 1
    objects: { v: { type: Array, fields: { "0": '"/"' } } }
    vars: { visited: "@v" }
    say: The customer opens dhaaga.in. The list holds one address, the home page.
  - line: 2
    objects: { v: { type: Array, fields: { "0": '"/"', "1": '"/items"' } } }
    say: She opens the catalogue. Its address goes on the end of the list.
  - line: 3
    objects: { v: { type: Array, fields: { "0": '"/"', "1": '"/items"', "2": '"/items/DC-1042"' } } }
    say: She taps Golu. A third address joins the list.
  - line: 4
    objects: { v: { type: Array, fields: { "0": '"/"', "1": '"/items"' } } }
    say: She presses Back. The browser steps back one place.
  - line: 5
    out: "/items"
    say: The address is now /items. Your SPA must notice and show the catalogue again.
```

The browser keeps this list for you. In an SPA, your job is to add to it when the screen changes, and to redraw when Back moves along it. The next two tickets do exactly that.

### What you gain

- **Speed after the first load.** Only data travels, not whole pages.
- **Nothing resets.** The cart, a half-typed search and an open menu survive moving between screens.
- **Smooth moves.** No white flash between screens, which feels like an app on the phone.

### What it costs

- **A heavier first load.** All the JavaScript must arrive before the first screen.
- **It needs JavaScript.** With JavaScript off or broken, the customer sees an empty outlet.
- **You rebuild what the browser gave for free:** changing the URL, Back and Forward, the page title, scrolling to the top, and moving focus for screen readers.
- **Search engines and link previews** may see an empty page, unless the server also sends real HTML.

```callout tip
The server still matters in an SPA. It sends data, and it must send the same `index.html` for every address, so a refreshed deep link like `/items/DC-1042` doesn't get a 404.
```

### Which one for dhaaga.in?

Many real sites mix both. Product pages that Google should find can stay as server pages. The cart and checkout, where customers tap back and forth, gain the most from an SPA.

React Router, Vue Router and similar libraries do this job inside frameworks. In this module you build the same parts with plain JavaScript, so you know what they do.

## Worked example

Follow one tap on **Golu the elephant** through a plain-JavaScript SPA:

```js
document.addEventListener("click", (event) => {
  const link = event.target.closest("a[data-link]");
  if (link === null) {
    return;
  }
  event.preventDefault();
  history.pushState(null, "", link.getAttribute("href"));
  showScreen(location.pathname);
});
```

1. One listener on the whole document catches every tap, using event delegation from web-05.
2. `closest("a[data-link]")` checks that the tap landed on one of the app's own links.
3. `preventDefault()` stops the browser from loading a new page.
4. `history.pushState` changes the address bar and adds an entry to the visited list. Topic 03 covers it.
5. `showScreen` looks the address up in the route table and swaps the outlet. That screen then fetches Golu's JSON.

Nothing else on the page moves.

## Try it

In the route table example above, add a `"/checkout"` route that prints `Checkout: step 1 of 3`. Then call `show("/checkout")` and `show("/Cart")`. Why does the second one say not found?

## Recap

- A multi-page app loads a new HTML page for every tap. A single-page app loads one page once, then swaps screens with JavaScript.
- The app shell (header, menu, cart badge) stays put. Screens are swapped inside one outlet.
- Client-side routing maps each URL to a screen, so Back, refresh and shared deep links still work.
- SPAs gain speed and keep state between screens. They cost a heavier first load, and you must rebuild the URL, title, focus and Back handling yourself.
- The server still sends data, and must answer every app address with the same `index.html`.

## Common mistakes

**1. Swapping the screen without changing the URL.**

```js
function openCart(outlet) {
  outlet.replaceChildren(cartScreen());
}
```

The cart appears, but the address bar still says `/items`. Refresh shows the catalogue, Back leaves the site, and a shared link opens the wrong screen. Fix: change the URL whenever the screen changes, and draw the screen from the URL.

**2. Forgetting the page title.**

```js
outlet.replaceChildren(itemScreen("DC-1042"));
```

The browser tab still says "Catalogue", and so does the history list. Screen readers announce the old title too. Fix: set `document.title`, such as `Golu the elephant | Dhaaga & Co.`, for every screen.

**3. Thinking an SPA needs no server setup.**

```text
GET /items/DC-1042  ->  404 Not Found
```

Clicking around works, but refreshing a deep link asks the server for a file that doesn't exist. Fix: make the server answer every app address with `index.html`, and let the router pick the screen.

**4. Loading every screen's data on the first load.**

```js
async function start() {
  const everything = await Promise.all([loadItems(), loadOrders(), loadReviews()]);
  drawHome(everything);
}
```

The customer waits for data that most screens never show. Fix: let each screen fetch what it needs when it opens, and keep it for next time.
