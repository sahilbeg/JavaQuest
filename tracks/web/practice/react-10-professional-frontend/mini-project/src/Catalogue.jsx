import { useState } from "react";
import { CATEGORIES } from "./catalogue.js";
import { filterProducts } from "./filters.js";
import { formatRupees } from "./money.js";

/** The product grid, with a search box and a category filter. onAdd(product) puts one in the cart. */
export function Catalogue({ products, onAdd }) {
  // TODO 3: keep the search words and the category ("All" at first) in state, wire them to the two inputs with value and onChange,
  // and show only filterProducts(products, { search, category }). Write the count in the status line: "6 products", "1 product".
  // Give each Add button aria-label={`Add ${product.name} to cart`}, so a screen reader says which product it adds.
  const shown = products;

  return (
    <section className="catalogue" aria-labelledby="catalogue-title">
      <h2 id="catalogue-title">Catalogue</h2>
      <div className="filters">
        <label htmlFor="search">Search</label>
        <input id="search" type="search" />
        <label htmlFor="category">Category</label>
        <select id="category">
          {CATEGORIES.map((name) => (
            <option key={name} value={name}>
              {name}
            </option>
          ))}
        </select>
      </div>
      <p role="status"></p>
      <ul className="grid">
        {shown.map((product) => (
          <li key={product.id} className="card">
            <div className="photo" aria-hidden="true">
              {product.code}
            </div>
            <h3>{product.name}</h3>
            <p className="price">{formatRupees(product.paise)}</p>
            <button type="button" onClick={() => onAdd(product)}>
              Add to cart
            </button>
          </li>
        ))}
      </ul>
    </section>
  );
}
