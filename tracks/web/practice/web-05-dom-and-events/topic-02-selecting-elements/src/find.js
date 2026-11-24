// The counter's price checker. Every function searches inside root: document, or one element.

/** Every .product card, as a real array. */
export function productCards(root) {
  // TODO 1: find every .product with root.querySelectorAll and spread it into an array
  return [];
}

/** The trimmed <h3> text of every product, in page order. */
export function productTitles(root) {
  // TODO 2: select only the <h3> elements inside a .product, then map each to its trimmed textContent
  return [];
}

/** The .product whose data-id is id, or null. */
export function findProduct(root, id) {
  // TODO 3: build the selector .product[data-id="..."] with a template literal
  return null;
}

/** The product's own price text, like "₹120.00", or "" when there is no such product. */
export function priceOf(root, id) {
  // TODO 4: find the card with findProduct; return "" for null; otherwise search for .price inside the card
  return "";
}

/** The <h3> text of every product that also has the class sold-out. */
export function soldOutTitles(root) {
  // TODO 5: select the <h3> inside elements that have both classes, product and sold-out
  return [];
}
