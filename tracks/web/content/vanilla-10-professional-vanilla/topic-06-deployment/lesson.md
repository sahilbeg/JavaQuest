## Hook

When Dhaaga changed the booties' price, Raju repainted the board at the Aundh shop. But copies of the old price list were still stuck on fridges all over Pune, and customers kept quoting them.

The web has fridges too: browser caches, CDN caches and service workers. Deploying is not just uploading new files. It's making sure every customer gets the new ones quickly, that a refresh never breaks, and that you can undo a bad release in a minute.

## Concept

### A static site is just files

```callout jargon
A **static site** is a website made only of files (HTML, CSS, JavaScript, images) that the server sends exactly as they are. Nothing runs on the server for each visit. A **static host** is a service that stores those files and serves them.
```

A plain-JavaScript store fits this perfectly. You might upload the `src` files as they are, or run a build first, like the Vite build from [Production builds and caching](/t/web-07-tooling/topic-10-production-builds). Either way, what goes online is a folder of files.

Static hosts such as Netlify, Cloudflare Pages, GitHub Pages, or Amazon S3 with CloudFront serve those files from a **CDN**: copies on servers all over the world, so a customer in Guwahati gets them from somewhere close. Most of them also connect to GitHub, run your build, and handle HTTPS for you.

### Cache busting: a new name for new content

Browsers keep downloaded files in their cache, so the next visit is fast. That's great until you change a file: a customer with yesterday's `main.js` keeps running yesterday's code.

```callout jargon
**Cache busting** means giving a file a new address whenever its content changes, so no cache can serve the old one. The usual way is a **content hash** in the name: `main-7c1e2a.js`. Change one character, and the build gives it a new name.
```

```text
before the fix   index.html → main-7c1e2a.js   (booties ₹399)
after the fix    index.html → main-b04f9d.js   (booties ₹350)
```

The new `index.html` points at the new name. A browser has never seen `main-b04f9d.js`, so it has to download it fresh.

Without a build step, you can do the same by hand with a version in the address: `main.js?v=42`. A build does it for you, and never forgets.

### Syntax: Cache-Control headers

```text
/assets/*
  Cache-Control: public, max-age=31536000, immutable
/*.html
  Cache-Control: no-cache
```

Many static hosts, including Netlify and Cloudflare Pages, read a file called `_headers` written like this. Each path pattern gets the headers under it.

```callout jargon
**Cache-Control** is a response header that tells browsers and CDNs how long they may reuse a file. `max-age=31536000` means one year in seconds. `immutable` means "this file will never change, don't even check". `no-cache` means "you may keep it, but ask the server before every use".
```

- Hashed files can be cached for a year, because new content always gets a new name.
- HTML files must be `no-cache`. They're tiny, and they're the ones that point at the newest names.

### Real paths need a fallback

The single-page app routing from [Routing with the History API](/t/vanilla-09-single-page-apps/topic-03-history-routing) gives addresses like `/orders/order-17`. Clicking around works. But a refresh asks the host for a file called `orders/order-17`, and there isn't one.

```text
/*   /index.html   200
```

This line, in a `_redirects` file, is the **fallback**: any path that isn't a real file is answered with `index.html`, and status `200`, so your router draws the right screen. Real files, like `/assets/main-b04f9d.js` or a prerendered `/products/DC-1042/index.html`, are still served as themselves.

### Service workers and updates

In [Offline: caching with a service worker](/t/vanilla-09-single-page-apps/topic-09-offline-caching), you cached the store's files so it works offline. A service worker sits between the page and the network, so it can keep serving its old copies long after you deploy.

```animate
title: A release reaches a customer with a service worker
kind: scene
nodes:
  - { id: phone, label: Customer's phone, emoji: "📱", value: "v1 page", row: 0, col: 0 }
  - { id: sw, label: Service worker, emoji: "🛡️", value: "cache dhaaga-v1", row: 0, col: 2 }
  - { id: host, label: Static host, emoji: "🌍", value: "v1", row: 0, col: 4 }
steps:
  - say: You deploy v2 with the new booties price. The host now serves v2.
    set: { host: "v2" }
    focus: [host]
  - say: The customer opens the store. The old service worker answers from its cache, so they still see v1.
    send: [{ from: phone, to: sw, label: "GET /" }, { from: sw, to: phone, label: "v1 from cache", back: true }]
  - say: In the background, the browser checks sw.js on the host and finds a changed file.
    send: { from: sw, to: host, label: "check sw.js" }
  - say: The new worker installs and fills a new cache, dhaaga-v2. It waits until the old pages close.
    set: { sw: "v2 waiting" }
  - say: On the next visit, v2 activates, deletes the old dhaaga-v1 cache, and serves the new price.
    send: { from: sw, to: phone, label: "v2 page", back: true }
    set: { sw: "cache dhaaga-v2", phone: "v2 page" }
```

Three rules keep this safe:

- **Change `sw.js` on every release**, for example the cache name `dhaaga-v2`. The browser only installs a new worker when `sw.js` itself changes.
- **Delete old caches** in the new worker's `activate` event.
- **Serve `sw.js` with `no-cache`**, so the browser always sees the newest one.

Some stores also show a small "A new version is ready. Refresh?" message when a new worker is waiting.

### Security headers

The host is also where you switch on browser protections, with more headers in `_headers`:

