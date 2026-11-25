// The catalogue's components, built with h.
import { h } from "./h.js";
import { rupees } from "./money.js";

/** One product card: { id, name, paise, stock } in, a detached <li> out. */
export function productCard(product, onAdd) {
  // TODO 5: return h("li", ...) with data-id, an h3 with the name, a p.price from rupees, and a button.add that calls onAdd(product.id);
  //         when stock is 0, the li's class is "product sold-out" and the button is disabled and says "Sold out"
  return h("li", null);
}

/** The catalogue: a ul.products with one card per product, or one li.empty when there are none. */
export function productList(products, onAdd) {
  // TODO 6: return h("ul", { class: "products" }, ...) with products.map(...) as its child, or an li.empty saying "No items match."
  return h("ul", null);
}
