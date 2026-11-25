## Hook

On a Sunday, Raju doesn't carry the whole Aundh shop to the FC Road stall. He brings the coasters, keychains and Golus that sell every week. The gift-wrap kit stays in a box at the back, and he fetches it only when someone asks for a gift.

Your SPA currently carries the whole shop to every visitor. This ticket leaves the big, rarely used parts in the box at the back.

## Concept

### One big download

```callout jargon
A **bundle** is the JavaScript file a build tool like Vite makes by joining your modules together. A customer's browser must download it before the app can start.
```

When `routes.js` says `import checkoutView from "./checkout.js"`, the checkout becomes part of the bundle. Every visitor downloads it, though most of them never check out. On 3G, every extra 100 KB is seconds of waiting.

### Syntax: dynamic import()

```js
import("./checkout.js").then((module) => {
  const checkoutView = module.default;
});
```

Inside an `async` function, you can write `const module = await import("./checkout.js");` instead.

```callout jargon
A **dynamic import** is `import(path)` used like a function, anywhere in your code. It starts loading the module at that moment and returns a promise for it.
```

- The static `import ... from` at the top of a file loads before any code runs. `import()` loads only when that line runs.
- The promise gives a **module object**. Its named exports are properties, such as `module.catalogueView`. The default export is `module.default`.
- It can fail, like `fetch`, so the promise can reject.

### Code splitting

```callout jargon
**Code splitting** means the build tool cuts your code into several files, called **chunks**. Each `import()` it finds becomes the start of a separate chunk, downloaded only when needed.
```

You don't make the chunks yourself. Write `import("./checkout.js")`, and Vite puts the checkout and everything only it uses into its own file.

```animate
title: The checkout arrives only when it is opened
kind: scene
nodes:
  - { id: phone, label: Customer's phone, emoji: "📱", value: "nothing yet" }
  - { id: main, label: main.js, emoji: "📦", value: "180 KB" }
  - { id: chunk, label: checkout chunk, emoji: "🧾", value: "220 KB" }
steps:
  - say: Mrs. Deshpande opens dhaaga.in. Her phone downloads main.js, with the shell, the router and the catalogue.
    send: { from: main, to: phone, label: "180 KB" }
    set: { phone: "catalogue ready" }
  - say: The checkout chunk stays on the server. She browses for ten minutes without ever needing it.
    focus: [chunk]
  - say: She taps Checkout. The router runs import("./checkout.js") and shows a loading message.
    set: { phone: "Loading this page..." }
  - say: Only now does the checkout chunk travel to her phone.
    send: { from: chunk, to: phone, label: "220 KB" }
    set: { phone: "checkout ready" }
  - say: She goes back to the catalogue and opens Checkout again. The module is already loaded, so it appears at once.
    set: { phone: "checkout ready, again" }
```

### Lazy routes

A route can carry a function that loads its view, instead of the view itself:

```js
const routes = [
  { path: "/items", view: catalogueView },
  { path: "/checkout", load: () => import("./checkout.js") },
];
```

The arrow function matters. `() => import(...)` waits until the router calls it. Without the arrow, `import(...)` would run while the routes are being written, at startup, and nothing would be saved.

```callout jargon
**Lazy loading** means waiting to load something until the moment it is needed, instead of loading it up front.
```

### Download once

The browser remembers modules it has loaded, but your code still waits for a promise each time. Keep the promise, and hand the same one to every caller:

```animate
title: Two taps, one download
kind: code
language: js
code: |
  let downloads = 0;
  let promise = null;
  function load() {
    if (promise === null) {
      downloads += 1;
      promise = Promise.resolve("checkout code");
    }
    return promise;
  }
  load();
  load();
  console.log(downloads);
steps:
  - line: 1
    vars: { downloads: 0 }
    say: A counter, to see how many downloads start.
  - line: 2
    vars: { promise: "null" }
    say: Nothing has been asked for yet.
  - line: 10
    say: The customer taps Checkout.
  - line: 4
    say: There is no promise yet, so this is the first time.
  - line: 5
    vars: { downloads: 1 }
    say: The download starts.
  - line: 6
    vars: { promise: "Promise" }
    say: Its promise is kept for next time.
  - line: 11
    say: An impatient second tap, a moment later.
  - line: 8
    say: The promise already exists, so the same one is returned. No second download.
  - line: 12
    out: "1"
    say: One download, shared by both taps.
```

