# Offline: caching with a service worker

Make dhaaga.in open, and show the last known catalogue, when the connection drops. Change only `src/offline.js`. `src/sw.js` is the finished service worker: it calls your functions with the browser's real `caches` and `fetch`. The checks call them with pretend ones, because jsdom has no service workers.

1. **TODO 1:** `chooseStrategy(path)` gives `"network-first"` for paths starting with `/api/`, and `"cache-first"` for everything else.
2. **TODO 2:** `precache(caches, urls)` opens the cache named `CACHE` and stores every url with `cache.addAll(urls)`.
3. **TODO 3:** `removeOldCaches(caches, keep)` deletes every cache whose name isn't `keep`, and returns the deleted names.
4. **TODO 4:** `cacheFirst(path, cache, fetchFn)` returns the saved copy if `cache.match(path)` finds one. Otherwise it fetches, saves `response.clone()` when `response.ok`, and returns the response.
5. **TODO 5:** `networkFirst(path, cache, fetchFn)` fetches first and saves a clone of a good answer. If the fetch **throws**, it returns the saved copy, or throws the error on when there is none.

An error answer, like a 404 or a 500, is returned to the page but never saved.
