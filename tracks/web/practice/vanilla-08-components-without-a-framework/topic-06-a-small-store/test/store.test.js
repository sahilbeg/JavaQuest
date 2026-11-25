import { describe, it, expect, vi } from "vitest";
import { createStore } from "../src/store.js";

describe("createStore", () => {
  it("gives back the state it started with", () => {
    const start = { lines: [], open: false };
    const store = createStore(start);
    expect(store.getState(), "getState should return the initial state").toBe(start);
  });

  it("merges changes into a new state object", () => {
    const store = createStore({ lines: [], open: false });
    const before = store.getState();
    store.setState({ open: true });
    const after = store.getState();
    expect(after, "after setState({ open: true }), the state should be { lines: [], open: true }").toEqual({ lines: [], open: true });
    expect(after === before, "setState should make a new object, not change the old one").toBe(false);
    expect(before.open, "the old state object must not change").toBe(false);
  });

  it("accepts a function of the current state", () => {
    const store = createStore({ count: 0 });
    store.setState((state) => ({ count: state.count + 1 }));
    store.setState((state) => ({ count: state.count + 1 }));
    expect(store.getState().count, "two setState calls that add 1 should give a count of 2").toBe(2);
  });

  it("tells every subscriber the new state", () => {
    const store = createStore({ count: 0 });
    const badge = vi.fn();
    const drawer = vi.fn();
    store.subscribe(badge);
    store.subscribe(drawer);
    store.setState({ count: 3 });
    expect(badge.mock.calls, "the badge should be called once with { count: 3 }").toEqual([[{ count: 3 }]]);
    expect(drawer.mock.calls, "the drawer should be called once with { count: 3 }").toEqual([[{ count: 3 }]]);
  });

  it("changes the state before it tells subscribers", () => {
    const store = createStore({ count: 0 });
    const seen = [];
    store.subscribe(() => seen.push(store.getState().count));
    store.setState({ count: 5 });
    expect(seen, "inside a subscriber, getState() should already give the new count").toEqual([5]);
  });

  it("stops calling a subscriber after it unsubscribes", () => {
    const store = createStore({ count: 0 });
    const badge = vi.fn();
    const drawer = vi.fn();
    const stop = store.subscribe(badge);
    store.subscribe(drawer);
    stop();
    store.setState({ count: 1 });
    expect(badge.mock.calls.length, "an unsubscribed badge should not be called").toBe(0);
    expect(drawer.mock.calls.length, "the other subscriber should still be called").toBe(1);
  });
});
