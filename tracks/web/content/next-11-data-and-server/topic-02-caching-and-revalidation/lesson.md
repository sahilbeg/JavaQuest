## Hook

Meera Aunty keeps a price board at the stall. She doesn't ring the yarn supplier every time a customer asks a price. She writes it once and reads it off the board all day. But the stock box is different. "How many elephants are left?" She opens the box and counts, every time.

Your server has the same choice for every piece of data. Keep a copy and answer fast, or ask again and be sure. Get it wrong, and the website sells an elephant that's already gone.

## Concept

### What a cache is

```callout jargon
A **cache** is a saved copy of an answer, kept so the next request can skip the slow trip. A **cache hit** means the copy was used. A **cache miss** means there was no copy, so the server asked for real.
```

Copies make pages fast and save the API work. The danger is a copy that's out of date. That's called **stale** data, the word you met in react-09.

### In Next.js 16, fetch is not cached unless you ask

By default, a `fetch` on the server keeps no copy of its own. You opt in to caching, one request at a time, with fetch's second argument.

There is one catch. A page that reads nothing about the request, like cookies, may be built once, when the site is built, with the answer baked in. `cache: "no-store"` rules that out: the API is asked on every request.

Asking every time is right for stock. It's wasteful for the catalogue, which changes a few times a month.

### Syntax: fetch caching options

```js
fetch(url, { cache: "no-store" });
fetch(url, { cache: "force-cache" });
fetch(url, { next: { revalidate: 3600 } });
fetch(url, { next: { tags: ["catalogue"] } });
```

- `cache: "no-store"`: never keep a copy. Ask the API on every request.
- `cache: "force-cache"`: keep a copy and use it until someone says it changed.
- `next: { revalidate: 3600 }`: keep a copy for at most 3600 seconds (an hour).
- `next: { tags: [...] }`: give the copy names, so you can throw it away later by name.

You can mix them: `{ next: { revalidate: 3600, tags: ["catalogue"] } }`.

### Revalidate: a copy with an expiry time

```callout jargon
To **revalidate** is to fetch fresh data to replace a cached copy.
```

With `revalidate: 3600`, a copy older than an hour isn't thrown away at once. The next request still gets the old copy, fast, while Next.js fetches a new one behind the scenes. The request after that gets the new one. This trick is called **stale-while-revalidate**.

Here is the idea in plain JavaScript, with the time passed in as seconds:

```js run
const cache = new Map();

function apiPrice(now) {
  return now < 3000 ? 89900 : 79900; // Meera lowers the tote bag price at second 3000
}

function cachedPrice(now, revalidate) {
  const entry = cache.get("BAG");
  if (!entry) {
    cache.set("BAG", { paise: apiPrice(now), at: now });
    return `${cache.get("BAG").paise} (asked the API)`;
  }
  if (now - entry.at >= revalidate) {
    cache.set("BAG", { paise: apiPrice(now), at: now });
    return `${entry.paise} (stale, refreshing behind the scenes)`;
  }
  return `${entry.paise} (from the cache)`;
}

console.log(cachedPrice(0, 3600));
console.log(cachedPrice(3500, 3600));
console.log(cachedPrice(3700, 3600));
console.log(cachedPrice(3800, 3600));
```

Notice that the new price showed up late. For up to an hour, and one more visit, customers saw ₹899. Time-based revalidation is fine for things that may be a little old. It's wrong for stock.

### Tags: throw a copy away the moment it changes

Waiting an hour is too slow when Meera Aunty changes a price. Instead, you tag the cached data, and when the price changes, you tell Next.js "everything tagged `catalogue` is stale now".

```animate
title: A price change clears the catalogue copy
kind: scene
nodes:
  - { id: phone, label: Customer, emoji: "📱" }
  - { id: next, label: Next.js server, emoji: "🖥️" }
  - { id: cache, label: Cache, emoji: "🗃️", value: empty }
  - { id: api, label: Dhaaga API, emoji: "🗄️" }
steps:
  - say: The first customer opens the catalogue. The cache is empty, so this is a miss.
    send: [{ from: phone, to: next, label: "GET /items" }, { from: next, to: api, label: "items, tag catalogue" }]
  - say: The API answers. Next.js keeps a copy, named with the tag catalogue.
    send: { from: api, to: next, label: "BAG ₹899", back: true }
    set: { cache: "catalogue: BAG ₹899" }
  - say: The next customer gets the copy. That's a hit, and the API isn't asked at all.
    send: { from: next, to: phone, label: "BAG ₹899 (from cache)", back: true }
  - say: Meera Aunty lowers the price. The admin code calls revalidateTag("catalogue", "max").
    set: { cache: "catalogue: stale" }
    focus: [cache]
  - say: The next visit gets the old copy one last time while Next.js fetches the new price behind it.
    send: { from: next, to: api, label: "items again" }
  - say: The fresh copy replaces the stale one. Everyone after this sees ₹799.
    send: { from: api, to: next, label: "BAG ₹799", back: true }
    set: { cache: "catalogue: BAG ₹799" }
```

### Syntax: revalidating on demand

```js
import { revalidatePath, revalidateTag } from "next/cache";

revalidateTag("catalogue", "max");
revalidatePath("/items/DC-3007");
```

