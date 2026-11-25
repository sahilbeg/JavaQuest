import type { CategoryFilter, Product } from "./types.ts";

export interface Filters {
  search: string;
  category: CategoryFilter;
}

/** The products whose name contains the search words and whose category matches. */
export function filterProducts(products: readonly Product[], filters: Filters): Product[] {
  // TODO 2: keep a product when its name contains the search words (trimmed, ignoring capitals)
  // and its category matches. "All" matches every category.
  return [...products];
}
