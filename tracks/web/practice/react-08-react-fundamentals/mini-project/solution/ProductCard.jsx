import { rupees } from "./money.js";

export function ProductCard({ product, onAdd }) {
  return (
    <article className="product-card">
      <h3>{product.name}</h3>
      <p className="price">{rupees(product.paise)}</p>
      <p className="colour">{product.colour}</p>
      <button type="button" aria-label={`Add ${product.name} to cart`} onClick={() => onAdd(product.id)}>
        Add
      </button>
    </article>
  );
}
