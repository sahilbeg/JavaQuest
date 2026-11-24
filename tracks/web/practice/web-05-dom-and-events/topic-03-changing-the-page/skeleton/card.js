// Changes one product card in place. Each function gets the card (an <article class="product">).
import { rupees } from "./money.js";

/** Shows the name in the card's <h3>, and labels its button for screen readers. */
export function setName(card, name) {
  // TODO 1: write name into the card's <h3> as plain text (textContent, never innerHTML)
  // TODO 2: give the .add button the attribute aria-label="Add <name> to cart"
}

/** Shows the price in rupees, and keeps the paise in data-paise. */
export function setPrice(card, paise) {
  // TODO 3: write rupees(paise) into .price, then store String(paise) in card.dataset.paise
}

/** Shows the stock: its text, the sold-out and low classes, the button and data-stock. */
export function showStock(card, stock) {
  // TODO 4: write "Sold out" (0), "Only N left" (1 to 3) or "In stock" (more) into .stock
  // TODO 5: toggle sold-out (0) and low (1 to 3) on the card, disable .add only at 0, and store String(stock) in card.dataset.stock
}
