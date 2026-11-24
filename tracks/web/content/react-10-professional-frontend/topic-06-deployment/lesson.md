## Hook

Cooking a great meal at home is one thing. Running a restaurant is another. The food must reach every table hot, the doors must open on time, and if a dish goes wrong you need to pull it off the menu fast.

Your store works on your laptop. Deploying is getting it to every customer's phone, quickly and safely, again and again. Done well, launch day is boring: you merge, the site updates in a minute, and if something breaks you undo it in one click.

## Concept

### What deploying a frontend means

```callout jargon
To **deploy** is to put a new version of your software where users can reach it. A **host** is the service that keeps your files online and answers requests for them.
```

A React app isn't a running program on a server, like the Java API. After the build, it's just files. In [Production builds and caching](/t/web-07-tooling/topic-10-production-builds) you ran the build:

```text
$ npm run build
dist/index.html                   0.62 kB
dist/assets/index-4f9a1c2b.js   148.31 kB
dist/assets/index-77de01aa.css    6.04 kB
```

Deploying a frontend is mostly: build these files, upload them, and point the address at them.

```animate
title: From merge to customers' phones
kind: scene
nodes:
  - { id: dev, label: You, emoji: "🧑‍💻", row: 0, col: 0 }
  - { id: github, label: GitHub, emoji: "🐙", row: 0, col: 2 }
  - { id: ci, label: CI build, emoji: "🤖", value: idle, row: 0, col: 4 }
  - { id: cdn, label: CDN, emoji: "🌍", value: "v41", row: 2, col: 4 }
  - { id: phone, label: Customer in Nagpur, emoji: "📱", row: 2, col: 1 }
steps:
  - say: You merge a pull request into main on GitHub.
    send: { from: dev, to: github, label: "merge" }
  - say: CI installs packages, runs the tests and builds the dist folder. Red tests stop the deploy here.
    send: { from: github, to: ci, label: "npm ci, test, build" }
    set: { ci: "green" }
  - say: CI uploads the new files to the host. The old version stays stored, ready for a rollback.
    send: { from: ci, to: cdn, label: "upload dist/" }
    set: { cdn: "v42 (v41 kept)" }
  - say: A customer opens dhaaga.in. The nearest CDN server answers in milliseconds.
    send: [{ from: phone, to: cdn, label: "GET /" }, { from: cdn, to: phone, label: "index.html v42", back: true }]
  - say: If v42 has a bug, one click points the address back at v41. Nothing is rebuilt.
    set: { cdn: "v41 again" }
    focus: [cdn]
```

### Static hosting and CDNs

Because the build is plain files, you don't need your own server. A **static host** serves them for you, often free for a small shop. Common choices are Netlify, Vercel, Cloudflare Pages, GitHub Pages, or Amazon S3 with CloudFront.

```callout jargon
A **CDN** (content delivery network) is many servers around the world that keep copies of your files. A customer in Nagpur gets them from a nearby server, not from one machine in Mumbai, so pages load faster.
```

Most static hosts put a CDN in front of your files automatically. They also connect to GitHub, run your build, and give you HTTPS.

### Caching: forever for assets, never for index.html

Vite names each asset after a **hash** of its content: `index-4f9a1c2b.js`. Change one character, and the name changes. That lets you cache very hard, with one exception.

```text
/assets/index-4f9a1c2b.js   Cache-Control: public, max-age=31536000, immutable
/index.html                 Cache-Control: no-cache
```

- **Hashed assets**: keep them for a year (31,536,000 seconds). A new version has a new name, so nobody gets a stale file.
- **`index.html`**: `no-cache` means "check with the server every time". It's tiny, and it points at the newest asset names.

If `index.html` were cached for a day, customers would keep loading yesterday's app, with yesterday's bugs, for a day after your fix.

### The refresh 404: send every page to index.html

