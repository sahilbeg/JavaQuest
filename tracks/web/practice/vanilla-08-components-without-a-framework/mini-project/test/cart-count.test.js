import { describe, it, expect, afterEach } from "vitest";
import { createStore } from "../src/store.js";
import { countItems } from "../src/cart-count.js";

afterEach(() => {
  document.body.innerHTML = "";
});

/** A real store that also counts how many subscribers are listening right now. */
function countedStore(state) {
  const store = createStore(state);
  const subscribe = store.subscribe;
  let active = 0;
  store.subscribe = (listener) => {
    active += 1;
    const stop = subscribe(listener);
    return () => {
      active -= 1;
      stop();
    };
  };
  return { store, active: () => active };
}

function cartCount(store) {
  const element = document.createElement("cart-count");
  element.store = store;
  document.body.append(element);
  return element;
}

describe("countItems", () => {
  it("adds up every line's quantity", () => {
    expect(countItems([{ id: "DC-1042", qty: 2 }, { id: "DC-1001", qty: 1 }]), "2 Golus and 1 coaster set make 3").toBe(3);
    expect(countItems([]), "an empty cart has 0 pieces").toBe(0);
  });
});

describe("<cart-count>", () => {
  it("shows the count as soon as it joins the page", () => {
    const { store } = countedStore({ products: [], lines: [{ id: "DC-1042", qty: 3 }] });
    expect(cartCount(store).textContent, "with 3 Golus in the cart it should show Cart (3)").toBe("Cart (3)");
  });

  it("follows the store", () => {
    const { store } = countedStore({ products: [], lines: [] });
    const element = cartCount(store);
    store.setState({ lines: [{ id: "DC-1013", qty: 2 }] });
    expect(element.textContent, "after 2 keychains are added it should show Cart (2)").toBe("Cart (2)");
  });

  it("stops listening when it leaves the page", () => {
    const { store, active } = countedStore({ products: [], lines: [] });
    const element = cartCount(store);
    expect(active(), "on the page, it should have one subscription").toBe(1);
    element.remove();
    expect(active(), "after it is removed, it should have unsubscribed").toBe(0);
    store.setState({ lines: [{ id: "DC-1042", qty: 1 }] });
    expect(element.textContent, "a removed count should not redraw").toBe("Cart (0)");
  });
});
