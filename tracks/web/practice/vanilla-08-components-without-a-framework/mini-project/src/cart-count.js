// <cart-count>: shows "Cart (3)" and follows the store while it is on the page.

/** The number of pieces in the cart: the sum of every line's qty. */
export function countItems(lines) {
  // TODO 5: add up every line's qty (0 for no lines)
  return 0;
}

export class CartCount extends HTMLElement {
  /** The store, set by the page before the element joins it. */
  store = null;
  #unsubscribe = null;

  connectedCallback() {
    // TODO 6: subscribe to this.store (keep what subscribe returns), then draw once from this.store.getState()
  }

  disconnectedCallback() {
    // TODO 6: unsubscribe
  }

  render(state) {
    this.textContent = `Cart (${countItems(state.lines)})`;
  }
}

if (!customElements.get("cart-count")) {
  customElements.define("cart-count", CartCount);
}
