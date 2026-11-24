## Hook

Meera Aunty prints her price list once and sticks a copy on the stall's board. Regular customers take a photo of it and use the photo for weeks. When a price changes, the board is right, but the photos in everyone's phones are wrong.

Browsers do the same with websites. They keep copies of the files they downloaded, so the next visit is fast. That is wonderful, until you ship a fix and some phones keep using their old copy.

This ticket is about what the build does to your files, and how to name them so phones always get the newest ones.

## Concept

```callout jargon
A **production build** is the version of the site made for real customers: small, fast and finished. With Vite, `npm run build` makes it in `dist/`. **Production** means the live site.
```

From ticket 2 you know the build bundles many files into a few. It does three more things:

1. **Minifies** the code, so there is less to download.
2. **Drops unused code**, so customers only get what the page uses.
3. **Names each file after its content**, so caching is safe.

### Minification

```callout jargon
**Minification** rewrites code to be as short as possible without changing what it does: no spaces, no comments, short names. The result is hard for people to read, but the browser does not mind.
```

```js run
// What you write:
function lineTotal(line) {
  return line.paise * line.qty;
}

// What the build might make of it:
const t=e=>e.paise*e.qty;

console.log(lineTotal({ paise: 12000, qty: 2 }));
console.log(t({paise:12000,qty:2}));
```

Both print the same total. The short version is a third of the size. Over a whole site, that is the difference between a fast page and a slow one on 4G.

### Tree shaking

```callout jargon
**Tree shaking** drops code that nothing imports. If `money.js` exports five functions and the page uses two, only those two reach the bundle.
```

The build follows the imports from `main.js`, as you saw in ticket 2. Code it never reaches is shaken off. This is one more reason to use `import` and `export` everywhere: the build can see exactly what is used.

### Caching

```callout jargon
A **cache** is a store of copies kept to save time. A browser's cache keeps files it downloaded. **Cache-Control** is a header the server sends with each file, saying how long the browser may use its copy without asking again.
```

```text
Cache-Control: max-age=31536000, immutable
Cache-Control: no-cache
```

- `max-age=31536000` means "use your copy for a year (in seconds), without asking". `immutable` adds "this file will never change".
- `no-cache` means "you may keep a copy, but check with me before every use". If nothing changed, the server answers with a tiny "not modified", so it is still fast.

The old dhaaga.in served `app.js` with a week-long `max-age`. Monday's fix changed `app.js`, but kept the name. Phones that had cached the old `app.js` on Sunday kept using it until next Sunday. That is Raju's ticket.

### Content hashing

```callout jargon
A **hash** is a short code worked out from a file's content. The same content always gives the same hash. Change one character, and the hash changes. **Content hashing** puts that code into the file's name.
```

This is why Vite's build output has odd names like `index-C3r9kQe1.js`. Here is the idea with a toy hash:

```js run
function tinyHash(text) {
  let h = 0;
  for (const ch of text) {
    h = (h * 31 + ch.codePointAt(0)) % 1000000007;
  }
  return h.toString(36);
}

console.log("index-" + tinyHash("total += price * line.qty") + ".js");
console.log("index-" + tinyHash("total += price * line.qty") + ".js");
console.log("index-" + tinyHash("total += PRICES[code] * line.qty") + ".js");
```

The same content gives the same name, twice. Changed content gets a new name. Real builds use a stronger hash, but the idea is the same.

Now caching is safe:

- A file in `assets/` never changes under its name. If the content changes, the name changes. So it can be cached for a year.
- `index.html` keeps its name, because it is the address people visit. It is small, so it gets `no-cache`. It lists the newest file names.

```animate
title: A fix reaches a phone that cached the old site
kind: scene
nodes:
  - { id: phone, label: Customer's phone, emoji: "📱", value: empty cache, row: 0, col: 0 }
  - { id: server, label: dhaaga.in, emoji: "🖥️", value: "index.html → index-a1.js", row: 0, col: 2 }
  - { id: cache, label: Phone's cache, emoji: "🗃️", row: 1, col: 0 }
steps:
  - say: Sunday. The phone downloads index.html, which names index-a1.js. It downloads that too.
    send: { from: server, to: phone, label: "index.html + index-a1.js", back: true }
  - say: index-a1.js came with max-age of a year, so the phone keeps it. index.html came with no-cache.
    show: [cache]
    send: { from: phone, to: cache, label: keep index-a1.js }
    set: { cache: "index-a1.js (1 year)" }
  - say: Monday. Kavya ships the fix. The new code has a new hash, so the build names it index-b7.js.
    set: { server: "index.html → index-b7.js" }
  - say: Tuesday. The phone must re-check index.html. It gets the new one, which names index-b7.js.
    send: { from: phone, to: server, label: "index.html changed?" }
  - say: index-b7.js is not in the cache, so the phone downloads it. The fix arrives. No one clears anything.
    send: { from: server, to: phone, label: index-b7.js, back: true }
    set: { cache: "index-a1.js, index-b7.js", phone: "cart works" }
```

