## Hook

Imagine posting a wedding album to someone who only wants to see one small photo on their phone. The postage is huge, it arrives late, and they have to make room on the shelf when it lands.

Sana's 4 MB camera photos are that album. Every phone downloads the full-size picture, even to show it as big as a stamp. Next.js has two helpers that fix this for images and for fonts.

## Concept

### The trouble with a plain img

```jsx
<img src="/photos/golu.jpg" alt="Golu the elephant" />
```

This one line causes three problems:

- **Too big.** The phone downloads the full 4 MB photo, even to show it 360 pixels wide.
- **Too early.** Every photo on the page starts downloading at once, even the ones far below that nobody scrolls to.
- **Jumpy.** The browser doesn't know the photo's shape until it arrives. So it saves no space, and when the photo lands, the text below jumps down.

```callout jargon
**Layout shift** is when things on the page move after they first appear, because something above them changed size. It makes people tap the wrong button. Google measures it as **CLS** (Cumulative Layout Shift), one of the **Core Web Vitals** that score how a page feels.
```

### Syntax: the Image component

```jsx
import Image from "next/image";

<Image src="/photos/golu.jpg" alt="Golu the elephant" width={800} height={800} />
```

`Image` from `next/image` renders an `<img>`, but it fixes all three problems:

- `src` is a path to a file in the `public` folder (or a photo you import, see below).
- `alt` describes the photo for screen readers, as you learned in web-01. It's required.
- `width` and `height` are the photo's real size in pixels. They don't set how big it shows: CSS does that. They tell the browser the photo's **shape**.

### How the shape stops the jump

From `width` and `height`, the browser knows the photo's **aspect ratio**: how wide it is compared to how tall. So it can save exactly the right space before a single byte arrives.

```js run
const width = 800;
const height = 600;
const shownWidth = 360;
console.log((shownWidth * height) / width);
```

An 800 by 600 photo shown 360 pixels wide needs 270 pixels of height. The browser saves that space at once, so nothing moves when the photo arrives.

### What Image does with your photo

```animate
title: Golu's photo on Raju's phone
kind: scene
nodes:
  - { id: phone, label: Raju's phone, emoji: "📱", value: "360 px wide" }
  - { id: next, label: Next.js image service, emoji: "🖼️" }
  - { id: file, label: public/photos/golu.jpg, emoji: "📷", value: "4 MB" }
steps:
  - say: The page arrives. Golu's photo is far down, so the phone waits. Images load lazily by default.
    focus: [phone]
  - say: Raju scrolls. The photo is about to come into view, so now the phone asks for it, at the size it needs.
    send: { from: phone, to: next, label: "golu.jpg, 640 wide" }
  - say: Next.js reads the original photo once.
    send: { from: file, to: next, label: "4 MB original" }
  - say: It shrinks it to 640 pixels, compresses it, and turns it into WebP, a smaller modern format.
    set: { file: "4 MB, untouched" }
  - say: The phone gets about 60 kB instead of 4 MB. The space was saved already, so nothing jumps.
    send: { from: next, to: phone, label: "golu.webp, 60 kB", back: true }
  - say: Next.js keeps the small copy. The next phone that asks gets it straight away.
    focus: [next]
```

```callout jargon
**Lazy loading** means waiting to download something until it's about to be needed. A lazy image loads when it nears the screen. The opposite, **eager** loading, starts at once.
```

The page offers the browser several sizes in a `srcset`, and the browser picks the smallest that looks sharp on its screen.

### Syntax: fill and sizes

```jsx
<div style={{ position: "relative", aspectRatio: "1 / 1" }}>
  <Image src="/photos/golu.jpg" alt="Golu the elephant" fill sizes="(max-width: 600px) 100vw, 33vw" />
</div>
```

When you don't know a photo's size, or it should stretch to fill a box, use `fill` instead of `width` and `height`. The image fills its parent, which must have `position: relative` and a size of its own.

`sizes` tells the browser how wide the image will show: here, the whole screen on phones, a third of it on bigger screens. Without it, the browser assumes full width and may download a bigger file than it needs.

### The first big photo: load it early

Lazy loading is right for photos further down. But the big photo at the top of the home page, the first thing customers see, should start loading at once.

```jsx
<Image src="/photos/hero.jpg" alt="Meera Aunty at the FC Road stall" width={1600} height={900} preload />
```

`preload` tells the browser about the photo in the page's `<head>`, before it even reaches the `<img>`. Use it for one image only: the main one at the top. In Next.js 16, `preload` replaced the older `priority` prop, which still appears in old tutorials. For photos that only need to skip lazy loading, `loading="eager"` is enough.