- `revalidateTag(tag, "max")` marks every copy with that tag as stale, on every page that uses it. `"max"` is the recommended profile: the next visit gets the old copy fast while the new one loads.
- `revalidatePath(path)` refreshes one page, like a single item page.
- You call them on the server, in a server action or a route handler, right after the data changes.

```callout tip
Inside a server action there's also `updateTag("catalogue")`. It throws the copy away at once, so the very next request waits for fresh data. Use it when the person who just made a change must see it straight away, like a customer who placed an order.
```

### Syntax: the use cache directive

Not every slow thing is a `fetch`. A database query or a heavy calculation can be cached too. In Next.js 16, you turn on Cache Components in `next.config.js` with `cacheComponents: true`. Then a function can say `"use cache"` at its top:

```js
import { cacheLife, cacheTag } from "next/cache";

export async function getBestSellers() {
  "use cache";
  cacheLife("hours");
  cacheTag("catalogue");
  return db.query("best sellers this week");
}
```

```callout jargon
A **directive** is a string at the top of a file or function, like `"use client"` or `"use cache"`, that tells Next.js how to treat that code.
```

- `"use cache"` saves the function's result, keyed by its arguments.
- `cacheLife("hours")` sets how long the copy lives, using a ready-made profile like `"minutes"`, `"hours"` or `"days"`.
- `cacheTag("catalogue")` names it, so `revalidateTag("catalogue", "max")` clears it too.

### Memoization: one request, asked once

There is one more, smaller kind of saving. During a single page render, two components may ask for the same URL with the same options. Next.js sends that GET request only once and shares the answer.

```callout jargon
**Memoization** means remembering a function's answer for the same input, for a short time. Here it lasts for one render of one page, then it's forgotten.
```

So a layout and a page can both call `getCatalogue()` without worrying. It is not a cache between customers: the next visit starts fresh. For database calls that aren't `fetch`, React's `cache()` function does the same job.

### Choosing for each piece of data

| Data | Changes | Choice |
|---|---|---|
| Stock | every sale | `cache: "no-store"` |
| Catalogue and prices | a few times a month | cached, `revalidate: 3600`, tag `catalogue` |
| Shop hours | a few times a year | `cache: "force-cache"`, tag `hours` |

Ask two questions. How bad is it if this is old? And who can tell the server when it changes?

## Worked example

Kavya's version of the API file and the code that runs after an admin edit.

```js
const API = "https://api.dhaaga.in";

async function getJson(path, options) {
  const res = await fetch(`${API}${path}`, options);
  if (!res.ok) throw new Error(`Dhaaga API answered ${res.status} for ${path}`);
  return res.json();
}

export function getCatalogue() {
  return getJson("/items", { next: { revalidate: 3600, tags: ["catalogue"] } });
}

export function getStock() {
  return getJson("/stock", { cache: "no-store" });
}
```

```js
import { revalidatePath, revalidateTag } from "next/cache";

export function afterPriceChange(itemId) {
  revalidateTag("catalogue", "max");
  revalidatePath(`/items/${itemId}`);
}
```

- The catalogue is cached for an hour at most, and named `catalogue`.
- Stock is fetched on every request, so it's never a sale behind.
- When a price changes, `afterPriceChange("DC-3007")` marks the catalogue stale everywhere, and refreshes the tote bag's own page.

## Try it

In the fix, look at each fetch and say out loud how old its data may be. Then run the checks. The stock check names the option it expects. Afterwards, change the catalogue's `revalidate` to `60` and run them again: the check tells you the catalogue should be cached for an hour.

## Recap

- In Next.js 16, a server `fetch` is not cached unless you opt in.
- `cache: "no-store"` always asks. `cache: "force-cache"` keeps a copy. `next: { revalidate: N }` keeps it for at most N seconds.
- Tag cached data with `next: { tags: [...] }` or `cacheTag`, then clear it with `revalidateTag(tag, "max")` when it changes. `revalidatePath` refreshes one page.
- `"use cache"` with `cacheLife` caches any function, not only `fetch`.
- Within one render, the same GET is sent only once: that's memoization, not a cache between customers.

## Common mistakes

**1. Caching stock.**

```js
export function getStock() {
  return getJson("/stock", { cache: "force-cache" });
}
```

Nothing breaks, and nothing warns you. The site just shows this morning's stock all day, and sells elephants that are gone.

Fix: `{ cache: "no-store" }` for anything that must be true right now.

**2. Revalidating a tag nobody set.**

```js
getJson("/items", { next: { revalidate: 3600 } });
// later
revalidateTag("catalogue", "max");
```

The call runs without an error, but nothing happens. The copy has no tag, so it waits out its hour.

Fix: tag the fetch: `{ next: { revalidate: 3600, tags: ["catalogue"] } }`.

**3. Calling revalidateTag with one argument.**

```js
revalidateTag("catalogue");
```

This older form is deprecated in Next.js 16, and TypeScript reports a missing argument. It also throws the copy away with no stale fallback, so the next customer waits.

Fix: `revalidateTag("catalogue", "max")`, or `updateTag("catalogue")` inside a server action.

**4. Expecting memoization to save the API between visits.**

```jsx
const items = await getCatalogue(); // with no cache options
```

Two components in one page share one request, so it looks cached. But every new visitor asks the API again, and on sale day the API slows down.

Fix: add real caching options when the data can be a little old.
