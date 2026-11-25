import type { CategoryFilter, Product } from "./types.ts";

export interface Filters {
  search: string;
  category: CategoryFilter;
}

/** The products whose name contains the search words and whose category matches. */
export function filterProducts(products: readonly Product[], filters: Filters): Product[] {
  const words = filters.search.trim().toLowerCase();
  return products.filter(
    (product) =>
      (filters.category === "All" || product.category === filters.category) && product.name.toLowerCase().includes(words)
  );
}
