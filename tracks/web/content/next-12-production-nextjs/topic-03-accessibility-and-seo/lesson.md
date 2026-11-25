## Hook

A shop on a busy road needs two kinds of signs. One is outside: a clear name board, so people walking past know what it sells and can find it again. The other is inside: labels on the shelves and a clear path to the counter, so every customer can get around, including one who uses a wheelchair.

A website needs both too. **SEO** is the sign outside, for search engines and shared links. **Accessibility** is the path inside, for every customer, whatever they use to browse. In Next.js, the same few files do most of both.

## Concept

### Who you're building for

```callout jargon
**Accessibility** (often written **a11y**) means everyone can use your site: with a mouse, a keyboard, a screen reader that reads the page aloud, or a zoomed-in screen. **SEO** (search engine optimisation) means search engines can find, read and show your pages well.
```

The two overlap more than you'd think. A screen reader and a search engine's crawler both read the HTML, not the picture on screen. Both need a clear page language, real headings, links with real words, alt text on photos, and a good title. Fix it once, and both get better.

### The page language and landmarks

```jsx
<html lang="en-IN">
```

The `lang` attribute tells a screen reader which voice and pronunciation to use, and tells search engines who the page is for. `en-IN` means Indian English. In Next.js you write it once, in the root `app/layout.jsx`.

```callout jargon
A **landmark** is a region of the page a screen reader can jump to directly, such as the header, the navigation, the main content or the footer. The HTML elements `<header>`, `<nav>`, `<main>` and `<footer>` create them.
```

Screen reader users move around a page by landmarks and headings, a bit like you skim a newspaper. A page built only from `<div>`s has none, so they must listen from the top every time. Give the page one `<main>`, a `<nav>` with an `aria-label` that names it, and one `<h1>` that says what the page is about.

### Skip links

```jsx
<a href="#main" className="skip-link">Skip to content</a>
```

A keyboard user moves with the Tab key, one link at a time. Without help, they Tab through the logo and every menu link on every page before reaching Golu's price.

A **skip link** is the first link on the page. It jumps straight to `<main id="main">`. It can be hidden until it gets keyboard focus, so it doesn't change the design. Since it sits in the shared layout, every page gets it.

### Syntax: the metadata object

```jsx
export const metadata = {
  metadataBase: new URL("https://dhaaga.in"),
  title: { default: "Dhaaga & Co.", template: "%s | Dhaaga & Co." },
  description: "Handmade crochet from Pune.",
};
```

You met `metadata` in [Metadata: titles and sharing](/t/next-10-nextjs-fundamentals/topic-08-metadata). Next.js turns it into the `<title>` and `<meta>` tags in the page's `<head>`. In the root layout, three fields do a lot:

- **`title.default`** is used on a page that sets no title of its own.
- **`title.template`** wraps every child page's title. `%s` is replaced with that page's title.
- **`metadataBase`** is the site's address. Relative links in metadata, like `/images/DC-1042.jpg`, are joined to it to make full addresses.

```animate
title: A title meets the template
kind: code
language: js
code: |
  const template = "%s | Dhaaga & Co.";
  const pageTitle = "Golu the elephant";
  const fullTitle = template.replace("%s", pageTitle);
  console.log(fullTitle);
steps:
  - line: 1
    vars: { template: '"%s | Dhaaga & Co."' }
    say: The root layout sets one template for the whole site. The %s marks where each page's own title goes.
  - line: 2
    vars: { pageTitle: '"Golu the elephant"' }
    say: Golu's page sets only its own title, his name.
  - line: 3
    vars: { fullTitle: '"Golu the elephant | Dhaaga & Co."' }
    say: Next.js puts the page's title in place of %s.
  - line: 4
    out: "Golu the elephant | Dhaaga & Co."
    say: The tab, the search result and the screen reader all get a title that says which page this is.
```

### Titles matter for screen readers too

When a customer clicks a `Link`, Next.js changes the page without a full reload. A sighted customer sees the new page. A screen reader user would hear nothing at all.

