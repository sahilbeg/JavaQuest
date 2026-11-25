// Round 3, frontend design: "Product pages ask for stock again and again. Add a small cache in front of the API."

/**
 * Wraps fetchJson(url), which returns a promise of the data. get(url) shares one request between callers,
 * remembers successes for maxAgeMs, and forgets failures at once. clear() forgets everything.
 */
export function createCachedFetcher(fetchJson, { maxAgeMs = 60000, now = () => Date.now() } = {}) {
  const cache = new Map();

  function get(url) {
    const entry = cache.get(url);
    if (entry !== undefined && now() - entry.at < maxAgeMs) {
      return entry.promise;
    }
    const promise = fetchJson(url);
    const fresh = { promise, at: now() };
    cache.set(url, fresh);
    promise.catch(() => {
      if (cache.get(url) === fresh) {
        cache.delete(url);
      }
    });
    return promise;
  }

  return { get, clear: () => cache.clear() };
}