### Loading and failing

Downloading code takes time and can fail, just like fetching data:

- Show a short loading message in the outlet while the chunk arrives.
- If it fails (the customer went offline, or a new deploy replaced the old chunk files), show a friendly message instead of a blank outlet.
- Forget the failed promise, so tapping again really tries again. A promise that failed stays failed forever.

### What is worth splitting?

- **Screens few people open:** checkout, order history, the gift-card editor.
- **Big tools used in one place:** a chart library for the sales page, a PDF maker for invoices.
- **Not tiny things.** Every chunk is an extra request. Splitting a 2 KB file costs more than it saves.

```callout tip
You can start a download before the tap. When the customer adds something to the cart, call the checkout's `load()` quietly in the background. By the time they tap Checkout, it is already there.
```

## Worked example

The gift-message editor is big and rarely used. It loads only when a customer taps **Add a gift message**:

```js
let editorPromise = null;

function loadEditor() {
  if (editorPromise === null) {
    editorPromise = import("./giftEditor.js").catch((error) => {
      editorPromise = null;
      throw error;
    });
  }
  return editorPromise;
}

document.querySelector("#gift").addEventListener("click", async () => {
  const box = document.querySelector("#gift-box");
  box.textContent = "Opening the editor...";
  try {
    const { openGiftEditor } = await loadEditor();
    openGiftEditor(box);
  } catch {
    box.textContent = "Couldn't open the editor. Please try again.";
  }
});
```

1. `loadEditor` keeps one promise, so repeated taps share one download.
2. Its `catch` forgets a failed download and passes the error on.
3. The click handler shows a message at once, then waits.
4. `{ openGiftEditor }` takes a **named** export from the module object. A default export would be `module.default`.

## Try it

Node can load modules with `import()` too. Run this, then load `"node:os"` the same way and print `os.EOL.length`.

```js run
async function main() {
  console.log("before");
  const path = await import("node:path");
  console.log(path.basename("/images/golu.jpg"));
}

main();
```

## Recap

- A static `import` puts code in the first download for everyone. `import(path)` loads a module only when that line runs, and returns a promise.
- Build tools turn each `import()` into its own chunk: that is code splitting.
- A lazy route keeps `() => import("./checkout.js")`. The arrow makes it wait until the route is opened.
- Keep one promise per module so it downloads once. On failure, forget it so a retry works.
- Show a loading message while code downloads, and a friendly message if it fails.

## Common mistakes

**1. Calling import() straight away.**

```js
const routes = [{ path: "/checkout", load: import("./checkout.js") }];
```

The download starts the moment `routes.js` runs, for every visitor, so nothing is saved. Fix: wrap it in an arrow, `load: () => import("./checkout.js")`.

**2. Leaving the static import in.**

```js
import checkoutView from "./checkout.js";
const load = () => import("./checkout.js");
```

As long as one static import of `checkout.js` remains, it stays in the main bundle. Fix: remove every static import of the module you want to split off.

**3. Using the module object as the view.**

```js
async function show(outlet) {
  const checkoutView = await import("./checkout.js");
  outlet.replaceChildren(checkoutView());
}
```

`import()` gives a module object, not the function, so this throws `TypeError: checkoutView is not a function`. Fix: use `module.default` for a default export, or destructure a named one.

**4. Keeping a failed download forever.**

```js
let promise = null;
function load() {
  promise = promise ?? import("./checkout.js");
  return promise;
}
```

One dropped connection, and every later tap gets the same failed promise until the page reloads. Fix: in `.catch`, set `promise` back to `null`, then throw the error on.