```text
/*
  Content-Security-Policy: script-src 'self'; object-src 'none'; base-uri 'self'
  Strict-Transport-Security: max-age=31536000
  X-Content-Type-Options: nosniff
```

- **Content-Security-Policy** is the second wall against XSS from [Security](/t/vanilla-10-professional-vanilla/topic-03-security).
- **Strict-Transport-Security** (HSTS) tells browsers to use only HTTPS for dhaaga.in from now on.
- **X-Content-Type-Options: nosniff** stops browsers from guessing that a text file is a script.

Turn on a strict CSP on a preview deploy first, and click through every page. A missed inline script breaks quietly.

### Previews, checks and rollbacks

```callout jargon
A **preview deploy** is a temporary copy of the site built from one pull request, at its own address. A **rollback** is switching the live site back to an earlier deploy.
```

A calm release process looks like this:

1. Every pull request runs the checks (tests, lint, build) in CI, and gets a preview link to click through on a real phone.
2. Merging to `main` builds and deploys automatically. Nobody drags folders from a laptop.
3. The host keeps every deploy. If checkout breaks, you roll back first, in one click, and fix calmly after.
4. Change your domain's DNS records days before launch, never on launch morning.

## Worked example

Here is Dhaaga's host config after the price incident. The build puts hashed files in `dist/assets/`, prerendered product pages in `dist/products/`, and `sw.js` at the root.

```text
# _headers
/assets/*
  Cache-Control: public, max-age=31536000, immutable
/*.html
  Cache-Control: no-cache
/sw.js
  Cache-Control: no-cache
/*
  Content-Security-Policy: script-src 'self'; object-src 'none'; base-uri 'self'
  Strict-Transport-Security: max-age=31536000
  X-Content-Type-Options: nosniff

# _redirects
/*   /index.html   200
```

Line by line:

- Hashed assets are kept for a year. The booties fix gets a new file name, so nobody is stuck with ₹399.
- HTML files and `sw.js` are checked every time, so a release reaches customers on their next visit.
- The `/*` block adds the security headers to every response.
- `_redirects` answers unknown paths with `index.html`, so refreshing `/orders/order-17` works. Prerendered product pages are real files, so they don't need it.

In `sw.js`, the release also bumps `const CACHE = "dhaaga-v2";`, and the `activate` handler deletes every cache whose name isn't `CACHE`.

## Try it

This works out which price three customers see after the ₹350 release, depending on how long their browser may keep `index.html`. Run it.

```js run
const deploys = { v1: "booties ₹399", v2: "booties ₹350" };
const live = "v2";
const customers = [
  { name: "first visit", cached: null, hoursAgo: 0, htmlMaxAgeHours: 0 },
  { name: "html cached 24 h", cached: "v1", hoursAgo: 5, htmlMaxAgeHours: 24 },
  { name: "html no-cache", cached: "v1", hoursAgo: 5, htmlMaxAgeHours: 0 },
];

for (const customer of customers) {
  const usesCache = customer.cached !== null && customer.hoursAgo < customer.htmlMaxAgeHours;
  const version = usesCache ? customer.cached : live;
  console.log(`${customer.name}: ${deploys[version]}`);
}
```

It prints:

```text
first visit: booties ₹350
html cached 24 h: booties ₹399
html no-cache: booties ₹350
```

The middle customer's browser may keep yesterday's `index.html` for a whole day, and that old page points at the old JavaScript.

1. Change the middle customer's `hoursAgo` to `30`. Which price do they see now, and why?
2. Add a customer whose service worker still holds `v1`. What extra rule would the program need?

## Recap

- A static site is a folder of files, served by a static host from a CDN. Build it in CI, never by hand.
- Give JavaScript and CSS hashed names, cache them for a year with `immutable`, and serve HTML with `no-cache`.
- With History API routes, add a fallback that answers unknown paths with `index.html` and status 200.
- A service worker serves its own copies: change `sw.js` and its cache name every release, delete old caches on `activate`, and serve `sw.js` with `no-cache`.
- Add security headers at the host, check every pull request on a preview deploy, and roll back first when a release breaks.

## Common mistakes

**1. Caching index.html for a day.**

```text
/*.html
  Cache-Control: public, max-age=86400
```

Symptom: you fix a price, but customers keep the old page, and the old JavaScript it points at, for up to a day. Fix: `Cache-Control: no-cache` for HTML.

**2. A long cache on a file whose name never changes.**

```text
/main.js
  Cache-Control: public, max-age=31536000
```

Symptom: after a release, returning customers run last year's `main.js` against today's HTML, and buttons break. Fix: hashed names (`main-b04f9d.js`) for anything cached long, or a short cache for fixed names.

**3. No fallback for History API routes.**

```text
GET https://dhaaga.in/orders/order-17   404 Not Found
```

Symptom: clicking to an order works, but refreshing it or opening a shared link shows the host's 404 page. Fix: `/*  /index.html  200` in the host's redirects.

**4. A service worker that never changes.**

```js
const CACHE = "dhaaga";
```

Symptom: customers who installed the worker keep seeing the old site for weeks, whatever you deploy. Fix: a new cache name each release (`dhaaga-v2`), and delete old caches in `activate`.

**5. Deploying by dragging a folder from a laptop.**

```text
dhaaga-final-FINAL-2/   dragged onto the host's upload page at 9:55
```

Symptom: the live site holds a half-finished change that never ran the tests, and nobody can tell which commit it was. Fix: deploy only from `main`, through CI, after the checks pass.
