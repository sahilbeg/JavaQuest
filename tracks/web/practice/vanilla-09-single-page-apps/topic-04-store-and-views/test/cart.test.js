import { describe, it, expect, vi } from "vitest";
import { createStore } from "../src/store.js";
import { addItem, removeItem, itemCount, watch, mountCartBadge } from "../src/cart.js";

const emptyCart = () => createStore({ lines: [], route: "/items" });
const goTo = (store, route) => store.setState((state) => ({ ...state, route }));

describe("addItem", () => {
  it("adds a new line with qty 1", () => {
    const store = emptyCart();
    addItem(store, "DC-1042");
    expect(store.getState().lines, "adding Golu to an empty cart should give one line with qty 1").toEqual([{ id: "DC-1042", qty: 1 }]);
  });

  it("adds one more to an item that already has a line", () => {
    const store = emptyCart();
    addItem(store, "DC-1042");
    addItem(store, "DC-2001");
    addItem(store, "DC-1042");
    expect(store.getState().lines, "a second Golu should raise its qty to 2, not add another line").toEqual([
      { id: "DC-1042", qty: 2 },
      { id: "DC-2001", qty: 1 },
    ]);
  });

  it("keeps the rest of the state", () => {
    const store = emptyCart();
    addItem(store, "DC-1042");
    expect(store.getState().route, "adding to the cart must not lose the route; copy the state with { ...state }").toBe("/items");
  });

  it("makes a new state instead of changing the old one", () => {
    const store = emptyCart();
    const before = store.getState();
    addItem(store, "DC-1042");
    expect(store.getState(), "setState should get a brand-new state object").not.toBe(before);
    expect(before.lines, "the old state's lines must stay empty; don't push into them").toEqual([]);
  });

  it("makes a new line object when the qty goes up", () => {
    const store = emptyCart();
    addItem(store, "DC-1042");
    const oldLine = store.getState().lines[0];
    addItem(store, "DC-1042");
    expect(oldLine.qty, "the old line object must keep qty 1; copy it with { ...line, qty: line.qty + 1 }").toBe(1);
  });
});

describe("removeItem", () => {
  it("takes one item's line out and keeps the others", () => {
    const store = createStore({ lines: [{ id: "DC-1042", qty: 2 }, { id: "DC-2001", qty: 1 }], route: "/cart" });
    removeItem(store, "DC-1042");
    expect(store.getState().lines, "only the coaster set's line should be left").toEqual([{ id: "DC-2001", qty: 1 }]);
    expect(store.getState().route, "removing must not lose the route").toBe("/cart");
  });

  it("makes new lines instead of changing the old ones", () => {
    const store = createStore({ lines: [{ id: "DC-1042", qty: 2 }], route: "/cart" });
    const before = store.getState();
    removeItem(store, "DC-1042");
    expect(before.lines.length, "the old state must still have its line; use filter, not splice").toBe(1);
    expect(store.getState().lines, "the new state should have no lines").toEqual([]);
  });
});

describe("itemCount", () => {
  it("adds up the qty of every line", () => {
    const state = { lines: [{ id: "DC-1042", qty: 2 }, { id: "DC-2001", qty: 3 }], route: "/" };
    expect(itemCount(state), "2 Golus and 3 coaster sets make 5 items").toBe(5);
  });

  it("gives 0 for an empty cart", () => {
    expect(itemCount({ lines: [], route: "/" }), "an empty cart has 0 items").toBe(0);
  });
});

describe("watch", () => {
  it("calls onChange straight away with the current value", () => {
    const store = createStore({ lines: [{ id: "DC-1042", qty: 2 }], route: "/" });
    const onChange = vi.fn();
    watch(store, itemCount, onChange);
    expect(onChange.mock.calls, "watch should call onChange once at the start, with the count 2").toEqual([[2]]);
  });

  it("calls onChange again when the selected value changes", () => {
    const store = emptyCart();
    const onChange = vi.fn();
    watch(store, itemCount, onChange);
    addItem(store, "DC-1042");
    expect(onChange.mock.calls, "after adding Golu, onChange should have been called with 0 and then 1").toEqual([[0], [1]]);
  });

  it("stays quiet when a different part of the state changes", () => {
    const store = emptyCart();
    const onChange = vi.fn();
    watch(store, itemCount, onChange);
    goTo(store, "/cart");
    goTo(store, "/items/DC-1042");
    expect(onChange.mock.calls.length, "changing only the route doesn't change the count, so onChange should run just once (at the start)").toBe(1);
  });

  it("returns a function that stops watching", () => {
    const store = emptyCart();
    const onChange = vi.fn();
    const stop = watch(store, itemCount, onChange);
    stop();
    addItem(store, "DC-1042");
    expect(onChange.mock.calls.length, "after stop(), onChange must not be called again").toBe(1);
  });
});

describe("mountCartBadge", () => {
  it("shows the count at once and follows the cart", () => {
    const badge = document.createElement("span");
    const store = emptyCart();
    mountCartBadge(badge, store);
    expect(badge.textContent, "a new badge on an empty cart should say 0").toBe("0");
    addItem(store, "DC-1042");
    addItem(store, "DC-1042");
    expect(badge.textContent, "after two Golus the badge should say 2").toBe("2");
  });

  it("doesn't redraw when only the route changes", () => {
    const badge = document.createElement("span");
    const store = emptyCart();
    mountCartBadge(badge, store);
    badge.textContent = "untouched";
    goTo(store, "/cart");
    expect(badge.textContent, "a route change must not rewrite the badge; only write when the count changes").toBe("untouched");
  });

  it("stops following the store once unmounted", () => {
    const badge = document.createElement("span");
    const store = emptyCart();
    const unmount = mountCartBadge(badge, store);
    unmount();
    addItem(store, "DC-1042");
    expect(badge.textContent, "after unmount, the badge must stop updating").toBe("0");
  });
});
