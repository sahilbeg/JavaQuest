// Draws the catalogue from data.
import { rupees } from "./money.js";

/** One new card for one product, not yet on the page. */
export function productCard(product) {
  const card = document.createElement("li");
  card.className = "product";
  card.dataset.id = product.id;

  const name = document.createElement("h3");
  name.textContent = product.name;

  const price = document.createElement("p");
  price.className = "price";
  price.textContent = rupees(product.paise);

  const button = document.createElement("button");
  button.className = "add";
  button.type = "button";
  button.textContent = "Add to cart";

  if (product.stock === 0) {
    card.classList.add("sold-out");
    button.disabled = true;
    button.textContent = "Sold out";
  }

  card.append(name, price, button);
  return card;
}

/** Replaces everything in the list with one card per product, or with an empty-state line. */
export function renderCatalogue(list, products) {
  if (products.length === 0) {
    const empty = document.createElement("li");
    empty.className = "empty";
    empty.textContent = "No items match.";
    list.replaceChildren(empty);
    return;
  }
  list.replaceChildren(...products.map(productCard));
}

/** "No items", "1 item" or "5 items". */
export function countText(count) {
  if (count === 0) {
    return "No items";
  }
  return count === 1 ? "1 item" : `${count} items`;
}
