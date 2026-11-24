import { useId, useState } from "react";
import { formatRupees } from "./money.js";

const MOST_PER_ORDER = 5;

/** One product in the catalogue. onAdd(id, quantity) puts it in the cart. This file already works: don't change it. */
export function ProductCard({ product, onAdd }) {
  const [quantity, setQuantity] = useState(1);
  const selectId = useId();
  const soldOut = product.stock === 0;
  const choices = Array.from({ length: Math.min(product.stock, MOST_PER_ORDER) }, (_, i) => i + 1);

  return (
    <article className="card">
      <h3>{product.name}</h3>
      <p className="price">{formatRupees(product.paise)}</p>
      {soldOut ? (
        <p className="sold-out">Out of stock</p>
      ) : (
        <p>
          <label htmlFor={selectId}>Quantity</label>{" "}
          <select id={selectId} value={quantity} onChange={(event) => setQuantity(Number(event.target.value))}>
            {choices.map((n) => (
              <option key={n} value={n}>
                {n}
              </option>
            ))}
          </select>
        </p>
      )}
      <button type="button" disabled={soldOut} onClick={() => onAdd(product.id, quantity)}>
        Add to cart
      </button>
    </article>
  );
}
