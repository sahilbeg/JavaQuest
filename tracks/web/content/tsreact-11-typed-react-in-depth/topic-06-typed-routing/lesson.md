## Hook

A shop with no shelf labels works fine while you're inside. But you can't tell a friend "the elephant is on shelf B2". Every visit starts at the door.

dhaaga.in is like that today: every screen lives at the same address. This ticket gives each screen its own address, and uses TypeScript so that a link to a page that doesn't exist is caught before anyone clicks it.

## Concept

### One page, many screens

The store is a **single-page app**. The browser loads one HTML page, and React swaps what's shown inside it.

```callout jargon
A **single-page app** (SPA) loads one page once, then changes the screen with JavaScript instead of loading a new page from the server. **Routing** is choosing which screen to show from the address.
```

Without routing, refresh and Back both break: refresh always lands on the first screen, and Back leaves the site.

### The hash part of an address

The simplest place to keep "which screen" is after a `#` in the address:

```text
https://dhaaga.in/#/item/DC-1042
```

```callout jargon
The **hash** is the part of the address from `#` on. Changing it doesn't load a new page from the server. The browser fires a **hashchange** event on `window` instead, and it adds an entry to history, so Back works.
```

In code, `window.location.hash` is `"#/item/DC-1042"`. Stripping the `#` gives the **path**, `"/item/DC-1042"`. An empty hash means the path `"/"`.

### Routes as a union type

Every screen the store has is one shape in a union:

```ts
type Route =
  | { page: "shop" }
  | { page: "item"; id: string }
  | { page: "order"; orderId: string }
  | { page: "notFound"; path: string };
```

This is the same trick as the reducer's actions. `page` is the discriminant, and each page carries exactly what it needs: an item page has an `id`, an order page an `orderId`.

The rest of the app never handles raw address text. It handles `Route` values, and TypeScript checks them.

### Syntax: parsing a path into a Route

```ts
declare function parseRoute(path: string): Route;
```

Parsing turns text from the address bar into a typed `Route`. Splitting on `/` does most of the work:

```ts run
console.log("/item/DC-1042".split("/"));
console.log("/order/order-17".split("/"));
console.log("/item/".split("/"));
console.log("/".split("/"));
```

`"/item/"` gives an empty last part, so it is not a real item. Anything the parser doesn't recognise becomes `{ page: "notFound", path }`, so every address has an answer.

```animate
title: Turning an address into a route
kind: code
language: ts
code: |
  function parse(path: string) {
    if (path === "/") return { page: "shop" };
    const [empty, section, id, ...rest] = path.split("/");
    if (empty === "" && id && rest.length === 0 && section === "item") return { page: "item", id };
    return { page: "notFound", path };
  }
  console.log(parse("/").page);
  console.log(parse("/item/DC-1042").page);
  console.log(parse("/item/").page);
steps:
  - line: 7
    out: "shop"
    say: The path "/" is the front of the shop.
  - line: 3
    vars: { empty: '""', section: '"item"', id: '"DC-1042"', rest: "[]" }
    say: For "/item/DC-1042", the split gives an empty start, the section, and the id.
  - line: 8
    out: "item"
    say: Three parts, a real id, and the item section. It's Golu's item page.
  - line: 3
    vars: { id: '""' }
    say: For "/item/", the id is an empty string.
  - line: 9
    out: "notFound"
    say: An empty id is falsy, so the check fails and the parser says notFound.
```

### Syntax: building links from routes

```ts
declare function toHash(route: Route): string;
```

Links go the other way: from a `Route` to an address. A `Link` component takes a `Route`, not a string:

```tsx
function Link({ to, children }: { to: Route; children: ReactNode }) {
  return <a href={toHash(to)}>{children}</a>;
}

<Link to={{ page: "item", id: "DC-1042" }}>Golu the elephant</Link>
```

Now a typo in a link is a type error, not a broken page for a customer:

```tsx
<Link to={{ page: "itme", id: "DC-1042" }}>Golu</Link>
```

```text
error TS2322: Type '"itme"' is not assignable to type '"item" | "notFound" | "order" | "shop"'.
```

A string link like `href="#/itme/DC-1042"` would type-check fine and send Meera Aunty's customers to "Page not found".

### Syntax: a hook for the current route

```tsx
function useRoute(): Route {
  const [path, setPath] = useState<string>(() => pathFromHash(window.location.hash));
  useEffect(() => {
    function handleChange() {
      setPath(pathFromHash(window.location.hash));
    }
    window.addEventListener("hashchange", handleChange);
    return () => window.removeEventListener("hashchange", handleChange);
  }, []);
  return parseRoute(path);
}
```

The hook keeps the path in state, so a change re-renders the app. The listener is an effect, and its cleanup removes it, as in ticket 1. The route itself is worked out while rendering, from the path.

