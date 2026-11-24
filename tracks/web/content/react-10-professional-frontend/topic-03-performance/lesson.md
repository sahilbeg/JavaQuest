## Hook

At a busy haat, a customer who waits too long at a stall walks to the next one. Websites are the same, only faster. Studies by big online shops keep finding the same thing: every extra second of loading loses customers.

Most of Dhaaga's customers shop on mid-range Android phones, often on patchy mobile data. A page that feels instant on your laptop's Wi-Fi can take twenty seconds on their phone. This ticket is about what makes a page slow, how to measure it, and the few changes that fix most of it.

## Concept

### What "fast" means: Core Web Vitals

"Fast" is too vague to fix. Google measures three things on real customers' phones, and calls them the **Core Web Vitals**.

```callout jargon
**LCP** (Largest Contentful Paint) is how long until the biggest thing on the first screen appears, usually the main photo. **CLS** (Cumulative Layout Shift) measures how much the page jumps around while it loads. **INP** (Interaction to Next Paint) is how long the page takes to react visibly after a tap or a key press.
```

| Vital | Good | What it feels like when it's bad |
|---|---|---|
| LCP | 2.5 seconds or less | "Is this site even working?" |
| CLS | 0.1 or less | You tap Golu, the page jumps, and you hit the tote bag |
| INP | 200 milliseconds or less | You tap Add to cart and nothing happens for a moment |

Google also uses these numbers when it ranks search results. A slow store is harder to find, as well as harder to use.

### Where the time goes

```animate
title: A phone opens the catalogue on slow data
kind: scene
nodes:
  - { id: phone, label: Customer's phone, emoji: "📱", value: blank, row: 1, col: 0 }
  - { id: server, label: dhaaga.in, emoji: "🖥️", row: 1, col: 3 }
  - { id: js, label: JavaScript, emoji: "📦", value: "–", row: 0, col: 5 }
  - { id: photos, label: 40 photos, emoji: "🖼️", value: "–", row: 2, col: 5 }
steps:
  - say: The phone asks for the page. The HTML is small and arrives quickly.
    send: [{ from: phone, to: server, label: "GET /" }, { from: server, to: phone, label: "index.html", back: true }]
    set: { phone: "HTML" }
  - say: The HTML asks for the JavaScript bundle. React can't draw anything until it arrives and runs.
    send: { from: server, to: phone, label: "main.js", back: true }
    set: { js: "300 KB", phone: "React runs" }
    show: [js]
  - say: React draws the grid with forty img tags. The phone starts downloading all forty photos at once.
    show: [photos]
    set: { photos: "36 MB" }
    send: { from: server, to: phone, label: "40 photos", back: true }
  - say: The four photos on screen share the slow connection with thirty-six the customer can't even see. LCP is terrible.
    focus: [photos]
    set: { phone: "waiting..." }
  - say: With lazy loading and smaller files, only four small photos load first. The rest wait until the customer scrolls near them.
    set: { photos: "4 × 60 KB", phone: "ready" }
    focus: [phone]
```

There are two big weights here: **images** and **JavaScript**. Fix those, and most pages become fast.

### Images are most of the weight

A photo straight from a phone camera is about 4000 pixels wide and several megabytes. The catalogue shows it about 200 pixels wide. The phone downloads twenty times more pixels than it can show, then throws most of them away.

So, before any code:

- **Resize.** Save each photo at the sizes you actually show, such as 400 and 800 pixels wide.
- **Compress.** Use a modern format like WebP or AVIF. They are often half the size of a JPEG of the same quality.
- **Load less at first.** Only the photos on the first screen matter for LCP.

### Syntax: width and height

```jsx
<img src="/img/DC-1042-400.webp" alt="Golu the elephant" width={400} height={400} />
```

Without `width` and `height`, the browser doesn't know how tall a photo is until it arrives. It draws the text first, then pushes it down when the photo lands. That jump is layout shift, the "S" in CLS.

With both attributes, the browser reserves the right box from the start. In CSS, `max-width: 100%; height: auto;` lets the photo shrink on a small screen while keeping the same shape. The numbers set the shape, the ratio of width to height, not the final size.

### Syntax: loading="lazy"

