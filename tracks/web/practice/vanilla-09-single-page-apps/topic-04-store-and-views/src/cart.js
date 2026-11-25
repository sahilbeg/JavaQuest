// Cart actions, a selector, and views that follow the store.
// The state looks like { lines: [{ id: "DC-1042", qty: 2 }], route: "/items" }.

/** Adds one of item id: a new line with qty 1, or one more on its existing line. Never changes the old state. */
export function addItem(store, id) {
  // TODO 1: call store.setState with an update function that returns a NEW state object ({ ...state, lines }).
  //         If the item has a line, map to a new line with qty + 1; otherwise add { id, qty: 1 } at the end.
}

/** Takes item id's line out of the cart. Never changes the old state. */
export function removeItem(store, id) {
  // TODO 2: setState to a new state whose lines are filtered to leave out item id.
}

/** A selector: how many items are in the cart, adding up every line's qty. */
export function itemCount(state) {
  // TODO 3: add up line.qty for every line (reduce works well).
  return 0;
}

/**
 * Calls onChange(value) now with selector(state), then again only when the selected value changes (!==).
 * Returns a function that stops watching.
 */
export function watch(store, selector, onChange) {
  // TODO 4: remember selector(store.getState()) and call onChange with it. Subscribe to the store: on every
  //         change, select again, and call onChange only if the value !== the last one. Return the unsubscribe.
  return () => {};
}

/** Keeps el's text equal to the cart's item count. Returns the function that unmounts the badge. */
export function mountCartBadge(el, store) {
  // TODO 5: use watch with itemCount, and set el.textContent to String(count). Return what watch returns.
  return () => {};
}
