import { describe, it, expect, afterEach } from "vitest";
import { countItems } from "../src/cart-count.js";

afterEach(() => {
  document.body.innerHTML = "";
});

/** A simple store for these checks, so they test <cart-count> on its own. It shows its subscribers. */
function fakeStore(state) {
  const listeners = new Set();
  return {
    listeners,
    getState: () => state,
    setState(changes) {
      state = { ...state, ...changes };
      for (const listener of listeners) {
        listener(state);
      }
    },
    subscribe(listener) {
      listeners.add(listener);
      return () => {
        listeners.delete(listener);
      };
    },
  };
}

function cartCount(store) {
  const element = document.createElement("cart-count");
  element.store = store;
  document.body.append(element);
  return element;
}

describe("countItems", () => {
  it("adds up the quantity of every line", () => {
    const lines = [{ id: "DC-1042", qty: 2 }, { id: "DC-1001", qty: 3 }];
    expect(countItems(lines), "2 Golus and 3 coaster sets make 5 pieces").toBe(5);
    expect(countItems([]), "an empty cart has 0 pieces").toBe(0);
  });
});

describe("<cart-count>", () => {
  it("shows the count as soon as it joins the page", () => {
    const element = cartCount(fakeStore({ lines: [{ id: "DC-1042", qty: 2 }, { id: "DC-1013", qty: 1 }] }));
    expect(element.textContent, "with 3 pieces in the cart it should show Cart (3) straight away").toBe("Cart (3)");
  });

  it("subscribes once when it joins the page", () => {
    const store = fakeStore({ lines: [] });
    cartCount(store);
    expect(store.listeners.size, "joining the page should add exactly one subscriber").toBe(1);
  });

  it("follows every change in the store", () => {
    const store = fakeStore({ lines: [] });
    const element = cartCount(store);
    store.setState({ lines: [{ id: "DC-1001", qty: 4 }] });
    expect(element.textContent, "after 4 coaster sets are added it should show Cart (4)").toBe("Cart (4)");
    store.setState({ lines: [{ id: "DC-1001", qty: 4 }, { id: "DC-1042", qty: 1 }] });
    expect(element.textContent, "after one Golu more it should show Cart (5)").toBe("Cart (5)");
  });

  it("unsubscribes when it leaves the page", () => {
    const store = fakeStore({ lines: [{ id: "DC-1042", qty: 1 }] });
    const element = cartCount(store);
    element.remove();
    expect(store.listeners.size, "after the badge is removed, the store should have no subscribers").toBe(0);
    store.setState({ lines: [] });
    expect(element.textContent, "a removed badge should not redraw").toBe("Cart (1)");
  });

  it("subscribes again when it comes back", () => {
    const store = fakeStore({ lines: [] });
    const element = cartCount(store);
    element.remove();
    store.setState({ lines: [{ id: "DC-1013", qty: 2 }] });
    document.body.append(element);
    expect(store.listeners.size, "coming back should leave exactly one subscriber").toBe(1);
    expect(element.textContent, "coming back should draw the current count, Cart (2)").toBe("Cart (2)");
  });

  it("keeps two badges on one store in step", () => {
    const store = fakeStore({ lines: [] });
    const header = cartCount(store);
    const drawer = cartCount(store);
    store.setState({ lines: [{ id: "DC-1042", qty: 3 }] });
    expect([header.textContent, drawer.textContent], "both badges should show Cart (3)").toEqual(["Cart (3)", "Cart (3)"]);
  });
});
