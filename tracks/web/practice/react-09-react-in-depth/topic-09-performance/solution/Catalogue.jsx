import { useCallback, useMemo, useState } from "react";
import { ProductRow } from "./ProductRow.jsx";
import { sortByPrice } from "./sortByPrice.js";

/** The catalogue: every item, sorted by price, with Add buttons and a cart count. */
export function Catalogue({ items }) {
  const [count, setCount] = useState(0);
  const [order, setOrder] = useState("low-first");

  const sorted = useMemo(() => sortByPrice(items, order), [items, order]);

  const handleAdd = useCallback(() => {
    setCount((c) => c + 1);
  }, []);

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
