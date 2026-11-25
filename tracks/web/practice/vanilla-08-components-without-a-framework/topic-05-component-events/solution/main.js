// The page decides what add-to-cart means. The checks never load this file.
import { listenForAddToCart } from "./qty-picker.js";

const lines = new Map();
const cart = document.querySelector("#cart");

listenForAddToCart(document.querySelector("#catalogue"), (id, qty) => {
  lines.set(id, (lines.get(id) ?? 0) + qty);
  cart.textContent = [...lines].map(([item, count]) => `${count} × ${item}`).join(", ");
});
