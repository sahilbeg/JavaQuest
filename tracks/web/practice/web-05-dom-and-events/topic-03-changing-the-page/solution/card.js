// Changes one product card in place. Each function gets the card (an <article class="product">).
import { rupees } from "./money.js";

/** Shows the name in the card's <h3>, and labels its button for screen readers. */
export function setName(card, name) {
  card.querySelector("h3").textContent = name;
  card.querySelector(".add").setAttribute("aria-label", `Add ${name} to cart`);
}

/** Shows the price in rupees, and keeps the paise in data-paise. */
export function setPrice(card, paise) {
  card.querySelector(".price").textContent = rupees(paise);
  card.dataset.paise = String(paise);
}

function stockText(stock) {
  if (stock === 0) {
    return "Sold out";
  }
  if (stock <= 3) {
    return `Only ${stock} left`;
  }
  return "In stock";
}

/** Shows the stock: its text, the sold-out and low classes, the button and data-stock. */
export function showStock(card, stock) {
  card.querySelector(".stock").textContent = stockText(stock);
  card.classList.toggle("sold-out", stock === 0);
  card.classList.toggle("low", stock > 0 && stock <= 3);
  card.querySelector(".add").disabled = stock === 0;
  card.dataset.stock = String(stock);
}
