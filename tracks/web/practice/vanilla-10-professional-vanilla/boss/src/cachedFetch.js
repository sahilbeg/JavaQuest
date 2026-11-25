// Round 3, frontend design: "Product pages ask for stock again and again. Add a small cache in front of the API."
// The candidate's answer. Sharing one request between callers works. The rest doesn't.

/**
 * Wraps fetchJson(url), which returns a promise of the data. get(url) shares one request between callers,
 * remembers successes for maxAgeMs, and forgets failures at once. clear() forgets everything.
 */
export function createCachedFetcher(fetchJson, { maxAgeMs = 60000, now = () => Date.now() } = {}) {
  const cache = new Map();

  function get(url) {
    // TODO 5: a failed request must not stay in the cache: when the promise rejects, delete its entry,
    //         but only if the cache still holds that same entry (a newer request may have replaced it)
    // TODO 6: remember when each request started ({ promise, at: now() }), and treat an entry as stale
    //         once now() - at reaches maxAgeMs: then ask the server again
    if (!cache.has(url)) {
      cache.set(url, fetchJson(url));
    }
    return cache.get(url);
  }

  return { get, clear: () => cache.clear() };
}
