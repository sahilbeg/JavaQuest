## Hook

Raju keeps a printed price list in the stall's cash box. When the phone has no signal, he still knows that the tote bag is ₹899. It might be a day old, but it is far better than nothing.

A website can keep its own printed list. A small helper in the browser saves copies of the pages and data it fetched. When the signal drops, it answers from those copies. This ticket builds that helper's brain.

## Concept

### The service worker

```callout jargon
A **service worker** is a script the browser runs in the background, apart from your page. It sits between the page and the network, and can answer the page's requests itself, for example from saved copies.
```

- It is a separate file, such as `sw.js`, that the page **registers** once.
- It has no `document`: it can't touch the page, only requests and responses.
- It keeps working for that site after the first visit, even when the network is gone.
- It only works over HTTPS, or on `localhost` while you develop.

### Syntax: registering a service worker

```js
if ("serviceWorker" in navigator) {
  navigator.serviceWorker.register("/sw.js", { type: "module" });
}
```

The page asks the browser to install `sw.js`. `type: "module"` lets the worker use `import`. The `if` skips old browsers that have no service workers, and the site simply works online only there.

### The life of a service worker

```animate
title: Install, activate, then answer requests
kind: scene
nodes:
  - { id: page, label: The page, emoji: "📄" }
  - { id: sw, label: sw.js, emoji: "🛠️", value: new }
  - { id: cache, label: Cache dhaaga-v2, emoji: "🗄️", value: empty }
  - { id: net, label: Network, emoji: "📶", value: online }
steps:
  - say: On the first visit, the page registers sw.js. The browser fires install in the worker.
    send: { from: page, to: sw, label: register }
    set: { sw: installing }
  - say: On install, the worker downloads the app shell and saves it in the dhaaga-v2 cache.
    send: { from: net, to: cache, label: "/, main.js, style.css" }
    set: { cache: "app shell" }
  - say: Then activate fires. The worker deletes caches from older versions, like dhaaga-v1.
    set: { sw: active }
  - say: From now on, every request the page makes passes through the worker's fetch event.
    send: { from: page, to: sw, label: "GET /main.js" }
  - say: Next Sunday, the signal drops. The worker answers from the cache, and the store still opens.
    set: { net: offline }
    send: { from: cache, to: page, label: "main.js (saved)", back: true }
```

### Syntax: the three events

```js
self.addEventListener("install", (event) => {
  event.waitUntil(precache(caches, SHELL));
});
self.addEventListener("activate", (event) => {
  event.waitUntil(removeOldCaches(caches, CACHE));
});
self.addEventListener("fetch", (event) => {
  event.respondWith(answer(event.request));
});
```

- Inside a worker, `self` is the worker itself, like `window` in a page.
- `event.waitUntil(promise)` tells the browser not to move on until the promise settles, so the shell is fully saved before the worker goes live.
- `event.respondWith(promise)` answers the page's request with the response the promise gives.

### The Cache API

```callout jargon
The **Cache API** is the browser's store for saved responses. `caches.open(name)` gives one named cache. In it, `cache.match(url)` finds a saved response, or `undefined`. `cache.put(url, response)` saves one, and `cache.addAll(urls)` fetches and saves several.
```

A response's body can be read only once. If the worker saves a response and also gives it to the page, one of them gets an empty body. So save a copy: `cache.put(url, response.clone())`.

### Cache first, for things that rarely change

The app shell (`/`, `main.js`, `style.css`) and photos change only when you deploy. Answer them from the cache straight away, and fetch only what isn't saved yet:

```js run
const saved = new Map([["/main.js", "app code (saved)"]]);
let online = false;

async function pretendFetch(path) {
  if (!online) throw new TypeError("Failed to fetch");
  return `${path} (fresh)`;
}

async function cacheFirst(path) {
  if (saved.has(path)) return saved.get(path);
  const fresh = await pretendFetch(path);
  saved.set(path, fresh);
  return fresh;
}

async function main() {
  console.log(await cacheFirst("/main.js"));
  try {
    await cacheFirst("/images/golu.jpg");
  } catch (error) {
    console.log(`no copy: ${error.message}`);
  }
  online = true;
  console.log(await cacheFirst("/images/golu.jpg"));
  online = false;
  console.log(await cacheFirst("/images/golu.jpg"));
}

main();
```

Offline, the saved `main.js` still works, but Golu's photo was never saved, so it fails. Once fetched online, the photo is saved, and the last line finds it even offline.

### Network first, for data that changes