```jsx
<img src="/img/DC-1301-400.webp" alt="Rani pink tote bag" width={400} height={400} loading="lazy" />
```

```callout jargon
**Lazy loading** means not downloading something until it's about to be needed. With `loading="lazy"`, the browser waits to fetch a photo until the customer scrolls near it.
```

Every modern browser supports `loading="lazy"` on images and iframes, with no JavaScript. The default is `loading="eager"`: fetch it now.

`decoding="async"` is a smaller hint. It lets the browser unpack the photo without holding up the rest of the page.

### Don't lazy-load the first screen

```callout jargon
**Above the fold** means the part of the page a customer sees before scrolling. The phrase comes from newspapers: the top half of the front page, above where it's folded.
```

Lazy loading the photos above the fold makes things **slower**. The browser waits until it has worked out the layout before it even asks for them, and the biggest of them is usually your LCP.

So split the list:

- the first few photos, on the first screen: `loading="eager"`;
- everything below: `loading="lazy"`.

For the single most important photo, such as the big Golu photo on his own page, you can also add `fetchPriority="high"`. It tells the browser to fetch that photo before other images.

### Syntax: srcset and sizes

```jsx
<img
  src="/img/DC-1042-400.webp"
  srcSet="/img/DC-1042-400.webp 400w, /img/DC-1042-800.webp 800w"
  sizes="(min-width: 600px) 400px, 100vw"
  alt="Golu the elephant" width={400} height={400}
/>
```

`srcSet` lists every file you have, each with its real width in pixels and a `w`. `sizes` tells the browser how wide the photo will show: 400 CSS pixels on screens at least 600 pixels wide, otherwise the full screen width (`100vw`).

The browser then picks the smallest file that looks sharp. A sharp phone screen, which packs two real pixels into each CSS pixel, picks the 800 file. An older phone picks the 400 file. `src` is the fallback for very old browsers.

### Smaller JavaScript: code splitting

The second weight is JavaScript. React can't draw anything until the bundle has downloaded and run, so a big bundle delays everything.

```callout jargon
A **bundle** is the JavaScript file a build tool like Vite makes by joining all your modules together. **Code splitting** breaks it into pieces, so a page downloads only the pieces it needs right now.
```

Good candidates to split off: parts most customers never open, like the size guide, the reviews or the admin pages. In [Production builds and caching](/t/web-07-tooling/topic-10-production-builds) you saw how Vite names its files. A dynamic `import()` makes Vite put that module in a separate file.

### Syntax: React.lazy and Suspense

```jsx
const SizeGuide = lazy(() => import("./SizeGuide.jsx"));

<Suspense fallback={<p>Loading the size guide…</p>}>
  <SizeGuide />
</Suspense>
```

`lazy` takes a function that imports the component, and React calls it the first time the component is drawn. `Suspense` shows its `fallback` while that file downloads. The lazy file must `export default` its component.

Split by what customers do, not by every component. Each extra file is one more request, so dozens of tiny pieces can be slower than one bundle.

### Measuring: Lighthouse and DevTools

Never guess about speed. Measure, change one thing, and measure again.

- **Lighthouse**, in Chrome DevTools, loads your page on a pretend slow phone and scores LCP, CLS and more. It lists what to fix first.
- **The Network panel** shows every file, its size and its timing. Turn on throttling to feel a slow connection.
- **The Performance panel** records what the browser did, frame by frame. Use it for slow taps (INP).
- **Real-user data**: the `web-vitals` npm package reports the vitals from customers' actual phones. Lab tests on your laptop can't see their networks.

The [React performance](/t/react-09-react-in-depth/topic-09-performance) tools, such as `memo`, help with INP when too much re-renders. Check images and bundles first, though. They are usually the bigger win.

## Worked example

Golu's own product page has one big photo at the top, a description, and a size guide most people never open. Here it is, tuned.