### Source maps

Minified code makes the debugger from ticket 6 hard to use. An error on the live site says `at t (index-C3r9kQe1.js:1:2783)`, which tells you nothing.

```callout jargon
A **source map** is a file, ending in `.map`, that links each spot in the minified code back to the line you wrote. DevTools reads it and shows your own files and line numbers.
```

```js
import { defineConfig } from "vite";

export default defineConfig({
  build: { sourcemap: true },
});
```

This goes in `vite.config.js`. Now the build writes `index-C3r9kQe1.js.map` next to each bundle.

```callout warning
A public source map lets anyone read your original code. For a shop's website that is usually fine, because the code runs in their browser anyway. It is never a place for secrets: those stay on the server, as ticket 9 said.
```

### Bundle size

Every kilobyte is time on a customer's phone. The build prints each file's size, and its gzip size, which is what really travels:

```text
dist/index.html                  0.46 kB │ gzip: 0.30 kB
dist/assets/index-BQ2f7xLp.css   1.20 kB │ gzip: 0.62 kB
dist/assets/index-C3r9kQe1.js   84.10 kB │ gzip: 31.20 kB
```

If one file grows a lot, look for a big package you only use a little. Some parts of the site, like a Diwali gift guide, are rarely opened. A **dynamic import** loads them only when needed:

```js
const button = document.querySelector("#gift-guide");
button.addEventListener("click", async () => {
  const { showGiftGuide } = await import("./giftGuide.js");
  showGiftGuide();
});
```

`import(...)` with brackets returns a promise, as in web-06. The build puts `giftGuide.js` in its own small file, which is downloaded only on the first click.

## Worked example

Raju's ticket: some phones still run Monday's broken code. You trace it and fix it for good.

**Step 1: find the cause.** In DevTools, open the Network tab on the live site. `app.js` shows "(disk cache)" and its response headers say:

```text
Cache-Control: max-age=604800
```

604800 seconds is a week. Phones that visited before Monday keep the old `app.js` until then.

**Step 2: build with Vite.** The site already moved to Vite in ticket 2. `npm run build` gives hashed names:

```text
dist/index.html                  0.46 kB │ gzip: 0.30 kB
dist/assets/index-BQ2f7xLp.css   1.20 kB │ gzip: 0.62 kB
dist/assets/index-D8kLm2Qa.js    3.12 kB │ gzip: 1.42 kB
```

**Step 3: set the headers on the server.** How you write this depends on the host, but the rules are always these two:

```text
/index.html    Cache-Control: no-cache
/assets/*      Cache-Control: max-age=31536000, immutable
```

**Step 4: check it.** Run `npm run preview` and look at the Network tab, or check the live site after the deploy. Change one line, build again: the `.js` file gets a new name, and `index.html` points at it.

From now on, a phone always checks `index.html`, finds the newest names, and downloads only files it has not seen.

## Try it

Press Run on the toy hash above.

1. Change one letter in the third string. Does its name change? Does the first line's name change?
2. In the Vite project from ticket 2, run `npm run build` and note the `.js` file's name in `dist/assets/`.
3. Change one word of text in `src/main.js` and build again. Which file names changed? Did the `.css` file's name change?
4. Add `build: { sourcemap: true }` to `vite.config.js` and build again. Find the new `.map` file.

## Recap

- `npm run build` makes the production build: bundled, minified and tree-shaken, in `dist/`.
- Minification makes code short without changing what it does. Tree shaking drops code nothing imports.
- Browsers cache files. `Cache-Control` says for how long: `no-cache` re-checks every time, `max-age` trusts the copy.
- Content hashing puts a hash of the content in each file name. Cache `assets/` for a year, and re-check `index.html` every time.
- Source maps link minified code back to your lines. Watch the bundle size, and load rare parts with dynamic `import()`.

## Common mistakes

**1. Long caching on index.html.**

```text
/index.html    Cache-Control: max-age=31536000
```

What you see: after a deploy, phones keep the old `index.html`. It names the old hashed files, so customers run the old code for a year.

Fix: `index.html` gets `no-cache`. Only the hashed files in `assets/` get the long `max-age`.

**2. Fixed file names with long caching.**

```html
<script type="module" src="/app.js"></script>
```

What you see: Raju's ticket. The name never changes, so phones with a cached `app.js` never fetch the fix.

Fix: let Vite build the site, so every file in `assets/` carries a content hash.

**3. Telling customers to clear their cache.**

What you see: it works for the one customer who tries it. Everyone else still sees ₹NaN.

Fix: you cannot reach every phone. Fix the names and headers, so the next visit gets the new code by itself.

**4. Testing only with the dev server.**

What you see: everything works with `npm run dev`. The live build breaks, for example on a file the build handles differently.

Fix: before you ship, run `npm run build` and `npm run preview`, and click through the real built site.
