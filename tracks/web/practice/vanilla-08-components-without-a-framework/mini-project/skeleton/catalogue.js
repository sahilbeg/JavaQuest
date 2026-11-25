// The catalogue: <product-card> elements drawn from the store, and the one add-to-cart listener.
import "./product-card.js";

/** One more of this item: 1 more on its line, or a new line at the end. Returns a new array. */
export function addLine(lines, id) {
  // TODO 7: a new array; the line with this id gets a new object with qty + 1, or { id, qty: 1 } is added at the end
  return lines;
}

/** Fills root with one <product-card> per product in the store, and keeps it in step with the store. */
export function createCatalogue(root, store) {
  // TODO 8: a sync(products) function: keep the card of each id that stays (found by data-id) and set its product,
  //         make a <product-card> with data-id for each new id, remove the cards of ids that are gone, follow the order;
  //         run it with store.getState().products now, and again from store.subscribe
  // TODO 9: one "add-to-cart" listener on root: store.setState((state) => ({ lines: addLine(state.lines, event.detail.id) }))
}
