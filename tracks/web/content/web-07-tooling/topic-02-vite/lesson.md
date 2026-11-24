## Hook

A tailor's shop has two rooms. In the back room, the tailor pins, tries and re-pins a kurta, again and again, as fast as possible. The front room gets only the finished, pressed kurta, folded and ready for the customer.

Building a website needs the same two rooms. While you work, you want every change to show up at once. When you ship, you want small, finished files that load fast on a phone on slow 4G.

**Vite** (French for "fast", said like "veet") gives you both rooms with two commands.

## Concept

```callout jargon
A **build tool** turns the files you write into the files a browser should download. **Vite** is a build tool with a fast **dev server** for working, and a **build** command for shipping.
```

Without a tool, every `import` in your code is a separate file the browser downloads. A browser also cannot `import dayjs from "dayjs"`, because it has no idea where npm put `dayjs`. Vite handles both.

### Syntax: starting a Vite project

```text
npm create vite@latest <folder> -- --template vanilla
```

This downloads Vite's starter and makes a new folder. `vanilla` means plain JavaScript, with no framework. (In module react-08, you will pick `react` instead.) Then:

```text
cd dhaaga-web
npm install
npm run dev
```

The project looks like this:

```text
dhaaga-web/
  index.html        the page; Vite starts here
  package.json      scripts: dev, build, preview
  public/           files copied as they are, like favicon.svg
  src/
    main.js         the JavaScript the page loads
    style.css
```

`index.html` sits in the top folder, not in `src/`. It loads your code with one line:

```html
<script type="module" src="/src/main.js"></script>
```

### Syntax: the dev server

```text
npm run dev
```

The `dev` script runs `vite`. The real output looks like this:

```text
  VITE v7.1.3  ready in 312 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
  ➜  press h + enter to show help
```

```callout jargon
A **dev server** is a small web server on your own laptop that serves your project while you build it. **localhost** means "this computer", and **5173** is the **port**, the door number the server listens on.
```

Open `http://localhost:5173/` in your browser. Vite serves each file when the browser asks for it. When `main.js` says `import dayjs from "dayjs"`, Vite rewrites that line so the browser finds the file inside `node_modules`.

Press `Ctrl` + `C` in the terminal to stop the server.

### Hot reload

Save a file, and the page in the browser updates by itself. You do not press refresh.

```callout jargon
**Hot module replacement** (HMR) swaps only the file you changed into the running page. A CSS change appears without reloading at all, so the page keeps its state, like what is in the cart.
```

For CSS, Vite swaps the new styles in place. For a JavaScript change, Vite usually reloads the page, unless the code says how to swap itself. React's Vite plugin does that for components, as you will see later.

```animate
title: Sana changes the Marigold
kind: scene
nodes:
  - { id: editor, label: VS Code, emoji: "📝" }
  - { id: vite, label: Vite dev server, emoji: "⚡", value: watching files }
  - { id: browser, label: Browser tab, emoji: "🌐", value: "2 items in cart" }
steps:
  - say: The browser asks the dev server for the page. Vite serves index.html, main.js and style.css one by one.
    send: { from: browser, to: vite, label: "GET /src/main.js" }
  - say: "Sana changes the price tag colour to #F2A81D and saves style.css."
    send: { from: editor, to: vite, label: save style.css }
    set: { vite: style.css changed }
  - say: Vite is watching the files. It tells the open tab that one file changed.
    send: { from: vite, to: browser, label: "update: style.css" }
  - say: The tab swaps in the new CSS. No reload, so the cart still has its 2 items.
    set: { browser: "new colour, 2 items in cart" }
    focus: [browser]
```

### Importing CSS and images

With Vite, JavaScript can import more than JavaScript:

```js
import "./style.css";
import goluUrl from "./golu.png";

const img = document.querySelector("#mascot");
img.src = goluUrl;
```

- `import "./style.css"` adds the stylesheet to the page.
- `import goluUrl from "./golu.png"` gives you the image's address as text. In the build, the address points at the finished copy.

### The public folder

Some files must keep their exact name, like `favicon.svg` or `robots.txt`. Put them in `public/`.

Vite copies everything in `public/` into the finished site as it is. You refer to them from the top of the site, with a leading `/`:

```html
<link rel="icon" href="/favicon.svg" />
```

Use `src/` and `import` for files your code uses. Use `public/` only for files that need a fixed name.

### Syntax: the build

```text
npm run build
```

The `build` script runs `vite build`. It makes the front room's finished copy. The real output looks like this:

```text
vite v7.1.3 building for production...
✓ 6 modules transformed.
dist/index.html                  0.46 kB │ gzip: 0.30 kB
dist/assets/index-BQ2f7xLp.css   1.20 kB │ gzip: 0.62 kB
dist/assets/index-C3r9kQe1.js    3.10 kB │ gzip: 1.41 kB
✓ built in 214ms
```

