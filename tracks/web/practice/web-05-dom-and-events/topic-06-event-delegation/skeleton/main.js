// Wires the catalogue and the cart lines. The checks never load this file.
import { listenForAdds, listenForLineActions } from "./delegate.js";

const products = document.querySelector("#products");
const lines = document.querySelector("#lines");
const status = document.querySelector("#status");

listenForAdds(products, (id) => {
  status.textContent = `Added ${id} to your cart`;
});

function changeQty(id, delta) {
  const qty = lines.querySelector(`[data-id="${id}"] .qty`);
  qty.textContent = String(Math.max(1, Number(qty.textContent) + delta));
}

listenForLineActions(lines, {
  inc: (id) => changeQty(id, 1),
  dec: (id) => changeQty(id, -1),
  remove: (id) => lines.querySelector(`[data-id="${id}"]`).remove(),
});

// A card added after the page loaded: its button works only with delegation.
document.querySelector("#more").addEventListener("click", () => {
  const card = document.createElement("li");
  card.className = "product";
  card.dataset.id = "DC-1020";
  const name = document.createElement("h3");
  name.textContent = "Mint baby booties (Diwali special)";
  const button = document.createElement("button");
  button.className = "add";
  button.type = "button";
  button.textContent = "Add to cart";
  card.append(name, button);
  products.append(card);
}, { once: true });
