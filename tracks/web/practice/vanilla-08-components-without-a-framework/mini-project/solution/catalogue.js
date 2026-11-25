// The catalogue: <product-card> elements drawn from the store, and the one add-to-cart listener.
import "./product-card.js";

/** One more of this item: 1 more on its line, or a new line at the end. Returns a new array. */
export function addLine(lines, id) {
  if (lines.some((line) => line.id === id)) {
    return lines.map((line) => (line.id === id ? { ...line, qty: line.qty + 1 } : line));
  }
  return [...lines, { id, qty: 1 }];
}

/** Fills root with one <product-card> per product in the store, and keeps it in step with the store. */
export function createCatalogue(root, store) {
  root.addEventListener("add-to-cart", (event) => {
    store.setState((state) => ({ lines: addLine(state.lines, event.detail.id) }));
  });

  let shown = null;

  function sync(products) {
    if (products === shown) {
      return;
    }
    shown = products;

    const old = new Map();
    for (const card of root.children) {
      old.set(card.dataset.id, card);
    }

    products.forEach((product, index) => {
      let card = old.get(product.id);
      if (card === undefined) {
        card = document.createElement("product-card");
        card.dataset.id = product.id;
      } else {
        old.delete(product.id);
      }
      card.product = product;
      if (root.children[index] !== card) {
        root.insertBefore(card, root.children[index] ?? null);
      }
    });

    for (const card of old.values()) {
      card.remove();
    }
  }

  sync(store.getState().products);
  store.subscribe((state) => sync(state.products));
}
