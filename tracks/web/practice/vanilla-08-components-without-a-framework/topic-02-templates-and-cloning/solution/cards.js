// Product cards copied from the page's <template id="product-card">.
import { rupees } from "./money.js";

/** Clones the template and fills one card. Returns the <li>, not yet on the page. */
export function cardFromTemplate(template, product) {
  const card = template.content.cloneNode(true).firstElementChild;
  card.dataset.id = product.id;
  const photo = card.querySelector(".photo");
  photo.src = product.photo;
  photo.alt = product.name;
  card.querySelector(".name").textContent = product.name;
  card.querySelector(".price").textContent = rupees(product.paise);
  card.querySelector(".add").setAttribute("aria-label", `Add ${product.name} to cart`);
  return card;
}

/** Draws one card per product into the list, replacing whatever was there. */
export function renderCards(list, template, products) {
  list.replaceChildren(...products.map((product) => cardFromTemplate(template, product)));
}
