// <product-card>: one product, drawn in its own shadow root. Set card.product = { id, name, paise, stock }.
import { rupees } from "./money.js";

/** The stock line on a card. */
export function stockText(stock) {
  if (stock === 0) {
    return "Sold out";
  }
  if (stock <= 5) {
    return `Only ${stock} left`;
  }
  return "In stock";
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
    this.attachShadow({ mode: "open" }).innerHTML = CARD_HTML;
    this.shadowRoot.querySelector(".add").addEventListener("click", () => {
      if (this.#product === null) {
        return;
      }
      this.dispatchEvent(new CustomEvent("add-to-cart", {
        detail: { id: this.#product.id },
        bubbles: true,
        composed: true,
      }));
    });
  }

  get product() {
    return this.#product;
  }

  set product(product) {
    this.#product = product;
    const root = this.shadowRoot;
    root.querySelector(".name").textContent = product.name;
    root.querySelector(".price").textContent = rupees(product.paise);
    root.querySelector(".stock").textContent = stockText(product.stock);
    root.querySelector(".add").disabled = product.stock === 0;
  }
}

if (!customElements.get("product-card")) {
  customElements.define("product-card", ProductCard);
}
