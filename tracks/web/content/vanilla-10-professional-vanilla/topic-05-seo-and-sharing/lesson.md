## Hook

Most of Dhaaga's first customers came by word of mouth. Someone's aunt showed a coaster set at a kitty party, and two more people walked up to the stall.

Online, word of mouth is a shared link and a search result. When Meera Aunty drops Golu's link in a WhatsApp group, the message should show his photo, his name and his price. When someone searches "crochet elephant Pune", Golu should be there. Both depend on what your HTML says to the robots that read it.

## Concept

### How search engines find a page

```callout jargon
A **crawler** (or bot) is a program that visits web pages and follows their links, like Googlebot. The **index** is the search engine's giant catalogue of the pages it has read. **SEO** (search engine optimisation) is making your pages easy for crawlers to find, read and show well.
```

A search engine works in three steps:

1. **Crawl**: its bot downloads your page and follows your links to find more pages.
2. **Index**: it reads the words, the title and the headings, and files the page under what it's about.
3. **Rank**: when someone searches, it orders the pages it thinks answer them best.

You can't buy step 3, but you can make steps 1 and 2 easy. Real links, real headings and real text, like the progressive-enhancement baseline from [Progressive enhancement](/t/vanilla-10-professional-vanilla/topic-04-progressive-enhancement), are most of the work.

### Syntax: title and meta description

```html
<title>Golu the elephant, crochet amigurumi | Dhaaga &amp; Co.</title>
<meta name="description" content="Golu, our hand-crocheted elephant, made in Pune. ₹450, delivered free in Pune on orders above ₹999.">
```

- The **`<title>`** is the blue link in search results and the name of the browser tab. Each page needs its own, with the most important words first.
- The **meta description** is often the grey text under that link. It doesn't change your rank, but a clear one makes people click.

A shop where every page is titled "Dhaaga & Co." looks like one page to a search engine, repeated thirty times.

### Meaningful URLs

Every product should have one address of its own, and it should be readable:

```text
https://dhaaga.in/products/DC-1042        good: one page, one address
https://dhaaga.in/#/product?i=3           weak: the part after # never reaches the server
https://dhaaga.in/shop?id=DC-1042&ref=wa  the same page under many addresses
```

The part after `#` stays in the browser. Servers and most crawlers never see it, so every hash route looks like the home page to them. The History API routing from [Routing with the History API](/t/vanilla-09-single-page-apps/topic-03-history-routing) gives real paths instead.

When one page has several addresses, tell crawlers which one is the real one:

```html
<link rel="canonical" href="https://dhaaga.in/products/DC-1042">
```

```callout jargon
A **canonical URL** is the one main address of a page. With it, `?ref=whatsapp` and other copies count as the same page, instead of competing with each other.
```

### Syntax: Open Graph tags

```html
<meta property="og:title" content="Golu the elephant">
<meta property="og:description" content="Hand-crocheted in Pune. ₹450.">
<meta property="og:image" content="https://dhaaga.in/img/DC-1042-1200.jpg">
<meta property="og:url" content="https://dhaaga.in/products/DC-1042">
```

```callout jargon
**Open Graph** tags are `<meta property="og:...">` tags that say how a page looks when it is shared. WhatsApp, Facebook, LinkedIn and many others read them to draw the **link preview**: the card with a picture, a title and a line of text.
```

The image must be a **full** address starting with `https://`. A path like `/img/golu.jpg` means nothing to WhatsApp's servers. A wide picture of about 1200 by 630 pixels shows well everywhere.

### Pages drawn by JavaScript

Here is the catch for a plain-JavaScript store. If `main.js` fetches the product and fills in the page, the HTML the server sends is almost empty.

```animate
title: Two robots read Golu's page
kind: scene
nodes:
  - { id: wa, label: WhatsApp preview bot, emoji: "💬", value: waiting, row: 0, col: 0 }
  - { id: server, label: dhaaga.in, emoji: "🗄️", row: 0, col: 3 }
  - { id: google, label: Googlebot, emoji: "🔎", value: waiting, row: 2, col: 0 }
  - { id: js, label: main.js runs, emoji: "📜", row: 2, col: 3 }
steps:
  - say: Meera Aunty shares Golu's link. WhatsApp's bot fetches the page to draw a preview.
    send: { from: wa, to: server, label: "GET /products/DC-1042" }
  - say: The server sends an empty shell. Golu's name and photo would be added later by main.js.
    send: { from: server, to: wa, label: "<div id=app></div>", back: true }
  - say: The preview bot doesn't run JavaScript. It finds no og tags, so the preview is a grey box.
    set: { wa: "blank preview" }
    focus: [wa]
  - say: Googlebot fetches the same shell. It can run JavaScript, but in a separate step that waits in a queue and can come later.
    send: { from: google, to: server, label: "GET /products/DC-1042" }
    set: { google: "queued" }
  - say: When it finally runs main.js, it sees Golu. Until then, it only has the empty shell. Many other crawlers never run the script at all.
    send: { from: google, to: js, label: "render later" }
    set: { google: "indexed late" }
  - say: The fix is to send Golu's name, text and og tags in the HTML itself. Then both robots read them at once.
    set: { wa: "Golu preview", google: "indexed now" }
```

So the pages you want found and shared must arrive **with their content in the HTML**. For a static site, the usual way is to build one HTML file per product before you deploy.

```callout jargon
**Prerendering** (or **static generation**) means running your page-building code at build time, and saving the result as real HTML files: `products/DC-1042/index.html`, and so on. The server sends a full page, and the script enhances it.
```

