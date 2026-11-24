/**
 * The products that match the search words (anywhere in the name, any capitals, spaces trimmed)
 * and the category ("All" matches every category).
 */
export function filterProducts(products, { search = "", category = "All" } = {}) {
  const words = search.trim().toLowerCase();
  return products.filter(
    (product) => (category === "All" || product.category === category) && product.name.toLowerCase().includes(words)
  );
}
