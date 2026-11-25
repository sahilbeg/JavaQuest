// The catalogue's components, built with h.
import { h } from "./h.js";
import { rupees } from "./money.js";

/** One product card: { id, name, paise, stock } in, a detached <li> out. */
export function productCard(product, onAdd) {
  const soldOut = product.stock === 0;
  return h("li", { class: soldOut ? "product sold-out" : "product", dataset: { id: product.id } },
    h("h3", null, product.name),
    h("p", { class: "price" }, rupees(product.paise)),
    h("button", { class: "add", type: "button", disabled: soldOut, onClick: () => onAdd(product.id) },
      soldOut ? "Sold out" : "Add to cart"),
  );
}

/** The catalogue: a ul.products with one card per product, or one li.empty when there are none. */
export function productList(products, onAdd) {
  return h("ul", { class: "products" },
    products.length === 0
      ? h("li", { class: "empty" }, "No items match.")
      : products.map((product) => productCard(product, onAdd)),
  );
}
