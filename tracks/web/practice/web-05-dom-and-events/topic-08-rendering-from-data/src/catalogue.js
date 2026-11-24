// Draws the catalogue from data.
import { rupees } from "./money.js";

/** One new card for one product, not yet on the page. */
export function productCard(product) {
  // TODO 1: create <li class="product" data-id="..."> holding an <h3> with the name and a <p class="price"> with rupees(product.paise)
  // TODO 2: add <button class="add" type="button">Add to cart</button>; when stock is 0, add the class sold-out to the li, disable the button and make it say "Sold out"
  return null;
}

/** Replaces everything in the list with one card per product, or with an empty-state line. */
export function renderCatalogue(list, products) {
  // TODO 3: replace the list's children with one productCard per product, in order (replaceChildren and a spread)
  // TODO 4: when there are no products, replace them with one <li class="empty"> saying "No items match."
}

/** "No items", "1 item" or "5 items". */
export function countText(count) {
  // TODO 5: return "No items" for 0, "1 item" for 1, otherwise `${count} items`
  return "";
}
