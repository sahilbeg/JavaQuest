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
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState<CategoryFilter>("All");
  const shown = filterProducts(products, { search, category });

  function chooseCategory(event: ChangeEvent<HTMLSelectElement>) {
    const choice = event.target.value;
    if (isCategoryFilter(choice)) setCategory(choice);
  }

  return (
    <section className="catalogue" aria-labelledby="catalogue-title">
      <h2 id="catalogue-title">Catalogue</h2>
      <div className="filters">
        <label htmlFor="search">Search</label>
        <input id="search" type="search" value={search} onChange={(event) => setSearch(event.target.value)} />
        <label htmlFor="category">Category</label>
        <select id="category" value={category} onChange={chooseCategory}>
          {CATEGORY_FILTERS.map((choice) => (
            <option key={choice} value={choice}>
              {choice}
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