```callout jargon
A **route announcer** is a hidden live region Next.js adds to every app. After each client navigation, it reads out the new page's title, or its `<h1>` if there's no title.
```

So a unique title per page isn't only for Google. It's what a blind customer hears when they arrive on Golu's page. If every title is "Dhaaga & Co.", every page sounds the same.

### Syntax: generateMetadata

```jsx
export async function generateMetadata({ params }) {
  const { id } = await params;
  const item = await getItem(id);
  return { title: item.name, description: item.description };
}
```

A fixed `metadata` object can't know which item a dynamic page shows. `generateMetadata` is the async version. It gets the same `params` as the page, as a Promise, and returns the metadata for this one item.

If an item doesn't exist, return a title like "Item not found" and `robots: { index: false }`. That asks search engines not to list a page that only says "not found".

### Canonical addresses and sharing previews

```callout jargon
A **canonical URL** is the one official address of a page. If the same page can be reached at several addresses, like with `?ref=instagram` on the end, the canonical tells search engines which one to list.
```

```jsx
alternates: { canonical: "/items/DC-1042" },
openGraph: {
  title: "Golu the elephant",
  images: [{ url: "/images/DC-1042.jpg", width: 1200, height: 630, alt: "A small Indigo crocheted elephant" }],
},
```

**Open Graph** tags are what WhatsApp, Instagram and Facebook read to draw a link's preview: a title, a line of text and a picture. Without them, a shared Golu link shows only a bare address. 1200 by 630 pixels is the size most apps expect. Give the image `alt` text too.

### robots.txt and the sitemap

```callout jargon
A **crawler** is a search engine's program that visits pages and reads them. **robots.txt** is a file at the site's root with rules for crawlers. A **sitemap** (`sitemap.xml`) lists every page you want found.
```

In the app router, these are code, not hand-written files. `app/robots.js` returns the rules and `app/sitemap.js` returns the list. Next.js serves them at `/robots.txt` and `/sitemap.xml`. The sitemap can read the catalogue, so a new item appears in it automatically.

```animate
title: A crawler visits dhaaga.in
kind: scene
nodes:
  - { id: bot, label: Search crawler, emoji: "🕷️", row: 1, col: 0 }
  - { id: robots, label: /robots.txt, emoji: "📜", row: 0, col: 3 }
  - { id: sitemap, label: /sitemap.xml, emoji: "🗺️", row: 1, col: 3 }
  - { id: golu, label: /items/DC-1042, emoji: "🐘", value: "not listed", row: 2, col: 3 }
steps:
  - say: Before anything else, the crawler asks robots.txt what it may visit.
    send: { from: bot, to: robots, label: "GET /robots.txt" }
  - say: The old file, copied from the preview site, says Disallow /. The crawler politely leaves. Nothing gets listed.
    send: { from: robots, to: bot, label: "Disallow: /", back: true }
    focus: [robots]
  - say: After the fix, robots.txt allows the shop, keeps out the cart, and names the sitemap.
    send: { from: robots, to: bot, label: "Allow: /, Sitemap: ...", back: true }
  - say: The sitemap lists the home page and every item page with full addresses.
    send: [{ from: bot, to: sitemap, label: "GET /sitemap.xml" }, { from: sitemap, to: bot, label: "6 pages", back: true }]
  - say: The crawler reads Golu's page, its title, description and canonical address. Now it can be found.
    send: { from: bot, to: golu, label: "GET" }
    set: { golu: listed }
```

A preview or staging site should block crawlers, so Google doesn't list half-finished pages. The classic launch-day bug is copying that rule to the real site.

## Worked example

Here is the metadata for the Baby booties page, written by hand so you can see every field.

```jsx
export async function generateMetadata({ params }) {
  const { id } = await params;
  const item = await getItem(id);
  if (!item) {
    return { title: "Item not found", robots: { index: false } };
  }
  return {
    title: item.name,
    description: item.description,
    alternates: { canonical: `/items/${item.id}` },
    openGraph: {
      title: item.name,
      description: item.description,
      images: [{ url: `/images/${item.id}.jpg`, width: 1200, height: 630, alt: item.alt }],
    },
  };
}
```