A customer opens `https://dhaaga.in/products/DC-1042` from a WhatsApp link. In your router from [Pages and routing](/t/react-09-react-in-depth/topic-06-routing), that path is a screen drawn by React. On the host, though, there's no file called `products/DC-1042`.

Without help, the host answers **404 Not Found**. The fix is a **fallback rule**: any path that isn't a real file gets `index.html`, and React's router draws the right page.

```text
# Netlify's _redirects file
/*    /index.html   200
```

The `200` means "answer with index.html, and say it worked". Every host has its own way to write this rule: look for "SPA fallback" or "rewrites" in its docs.

If you use static generation from [Search engines and sharing links](/t/react-10-professional-frontend/topic-05-seo-and-sharing), each product already has its own HTML file, and the fallback only catches the rest.

### Environment variables are public

In [Configuration and environment variables](/t/web-07-tooling/topic-09-environment-config) you met `import.meta.env.VITE_API_URL`. Vite replaces it with its value **at build time**, inside the JavaScript file.

```jsx
const api = import.meta.env.VITE_API_URL;      // fine: everyone may know the API's address
const key = import.meta.env.VITE_UPI_SECRET;   // a leak: the secret is now inside a public file
```

Anyone can open DevTools and read your bundle. So:

- a `VITE_` variable is **configuration**, never a secret;
- payment keys, database passwords and admin tokens live on a **server**, and the frontend asks the server to act for it;
- different environments (preview, production) get different values from the host's settings, not from code.

### Preview deploys

```callout jargon
A **preview deploy** is a temporary copy of the site, built from one pull request, with its own address, like `pr-87--dhaaga.netlify.app`.
```

Most static hosts make one for every pull request, automatically. Reviewers open it on their phones and click through the change, instead of imagining it from the diff. Designers check it against their screens. Nothing touches the live store until the PR is merged.

### Rolling back

Every deploy is kept as an **immutable** version: once uploaded, it never changes. Going back is just pointing `dhaaga.in` at an older version. That takes seconds, and there's nothing to rebuild.

Rules for a calm launch day:

- **Deploy small and often.** A deploy with one change is easy to undo and easy to blame.
- **Know your rollback before you need it.** Practise it once, on a quiet day.
- **Roll back first, fix second.** When checkout breaks, customers can't wait for your perfect fix.
- **Be careful with shared things.** A rollback brings back old frontend code, but not an old database. If a release also changed the API, the old frontend must still work with the new API.

### Your domain and HTTPS

```callout jargon
**DNS** (Domain Name System) is the internet's phone book: it turns `dhaaga.in` into the address of the server to talk to. A **DNS record** is one line in that book, set where you bought the domain.
```

To put the store on `dhaaga.in`, you add the DNS records your host asks for, usually an `A` record or a `CNAME`. DNS changes can take minutes to hours to reach everyone, so do this days before launch, not on the morning.

**HTTPS** is not optional. Browsers mark plain `http://` pages "Not secure", and UPI apps and payment pages require it. Static hosts get and renew the certificate for you, usually from Let's Encrypt.

### After launch: watch it

Your tests passed, but real customers have old phones, odd browsers and slow networks. After every deploy, watch:

- **Errors from real browsers.** An error-tracking service, like Sentry, collects JavaScript errors from customers' phones with their stack traces. An [error boundary](/t/react-09-react-in-depth/topic-10-error-boundaries) can report to it.
- **Web Vitals** from real visits, as in [Fast pages](/t/react-10-professional-frontend/topic-03-performance).
- **Simple uptime checks**, which load the home page every minute and alert you if it fails.

## Worked example

Here is Dhaaga's deploy, written as the config for a static host. This example uses Netlify's `netlify.toml`; other hosts have the same ideas under different names.

```text
[build]
  command = "npm ci && npm test && npm run build"
  publish = "dist"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200

[[headers]]
  for = "/assets/*"
  [headers.values]
    Cache-Control = "public, max-age=31536000, immutable"

[[headers]]
  for = "/index.html"
  [headers.values]
    Cache-Control = "no-cache"
```

