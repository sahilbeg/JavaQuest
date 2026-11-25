// Product cards copied from the page's <template id="product-card">.
import { rupees } from "./money.js";

/** Clones the template and fills one card. Returns the <li>, not yet on the page. */
export function cardFromTemplate(template, product) {
  // TODO 1: clone template.content deeply with cloneNode(true) and take its firstElementChild
  const card = document.createElement("li");
  // TODO 2: set card.dataset.id, and the .photo's src (product.photo) and alt (product.name)
  // TODO 3: fill .name and .price with textContent; give .add the aria-label "Add <name> to cart"
  return card;
}

/** Draws one card per product into the list, replacing whatever was there. */
export function renderCards(list, template, products) {
  // TODO 4: list.replaceChildren(...) with one cardFromTemplate per product
}
