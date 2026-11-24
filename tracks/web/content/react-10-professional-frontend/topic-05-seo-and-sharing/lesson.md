## Hook

When the Aundh shop opened, Meera Aunty did two things besides hanging a sign. She put the shop on Google Maps, with photos and opening hours. And she printed small cards with a photo of Golu, for customers to pass to friends.

A website needs the same two things. Search engines must find and understand every page, so people searching "crochet elephant Pune" land on Golu. And every shared link should unfold into a little poster, with a photo, a name and a price. Neither happens by accident, and a React app makes both harder than you'd expect.

## Concept

### How a search engine finds pages

```callout jargon
**SEO** (search engine optimisation) is making pages easy for search engines to find, understand and show. A **crawler**, or bot, is a program that fetches web pages automatically, such as Googlebot.
```

Search works in three steps:

1. **Crawl.** The crawler fetches a page, reads its links, and adds them to its list to fetch next.
2. **Index.** It stores what each page is about: its title, words, headings, photos and links.
3. **Rank.** When someone searches, it picks the most useful indexed pages and orders them.

You can't buy a better rank. You can make sure every page is reachable, readable and fast, and that it answers what people search for.

```animate
title: A crawler and a preview bot visit dhaaga.in
kind: scene
nodes:
  - { id: google, label: Googlebot, emoji: "🤖", row: 0, col: 0 }
  - { id: site, label: dhaaga.in, emoji: "🖥️", row: 1, col: 2 }
  - { id: index, label: Search index, emoji: "🗂️", value: "0 pages", row: 0, col: 4 }
  - { id: whatsapp, label: WhatsApp preview bot, emoji: "💬", row: 2, col: 0 }
steps:
  - say: Googlebot fetches the home page and finds links to every product page.
    send: [{ from: google, to: site, label: "GET /" }, { from: site, to: google, label: "HTML + links", back: true }]
  - say: It fetches each product page, reads the title and words, and stores them in the index.
    send: { from: google, to: index, label: "Golu the elephant, ₹450" }
    set: { index: "6 pages" }
  - say: Meera Aunty pastes Golu's link into WhatsApp. WhatsApp's bot fetches the page to build a preview card.
    send: { from: whatsapp, to: site, label: "GET /products/DC-1042" }
  - say: The bot reads only the HTML that arrives. It doesn't run JavaScript, so an empty React root gives an empty card.
    send: { from: site, to: whatsapp, label: "<div id=root></div>", back: true }
    focus: [whatsapp]
  - say: With the tags in the HTML itself, the card shows Golu's photo, name and price.
    send: { from: site, to: whatsapp, label: "og:title, og:image", back: true }
```

### Syntax: the title and the description

```html
<title>Golu the elephant, crocheted toy | Dhaaga & Co.</title>
<meta name="description" content="A hand-crocheted indigo elephant with marigold ears, made in Pune. ₹450, ships across India.">
```

The `<title>` is the blue link in search results and the name of the browser tab. The description is often the grey text under it. Both go in the `<head>`, as in [The head: title, meta tags and the viewport](/t/web-01-html/topic-10-head-and-meta).

Good titles:

- are **different on every page**, and lead with what the page is about;
- are about 50 to 60 characters, so they aren't cut off;
- end with the brand.

A description is a short, honest advert for the page, about 150 characters. It doesn't change your rank, but a good one makes more people click.

### One page, one address

Search engines rank **addresses**. So every product needs its own URL that loads that product directly, such as `https://dhaaga.in/products/DC-1042`.

Two traps:

- **Everything on one address.** If clicking Golu only changes React state, Google sees one page, not six.
- **One page on many addresses.** `/products/DC-1042`, `/products/DC-1042?ref=whatsapp` and `/toys/golu` all show Golu. Search engines split his rank between them.

```callout jargon
A **canonical URL** is the one address you want search engines to use for a page. You declare it with `<link rel="canonical" href="https://dhaaga.in/products/DC-1042">`, and the other addresses count towards it.
```

### The single-page app problem

In [Why React](/t/react-08-react-fundamentals/topic-01-why-react) you saw how a React app starts: the server sends an almost empty `index.html`, then JavaScript draws everything.

```html
<body>
  <div id="root"></div>
  <script type="module" src="/assets/index-4f9a1c2b.js"></script>
</body>
```

That's a problem for bots:

