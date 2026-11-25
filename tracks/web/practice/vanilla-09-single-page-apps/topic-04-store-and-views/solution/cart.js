// Cart actions, a selector, and views that follow the store.
// The state looks like { lines: [{ id: "DC-1042", qty: 2 }], route: "/items" }.

/** Adds one of item id: a new line with qty 1, or one more on its existing line. Never changes the old state. */
export function addItem(store, id) {
  store.setState((state) => {
    const exists = state.lines.some((line) => line.id === id);
    const lines = exists
      ? state.lines.map((line) => (line.id === id ? { ...line, qty: line.qty + 1 } : line))
      : [...state.lines, { id, qty: 1 }];
    return { ...state, lines };
  });
}

/** Takes item id's line out of the cart. Never changes the old state. */
export function removeItem(store, id) {
  store.setState((state) => ({ ...state, lines: state.lines.filter((line) => line.id !== id) }));
}

/** A selector: how many items are in the cart, adding up every line's qty. */
export function itemCount(state) {
  return state.lines.reduce((sum, line) => sum + line.qty, 0);
}

/**
 * Calls onChange(value) now with selector(state), then again only when the selected value changes (!==).
 * Returns a function that stops watching.
 */
export function watch(store, selector, onChange) {
  let last = selector(store.getState());
  onChange(last);
  return store.subscribe((state) => {
    const next = selector(state);
    if (next !== last) {
      last = next;
      onChange(next);
    }
  });
}

/** Keeps el's text equal to the cart's item count. Returns the function that unmounts the badge. */
export function mountCartBadge(el, store) {
  return watch(store, itemCount, (count) => {
    el.textContent = String(count);
  });
}
