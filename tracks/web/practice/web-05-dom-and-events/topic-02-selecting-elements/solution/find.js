// The counter's price checker. Every function searches inside root: document, or one element.

/** Every .product card, as a real array. */
export function productCards(root) {
  return [...root.querySelectorAll(".product")];
}

/** The trimmed <h3> text of every product, in page order. */
export function productTitles(root) {
  return [...root.querySelectorAll(".product h3")].map((h3) => h3.textContent.trim());
}

/** The .product whose data-id is id, or null. */
export function findProduct(root, id) {
  return root.querySelector(`.product[data-id="${id}"]`);
}

/** The product's own price text, like "₹120.00", or "" when there is no such product. */
export function priceOf(root, id) {
  const card = findProduct(root, id);
  if (card === null) {
    return "";
  }
  return card.querySelector(".price").textContent.trim();
}

/** The <h3> text of every product that also has the class sold-out. */
export function soldOutTitles(root) {
  return [...root.querySelectorAll(".product.sold-out h3")].map((h3) => h3.textContent.trim());
}
