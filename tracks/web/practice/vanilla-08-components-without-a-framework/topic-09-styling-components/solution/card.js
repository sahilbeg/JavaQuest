// The product card, in the light DOM, styled by style.css.
import { rupees } from "./money.js";

/** One card: { id, name, paise, stock } in, a detached <article> out. */
export function productCard(product) {
  const soldOut = product.stock === 0;
  const card = document.createElement("article");
  card.className = soldOut ? "product-card product-card--sold-out" : "product-card";
  card.dataset.id = product.id;

  const name = document.createElement("h3");
  name.className = "product-card__name";
  name.textContent = product.name;

  const price = document.createElement("p");
  price.className = "product-card__price";
  price.textContent = rupees(product.paise);

  const add = document.createElement("button");
  add.className = "product-card__add";
  add.type = "button";
  add.disabled = soldOut;
  add.textContent = soldOut ? "Sold out" : "Add to cart";

  card.append(name, price, add);
  return card;
}
