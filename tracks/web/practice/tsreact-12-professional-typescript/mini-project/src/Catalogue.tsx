import { useState, type ChangeEvent } from "react";
import { CATEGORY_FILTERS, isCategoryFilter } from "./catalogue.ts";
import { filterProducts } from "./filters.ts";
import { formatRupees } from "./money.ts";
import type { CategoryFilter, Product } from "./types.ts";

export interface CatalogueProps {
  products: readonly Product[];
  onAdd: (product: Product) => void;
}

/** The product grid, with a search box and a category filter. */
export function Catalogue({ products, onAdd }: CatalogueProps) {
  // TODO 3: keep search (a string) and category (a CategoryFilter, starting at "All") in state, and show only filterProducts(...).
  // Wire the Search input and the Category select to them. The select gives a string: check it with isCategoryFilter before setting state.
  // The status line says "6 products", "2 products" or "1 product". Each Add button gets aria-label="Add <name> to cart" and calls onAdd(product).
  const shown = products;

  return (
    <section className="catalogue" aria-labelledby="catalogue-title">
      <h2 id="catalogue-title">Catalogue</h2>
      <div className="filters">
        <label htmlFor="search">Search</label>
        <input id="search" type="search" />
        <label htmlFor="category">Category</label>
        <select id="category">
          {CATEGORY_FILTERS.map((choice) => (
            <option key={choice} value={choice}>
              {choice}
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
            <button type="button">Add to cart</button>
          </li>
        ))}
      </ul>
    </section>
  );
}
