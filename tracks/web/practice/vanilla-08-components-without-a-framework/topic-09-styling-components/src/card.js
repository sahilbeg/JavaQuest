// The product card, in the light DOM, styled by style.css.
import { rupees } from "./money.js";

/** One card: { id, name, paise, stock } in, a detached <article> out. */
export function productCard(product) {
  const soldOut = product.stock === 0;
  // TODO 4: use the BEM classes: product-card (plus product-card--sold-out), product-card__name,
  //         product-card__price and product-card__add
  const card = document.createElement("article");
  card.className = soldOut ? "card sold-out" : "card";
  card.dataset.id = product.id;

  const name = document.createElement("h3");
  name.textContent = product.name;

  const price = document.createElement("p");
  price.className = "price";
  price.textContent = rupees(product.paise);

  const add = document.createElement("button");
  add.type = "button";
  add.disabled = soldOut;
  add.textContent = soldOut ? "Sold out" : "Add to cart";

  card.append(name, price, add);
  return card;
}
