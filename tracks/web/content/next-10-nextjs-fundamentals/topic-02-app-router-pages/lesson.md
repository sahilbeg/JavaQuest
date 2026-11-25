## Hook

A big school has corridors, and each corridor has rooms. "Science block, first floor, lab 2" tells you exactly where to walk. Nobody needs a map that lists every room, because the building itself is the map.

In react-09 you wrote `matchRoute` to turn addresses into pages. In Next.js, the folders are the map. Make a folder, put a page in it, and that address exists.

## Concept

### Folders are addresses

Everything starts in the `app` folder (here, `src/app`). Each folder inside it adds one part to the address. A part of an address between two slashes is called a **segment**.

```text
src/app/page.jsx              →  /
src/app/about/page.jsx        →  /about
src/app/visit/page.jsx        →  /visit
src/app/help/delivery/page.jsx →  /help/delivery
```

```callout jargon
A **route** is an address the site answers, like `/about`. A **segment** is one part of it between slashes: `/help/delivery` has two segments, `help` and `delivery`. In the app router, each folder is one segment.
```

### Syntax: a page

```jsx
export default function AboutPage() {
  return <h1>Our story</h1>;
}
```

A page is a React component that is the **default export** of a file called `page.jsx` (or `page.js`). Next.js finds the file by its name and shows whatever the component returns at that folder's address.

- The function's name doesn't matter to Next.js. `AboutPage` just reads well.
- It must be `export default`. A named export like `export function AboutPage` doesn't count as the page.
- It's ordinary JSX. Everything from react-08 works here.

### Following a request to its page

```animate
title: A customer opens dhaaga.in/visit
kind: scene
nodes:
  - { id: phone, label: Phone, emoji: "📱" }
  - { id: next, label: Next.js, emoji: "🖥️" }
  - { id: app, label: "src/app", emoji: "📁" }
  - { id: visit, label: "visit/", emoji: "📁" }
  - { id: page, label: "page.jsx", emoji: "📄" }
links:
  - { from: app, to: visit }
  - { from: visit, to: page }
steps:
  - say: The customer opens dhaaga.in/visit. The address has one segment, visit.
    send: { from: phone, to: next, label: "GET /visit" }
  - say: Next.js starts at the app folder, which stands for the address /.
    focus: [app]
  - say: The first segment is visit, so it looks for a folder called visit inside app.
    focus: [visit]
  - say: There are no more segments. Next.js looks for a page.jsx in this folder, and finds one.
    focus: [page]
  - say: It runs the page's component on the server and sends the HTML back.
    send: { from: next, to: phone, label: "HTML: Visit us", back: true }
  - say: An address with no matching folder, or a folder with no page.jsx, gets a 404 Not Found page instead.
    focus: [next]
```

### Only page files become pages

You can keep other files next to a page. A folder can hold a `Hours.jsx` component, a CSS file or a helper. None of them become addresses. Only a file called `page` does.

```text
src/app/visit/page.jsx     →  /visit
src/app/visit/Hours.jsx    →  not an address, just a component the page imports
src/app/visit/map.png      →  not an address either
```

```callout jargon
Keeping a page's helper files in the same folder as the page is called **colocation**. It keeps things that change together close together.
```

Other special names exist too, like `layout.jsx`, `loading.jsx` and `error.jsx`. They change how a route looks, but they aren't pages. The next tickets cover them.

### Syntax: folders that don't become segments

```text
src/app/(shop)/cart/page.jsx      →  /cart
src/app/_components/Header.jsx    →  never an address
```

Two kinds of folder are for organising, not for addresses:

- **A route group**, a folder name in round brackets like `(shop)`, is left out of the address. `(shop)/cart/page.jsx` is just `/cart`. Teams use groups to sort pages into sections.
- **A private folder**, a name starting with `_` like `_components`, is left out of routing completely. Even a `page.jsx` inside it is never an address. It's a safe home for shared components.

### Pages run on the server

By default, every page is a **server component**: Next.js runs it on the server and sends the finished HTML. So a page can't use `useState` or `onClick` on its own. You'll split pages into server and client parts in ticket 5. Pages that only show words, like About and Visit us, need nothing more.

## Worked example

Here's the home page, `src/app/page.jsx`:

```jsx
export default function HomePage() {
  return (
    <>
      <h1>Handmade in Pune</h1>
      <p>Crochet coasters, keychains, toys, bags and booties, made by Meera Aunty and friends.</p>
    </>
  );
}
```

- The file sits directly in `app`, so it's the page for `/`.
- It's the file's default export, so Next.js uses it.
- It returns a fragment, `<>...</>`, holding a heading and a paragraph.

To add `/visit`, make a folder called `visit` and a `page.jsx` inside it. There's no router to update and no list of routes to edit.

You can work out the address of any file the same way Next.js does. Split the path into parts, then keep the folders between `app` and the file name:

```js run
const file = "app/help/delivery/page.jsx";
const parts = file.split("/");
console.log(parts);
console.log(parts.slice(1, -1));
console.log("/" + parts.slice(1, -1).join("/"));
```

## Try it

Try the snippet above with `app/page.jsx`. `slice(1, -1)` is empty, so the address is `/`. Then try `app/(shop)/cart/page.jsx`: the brackets show up as a segment, which isn't what Next.js does. Your `routeFor` in this ticket's code fixes that.

## Recap

- In the app router, each folder inside `app` is one segment of the address.
- A page is the default export of a file called `page.jsx`. Other files in the folder never become addresses.
- A route group like `(shop)` is left out of the address. A private folder like `_components` is left out of routing.
- Pages are server components by default, so plain pages need no hooks.

## Common mistakes

**1. Naming the file after the page.**

```text
src/app/about.jsx
```

Opening `/about` shows the 404 page. Next.js only looks for files called `page`.

Fix: make a folder and a page inside it: `src/app/about/page.jsx`.

**2. A named export instead of a default export.**

```jsx
export function AboutPage() {
  return <h1>Our story</h1>;
}
```

Next.js stops with an error saying the default export of the page is not a React component. It found the file, but nothing was exported as default.

Fix: `export default function AboutPage() { ... }`.

**3. Putting a page in a private folder.**

```text
src/app/_offers/page.jsx
```

The address `/_offers` shows the 404 page. Folders starting with `_` are left out of routing completely.

Fix: rename the folder to `offers`, or keep only shared components in `_` folders.

**4. Expecting brackets in the address.**

```jsx
<a href="/(shop)/cart">Cart</a>
```

The link goes to a 404 page. Route group names never appear in addresses.

Fix: link to `/cart`.
