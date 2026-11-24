/**
 * The products that match the search words (anywhere in the name, any capitals, spaces trimmed)
 * and the category ("All" matches every category).
 */
export function filterProducts(products, { search = "", category = "All" } = {}) {
  // TODO 2: keep the products whose name contains the trimmed, lower-cased search words,
  // and whose category matches (or the category is "All").
  return products;
}