- **Link-preview bots** (WhatsApp, Instagram, LinkedIn, Slack) usually don't run JavaScript at all. They see an empty page.
- **Googlebot** does run JavaScript, but later and with limits. Pages that need it are indexed more slowly and less reliably.
- Every page has the same `<title>` from `index.html`, often "Vite + React".

```callout jargon
A **single-page app** (SPA) loads one HTML page and draws every screen with JavaScript in the browser. This is called **client-side rendering**.
```

### Rendering before the browser

The fix is to send HTML that already contains the content.

```callout jargon
**Server-side rendering** (SSR) runs your React components on the server for each request and sends the finished HTML. **Static site generation** (SSG), or prerendering, does the same once, at build time, and saves an HTML file per page.
```

After the HTML arrives, React takes over in the browser and makes it interactive. This step is called **hydration**.

| Approach | Good for | Example tools |
|---|---|---|
| Client-side only | dashboards and pages behind a login | Vite + React |
| Static generation | pages that change rarely: products, help pages | Astro, Next.js, Vite prerender plugins |
| Server rendering | pages that change every minute, like live stock | Next.js, React Router framework mode |

Most stores mix them. The product pages are generated at build time, and the cart and checkout run in the browser, where no bot needs to look.

### Syntax: Open Graph tags

```html
<meta property="og:title" content="Golu the elephant">
<meta property="og:description" content="Hand-crocheted in Pune. ₹450.">
<meta property="og:image" content="https://dhaaga.in/img/DC-1042-1200.webp">
<meta property="og:url" content="https://dhaaga.in/products/DC-1042">
```

```callout jargon
**Open Graph** is a set of meta tags, started by Facebook, that describe how a link should look when it's shared. WhatsApp, LinkedIn, Slack and most chat apps read them to build the preview card.
```

Rules that trip people up:

- `og:image` must be a **full address**, starting with `https://`. A path like `/img/golu.webp` doesn't work.
- Use a big, clear image, about 1200 by 630 pixels, and keep the file small. WhatsApp may skip very large files.
- Apps cache previews. After you fix the tags, an old share may keep its blank card for a while.

### Titles inside React

In a client-rendered app, you can still change the title as the customer moves around. React 19 lets a component render `<title>` and `<meta>` anywhere, and moves them into the `<head>` for you:

```jsx
function ProductPage({ product }) {
  return (
    <article>
      <title>{`${product.name} | Dhaaga & Co.`}</title>
      <meta name="description" content={product.blurb} />
      <h1>{product.name}</h1>
    </article>
  );
}
```

This helps customers with many tabs open, and Googlebot when it runs your JavaScript. Preview bots still need the tags in the HTML that arrives, from SSR or SSG.

### robots.txt and sitemap.xml

Two small files at the top of your site talk to crawlers:

```text
# https://dhaaga.in/robots.txt
User-agent: *
Disallow: /checkout
Disallow: /admin
Sitemap: https://dhaaga.in/sitemap.xml
```

- **`robots.txt`** asks crawlers to skip some paths, such as the checkout. It's a polite request, not security: never use it to hide private data.
- **`sitemap.xml`** lists every page you want indexed, with when it last changed. It helps crawlers find pages that few links point to.

A staging site usually has `Disallow: /` so that Google ignores it. Copying that file to production by mistake hides the whole store.

### Structured data

Search engines can show rich results: a price, stock and star rating right under the link. They read these from **structured data**, a small block of JSON in a standard vocabulary called schema.org.

```html
<script type="application/ld+json">
{
  "@context": "https://schema.org",
  "@type": "Product",
  "name": "Golu the elephant",
  "sku": "DC-1042",
  "offers": { "@type": "Offer", "price": "450.00", "priceCurrency": "INR", "availability": "https://schema.org/InStock" }
}
</script>
```

This block is data, not code: the browser never runs it. It must match what the page shows. A price in the data that differs from the page can get the rich result removed.

### Good pages beat tricks

Everything from this module helps SEO too:

- **Semantic HTML**: one `<h1>`, real headings, `<nav>`, and link text that says where it goes.
- **Alt text** on product photos, which also lets them show in image search.
- **Speed**: Core Web Vitals are part of the ranking.
- **Phone-friendly pages**: Google indexes the mobile version of your site first.

Hidden keywords, white text on a white background, and pages stuffed with "crochet Pune crochet Pune" get sites pushed down, not up.

## Worked example

Here is the `<head>` that Golu's product page should send, generated at build time. Each line answers one question a bot asks.

