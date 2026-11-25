// <product-card>: one product, drawn in its own shadow root. Set card.product = { id, name, paise, stock }.
import { rupees } from "./money.js";

/** The stock line on a card. */
export function stockText(stock) {
  // TODO 3: "Sold out" for 0, "Only N left" for 1 to 5, "In stock" above 5
  return "";
}

const CARD_HTML = `
  <style>
    :host { display: block; background: var(--card-bg, #FAF7F0); border: 2px solid var(--card-border, #2E3A8C); border-radius: 12px; padding: 1rem; color: #2E3A8C; font-family: system-ui, sans-serif; }
    :host([hidden]) { display: none; }
    .name { margin: 0 0 0.25rem; }
    .price { font-weight: 700; margin: 0; }
    .add { background: #F2A81D; border: 0; border-radius: 8px; padding: 0.5rem 1rem; font: inherit; }
    .add:disabled { opacity: 0.5; }
  </style>
  <article part="card">
    <h3 class="name" part="name"></h3>
    <p class="price"></p>
    <p class="stock"></p>
    <button type="button" class="add" part="add">Add to cart</button>
  </article>
`;

export class ProductCard extends HTMLElement {
  #product = null;

  constructor() {
    super();
    // TODO 1: attach an open shadow root and set its innerHTML to CARD_HTML
    // TODO 4: listen for clicks on the shadow root's .add button, and dispatch "add-to-cart" from this card
    //         with detail { id: this.#product.id }, bubbles: true and composed: true
  }

  get product() {
    return this.#product;
  }

  set product(product) {
    this.#product = product;
    // TODO 2: draw it in the shadow root: .name, .price with rupees(paise), .stock with stockText(stock),
    //         and .add disabled when stock is 0
  }
}

if (!customElements.get("product-card")) {
  customElements.define("product-card", ProductCard);
}
