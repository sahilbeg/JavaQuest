// The caching logic of dhaaga.in's service worker, written as plain functions so the checks can test it.
// caches, cache and fetchFn are passed in: in sw.js they are the browser's real ones.

export const CACHE = "dhaaga-v2";
export const SHELL = ["/", "/index.html", "/main.js", "/style.css"];

/** "network-first" for API requests (paths starting /api/), "cache-first" for everything else. */
export function chooseStrategy(path) {
  // TODO 1: return "network-first" when path starts with "/api/", and "cache-first" otherwise.
  return "";
}

/** On install: opens the CACHE cache and stores every url of the app shell. */
export async function precache(caches, urls) {
  // TODO 2: open the cache named CACHE with await caches.open(CACHE), then await cache.addAll(urls).
}

/** On activate: deletes every cache whose name isn't keep. Returns the deleted names. */
export async function removeOldCaches(caches, keep) {
  // TODO 3: get every name with await caches.keys(). Delete each one that isn't keep with caches.delete(name),
  //         wait for all the deletes, and return the list of deleted names.
  return [];
}

/** The saved copy if there is one. Otherwise fetches, saves a copy of a good answer, and returns it. */
export async function cacheFirst(path, cache, fetchFn) {
  // TODO 4: return await cache.match(path) when it isn't undefined. Otherwise await fetchFn(path);
  //         when response.ok, save a copy with cache.put(path, response.clone()). Return the response.
  return null;
}

/** Fetches first, saving a copy of a good answer. When the network fails, the saved copy; with none, the error. */
export async function networkFirst(path, cache, fetchFn) {
  // TODO 5: in try, await fetchFn(path), save a copy with cache.put when response.ok, and return the response.
  //         In catch, return the saved copy from cache.match(path) if there is one; otherwise throw the error again.
  return null;
}
