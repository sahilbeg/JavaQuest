import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { fetchStock } from "../src/badges.js";

const shelf = { "DC-1042": 2, "DC-2001": 12, "DC-3001": 0 };

/** A pretend stock API where every answer takes one second. Unknown ids get a 404. */
function slowServer() {
  const fetchMock = vi.fn(
    (url) =>
      new Promise((resolve) => {
        const id = String(url).split("/").pop();
        setTimeout(() => {
          if (id in shelf) resolve({ ok: true, status: 200, json: async () => ({ id, stock: shelf[id] }) });
          else resolve({ ok: false, status: 404, json: async () => ({ error: "No such item" }) });
        }, 1000);
      }),
  );
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

/** Watches a promise, so a test can check whether it has finished yet. */
function track(promise) {
  const state = { done: false, value: undefined, error: null };
  Promise.resolve(promise).then(
    (value) => {
      state.done = true;
      state.value = value;
    },
    (error) => {
      state.done = true;
      state.error = error;
    },
  );
  return state;
}

beforeEach(() => {
  vi.useFakeTimers();
});

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe("fetchStock", () => {
  it("asks the stock endpoint for every id", async () => {
    const fetchMock = slowServer();
    track(fetchStock(["DC-1042", "DC-2001"]));
    await vi.advanceTimersByTimeAsync(0);
    expect(fetchMock.mock.calls.map((call) => call[0]), "both requests should start straight away, at /api/stock/<id>").toEqual([
      "/api/stock/DC-1042",
      "/api/stock/DC-2001",
    ]);
  });

  it("gives an object of stock by id after one second, because the requests run together", async () => {
    slowServer();
    const result = track(fetchStock(["DC-1042", "DC-2001", "DC-3001"]));
    await vi.advanceTimersByTimeAsync(1000);
    expect(result.done, "three 1-second requests with Promise.all should be done after 1 second").toBe(true);
    expect(result.value, "each id should map to its stock").toEqual({ "DC-1042": 2, "DC-2001": 12, "DC-3001": 0 });
  });

  it("rejects when any answer isn't ok", async () => {
    slowServer();
    const result = track(fetchStock(["DC-1042", "DC-9999"]));
    await vi.advanceTimersByTimeAsync(1000);
    expect(result.error, "a 404 for DC-9999 should make fetchStock reject").not.toBeNull();
  });
});