For `DC-1402`, Next.js writes tags like these into the page's `<head>`, among others:

```html
<title>Baby booties | Dhaaga &amp; Co.</title>
<meta name="description" content="Soft Mint booties for the first six months.">
<link rel="canonical" href="https://dhaaga.in/items/DC-1402">
<meta property="og:title" content="Baby booties">
<meta property="og:image" content="https://dhaaga.in/images/DC-1402.jpg">
<meta property="og:image:alt" content="A pair of Mint crocheted baby booties">
```

- The title is just "Baby booties". The layout's template adds the shop's name.
- The canonical and the image became full addresses, thanks to `metadataBase` in the layout.
- `params` is awaited first, because in Next.js 16 it's a Promise.

## Try it

This builds a sitemap from a catalogue and checks a robots rule, the same way the app's files do. Run it.

```js run
const site = "https://dhaaga.in";
const items = [{ id: "DC-1042" }, { id: "DC-1101" }];

const sitemap = [{ url: site + "/" }, ...items.map((item) => ({ url: site + "/items/" + item.id }))];
console.log(sitemap.map((entry) => entry.url).join("\n"));

const disallow = ["/cart", "/checkout"];
const allowed = (path) => !disallow.some((blocked) => path.startsWith(blocked));
console.log(allowed("/items/DC-1042"), allowed("/cart"));
```

It prints:

```text
https://dhaaga.in/
https://dhaaga.in/items/DC-1042
https://dhaaga.in/items/DC-1101
true false
```

1. Add `{ id: "DC-1402" }` to `items`. What new line appears, and why does a real sitemap stay right without anyone editing it?
2. Change `disallow` to `["/"]`, the preview site's rule. What does `allowed` say now for Golu's page?

## Recap

- Set `lang="en-IN"` on `<html>` in the root layout, and build pages from landmarks: one `<main>`, a named `<nav>`, and one `<h1>`.
- Put a "Skip to content" link first in the layout, pointing at `<main id="main">`, so keyboard users skip the menu.
- In the root layout's `metadata`, set `metadataBase`, a `title` with a `default` and a `template`, and a `description`. Unique titles also tell screen reader users where they've arrived.
- Use `generateMetadata` for dynamic pages: the item's title and description, a canonical address, Open Graph tags for sharing previews, and `robots: { index: false }` for "not found".
- `app/robots.js` and `app/sitemap.js` tell crawlers what to read. Never ship the preview site's "Disallow: /".

## Common mistakes

**1. The same title on every page.**

```jsx
export const metadata = { title: "Dhaaga & Co." };   // in the layout, and nothing on the pages
```

Symptom: ten tabs all say "Dhaaga & Co.", search results look identical, and the route announcer says the same thing on every page. Fix: a `title` object with a `template` in the layout, and a title on each page.

**2. Copying the preview site's robots rule.**

```jsx
return { rules: { userAgent: "*", disallow: "/" } };
```

Symptom: weeks after launch, Google still shows nothing for "crochet elephant Pune". Fix: allow `/` on the live site, disallow only private pages like `/cart`, and name the sitemap.

**3. A layout made only of divs.**

```jsx
<div className="top">...</div>
<div className="content">{children}</div>
```

Symptom: a screen reader user can't jump to the main content and must listen to the whole menu on every page. Fix: `<header>`, `<nav aria-label="Main">`, `<main id="main">` and `<footer>`, plus a skip link.

**4. Relative image addresses without metadataBase.**

```jsx
openGraph: { images: [{ url: "/images/DC-1042.jpg" }] }   // and no metadataBase in the layout
```

Symptom: WhatsApp can't load the preview picture, because `/images/...` isn't a full address. Fix: set `metadataBase: new URL("https://dhaaga.in")` in the root layout.

**5. Reading params without await in generateMetadata.**

```jsx
const item = await getItem(params.id);
```

Symptom: every item page gets the "Item not found" title and is hidden from search, though the page itself shows fine. Fix: `const { id } = await params;` first, exactly as in the page.
