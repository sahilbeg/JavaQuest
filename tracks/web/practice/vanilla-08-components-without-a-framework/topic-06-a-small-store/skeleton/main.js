// Wires one cart store to two badges. The checks never load this file.
import { createStore } from "./store.js";
import "./cart-count.js";

const cart = createStore({ lines: [{ id: "DC-1001", qty: 2 }] });

/** A <cart-count> that is given the store before it joins the page. */
function cartCount() {
  const element = document.createElement("cart-count");
  element.store = cart;
  return element;
}

const headerCount = cartCount();
const badgeSpot = document.querySelector("#badge-spot");
badgeSpot.append(headerCount);
document.querySelector("#drawer-spot").append(cartCount());

document.addEventListener("click", (event) => {
  const button = event.target.closest("button[data-add]");
  if (button === null) {
    return;
  }
  const id = button.dataset.add;
  cart.setState((state) => {
    const found = state.lines.some((line) => line.id === id);
    const lines = found
      ? state.lines.map((line) => (line.id === id ? { ...line, qty: line.qty + 1 } : line))
      : [...state.lines, { id, qty: 1 }];
    return { lines };
  });
});

document.querySelector("#toggle-badge").addEventListener("click", () => {
  if (headerCount.isConnected) {
    headerCount.remove();
  } else {
    badgeSpot.append(headerCount);
  }
});
