// <cart-count>: shows "Cart (3)" and follows the store while it is on the page.

/** The number of pieces in the cart: the sum of every line's qty. */
export function countItems(lines) {
  return lines.reduce((sum, line) => sum + line.qty, 0);
}

export class CartCount extends HTMLElement {
  /** The store, set by the page before the element joins it. */
  store = null;
  #unsubscribe = null;

  connectedCallback() {
    this.#unsubscribe = this.store.subscribe((state) => this.render(state));
    this.render(this.store.getState());
  }

  disconnectedCallback() {
    this.#unsubscribe?.();
    this.#unsubscribe = null;
  }

  render(state) {
    this.textContent = `Cart (${countItems(state.lines)})`;
  }
}

if (!customElements.get("cart-count")) {
  customElements.define("cart-count", CartCount);
}