```jsx
import { lazy, Suspense, useState } from "react";

const SizeGuide = lazy(() => import("./SizeGuide.jsx"));

export function GoluPage() {
  const [showGuide, setShowGuide] = useState(false);
  return (
    <main>
      <img
        src="/img/DC-1042-800.webp"
        srcSet="/img/DC-1042-800.webp 800w, /img/DC-1042-1600.webp 1600w"
        sizes="(min-width: 900px) 800px, 100vw"
        alt="Golu the elephant, an indigo crocheted toy with marigold ears"
        width={800}
        height={800}
        fetchPriority="high"
      />
      <h1>Golu the elephant</h1>
      <button type="button" onClick={() => setShowGuide(true)}>Show size guide</button>
      {showGuide && (
        <Suspense fallback={<p>Loading the size guide…</p>}>
          <SizeGuide />
        </Suspense>
      )}
    </main>
  );
}
```

Line by line:

- The photo is the LCP, so it is **not** lazy. `fetchPriority="high"` puts it first in the queue.
- `width` and `height` reserve an 800 by 800 box, so the heading never jumps.
- `srcSet` and `sizes` let a phone pick the 800 file and a big desktop the 1600 file.
- `SizeGuide` is split into its own file. It downloads only when someone presses the button.

## Try it

This adds up how much a phone downloads when the catalogue first opens, before and after the fix.

```js run
const photos = 40;
const fullSizeKB = 900;
const smallKB = 60;
const aboveTheFold = 4;

const before = photos * fullSizeKB;
const after = aboveTheFold * smallKB;
console.log("Before: " + before + " KB on first load");
console.log("After: " + after + " KB on first load");
console.log("Seconds on slow 3G (50 KB/s): " + before / 50 + " vs " + after / 50);
```

It prints:

```text
Before: 36000 KB on first load
After: 240 KB on first load
Seconds on slow 3G (50 KB/s): 720 vs 4.8
```

1. Keep the full-size photos but add lazy loading: set `smallKB` to `900`. How many seconds now?
2. Keep all 40 photos eager, but small: set `aboveTheFold` to `40` and `smallKB` back to `60`. Which change helped more?

## Recap

- Core Web Vitals measure speed as customers feel it: LCP (main content shows), CLS (the page doesn't jump) and INP (taps answer quickly).
- Images are usually the heaviest part. Resize them, use WebP or AVIF, and offer sizes with `srcSet` and `sizes`.
- `width` and `height` on every `<img>` reserve its box, which stops layout shift.
- `loading="lazy"` delays photos below the fold. Never lazy-load the first screen, and give the main photo `fetchPriority="high"`.
- Split rarely used parts out of the bundle with `React.lazy` and `Suspense`. Measure with Lighthouse and real-user data before and after every change.

## Common mistakes

**1. Lazy-loading everything.**

```jsx
<img src={photoUrl(product.id, 400)} alt={product.name} loading="lazy" />   // on every photo, even the first
```

Symptom: Lighthouse flags the main photo as lazily loaded, and LCP gets worse, not better. The check says the first four photos must not be lazy. Fix: `loading={index < ABOVE_THE_FOLD ? "eager" : "lazy"}`.

**2. No width and height.**

```jsx
<img src="/img/DC-1042-400.webp" alt="Golu the elephant" />
```

Symptom: the text appears, then jumps down as each photo lands. Customers tap the wrong product. Fix: add `width={400} height={400}`, and `height: auto` in CSS so it still shrinks on small screens.

**3. Setting a fixed height in CSS to stop the jump.**

```css
.card img { width: 100%; height: 400px; }
```

Symptom: on a narrow phone the photo is squashed or stretched, because its width changed and its height didn't. Fix: put the real `width` and `height` on the `<img>`, and use `max-width: 100%; height: auto;`.

**4. Writing srcset widths in CSS pixels or without the w.**

```jsx
srcSet="/img/DC-1042-400.webp 400, /img/DC-1042-800.webp 800"
```

Symptom: the browser ignores the broken entries, and the check says srcSet should list both sizes. Fix: each entry is the file, a space, its real pixel width and `w`: `/img/DC-1042-400.webp 400w`.

**5. Splitting every component with React.lazy.**

```jsx
const PriceTag = lazy(() => import("./PriceTag.jsx"));
```

Symptom: the page now makes dozens of tiny requests, and every price flashes a loading message. Fix: split only big parts that most customers never open, such as the size guide or the admin pages.