Prices and stock change during the day, so a saved copy is only a backup. Try the network. If it answers, save a fresh copy. If the fetch **throws**, use the saved copy:

```animate
title: Network first for the item list
kind: code
language: js
code: |
  const saved = { "/api/items": "list from 9 am" };
  function networkFirst(path, online) {
    if (online) {
      saved[path] = "list from now";
      return saved[path];
    }
    return saved[path] ?? "error";
  }
  console.log(networkFirst("/api/items", true));
  console.log(networkFirst("/api/items", false));
  console.log(networkFirst("/api/orders", false));
steps:
  - line: 1
    objects: { s: { type: Object, fields: { "/api/items": '"list from 9 am"' } } }
    vars: { saved: "@s" }
    say: The worker saved the item list this morning.
  - line: 9
    say: At noon Raju is online. The network answers first.
  - line: 4
    objects: { s: { type: Object, fields: { "/api/items": '"list from now"' } } }
    say: The fresh list replaces the saved copy, so the backup stays as new as possible.
  - line: 9
    out: "list from now"
    say: Raju sees today's prices.
  - line: 10
    say: The signal drops. The fetch fails, so the worker turns to its saved copy.
  - line: 10
    out: "list from now"
    say: He still sees the list from noon, which is far better than the dinosaur page.
  - line: 11
    out: "error"
    say: Orders were never saved, so with no network there is nothing to show. The page shows its error state.
```

```callout tip
A `500` answer is not a dropped connection: the server did answer. Pass it to the page, which shows its error state, and don't let it replace a good saved copy.
```

### Versioning your caches

When you deploy a new `main.js`, the old one is still in the cache, and cache-first would serve it forever. So name each cache with a version, such as `dhaaga-v2`. A new deploy uses `dhaaga-v3`: its install saves the new shell, and its activate deletes every cache whose name isn't `dhaaga-v3`.

### What a worker should not cache

- **Anything that isn't GET.** A saved `POST /api/orders` would place no order at all.
- **Other websites' requests,** such as a payment page. Let them pass through.
- **Error answers,** like a 404. Saving them would serve the error forever.

## Worked example

The worker's fetch event picks a strategy for each request:

```js
self.addEventListener("fetch", (event) => {
  const url = new URL(event.request.url);
  if (event.request.method !== "GET" || url.origin !== self.location.origin) {
    return;
  }
  const strategy = url.pathname.startsWith("/api/") ? networkFirst : cacheFirst;
  event.respondWith(caches.open(CACHE).then((cache) => strategy(url.pathname, cache, (path) => fetch(path))));
});
```

1. It ignores anything that isn't a GET, and anything for another website. Returning without `respondWith` lets the browser handle it normally.
2. API data is network-first. Everything else is cache-first.
3. `respondWith` gets a promise: open the cache, then run the strategy.
4. The strategy gets the cache and a fetch function passed in. That is what lets the checks test it with pretend ones.

## Try it

In the cache-first example above, add a `networkFirst(path)` function that tries `pretendFetch` first, saves the answer, and falls back to `saved` in `catch`. Call it with `online` set to `true`, then to `false`.

## Recap

- A service worker is a background script between the page and the network. Register it once, and it can answer requests itself, even offline.
- On install, save the app shell. On activate, delete old versions' caches. On fetch, answer with `respondWith`.
- Cache first suits the app shell and photos. Network first suits data like prices and stock.
- Save `response.clone()`, and only for good answers to GET requests from your own site.
- Put a version in the cache name, so each deploy replaces the old files.

## Common mistakes

**1. Saving the response instead of a copy.**

```js
async function save(cache, path, response) {
  await cache.put(path, response);
  return response;
}
```

The cache reads the body, so the page gets an empty one, or `TypeError: Body is unusable: Body has already been read`. Fix: save `response.clone()`, and return the original.

**2. Caching the API with cache first.**

```js
const strategy = cacheFirst;
```

The first item list ever fetched is served forever, so the site shows Golu in stock long after he sold out. Fix: use network first for `/api/` paths.

**3. Never changing the cache name.**

```js
const CACHE = "dhaaga";
```

After a deploy, customers keep getting the old `main.js` from the cache, with old bugs, until they clear their browser data. Fix: version the name, such as `dhaaga-v3`, and delete other names on activate.

**4. Saving error answers.**

```js
async function cacheFirstNoCheck(cache, path) {
  const response = await fetch(path);
  await cache.put(path, response.clone());
  return response;
}
```

One 404 during a deploy is saved and served for good, even after the file exists. Fix: save only when `response.ok` is true.
