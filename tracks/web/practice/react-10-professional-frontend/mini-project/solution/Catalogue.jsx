import { useState } from "react";
import { CATEGORIES } from "./catalogue.js";
import { filterProducts } from "./filters.js";
import { formatRupees } from "./money.js";

/** The product grid, with a search box and a category filter. onAdd(product) puts one in the cart. */
export function Catalogue({ products, onAdd }) {
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("All");
  const shown = filterProducts(products, { search, category });

  return (
    <section className="catalogue" aria-labelledby="catalogue-title">
      <h2 id="catalogue-title">Catalogue</h2>
      <div className="filters">
        <label htmlFor="search">Search</label>
        <input id="search" type="search" value={search} onChange={(event) => setSearch(event.target.value)} />
        <label htmlFor="category">Category</label>
        <select id="category" value={category} onChange={(event) => setCategory(event.target.value)}>
          {CATEGORIES.map((name) => (
            <option key={name} value={name}>
              {name}
            </option>
          ))}
        </select>
      </div>
      <p role="status">
        {shown.length} {shown.length === 1 ? "product" : "products"}
      </p>
      <ul className="grid">
        {shown.map((product) => (
          <li key={product.id} className="card">
            <div className="photo" aria-hidden="true">
              {product.code}
            </div>
            <h3>{product.name}</h3>
            <p className="price">{formatRupees(product.paise)}</p>
            <button type="button" aria-label={`Add ${product.name} to cart`} onClick={() => onAdd(product)}>
              Add to cart
            </button>
          </li>
        ))}
      </ul>
    </section>
  );
}
