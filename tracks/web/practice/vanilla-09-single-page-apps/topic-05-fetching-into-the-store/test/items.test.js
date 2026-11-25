import { describe, it, expect, afterEach, vi } from "vitest";
import { createStore } from "../src/store.js";
import { createItemLoader, selectItem, initialItems } from "../src/items.js";

const FAILED = "Couldn't load the catalogue. Check your connection and try again.";

const catalogue = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-2001", name: "Marigold coaster set", paise: 15000 },
  { id: "DC-3001", name: "Indigo tote bag", paise: 89900 },
];

function reply(status, body) {
  return { ok: status >= 200 && status < 300, status, json: async () => body };
}

/** fetch answers at once with this status and body. */
function serverAnswers(status, body) {
  const fetchMock = vi.fn(async () => reply(status, body));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

/** fetch fails with no answer at all, like flight mode. */
function networkDown() {
  const fetchMock = vi.fn(async () => {
    throw new TypeError("Failed to fetch");
  });
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

/** fetch waits until the test calls answer(status, body). */
function slowServer() {
  const waiting = [];
  const fetchMock = vi.fn(() => new Promise((resolve) => waiting.push(resolve)));
  vi.stubGlobal("fetch", fetchMock);
  return { fetchMock, answer: (status, body) => waiting.shift()?.(reply(status, body)) };
}

/** Waits for load(), ignoring a thrown error so the checks below can explain what went wrong. */
async function settle(promise) {
  try {
    await promise;
  } catch {
    // the "never throws" checks report this
  }
}

const newStore = (items = initialItems) => createStore({ items, route: "/items" });

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("load", () => {
  it("shows loading at once, before the answer arrives", async () => {
    const server = slowServer();
    const store = newStore();
    const done = createItemLoader(store).load();
    expect(store.getState().items.status, "items.status should be \"loading\" while the request waits").toBe("loading");
    server.answer(200, catalogue);
    await settle(done);
  });

  it("puts the items in the store and moves to ready", async () => {
    const fetchMock = serverAnswers(200, catalogue);
    const store = newStore();
    await settle(createItemLoader(store).load());
    expect(fetchMock.mock.calls[0]?.[0], "the items come from GET /api/items").toBe("/api/items");
    expect(store.getState().items, "after a good answer, items should be { status: \"ready\", data, error: null }").toEqual({ status: "ready", data: catalogue, error: null });
  });

  it("keeps the rest of the state", async () => {
    serverAnswers(200, catalogue);
    const store = newStore();
    await settle(createItemLoader(store).load());
    expect(store.getState().route, "loading items must not lose the route; copy the state with { ...state }").toBe("/items");
  });

  it("doesn't fetch again when the items are already loaded", async () => {
    const fetchMock = serverAnswers(200, catalogue);
    const loader = createItemLoader(newStore());
    await settle(loader.load());
    await settle(loader.load());
    expect(fetchMock.mock.calls.length, "going back to the catalogue should use the items in the store, not fetch again").toBe(1);
  });

  it("shares one request when two screens ask at the same time", async () => {
    const server = slowServer();
    const store = newStore();
    const loader = createItemLoader(store);
    const first = loader.load();
    const second = loader.load();
    expect(server.fetchMock.mock.calls.length, "two loads while the first is still waiting should make only one request").toBe(1);
    server.answer(200, catalogue);
    await settle(first);
    await settle(second);
    expect(store.getState().items.status, "both loads should end with the items ready").toBe("ready");
  });
});

describe("when loading fails", () => {
  it("moves to error with the friendly message when the server answers 500", async () => {
    serverAnswers(500, { error: "Server error" });
    const store = newStore();
    await settle(createItemLoader(store).load());
    expect(store.getState().items.status, "a 500 answer should set items.status to \"error\"").toBe("error");
    expect(store.getState().items.error, "a 500 answer should store the FAILED message").toBe(FAILED);
  });

  it("handles a dropped connection without throwing", async () => {
    networkDown();
    const store = newStore();
    let error = null;
    try {
      await createItemLoader(store).load();
    } catch (e) {
      error = e;
    }
    expect(error, "load should catch the network error itself, not throw it").toBeNull();
    expect(store.getState().items.status, "a dropped connection should set items.status to \"error\"").toBe("error");
  });

  it("keeps the items it already had", async () => {
    networkDown();
    const store = newStore({ status: "idle", data: [catalogue[0]], error: null });
    await settle(createItemLoader(store).load());
    expect(store.getState().items.data, "a failed load must keep the items the store already had").toEqual([catalogue[0]]);
  });

  it("tries again on the next load after an error", async () => {
    networkDown();
    const store = newStore();
    const loader = createItemLoader(store);
    await settle(loader.load());
    const fetchMock = serverAnswers(200, catalogue);
    await settle(loader.load());
    expect(fetchMock.mock.calls.length, "an error must not be kept like a result: the next load should fetch again").toBe(1);
    expect(store.getState().items, "the retry should end ready, with the items and no error").toEqual({ status: "ready", data: catalogue, error: null });
  });
});

describe("selectItem", () => {
  const state = { items: { status: "ready", data: catalogue, error: null }, route: "/items/DC-1042" };

  it("finds an item by its id", () => {
    expect(selectItem(state, "DC-1042"), "DC-1042 is Golu").toEqual(catalogue[0]);
  });

  it("gives null for an id that isn't loaded", () => {
    expect(selectItem(state, "DC-9999"), "there is no DC-9999, so the answer is null").toBe(null);
  });
});
