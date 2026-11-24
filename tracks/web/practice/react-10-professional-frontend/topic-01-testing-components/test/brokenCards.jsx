import { useId, useState } from "react";
import { formatRupees } from "../src/money.js";

/** Builds a copy of ProductCard with one bug switched on. Your checks must fail on each copy. */
function brokenCard(bug) {
  return function BrokenCard({ product, onAdd }) {
    const [quantity, setQuantity] = useState(1);
    const selectId = useId();
    const soldOut = product.stock === 0;
    const choices = Array.from({ length: Math.min(product.stock, 5) }, (_, i) => i + 1);
    const add = () => {
      const times = bug.addsTwice ? 2 : 1;
      for (let i = 0; i < times; i++) {
        onAdd(bug.sendsName ? product.name : product.id, bug.ignoresQuantity ? 1 : quantity);
      }
    };

    return (
      <article className="card">
        <h3>{product.name}</h3>
        <p className="price">{bug.showsPaise ? `₹${product.paise}.00` : formatRupees(product.paise)}</p>
        {soldOut ? (
          <p className="sold-out">Out of stock</p>
        ) : (
          <p>
            {bug.unlabelled ? <span>Quantity</span> : <label htmlFor={selectId}>Quantity</label>}{" "}
            <select id={selectId} value={quantity} onChange={(event) => setQuantity(Number(event.target.value))}>
              {choices.map((n) => (
                <option key={n} value={n}>
                  {n}
                </option>
              ))}
            </select>
          </p>
        )}
        <button type="button" disabled={soldOut && !bug.soldOutSells} onClick={add}>
          Add to cart
        </button>
      </article>
    );
  };
}

export const ShowsPaise = brokenCard({ showsPaise: true });
export const SendsName = brokenCard({ sendsName: true });
export const AddsTwice = brokenCard({ addsTwice: true });
export const IgnoresQuantity = brokenCard({ ignoresQuantity: true });
export const UnlabelledQuantity = brokenCard({ unlabelled: true });
export const SoldOutStillSells = brokenCard({ soldOutSells: true });