```callout jargon
**Bundling** joins many JavaScript files, and the packages they import, into a few files. A **bundle** is one of those joined files. **dist** (short for distribution) is the folder the build writes them into.
```

Six modules went in. One JavaScript file and one CSS file came out. A phone on slow 4G now downloads two files instead of twelve.

Each line shows a file's size, and its size after **gzip**, the squeezing a server does before sending. The odd letters in `index-C3r9kQe1.js` matter a lot for caching, as you will see in ticket 10.

```callout tip
`dist/` is made by the build, so never edit it by hand and never commit it. Change `src/`, then build again.
```

### Syntax: preview the build

```text
npm run preview
```

`vite preview` serves `dist/` on `http://localhost:4173/`, the way a real server would. The dev server takes shortcuts, so check the built site here before you ship it.

| Command | Serves | Use it to |
|---|---|---|
| `npm run dev` | your source files, live | work, with hot reload |
| `npm run build` | nothing; writes `dist/` | make the finished site |
| `npm run preview` | `dist/` | check the finished site |

## Worked example

Sana's ticket has two parts: instant updates while she works, and fewer files for customers. You move the old site into a Vite project.

**Step 1: make the project and copy the code in.**

```text
npm create vite@latest dhaaga-web -- --template vanilla
cd dhaaga-web
npm install
npm install dayjs
```

You copy the old `cart.js`, `catalogue.js` and `style.css` into `src/`.

**Step 2: one entry file.** The old `index.html` had twelve `<script>` tags. Now it has one, and `main.js` imports the rest:

```js
import "./style.css";
import { renderCatalogue } from "./catalogue.js";
import { loadCart } from "./cart.js";

renderCatalogue(document.querySelector("#catalogue"));
loadCart();
```

Each file imports what it needs. Vite follows the imports from `main.js` to find every file.

**Step 3: work.** `npm run dev`, open `http://localhost:5173/`, put two coaster sets in the cart. Sana edits the price tag colour. The page changes, and the cart keeps both sets.

**Step 4: build and check.**

```text
npm run build
npm run preview
```

The build writes `dist/`. On `http://localhost:4173/`, the Network tab in DevTools shows one `.js` file and one `.css` file instead of twelve scripts.

**Step 5: ignore the output.** `dist/` is already in `.gitignore` from the last ticket. The server builds its own copy from the source.

## Try it

1. Run `npm create vite@latest vite-try -- --template vanilla`, then `cd vite-try`, `npm install` and `npm run dev`.
2. Open the page. Change the heading text in `src/main.js` and save. Watch the browser.
3. Change a colour in `src/style.css` and save. Did the page reload, or just change?
4. Stop the server with `Ctrl` + `C`. Run `npm run build` and look inside `dist/assets/`.
5. Run `npm run preview` and open `http://localhost:4173/`.

## Recap

- Vite is a build tool with two jobs: a dev server for working, and a build for shipping.
- `npm run dev` serves your project on `http://localhost:5173/`. Saved changes appear by themselves, and CSS swaps in without a reload.
- `index.html` sits in the top folder and loads `/src/main.js`. JavaScript can import CSS, images and npm packages.
- `npm run build` bundles everything into a few small files in `dist/`. `npm run preview` serves `dist/` to check it.
- Files that need a fixed name go in `public/`. Never edit or commit `dist/`.

## Common mistakes

**1. Opening index.html straight from the disk.**

```text
file:///Users/you/dhaaga-web/index.html
```

What you see: a blank page. The Console says the module script was blocked, because the browser will not load modules from `file://` addresses. Even if it did, `import dayjs from "dayjs"` would fail.

Fix: always start the dev server with `npm run dev`, and open the `http://localhost:5173/` address it prints.

**2. Uploading the source folder instead of dist.**

What you see: on the live site, `main.js` fails with `Failed to resolve module specifier "dayjs"`. The server has your raw source, and no Vite to rewrite the imports.

Fix: run `npm run build`, and put the contents of `dist/` on the server.

**3. Editing files inside dist.**

You fix a typo straight in `dist/assets/index-C3r9kQe1.js`. It works, until the next build overwrites the whole folder and the typo is back.

Fix: change the file in `src/`, then build again.

**4. Putting a file your code imports into public.**

```js
import "/public/style.css";
```

What you see: Vite refuses. Its message says files in the public directory cannot be imported from JavaScript, and tells you to move the file into `src/`.

Fix: keep code and styles in `src/` and import them with `./style.css`. Keep `public/` for fixed-name files like `favicon.svg`.
