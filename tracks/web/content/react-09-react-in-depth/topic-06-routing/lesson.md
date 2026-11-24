## Hook

A book has page numbers. You can tell a friend "look at page 42", put a bookmark in it, or flip back to where you were.

Dhaaga's React store is one big page that swaps what it shows. It has no page numbers. A customer can't share the link to Golu the elephant, and the Back button leaves the site. This ticket gives every screen its own address.

## Concept

### One app, many pages

```callout jargon
A **single-page app** (SPA) loads one HTML page once, then JavaScript swaps the content as you move around. **Routing** means choosing what to show based on the address in the browser's address bar. When JavaScript does this in the browser, without asking the server for a new page, it's **client-side routing**.
```

The address is the best place to keep "which page am I on", because the browser already looks after it:

- **Back and Forward** move through the addresses the customer visited.
- **Bookmarks and shared links** save the address, so they open the same page.
- **Refresh** keeps the address, so the customer stays where they were.

### The hash: the part after #

The simplest kind of client-side routing uses the **hash**: everything after `#` in the address. Changing it never asks the server for a new page, so the React app stays loaded.

```js run
const url = new URL("https://dhaaga.in/#/item/DC-1042");
console.log(url.hash);
console.log(url.hash.slice(1));

const home = new URL("https://dhaaga.in/");
console.log(JSON.stringify(home.hash));
console.log(home.hash.slice(1) || "/");
```

In the browser, the current address is `window.location`. So the current path is:

```js
function currentPath() {
  return window.location.hash.slice(1) || "/";
}
```

`slice(1)` drops the `#`. With no hash at all, `|| "/"` falls back to the home page.

### Syntax: listening for hashchange

```jsx
window.addEventListener("hashchange", onChange);
```

The address lives outside React. When it changes, React doesn't know. But the browser fires a `hashchange` event on `window` every time the hash changes: from a link, from Back, or from code. A custom hook can listen for it and keep the path in state.

```jsx
export function useHashRoute() {
  const [path, setPath] = useState(currentPath);

  useEffect(() => {
    function onChange() {
      setPath(currentPath());
    }
    window.addEventListener("hashchange", onChange);
    return () => window.removeEventListener("hashchange", onChange);
  }, []);

  return path;
}
```

- `useState(currentPath)` passes the function itself, so React calls it once, for the first render.
- The effect subscribes once (empty array) and unsubscribes in its cleanup.
- `removeEventListener` needs the very same function that was added. That's why `onChange` has a name.

### What happens when a link is clicked

```animate
title: From a click to a new page
kind: scene
nodes:
  - { id: link, label: "Link to Cart", emoji: "🔗" }
  - { id: browser, label: Address bar, emoji: "🌐", value: "dhaaga.in/#/" }
  - { id: hook, label: useHashRoute, emoji: "🪝", value: "path /" }
  - { id: app, label: App, emoji: "⚛️", value: Home page }
steps:
  - say: The customer taps the Cart link. It's a normal link to "#/cart".
    focus: [link]
  - say: The browser changes the hash in the address bar. It does not load a new page from the server.
    send: { from: link, to: browser, label: "#/cart" }
    set: { browser: "dhaaga.in/#/cart" }
  - say: The browser fires a hashchange event on window. Our hook is listening.
    send: { from: browser, to: hook, label: hashchange }
  - say: The listener reads the new path and puts it in state.
    set: { hook: "path /cart" }
  - say: New state means App renders again. matchRoute picks the cart page.
    send: { from: hook, to: app, label: "/cart" }
    set: { app: Cart page }
  - say: Pressing Back changes the hash to "#/" again. The same event brings the home page back.
    send: { from: browser, to: hook, label: hashchange }
    set: { browser: "dhaaga.in/#/", hook: "path /", app: Home page }
```

### Syntax: matching a route

```jsx
function matchRoute(path) {
  if (path === "/") return { page: "home" };
  return { page: "not-found" };
}
```

```callout jargon
A **route** is a pattern of addresses and the page it shows. **Route matching** turns an address into "which page, and with what details". `/item/DC-1042` matches the item route, with the id `DC-1042`.
```

`matchRoute` is a plain function: no hooks, no React. That makes it easy to test.

```js run
function matchRoute(path) {
  if (path === "/" || path === "") return { page: "home" };
  if (path === "/cart") return { page: "cart" };
  if (path.startsWith("/item/") && path.length > "/item/".length) {
    return { page: "item", id: path.slice("/item/".length) };
  }
  return { page: "not-found" };
}

for (const path of ["/", "/cart", "/item/DC-1042", "/item/", "/about"]) {
  console.log(path, JSON.stringify(matchRoute(path)));
}
```

`/item/` with no id doesn't match the item route. Every address that matches nothing gets a "Page not found" page, so a typo never shows a blank screen.

