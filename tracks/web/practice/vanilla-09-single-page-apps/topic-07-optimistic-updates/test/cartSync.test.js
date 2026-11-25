import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { createStore } from "../src/store.js";
import { insertAt, changeQty, removeWithUndo } from "../src/cartSync.js";

const SAVE_FAILED = "Couldn't update your cart. Please try again.";
const REMOVE_FAILED = "Couldn't remove that item. It's back in your cart.";

const golu = { id: "DC-1042", qty: 1 };
const coasters = { id: "DC-2001", qty: 2 };
const tote = { id: "DC-3001", qty: 1 };

const cartWith = (...lines) => createStore({ lines, undo: null, error: null });
const qtyOf = (store, id) => store.getState().lines.find((line) => line.id === id)?.qty;
const ids = (store) => store.getState().lines.map((line) => line.id);

function reply(status) {
  return { ok: status >= 200 && status < 300, status, json: async () => ({}) };
}

/** fetch answers at once with this status. */
function serverAnswers(status) {
  const fetchMock = vi.fn(async () => reply(status));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

/** fetch fails with no answer at all, like flight mode. */
function networkDown() {
  vi.stubGlobal("fetch", vi.fn(async () => {
    throw new TypeError("Failed to fetch");
  }));
}

/** fetch waits until the test calls answer(status). */
function slowServer() {
  let finish = () => {};
  vi.stubGlobal("fetch", vi.fn(() => new Promise((resolve) => {
    finish = resolve;
  })));
  return { answer: (status) => finish(reply(status)) };
}

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("insertAt", () => {
  it("puts a line back in the middle", () => {
    expect(insertAt([golu, tote], coasters, 1), "the coaster set should go back between Golu and the tote bag").toEqual([golu, coasters, tote]);
  });

  it("puts a line back at the start and at the end", () => {
    expect(insertAt([coasters, tote], golu, 0), "index 0 puts Golu first").toEqual([golu, coasters, tote]);
    expect(insertAt([golu, coasters], tote, 2), "index 2 puts the tote bag last").toEqual([golu, coasters, tote]);
  });

  it("doesn't change the old array", () => {
    const lines = [golu, tote];
    insertAt(lines, coasters, 1);
    expect(lines, "insertAt must return a new array and leave the old one alone").toEqual([golu, tote]);
  });
});

describe("changeQty", () => {
  it("shows the new qty before the server answers", async () => {
    const server = slowServer();
    const store = cartWith(golu);
    const done = changeQty(store, "DC-1042", 3);
    expect(qtyOf(store, "DC-1042"), "the cart should say 3 Golus at once, without waiting for the server").toBe(3);
    server.answer(200);
    await done;
  });

  it("saves the new qty with PATCH and a JSON body", async () => {
    const fetchMock = serverAnswers(200);
    await changeQty(cartWith(golu), "DC-1042", 3);
    const [url, options] = fetchMock.mock.calls[0] ?? [];
    expect(url, "the qty is saved at /api/cart/DC-1042").toBe("/api/cart/DC-1042");
    expect(options?.method, "changing a qty uses the PATCH method").toBe("PATCH");
    expect(JSON.parse(options?.body ?? "null"), "the body should be the JSON text of { qty: 3 }").toEqual({ qty: 3 });
  });

  it("keeps the new qty when the save works", async () => {
    serverAnswers(200);
    const store = cartWith(golu);
    await changeQty(store, "DC-1042", 3);
    expect(qtyOf(store, "DC-1042"), "a saved change should stay at 3").toBe(3);
    expect(store.getState().error, "a saved change shows no error").toBe(null);
  });

  it("puts the old qty back and explains when the server refuses", async () => {
    serverAnswers(500);
    const store = cartWith(golu);
    await changeQty(store, "DC-1042", 3);
    expect(qtyOf(store, "DC-1042"), "the server said no, so the cart should go back to 1 Golu").toBe(1);
    expect(store.getState().error, "the customer should see the SAVE_FAILED message").toBe(SAVE_FAILED);
  });

  it("puts the old qty back on a dropped connection, without throwing", async () => {
    networkDown();
    const store = cartWith(golu);
    let error = null;
    try {
      await changeQty(store, "DC-1042", 3);
    } catch (e) {
      error = e;
    }
    expect(error, "changeQty should catch the network error itself").toBeNull();
    expect(qtyOf(store, "DC-1042"), "a failed save should put 1 Golu back").toBe(1);
  });

  it("rolls back only its own line", async () => {
    const server = slowServer();
    const store = cartWith(golu, coasters);
    const done = changeQty(store, "DC-1042", 3);
    store.setState((state) => ({ ...state, lines: state.lines.map((l) => (l.id === "DC-2001" ? { ...l, qty: 5 } : l)) }));
    server.answer(500);
    await done;
    expect(qtyOf(store, "DC-1042"), "Golu's failed change should go back to 1").toBe(1);
    expect(qtyOf(store, "DC-2001"), "the coaster set changed meanwhile; its 5 must not be undone").toBe(5);
  });
});

describe("removeWithUndo", () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("takes the line out at once and offers undo", () => {
    const fetchMock = serverAnswers(200);
    const store = cartWith(golu, coasters);
    removeWithUndo(store, "DC-1042");
    expect(ids(store), "Golu should leave the cart straight away").toEqual(["DC-2001"]);
    expect(store.getState().undo, "the state's undo should name DC-1042, so the page can show Undo").toBe("DC-1042");
    expect(fetchMock.mock.calls.length, "nothing is sent to the server while Undo is still possible").toBe(0);
  });

  it("tells the server with DELETE after five seconds", async () => {
    const fetchMock = serverAnswers(200);
    const store = cartWith(golu, coasters);
    const { finished } = removeWithUndo(store, "DC-1042");
    await vi.advanceTimersByTimeAsync(4999);
    expect(fetchMock.mock.calls.length, "at 4.999 seconds the customer can still undo, so nothing is sent").toBe(0);
    await vi.advanceTimersByTimeAsync(1);
    await finished;
    expect(fetchMock.mock.calls[0]?.[0], "after 5 seconds, the removal goes to /api/cart/DC-1042").toBe("/api/cart/DC-1042");
    expect(fetchMock.mock.calls[0]?.[1]?.method, "removing uses the DELETE method").toBe("DELETE");
    expect(store.getState().undo, "once the time is up, the Undo bar should go away").toBe(null);
    expect(ids(store), "Golu stays out of the cart").toEqual(["DC-2001"]);
  });

  it("puts the line back in its old place when the customer presses Undo", async () => {
    const fetchMock = serverAnswers(200);
    const store = cartWith(golu, coasters, tote);
    const { undo, finished } = removeWithUndo(store, "DC-2001");
    undo();
    await finished;
    expect(ids(store), "the coaster set should be back in the middle, where it was").toEqual(["DC-1042", "DC-2001", "DC-3001"]);
    expect(store.getState().undo, "after Undo, the Undo bar should go away").toBe(null);
    await vi.advanceTimersByTimeAsync(5000);
    expect(fetchMock.mock.calls.length, "an undone removal must never reach the server").toBe(0);
  });

  it("puts the line back and explains when the server refuses", async () => {
    serverAnswers(500);
    const store = cartWith(golu, coasters, tote);
    const { finished } = removeWithUndo(store, "DC-2001");
    await vi.advanceTimersByTimeAsync(5000);
    await finished;
    expect(ids(store), "the server said no, so the coaster set should come back in its old place").toEqual(["DC-1042", "DC-2001", "DC-3001"]);
    expect(store.getState().error, "the customer should see the REMOVE_FAILED message").toBe(REMOVE_FAILED);
  });
});
