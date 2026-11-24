import { formatRupees } from "./money.js";

/** The stock line on a card: "Sold out", "Only N left" (1 to 3) or "In stock". */
export function stockText(stock) {
  // TODO 1: return "Sold out" for 0, `Only ${stock} left` for 1 to 3, and "In stock" for more than 3.
  return "";
}

/**
 * Draws one product card into container, replacing what was there, and returns the <article>.
 * product looks like { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 2 }.
 */
export function renderProduct(container, product, onAdd) {
  // TODO 2: create <article class="product" data-id="..."> holding an <h3> with the name,
  //         <p class="price"> with formatRupees(product.paise), <p class="stock"> with stockText(product.stock)
  //         and <button type="button">Add to cart</button>. Set every text with textContent, never innerHTML.
  // TODO 3: disable the button when product.stock is 0.
  // TODO 4: when the button is clicked, call onAdd(product.id).
  // TODO 5: put the article into container with container.replaceChildren(article), and return it.
  return null;
}
