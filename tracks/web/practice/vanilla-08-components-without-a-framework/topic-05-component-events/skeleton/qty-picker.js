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
    // TODO 1: for data-action "inc" or "dec", change this.#qty by one, kept between 1 and the max attribute (10 when missing),
    //         and show it in the shadow root's .qty
    // TODO 2: for the .add button, dispatch a CustomEvent "add-to-cart" from this, with detail { id: the item-id attribute, qty }
    // TODO 3: give that event bubbles: true and composed: true
    // TODO 4: after dispatching, set the quantity back to 1 and show it
  }
}

if (!customElements.get("qty-picker")) {
  customElements.define("qty-picker", QtyPicker);
}

/** The page's side: one listener on root, calling onAdd(id, qty) for every add-to-cart from inside it. */
export function listenForAddToCart(root, onAdd) {
  // TODO 5: root.addEventListener("add-to-cart", ...) and call onAdd with event.detail.id and event.detail.qty
}
