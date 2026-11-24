import { rupees } from "./money.js";

// TODO 1: Inside the <article>, show:
//   the product's name in an <h3>, rupees(product.paise) in <p className="price">,
//   the colour in <p className="colour">, and a button with the text Add,
//   aria-label={`Add ${product.name} to cart`}, that calls onAdd(product.id).
export function ProductCard({ product, onAdd }) {
  return <article className="product-card"></article>;
}