Line by line:

- **`command`** installs exactly the locked versions, runs the tests and builds. If a test fails, nothing is deployed.
- **`publish`** says which folder to put online.
- **`redirects`** is the SPA fallback. The host only uses it when no real file matches, so `/assets/...` and `/robots.txt` are still served as themselves.
- The two **`headers`** blocks are the caching rules: a year for hashed assets, a check every time for `index.html`.

`VITE_API_URL` is set in the host's settings, once for previews and once for production. No secret appears anywhere in this file.

## Try it

This program acts like the host: it picks which file answers each path, and how long the browser may keep it.

```js run
const files = ["/index.html", "/assets/index-4f9a1c2b.js", "/robots.txt"];

function respond(path) {
  const file = files.includes(path) ? path : "/index.html";
  const cache = file.startsWith("/assets/") ? "max-age=31536000, immutable" : "no-cache";
  return `${path} -> ${file} (${cache})`;
}

for (const path of ["/", "/products/DC-1042", "/assets/index-4f9a1c2b.js", "/robots.txt"]) {
  console.log(respond(path));
}
```

It prints:

```text
/ -> /index.html (no-cache)
/products/DC-1042 -> /index.html (no-cache)
/assets/index-4f9a1c2b.js -> /assets/index-4f9a1c2b.js (max-age=31536000, immutable)
/robots.txt -> /robots.txt (no-cache)
```

1. Ask for `/assets/index-0ld0ld00.js`, a file from last week's deploy that no longer exists. What does this host send back, and why would that break a customer's page in a confusing way?
2. Change the rule so a missing path under `/assets/` answers `404` instead.

## Recap

- A built React app is plain files: `index.html` plus hashed assets. Deploying means building them in CI, uploading them to a static host or CDN, and pointing your domain at them.
- Cache hashed assets for a year, and `index.html` with `no-cache`, so every deploy reaches customers straight away.
- Add a fallback that sends unknown paths to `index.html`, so links like `/products/DC-1042` work on refresh.
- `VITE_` variables are baked into a public file. Never put secrets in the frontend.
- Use preview deploys for every pull request, keep old versions for one-click rollbacks, set up DNS and HTTPS early, and watch errors and Web Vitals after every deploy.

## Common mistakes

**1. No SPA fallback.**

```text
GET https://dhaaga.in/products/DC-1042   404 Not Found
```

Symptom: the home page works, but every shared product link and every refresh shows the host's 404 page. Fix: add the host's rewrite rule that sends unknown paths to `/index.html` with status 200.

**2. Caching index.html.**

```text
/index.html   Cache-Control: public, max-age=86400
```

Symptom: you deploy a fix, but customers keep getting the broken version for up to a day. Fix: `Cache-Control: no-cache` for `index.html`. Only hashed assets get the long cache.

**3. A secret in a VITE_ variable.**

```jsx
fetch(url, { headers: { Authorization: `Bearer ${import.meta.env.VITE_ADMIN_TOKEN}` } });
```

Symptom: anyone who opens DevTools can read the admin token from the bundle, and use it. Fix: move the call to a server you control. The frontend sends the customer's request there, and the server holds the token.

**4. Setting up the domain on launch morning.**

```text
dhaaga.in   CNAME   dhaaga.netlify.app   (added at 9:40, launch at 10:00)
```

Symptom: at 10:00 some customers see the new store, others see an error, depending on how fast their DNS updates. Fix: connect the domain and HTTPS days before launch, and check it from mobile data, not only office Wi-Fi.

**5. Fixing forward while checkout is broken.**

```text
10:05 checkout broken -> 10:20 hotfix #1 -> 10:40 hotfix #2 -> 11:10 working
```

Symptom: an hour of lost Diwali orders while you rush fixes, each one a fresh risk. Fix: roll back to the last good version in one click, then fix calmly and deploy again.