```callout jargon
**LCP** (Largest Contentful Paint) is the time until the biggest thing near the top, often a photo, has appeared. It's another Core Web Vital. Loading that one photo early improves it.
```

### Photos from other websites

```js
// next.config.js
module.exports = {
  images: {
    remotePatterns: [new URL("https://photos.dhaaga.in/**")],
  },
};
```

A `src` like `https://photos.dhaaga.in/golu.jpg` only works once its host is listed in `next.config.js`. Otherwise anyone could use your image service to shrink their pictures. For a photo you import, like `import golu from "./golu.jpg"`, Next.js reads the width and height itself.

### The trouble with web fonts

Sana's brand font is Nunito, from Google Fonts. The usual way to load it is a `<link>` to Google. That causes its own problems:

- The page first shows in a plain font, then flicks to Nunito when it arrives. Text can change size and shift.
- Every visitor's browser contacts Google's servers, which is slower and shares their visit with another company.

### Syntax: next/font

```jsx
import { Nunito } from "next/font/google";

const nunito = Nunito({ subsets: ["latin"] });

export default function RootLayout({ children }) {
  return (
    <html lang="en" className={nunito.className}>
      <body>{children}</body>
    </html>
  );
}
```

`next/font/google` downloads the font files **when you build the site**, and serves them from dhaaga.in itself. No visitor ever contacts Google. Next.js also sizes the plain fallback font to match Nunito, so the switch barely moves anything.

- Call the font function at the top of the file, outside any component, so it runs once.
- `subsets: ["latin"]` keeps only the letters you need, so the file is small.
- `nunito.className` is a class name that applies the font. Put it on `<html>` to use it everywhere.
- For a font file of your own, use `next/font/local` with `src: "./fonts/MyFont.woff2"`.

## Worked example

Golu's item page, with his photo and the brand font:

```jsx
// src/app/layout.jsx
import { Nunito } from "next/font/google";

const nunito = Nunito({ subsets: ["latin"], variable: "--font-body" });

export default function RootLayout({ children }) {
  return (
    <html lang="en" className={nunito.variable}>
      <body>{children}</body>
    </html>
  );
}
```

```jsx
// in the item page
import Image from "next/image";

<Image
  src="/photos/DC-1042.jpg"
  alt="Golu, an indigo crocheted elephant with a marigold blanket"
  width={1200}
  height={1200}
  sizes="(max-width: 600px) 100vw, 50vw"
  preload
/>
```

- `variable` turns the font into a CSS variable, `--font-body`, so CSS can use `font-family: var(--font-body)`.
- Golu's photo is the main image on his page, so it's preloaded. Photos of other items lower down stay lazy.
- `width` and `height` say it's square, so the space is saved before it loads.
- `sizes` says it shows full width on phones and half width on bigger screens, so phones get a small file.

## Try it

This ticket's check is a quiz, and these components can't run in the checks. On your own Next.js project from ticket 1, put a large photo in `public/`, show it with `<Image>`, and open the Network tab in DevTools. Find the request to `/_next/image`: look at its size and its type, `webp`. Then compare with a plain `<img>` of the same file.

## Recap

- A plain `<img>` downloads the full photo, loads everything at once, and makes the page jump.
- `Image` from `next/image` resizes and compresses photos per screen, loads them lazily, and saves their space using `width` and `height`.
- Use `fill` with `sizes` for photos that fill a box, and `preload` for the one big photo at the top.
- Remote photos need their host in `next.config.js`.
- `next/font` downloads fonts at build time and serves them from your own site, with no flash and no request to Google.

## Common mistakes

**1. Leaving out width and height.**

```jsx
<Image src="/photos/golu.jpg" alt="Golu the elephant" />
```

Next.js stops with an error saying the image is missing its required `width` property. It needs the shape to save the space.

Fix: give the real `width` and `height`, or use `fill` inside a sized box.

**2. An image from a host that isn't listed.**

```jsx
<Image src="https://photos.dhaaga.in/golu.jpg" alt="Golu" width={800} height={800} />
```

Next.js stops with an error saying the hostname is not configured under `images` in `next.config.js`.

Fix: add the host to `images.remotePatterns`.

**3. Preloading every photo.**

```jsx
{items.map((item) => (
  <Image key={item.id} src={item.photo} alt={item.name} width={400} height={400} preload />
))}
```

Every photo now competes to load first, so the one that matters arrives later, not sooner.

Fix: `preload` only the main photo at the top of the page.

**4. Calling the font function inside a component.**

```jsx
export default function RootLayout({ children }) {
  const nunito = Nunito({ subsets: ["latin"] });
  // ...
}
```

Next.js refuses: font loaders must be called at the top level of the file, so they can run at build time.

Fix: move `const nunito = Nunito({ ... })` above the component.
