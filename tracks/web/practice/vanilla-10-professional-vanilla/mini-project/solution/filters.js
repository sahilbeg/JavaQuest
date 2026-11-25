/** The products whose name contains the search words (trimmed, any capitals) and whose category matches. "All" matches every category. */
export function filterProducts(products, { search, category }) {
  const words = search.trim().toLowerCase();
  return products.filter(
    (product) => product.name.toLowerCase().includes(words) && (category === "All" || product.category === category),
  );
}
