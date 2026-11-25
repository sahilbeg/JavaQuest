## Hook

Imagine ordering a bookshelf. One shop sends a flat box of planks and a booklet. You build it yourself before you can put a single book on it. Another shop delivers it built. You can use it the moment it arrives, and you can still move the shelves around later.

Dhaaga's React store is the flat box. Every phone gets an empty page plus a big bundle of JavaScript, and it has to build the page itself. On Raju's old phone, that takes three seconds of white screen. WhatsApp's link preview never builds it at all. Next.js delivers the page built.

## Concept

### What the React store sends today

When a customer opens dhaaga.in today, the server sends an HTML file like this:

```html
<!doctype html>
<html lang="en">
  <head>
    <title>dhaaga.in</title>
    <script type="module" src="/assets/index-4f2a.js"></script>
  </head>
  <body>
    <div id="root"></div>
  </body>
</html>
```

The `<div id="root">` is empty. Nothing shows until the browser downloads the JavaScript, runs React, fetches the catalogue, and draws the page. That's the white screen.

```callout jargon
**Client-side rendering** means the browser (the **client**) builds the page with JavaScript after it arrives. A React app made with Vite works this way.
```

### Who can't wait for JavaScript

Some visitors never run your JavaScript at all:

- **Link previews.** WhatsApp, Instagram and others send a small program to fetch the link. It reads the HTML and looks for a title and a photo. It doesn't run React, so it sees an empty page.
- **Search engines.** Google can run JavaScript, but later and less reliably. A page that already contains its words is found sooner.
- **Slow phones and slow networks.** They can run JavaScript, but every extra second of white screen loses customers.

### Rendering on the server

```callout jargon
**Server rendering** means the server runs your components and sends HTML that already contains the page. When this happens once, at build time, it's called **prerendering** (or static rendering). When it happens for each request, it's **dynamic rendering**.
```

With Next.js, the HTML for Golu's page arrives already filled in:

```html
<body>
  <main>
    <h1>Golu the elephant</h1>
    <p>₹450.00</p>
    <button>Add to cart</button>
  </main>
</body>
```

The phone can show it straight away. The link preview finds the name. The search engine finds the words.

### Watch the two ways side by side

```animate
title: Golu's page, two ways
kind: scene
nodes:
  - { id: phone, label: Raju's phone, emoji: "📱", value: blank }
  - { id: vite, label: React app server, emoji: "📦" }
  - { id: next, label: Next.js server, emoji: "🖥️" }
steps:
  - say: "First, the React app. The phone asks for Golu's page. The part after the # never reaches the server, so it just asks for /."
    send: { from: phone, to: vite, label: "GET /" }
  - say: The server sends an empty page and a big bundle of JavaScript. The screen stays white.
    send: { from: vite, to: phone, label: "empty div + 300 kB JS", back: true }
    set: { phone: "white screen" }
  - say: The phone runs React, which asks for the catalogue. Only then can it draw Golu.
    set: { phone: "Golu, after 3 s" }
  - say: Now Next.js. The phone asks for the same page.
    send: { from: phone, to: next, label: "GET /items/DC-1042" }
    set: { phone: blank }
  - say: The server runs the React components itself, reads the catalogue, and builds the HTML.
    focus: [next]
  - say: The HTML arrives with Golu's name and price in it. The phone shows it at once.
    send: { from: next, to: phone, label: "HTML with Golu", back: true }
    set: { phone: "Golu, at once" }
```

### Hydration: making the page work

The HTML from the server looks right, but its buttons do nothing yet. HTML can't hold `onClick` functions. So Next.js also sends the JavaScript for the parts that need it.

```callout jargon
**Hydration** is React attaching itself to HTML that the server already built. It reuses the elements that are there and adds the event handlers, so buttons start working. It doesn't draw the page again.
```

So the customer sees the page first and can tap it a moment later. That's much better than seeing nothing.

### A framework around React

```callout jargon
A **library** is code you call when you need it. React is a library: it draws the UI. A **framework** is a structure your code fits into. It decides where files go and calls your code at the right time. Next.js is a framework built around React.
```

You still write React components, with props, state and hooks. Next.js adds the parts a real website needs:

- **Routing from folders:** a folder becomes an address, with no router to write.
- **Rendering on the server**, for every page, by default.
- **Server code in the same project:** your own API and form handling, in later tickets.
- **Metadata:** titles and link previews for every page.
- **Optimised images and fonts.**
- **A build** that makes everything small and fast for production.

### The app router: folders are addresses

