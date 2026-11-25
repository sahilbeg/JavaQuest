import { describe, it, expect, vi } from "vitest";
import { CACHE, SHELL, chooseStrategy, precache, removeOldCaches, cacheFirst, networkFirst } from "../src/offline.js";
import { fakeResponse, fakeCache, fakeCaches } from "./fakeCaches.js";

/** A fetch that answers every path with this response. */
const online = (response) => vi.fn(async () => response);

/** A fetch that fails like flight mode. */
const offline = () => vi.fn(async () => {
  throw new TypeError("Failed to fetch");
});

describe("chooseStrategy", () => {
  it("asks the network first for API requests", () => {
    expect(chooseStrategy("/api/items"), "stock and prices change, so /api/items is network-first").toBe("network-first");
    expect(chooseStrategy("/api/stock/DC-1042"), "/api/stock/DC-1042 is network-first too").toBe("network-first");
  });

  it("uses the cache first for the app shell and pictures", () => {
    expect(chooseStrategy("/main.js"), "the app's code is cache-first").toBe("cache-first");
    expect(chooseStrategy("/"), "the home page is cache-first").toBe("cache-first");
    expect(chooseStrategy("/images/golu.jpg"), "pictures are cache-first").toBe("cache-first");
  });
});

describe("precache", () => {
  it("stores the app shell in the dhaaga-v2 cache", async () => {
    const caches = fakeCaches();
    await precache(caches, SHELL);
    expect(caches.open.mock.calls[0]?.[0], "precache should open the cache named CACHE, \"dhaaga-v2\"").toBe(CACHE);
    const cache = caches.all.get(CACHE);
    expect(cache?.addAll.mock.calls[0]?.[0], "precache should call cache.addAll with every shell url").toEqual(SHELL);
  });
});

describe("removeOldCaches", () => {
  it("deletes every cache except the current one", async () => {
    const caches = fakeCaches(["dhaaga-v1", "dhaaga-v2", "dhaaga-test"]);
    const deleted = await removeOldCaches(caches, "dhaaga-v2");
    expect([...caches.all.keys()], "only dhaaga-v2 should be left").toEqual(["dhaaga-v2"]);
    expect(deleted, "removeOldCaches should return the names it deleted").toEqual(["dhaaga-v1", "dhaaga-test"]);
  });

  it("deletes nothing when only the current cache exists", async () => {
    const caches = fakeCaches(["dhaaga-v2"]);
    expect(await removeOldCaches(caches, "dhaaga-v2"), "nothing is old, so the answer is []").toEqual([]);
    expect(caches.delete.mock.calls.length, "caches.delete must not be called for the current cache").toBe(0);
  });
});

describe("cacheFirst", () => {
  it("answers from the cache without touching the network", async () => {
    const saved = fakeResponse("main.js code");
    const cache = fakeCache({ "/main.js": saved });
    const fetchFn = online(fakeResponse("new code"));
    const response = await cacheFirst("/main.js", cache, fetchFn);
    expect(response, "a saved /main.js should be returned as it is").toBe(saved);
    expect(fetchFn.mock.calls.length, "with a saved copy, the network must not be used").toBe(0);
  });

  it("fetches and saves a copy when nothing is saved", async () => {
    const cache = fakeCache();
    const fresh = fakeResponse("golu photo");
    const response = await cacheFirst("/images/golu.jpg", cache, online(fresh));
    expect(response, "with nothing saved, the network's answer should be returned").toBe(fresh);
    expect(cache.saved.get("/images/golu.jpg")?.body, "a copy should now be saved for next time").toBe("golu photo");
    expect(cache.saved.get("/images/golu.jpg"), "save response.clone(), not the response itself, because a body can only be read once").not.toBe(fresh);
  });

  it("doesn't save an error answer", async () => {
    const cache = fakeCache();
    const response = await cacheFirst("/images/hat.jpg", cache, online(fakeResponse("not found", 404)));
    expect(response?.status, "the 404 should still be returned to the page").toBe(404);
    expect(cache.saved.has("/images/hat.jpg"), "a 404 must not be saved, or it would be served forever").toBe(false);
  });
});

describe("networkFirst", () => {
  it("returns the fresh answer and saves a copy when online", async () => {
    const cache = fakeCache({ "/api/items": fakeResponse("old list") });
    const fresh = fakeResponse("new list");
    const response = await networkFirst("/api/items", cache, online(fresh));
    expect(response, "online, the fresh answer should be returned").toBe(fresh);
    expect(cache.saved.get("/api/items")?.body, "the saved copy should be replaced by the new list").toBe("new list");
    expect(cache.saved.get("/api/items"), "save a clone, not the response the page will read").not.toBe(fresh);
  });

  it("falls back to the saved copy when offline", async () => {
    const saved = fakeResponse("list from this morning");
    const cache = fakeCache({ "/api/items": saved });
    const response = await networkFirst("/api/items", cache, offline());
    expect(response, "offline, the saved list should be returned").toBe(saved);
  });

  it("passes the error on when offline with nothing saved", async () => {
    let error = null;
    try {
      await networkFirst("/api/items", fakeCache(), offline());
    } catch (e) {
      error = e;
    }
    expect(error?.message, "with no network and no saved copy, the network error should be thrown on").toBe("Failed to fetch");
  });

  it("keeps the good saved copy when the server answers 500", async () => {
    const cache = fakeCache({ "/api/items": fakeResponse("good list") });
    const response = await networkFirst("/api/items", cache, online(fakeResponse("oops", 500)));
    expect(response?.status, "the 500 answer is returned to the page, which shows its error state").toBe(500);
    expect(cache.saved.get("/api/items")?.body, "a 500 must not replace the good saved list").toBe("good list");
  });
});