```animate
title: A link click changes the page
kind: scene
nodes:
  - { id: link, label: "Link to Golu", emoji: "🔗" }
  - { id: browser, label: Browser, emoji: "🌐", value: "#/" }
  - { id: hook, label: useRoute, emoji: "🧭", value: "shop" }
  - { id: app, label: App, emoji: "🧶", value: "All items" }
steps:
  - say: A customer taps the link. Its href is "#/item/DC-1042", made by toHash.
    send: { from: link, to: browser, label: "#/item/DC-1042" }
    set: { browser: "#/item/DC-1042" }
  - say: The browser changes the hash without loading a page, and fires hashchange on window.
    send: { from: browser, to: hook, label: hashchange }
  - say: The hook's listener reads the new path and sets state. parseRoute turns it into an item route.
    set: { hook: "item DC-1042" }
  - say: The state changed, so App renders again and shows Golu's page.
    send: { from: hook, to: app, label: "{ page: item, id }" }
    set: { app: "Golu the elephant" }
  - say: Pressing Back changes the hash back to "#/", and the same steps show the shop again.
    send: { from: browser, to: hook, label: hashchange, back: true }
    set: { browser: "#/", hook: shop, app: "All items" }
```

### Choosing the page with an exhaustive switch

```tsx
function Page({ route }: { route: Route }) {
  switch (route.page) {
    case "shop": return <ShopPage />;
    case "item": return <ItemPage id={route.id} />;
    case "order": return <OrderPage orderId={route.orderId} />;
    case "notFound": return <NotFoundPage path={route.path} />;
    default: return assertNever(route);
  }
}
```

Each case narrows `route`, so `route.id` is only allowed under `"item"`. And with `assertNever`, adding a new page to `Route` without a screen for it is a type error.

### What teams use

Real projects usually use a router library. **React Router** is the most common, and **TanStack Router** types every route and link, much like the union you built here. They also use real paths (`/item/DC-1042`) with the History API instead of a hash. The ideas are the same: parse the address, render the matching page, build links from known routes.

## Worked example

Here's routing for a tiny admin area, with its own union and its own hash rules.

```tsx
type AdminRoute =
  | { page: "dashboard" }
  | { page: "yarn"; colour: string }
  | { page: "missing" };

function parseAdmin(path: string): AdminRoute {
  if (path === "/") return { page: "dashboard" };
  const [empty, section, colour, ...rest] = path.split("/");
  if (empty === "" && section === "yarn" && colour && rest.length === 0) {
    return { page: "yarn", colour: decodeURIComponent(colour) };
  }
  return { page: "missing" };
}

function adminHash(route: AdminRoute): string {
  switch (route.page) {
    case "dashboard": return "#/";
    case "yarn": return `#/yarn/${encodeURIComponent(route.colour)}`;
    case "missing": return "#/missing";
  }
}
```

- `"/yarn/Rani%20pink"` parses to `{ page: "yarn", colour: "Rani pink" }`, because `decodeURIComponent` turns `%20` back into a space.
- `adminHash` covers every page, so TypeScript knows the function always returns a string, even without a `default`.
- Parsing and building links are opposites: `parseAdmin(adminHash(route).slice(1))` gives back the same route.

## Try it

Open the preview. Click **Golu the elephant**, then **My order**. Watch the address change, then press the browser's Back button.

Then, in `App.tsx`, change one `Link` to `{ page: "itme", id: "DC-1042" }` and read the type error. Put it back. Finally, type `#/item/` by hand at the end of the address: it should show Page not found.

## Recap

- A single-page app shows different screens in one page. Routing picks the screen from the address.
- With hash routing, the screen lives after `#`. Changing it fires `hashchange` on `window`, and Back works.
- Model every screen as one shape in a `Route` union, then parse addresses into routes and build links from routes.
- A `Link` that takes a `Route` turns a typo in a link into a type error.
- A `useRoute` hook keeps the path in state, listens for `hashchange`, and removes its listener in the cleanup.

## Common mistakes

**1. Links as plain strings.**

```tsx
<a href="#/itme/DC-1042">Golu the elephant</a>
```

It type-checks, and every customer who taps it gets "Page not found".

Fix: build every link from a `Route` with `toHash`, through a typed `Link`.

**2. Reading the hash once and never again.**

```tsx
function useRoute(): Route {
  return parseRoute(pathFromHash(window.location.hash));
}
```

The first page is right. After a link click the address changes, but nothing re-renders, so the screen stays the same.

Fix: keep the path in state and update it in a `hashchange` listener.

**3. Matching with startsWith.**

```tsx
if (path.startsWith("/item/")) return { page: "item", id: path.slice(6) };
```

`"/item/"` gives an item page with an empty id, and `"/item/DC-1042/extra"` gives the id `"DC-1042/extra"`.

Fix: split on `/`, and check the number of parts and that the id isn't empty.

**4. Forgetting the cleanup.**

```tsx
useEffect(() => {
  window.addEventListener("hashchange", handleChange);
}, []);
```

Each time the app mounts (twice in Strict Mode), another listener is added and never removed.

Fix: `return () => window.removeEventListener("hashchange", handleChange);`.
