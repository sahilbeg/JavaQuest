import { describe, it, expect, vi } from "vitest";
import { createCachedFetcher } from "../src/cachedFetch.js";

const GOLU = "/api/stock/DC-1042";
const TOTE = "/api/stock/DC-1301";

/** A promise the test resolves or rejects by hand. */
function deferred() {
  let resolve;
  let reject;
  const promise = new Promise((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
}

/** Lets every waiting promise callback run. */
async function settle() {
  for (let i = 0; i < 20; i += 1) {
    await Promise.resolve();
  }
}

/** The error message if the promise rejects, or "no error". */
const outcome = (promise) => promise.then(() => "no error", (error) => error.message);

describe("Gold: createCachedFetcher", () => {
  it("asks the server once when two parts of the page ask at the same time", async () => {
    const answer = deferred();
    const fetchJson = vi.fn(() => answer.promise);
    const { get } = createCachedFetcher(fetchJson);
    const first = get(GOLU);
    const second = get(GOLU);
    expect(fetchJson.mock.calls.length, "two requests for Golu in flight should share one call to the server").toBe(1);
    answer.resolve({ stock: 7 });
    expect(await first, "the first caller should get Golu's stock").toEqual({ stock: 7 });
    expect(await second, "the second caller should get the same answer").toEqual({ stock: 7 });
  });

  it("answers from the cache once it has the data", async () => {
    const fetchJson = vi.fn(async () => ({ stock: 7 }));
    const { get } = createCachedFetcher(fetchJson);
    await get(GOLU);
    expect(await get(GOLU), "the second get should give the cached stock").toEqual({ stock: 7 });
    expect(fetchJson.mock.calls.length, "a fresh answer should be reused, not fetched again").toBe(1);
  });

  it("keeps each address separately", async () => {
    const fetchJson = vi.fn(async (url) => ({ url }));
    const { get } = createCachedFetcher(fetchJson);
    expect(await get(GOLU), "Golu's address should give Golu's data").toEqual({ url: GOLU });
    expect(await get(TOTE), "the tote bag's address should give the tote bag's data").toEqual({ url: TOTE });
    expect(fetchJson.mock.calls.length, "two different addresses need two requests").toBe(2);
  });

  it("gives every caller waiting on a failed request the same error", async () => {
    const answer = deferred();
    const { get } = createCachedFetcher(vi.fn(() => answer.promise));
    const first = outcome(get(GOLU));
    const second = outcome(get(GOLU));
    answer.reject(new Error("HTTP 503"));
    expect(await first, "the first caller should see the server's error").toBe("HTTP 503");
    expect(await second, "the second caller should see the same error").toBe("HTTP 503");
  });

  it("doesn't remember a failure: the next request asks again", async () => {
    const fetchJson = vi.fn()
      .mockImplementationOnce(async () => {
        throw new Error("HTTP 503");
      })
      .mockImplementationOnce(async () => ({ stock: 7 }));
    const { get } = createCachedFetcher(fetchJson);
    expect(await outcome(get(GOLU)), "the first request fails with HTTP 503").toBe("HTTP 503");
    await settle();
    const second = await get(GOLU).catch((error) => `failed again with the cached error: ${error.message}`);
    expect(second, "after a failure, the next get should ask the server again and get the stock").toEqual({ stock: 7 });
    expect(fetchJson.mock.calls.length, "the server should have been asked twice").toBe(2);
  });

  it("asks again once the data is older than maxAgeMs", async () => {
    let time = 0;
    let calls = 0;
    const fetchJson = vi.fn(async () => {
      calls += 1;
      return { stock: 10 - calls };
    });
    const { get } = createCachedFetcher(fetchJson, { maxAgeMs: 60000, now: () => time });
    expect(await get(GOLU), "the first answer is 9 in stock").toEqual({ stock: 9 });
    time = 59999;
    expect(await get(GOLU), "at 59,999 ms the cached answer is still fresh").toEqual({ stock: 9 });
    time = 60000;
    expect(await get(GOLU), "at 60,000 ms the answer is stale, so the server is asked again").toEqual({ stock: 8 });
    expect(fetchJson.mock.calls.length, "two requests in all: one at the start, one after a minute").toBe(2);
  });

  it("doesn't let an old failure throw away a newer request", async () => {
    let time = 0;
    const old = deferred();
    const fresh = deferred();
    const fetchJson = vi.fn().mockReturnValueOnce(old.promise).mockReturnValueOnce(fresh.promise).mockImplementation(async () => ({ stock: 0 }));
    const { get } = createCachedFetcher(fetchJson, { maxAgeMs: 60000, now: () => time });
    get(GOLU).catch(() => {});
    time = 60000;
    get(GOLU).catch(() => {});
    expect(fetchJson.mock.calls.length, "a stale entry should start a second request").toBe(2);
    old.reject(new Error("HTTP 504"));
    fresh.resolve({ stock: 5 });
    await settle();
    time = 60001;
    expect(await get(GOLU), "the newer request's answer should still be cached after the old one failed").toEqual({ stock: 5 });
    expect(fetchJson.mock.calls.length, "no third request: only delete the entry if it is still the failed one").toBe(2);
  });

  it("clear() forgets everything", async () => {
    const fetchJson = vi.fn(async () => ({ stock: 7 }));
    const { get, clear } = createCachedFetcher(fetchJson);
    await get(GOLU);
    clear();
    await get(GOLU);
    expect(fetchJson.mock.calls.length, "after clear(), the next get should ask the server again").toBe(2);
  });
});