### Syntax: links

```jsx
<a href="#/cart">Cart</a>
```

Use a real `<a>` with an `href`, not a `<button>` that sets state. A link can be opened in a new tab, copied, read out by a screen reader as a link, and reached with the keyboard. A small `Link` component saves typing the `#` each time:

```jsx
export function Link({ to, children }) {
  return <a href={"#" + to}>{children}</a>;
}
```

### Picking the page

The app reads the path, matches it, and returns one page:

```jsx
function Page({ route }) {
  switch (route.page) {
    case "home":
      return <HomePage />;
    case "item":
      return <ItemPage id={route.id} />;
    default:
      return <NotFound />;
  }
}
```

This is conditional rendering, which you already know. The only new part is where the condition comes from: the address bar.

### What teams use: React Router

Real projects use a routing library. The most common is **React Router**. It does the same three jobs: watch the address, match routes, and render links.

```jsx
<Routes>
  <Route path="/" element={<HomePage />} />
  <Route path="/item/:id" element={<ItemPage />} />
  <Route path="*" element={<NotFound />} />
</Routes>
```

`:id` marks a part of the path to capture, and `useParams()` reads it inside the page. React Router uses normal paths like `/item/DC-1042` without a `#`, through the browser's **History API**. That needs the server to send the app's `index.html` for every path, so it takes a little server setup. The hash version you're building needs none.

```callout tip
Now that you've built a tiny router, React Router's docs will read like a list of features you already understand.
```

## Worked example

Here is the whole router, in three small files and an `App`.

```jsx
// router.js
export function currentPath() {
  return window.location.hash.slice(1) || "/";
}

export function matchRoute(path) {
  if (path === "/" || path === "") return { page: "home" };
  if (path === "/cart") return { page: "cart" };
  if (path.startsWith("/item/") && path.length > "/item/".length) {
    return { page: "item", id: path.slice("/item/".length) };
  }
  return { page: "not-found" };
}
```

```jsx
// App.jsx
export function App() {
  const route = matchRoute(useHashRoute());
  return (
    <>
      <nav>
        <Link to="/">Home</Link> <Link to="/cart">Cart</Link>
      </nav>
      <main>
        <Page route={route} />
      </main>
    </>
  );
}
```

- `useHashRoute()` gives the current path and renders again when it changes.
- `matchRoute` turns the path into `{ page, id }`.
- `Page` picks what to show. `ItemPage` gets the id, so it can look up Golu the elephant.
- Back, Forward, bookmarks and shared links now all work, because the page follows the address.

## Try it

Open the preview and click between Home, Cart and Golu the elephant. Then use the preview's Back button. Type `#/item/DC-2001` at the end of the address, and then `#/nowhere`.

Now delete the `removeEventListener` line from the cleanup. Nothing looks different yet, but every time the app mounts it adds another listener and never removes it. In Strict Mode there would be two from the start.

## Recap

- Keep "which page" in the address, so Back, bookmarks, refresh and shared links work.
- The hash is everything after `#`. Changing it never reloads the page, and the browser fires `hashchange`.
- A `useHashRoute` hook keeps the path in state, listens for `hashchange`, and removes its listener in the cleanup.
- `matchRoute(path)` is a plain function that turns an address into `{ page, id }`, with a "not found" page for everything else.
- Use real `<a href="#/...">` links. In real projects, React Router does all of this with normal paths.

## Common mistakes

**1. Reading the address without listening.**

```jsx
export function App() {
  const route = matchRoute(currentPath());
  return <Page route={route} />;
}
```

The first page is right, but clicking a link changes the address bar and not the page. Nothing told React to render again.

Fix: keep the path in state with `useHashRoute`, which updates on `hashchange`.

**2. Buttons instead of links.**

```jsx
<button onClick={() => setPage("cart")}>Cart</button>
```

The cart shows, but the address never changes. Back leaves the site, and the customer can't share or bookmark the page.

Fix: `<Link to="/cart">Cart</Link>`, which renders `<a href="#/cart">`.

**3. Removing a different function than you added.**

```jsx
useEffect(() => {
  window.addEventListener("hashchange", () => setPath(currentPath()));
  return () => window.removeEventListener("hashchange", () => setPath(currentPath()));
}, []);
```

No error appears, but the listener is never removed. The two arrow functions look the same but are different objects.

Fix: give the function a name, `function onChange() { ... }`, and pass that same name to both.

**4. No page for unknown addresses.**

```jsx
switch (route.page) {
  case "home":
    return <HomePage />;
  case "cart":
    return <CartPage />;
}
```

`#/about` matches no case, so `Page` returns `undefined` and the customer sees a blank screen.

Fix: add `default: return <NotFound />;`.
