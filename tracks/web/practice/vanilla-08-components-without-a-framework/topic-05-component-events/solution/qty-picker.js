// <qty-picker item-id="DC-1042" max="4">: choose how many, then announce it with an add-to-cart event.

const PICKER_HTML = `
  <style>
    :host { display: inline-flex; align-items: center; gap: 0.5rem; }
    button { font: inherit; min-width: 2.5rem; min-height: 2.5rem; border-radius: 8px; border: 2px solid #2E3A8C; background: #FAF7F0; }
    .add { background: #F2A81D; border-color: #F2A81D; }
  </style>
  <button type="button" data-action="dec" aria-label="One less">−</button>
  <span class="qty">1</span>
  <button type="button" data-action="inc" aria-label="One more">+</button>
  <button type="button" class="add">Add to cart</button>
`;

export class QtyPicker extends HTMLElement {
  #qty = 1;

  constructor() {
    super();
    this.attachShadow({ mode: "open" }).innerHTML = PICKER_HTML;
    this.shadowRoot.addEventListener("click", (event) => this.#onClick(event));
  }

  get qty() {
    return this.#qty;
  }

  #onClick(event) {
    const button = event.target.closest("button");
    if (button === null) {
      return;
    }
    if (button.dataset.action === "inc") {
      this.#setQty(this.#qty + 1);
    } else if (button.dataset.action === "dec") {
      this.#setQty(this.#qty - 1);
    } else if (button.classList.contains("add")) {
      this.dispatchEvent(new CustomEvent("add-to-cart", {
        detail: { id: this.getAttribute("item-id"), qty: this.#qty },
        bubbles: true,
        composed: true,
      }));
      this.#setQty(1);
    }
  }

  #setQty(qty) {
    const max = Number(this.getAttribute("max") ?? 10);
    this.#qty = Math.min(max, Math.max(1, qty));
    this.shadowRoot.querySelector(".qty").textContent = String(this.#qty);
  }
}

if (!customElements.get("qty-picker")) {
  customElements.define("qty-picker", QtyPicker);
}

/** The page's side: one listener on root, calling onAdd(id, qty) for every add-to-cart from inside it. */
export function listenForAddToCart(root, onAdd) {
  root.addEventListener("add-to-cart", (event) => onAdd(event.detail.id, event.detail.qty));
}
