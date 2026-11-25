// Wires the preview: one store, a badge that follows it, and buttons that change it. The checks never load this file.
import { createStore } from "./store.js";
import { addItem, removeItem, watch, mountCartBadge } from "./cart.js";

const store = createStore({ lines: [], route: "/" });
mountCartBadge(document.querySelector("#cart-badge"), store);

watch(store, (state) => state.lines, (lines) => {
  const text = lines.map((line) => `${line.id} × ${line.qty}`).join(", ");
  document.querySelector("#lines").textContent = text || "Your cart is empty.";
});

document.querySelector("#items").addEventListener("click", (event) => {
  const add = event.target.closest("button[data-add]");
  if (add !== null) {
    addItem(store, add.dataset.add);
  }
  const remove = event.target.closest("button[data-remove]");
  if (remove !== null) {
    removeItem(store, remove.dataset.remove);
  }
});
