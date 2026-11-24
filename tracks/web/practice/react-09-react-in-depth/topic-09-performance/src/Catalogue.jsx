import { useCallback, useMemo, useState } from "react";
import { ProductRow } from "./ProductRow.jsx";
import { sortByPrice } from "./sortByPrice.js";

/** The catalogue: every item, sorted by price, with Add buttons and a cart count. */
export function Catalogue({ items }) {
  const [count, setCount] = useState(0);
  const [order, setOrder] = useState("low-first");

  // TODO 2: keep the sorted list with useMemo, so it is sorted again only when items or order change.
  const sorted = sortByPrice(items, order);

  // TODO 3: keep this the same function between renders with useCallback.
  function handleAdd() {
    setCount((c) => c + 1);
  }

  return (
    <section>
      <p>Cart ({count})</p>
      <label htmlFor="order">Sort by price</label>{" "}
      <select id="order" value={order} onChange={(e) => setOrder(e.target.value)}>
        <option value="low-first">Low to high</option>
        <option value="high-first">High to low</option>
      </select>
      <ul>
        {sorted.map((item) => (
          <ProductRow key={item.code} item={item} onAdd={handleAdd} />
        ))}
      </ul>
    </section>
  );
}