```html
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Golu the elephant, crocheted toy | Dhaaga & Co.</title>
  <meta name="description" content="A hand-crocheted indigo elephant with marigold ears, made in Pune. ₹450, ships across India.">
  <link rel="canonical" href="https://dhaaga.in/products/DC-1042">
  <meta property="og:title" content="Golu the elephant">
  <meta property="og:description" content="Hand-crocheted in Pune. ₹450.">
  <meta property="og:image" content="https://dhaaga.in/img/DC-1042-1200.webp">
  <meta property="og:url" content="https://dhaaga.in/products/DC-1042">
  <meta property="og:type" content="product">
</head>
```

- **What is this page?** The title and description, unique to Golu.
- **Which address counts?** The canonical link. A share with `?ref=whatsapp` still counts for the main page.
- **What should a shared link look like?** The four `og:` tags, with a full image address.

The body that follows would also contain Golu's name, photo, price and description as real HTML, before any JavaScript runs.

## Try it

A build step can write these tags from the catalogue data, so no page is forgotten. This program does it for one product.

```js run
function headFor(product) {
  const price = "₹" + (product.paise / 100).toFixed(2);
  return [
    `<title>${product.name} (${price}) | Dhaaga & Co.</title>`,
    `<meta name="description" content="${product.blurb}">`,
    `<meta property="og:title" content="${product.name}">`,
    `<meta property="og:image" content="https://dhaaga.in/img/${product.id}-1200.webp">`,
    `<link rel="canonical" href="https://dhaaga.in/products/${product.id}">`,
  ];
}

const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000, blurb: "A hand-crocheted indigo elephant, made in Pune." };
for (const tag of headFor(golu)) {
  console.log(tag);
}
```

It prints:

```text
<title>Golu the elephant (₹450.00) | Dhaaga & Co.</title>
<meta name="description" content="A hand-crocheted indigo elephant, made in Pune.">
<meta property="og:title" content="Golu the elephant">
<meta property="og:image" content="https://dhaaga.in/img/DC-1042-1200.webp">
<link rel="canonical" href="https://dhaaga.in/products/DC-1042">
```

1. Run it for the tote bag: `DC-1301`, "Rani pink tote bag", 89900 paise. Is every tag different from Golu's?
2. Give a product the blurb `Soft & "cute"`. What goes wrong in the description tag? (Hint: think back to escaping in [Security: XSS and safe rendering](/t/react-10-professional-frontend/topic-04-security).)

## Recap

- Search engines crawl pages through links, index what each page says, then rank them. Every product needs its own address, with one canonical URL.
- Every page needs its own `<title>` and `<meta name="description">`.
- Link-preview bots like WhatsApp's don't run JavaScript, and Googlebot runs it late. Send real HTML with server-side rendering or static generation, and let React hydrate it.
- Open Graph tags (`og:title`, `og:description`, `og:image` with a full address, `og:url`) build a shared link's card.
- `robots.txt` asks crawlers to skip paths, `sitemap.xml` lists your pages, and schema.org structured data can add prices and stock to search results.

## Common mistakes

**1. The same title on every page.**

```html
<title>Vite + React</title>
```

Symptom: every tab and every search result says "Vite + React", and shared links have no name. Fix: a unique title per page, written at build time or by the server, and updated with React 19's `<title>` as the customer moves around.

**2. A relative og:image.**

```html
<meta property="og:image" content="/img/DC-1042-1200.webp">
```

Symptom: the WhatsApp card has a title but no photo. Fix: use the full address, `https://dhaaga.in/img/DC-1042-1200.webp`.

**3. Content that only JavaScript draws.**

```html
<body><div id="root"></div></body>
```

Symptom: link previews are blank, and product pages take weeks to appear in Google, if at all. Fix: generate the product pages at build time, or render them on the server.

**4. Staging's robots.txt on the live site.**

```text
User-agent: *
Disallow: /
```

Symptom: the store slowly drops out of Google after launch. Fix: keep a separate `robots.txt` per environment, and check the live one after every deploy.

**5. Many addresses for one product.**

```text
https://dhaaga.in/products/DC-1042
https://dhaaga.in/products/DC-1042?ref=whatsapp
https://dhaaga.in/toys/golu
```

Symptom: Golu's rank is split three ways, and the wrong address shows in search. Fix: add `<link rel="canonical" href="https://dhaaga.in/products/DC-1042">` to every version.