```callout jargon
The **app router** is how Next.js finds pages: by folders inside a folder called `app`. Each folder is one part of the address, and a file called `page.jsx` is the page for that address. Older Next.js projects used a **pages router**, with a `pages` folder. New projects use the app router.
```

```text
src/app/
  page.jsx              →  dhaaga.in/
  about/
    page.jsx            →  dhaaga.in/about
  items/
    page.jsx            →  dhaaga.in/items
```

Compare that with the router you built by hand in react-09. There, `matchRoute` turned an address into a page. Here, the folders do that job.

### Syntax: starting a project

```bash
npx create-next-app@latest dhaaga-store
```

This makes a new folder with Next.js, React and a starter page. It asks a few questions first, like whether you want TypeScript, Tailwind and a `src/` folder. Then three commands do everything:

```bash
npm run dev     # next dev: a development server at http://localhost:3000
npm run build   # next build: builds the site for production
npm start       # next start: runs the production build
```

The development server rebuilds as you save, like Vite's did in web-07. Never put `next dev` in front of real customers: it's slower and shows developer error screens.

### The project's folders

```text
dhaaga-store/
  package.json        the scripts and packages (next, react, react-dom)
  next.config.js      Next.js settings, often empty at first
  public/             files served as they are, like /logo.png
  src/
    app/              the routes: pages, layouts, and more
    lib/              your own code, like the catalogue
```

Only `app/` is special. Next.js reads it to find routes. Everything else is ordinary JavaScript that you import, as in any project.

## Worked example

Follow one request from start to finish. A customer taps a WhatsApp link to `dhaaga.in/items/DC-1042`.

```jsx
// src/app/items/[id]/page.jsx
export default async function ItemPage({ params }) {
  const { id } = await params;
  const item = await getItem(id);
  return (
    <main>
      <h1>{item.name}</h1>
      <p>{rupees(item.paise)}</p>
      <AddToCartButton itemId={item.id} />
    </main>
  );
}
```

1. Next.js matches the address to a folder. `items/[id]` means "any id here", so `id` is `DC-1042`. You'll build this in ticket 6.
2. It runs `ItemPage` **on the server**. The component can be `async` and read the catalogue directly.
3. The server sends HTML with "Golu the elephant" and "₹450.00" already inside.
4. The phone shows the page straight away. WhatsApp's preview reads the title from the HTML too.
5. The JavaScript for `AddToCartButton` arrives, and hydration makes the button work.

Most of this page never needs JavaScript in the browser. Only the button does. Ticket 5 shows how Next.js decides which is which.

## Try it

This ticket's check is a quiz. If you'd like to see the difference yourself, on your own computer:

1. Run `npx create-next-app@latest try-next` and accept the defaults. Then `cd try-next` and `npm run dev`.
2. Open http://localhost:3000. Right-click and choose **View Page Source**. Search for the heading's words: they're in the HTML.
3. Do the same with any Vite React app from web-07. The source holds only an empty `<div id="root">`.
4. Edit the heading in `app/page` and save. The browser updates without a reload.

## Recap

- A plain React app renders in the browser, so the first HTML is empty until JavaScript runs.
- Next.js renders React on the server, so pages arrive filled in for phones, link previews and search engines.
- Hydration then attaches React to that HTML, so buttons start working without drawing the page again.
- Next.js is a framework around React. In the app router, folders inside `app/` are addresses and `page.jsx` is the page.
- `npm run dev` is for building, `npm run build` and `npm start` are for production.

## Common mistakes

**1. Thinking Next.js replaces React.**

```jsx
// "Now we use Next.js, so no more useState?"
```

Next.js runs React. Components, props, state and hooks all work as before. Next.js adds routing, server rendering and a build around them.

Fix: keep writing React. Learn where Next.js expects your files.

**2. Using browser-only things in server code.**

```jsx
export default function Page() {
  const saved = window.localStorage.getItem("cart");
  return <p>{saved}</p>;
}
```

The page fails with `ReferenceError: window is not defined`. The component ran on the server, and servers have no `window`, `document` or `localStorage`.

Fix: browser-only code goes in a client component, inside an effect or an event handler. Ticket 5 shows how.

**3. Putting the development server in front of customers.**

```bash
npm run dev   # on the real website
```

It works, but it's slow, rebuilds on every request and shows developer error screens to customers.

Fix: `npm run build`, then `npm start` (or a host that does both for you).

**4. Looking for the page in index.html.**

```text
public/index.html   (it isn't there)
```

A Next.js project has no `index.html` to edit. Next.js writes the HTML from your layouts and pages.

Fix: the home page is `app/page.jsx`. The `<html>` and `<body>` tags live in `app/layout.jsx`, which ticket 3 covers.