A small Node script can do this: read `catalogue.json`, fill a template for each product, and write the files. That's progressive enhancement again, applied to search and sharing.

### robots.txt and sitemap.xml

Two plain files at the root of the site help crawlers.

```text
# https://dhaaga.in/robots.txt
User-agent: *
Disallow: /checkout
Disallow: /account
Sitemap: https://dhaaga.in/sitemap.xml
```

- **`robots.txt`** says what crawlers may visit. Checkout and account pages are useless in search results. It's a polite request, not a lock: never use it to hide anything private.
- **`sitemap.xml`** lists every page you want found, so new products are discovered quickly, even before anything links to them.

### Structured data

Search results sometimes show a price, stock or star rating under a link. That comes from **structured data**.

```html
<script type="application/ld+json">
{
  "@context": "https://schema.org",
  "@type": "Product",
  "name": "Golu the elephant",
  "sku": "DC-1042",
  "image": "https://dhaaga.in/img/DC-1042-1200.jpg",
  "offers": { "@type": "Offer", "price": "450.00", "priceCurrency": "INR", "availability": "https://schema.org/InStock" }
}
</script>
```

```callout jargon
**JSON-LD** is a block of JSON inside a `<script type="application/ld+json">` tag. Browsers don't run it. Search engines read it, using the shared vocabulary from **schema.org**, to understand that this page is a product with a price.
```

The data must match what the page shows. A JSON-LD price of ₹400 on a page that says ₹450 can get the page's rich results removed.

## Worked example

Here is the `<head>` of Golu's prerendered page, with each line's job:

```html
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Golu the elephant, crochet amigurumi | Dhaaga &amp; Co.</title>
  <meta name="description" content="Golu, our hand-crocheted elephant, made in Pune. ₹450.">
  <link rel="canonical" href="https://dhaaga.in/products/DC-1042">
  <meta property="og:title" content="Golu the elephant">
  <meta property="og:description" content="Hand-crocheted in Pune. ₹450.">
  <meta property="og:image" content="https://dhaaga.in/img/DC-1042-1200.jpg">
  <meta property="og:url" content="https://dhaaga.in/products/DC-1042">
  <script type="module" src="/main.js"></script>
</head>
```

- `title` and `description` serve search results: product words first, the shop's name last.
- `canonical` makes `?ref=whatsapp` copies count as this one page.
- The four `og:` tags draw the WhatsApp card, with a full `https://` image address.
- `main.js` still loads. It enhances the page, but the page doesn't need it to be read.

The `<body>` holds a real `<h1>Golu the elephant</h1>`, the price, the photo with `alt` text, and the Add to cart form. Crawlers read those too.

## Try it

A sitemap is easy to build from the catalogue in a build script. This prints one for three products. Run it.

```js run
const ids = ["DC-1042", "DC-1101", "DC-1201"];
const urls = ids.map((id) => `  <url><loc>https://dhaaga.in/products/${id}</loc></url>`);
console.log('<?xml version="1.0" encoding="UTF-8"?>');
console.log('<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">');
console.log(urls.join("\n"));
console.log("</urlset>");
```

It prints:

```text
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <url><loc>https://dhaaga.in/products/DC-1042</loc></url>
  <url><loc>https://dhaaga.in/products/DC-1101</loc></url>
  <url><loc>https://dhaaga.in/products/DC-1201</loc></url>
</urlset>
```

1. Add the home page, `https://dhaaga.in/`, as the first entry.
2. Add a `<lastmod>2026-11-20</lastmod>` inside each `<url>`, after the `<loc>`.

## Recap

- Crawlers download pages, read them, and follow links. Real links, headings and text in the HTML make your pages easy to find.
- Every page needs its own `<title>` and meta description, one readable address like `/products/DC-1042`, and a canonical link when copies exist.
- Link previews come from Open Graph tags in the HTML, with a full `https://` image address. Preview bots don't run JavaScript.
- Pages you want found and shared must send their content in the HTML, for example by prerendering one file per product at build time.
- `robots.txt` asks crawlers to skip pages, `sitemap.xml` lists the pages you want found, and JSON-LD describes products in a way search engines can show.

## Common mistakes

**1. The same title on every page.**

```html
<title>Dhaaga &amp; Co.</title>
```

Symptom: search results show thirty identical blue links, and the tabs in a customer's browser all look the same. Fix: a title per page, with its own words first: `Golu the elephant, crochet amigurumi | Dhaaga &amp; Co.`.

**2. Product content only drawn by JavaScript.**

```html
<body><div id="app"></div><script type="module" src="/main.js"></script></body>
```

Symptom: WhatsApp shows a grey box, and Google indexes the page late or as empty. Fix: prerender each product page, so the HTML already holds the name, price, photo and og tags.

**3. A relative og:image.**

```html
<meta property="og:image" content="/img/DC-1042-1200.jpg">
```

Symptom: the preview shows a title but no picture. Fix: a full address, `https://dhaaga.in/img/DC-1042-1200.jpg`.

**4. Hash addresses for products.**

```text
https://dhaaga.in/#/products/DC-1042
```

Symptom: in search results, every product is the home page, because the server never sees what comes after `#`. Fix: real paths with History API routing, plus the host's fallback to `index.html` for paths that aren't files.

**5. Shipping the test site's robots.txt.**

```text
User-agent: *
Disallow: /
```

Symptom: weeks after launch, dhaaga.in is still missing from Google. The preview site's "crawl nothing" file went live too. Fix: keep `Disallow: /` only on preview deploys, and check `https://dhaaga.in/robots.txt` on launch day.
