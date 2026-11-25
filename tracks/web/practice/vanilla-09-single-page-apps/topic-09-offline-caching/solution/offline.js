// The caching logic of dhaaga.in's service worker, written as plain functions so the checks can test it.
// caches, cache and fetchFn are passed in: in sw.js they are the browser's real ones.

export const CACHE = "dhaaga-v2";
export const SHELL = ["/", "/index.html", "/main.js", "/style.css"];

/** "network-first" for API requests (paths starting /api/), "cache-first" for everything else. */
export function chooseStrategy(path) {
  return path.startsWith("/api/") ? "network-first" : "cache-first";
}

/** On install: opens the CACHE cache and stores every url of the app shell. */
export async function precache(caches, urls) {
  const cache = await caches.open(CACHE);
  await cache.addAll(urls);
}

/** On activate: deletes every cache whose name isn't keep. Returns the deleted names. */
export async function removeOldCaches(caches, keep) {
  const names = await caches.keys();
  const old = names.filter((name) => name !== keep);
  await Promise.all(old.map((name) => caches.delete(name)));
  return old;
}

/** The saved copy if there is one. Otherwise fetches, saves a copy of a good answer, and returns it. */
export async function cacheFirst(path, cache, fetchFn) {
  const cached = await cache.match(path);
  if (cached !== undefined) {
    return cached;
  }
  const response = await fetchFn(path);
  if (response.ok) {
    await cache.put(path, response.clone());
  }
  return response;
}

/** Fetches first, saving a copy of a good answer. When the network fails, the saved copy; with none, the error. */
export async function networkFirst(path, cache, fetchFn) {
  try {
    const response = await fetchFn(path);
    if (response.ok) {
      await cache.put(path, response.clone());
    }
    return response;
  } catch (error) {
    const cached = await cache.match(path);
    if (cached !== undefined) {
      return cached;
    }
    throw error;
  }
}
