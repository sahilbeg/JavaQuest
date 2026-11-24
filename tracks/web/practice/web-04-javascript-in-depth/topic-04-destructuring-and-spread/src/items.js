// Small helpers for the item cards.

export function cardLabel(item) {
  // TODO 1: destructure { name, paise, colour = "Off-white" } in the parameter list, then
  // return name + " (" + colour + "): ₹" + (paise / 100).toFixed(2)
  return "";
}

export function withStock(item, stock) {
  // TODO 2: return a new object with every field of item, then stock set to the new number
  return null;
}

export function mergeCatalogues(stall, shop) {
  // TODO 3: return one new array with the stall's items, then the shop's
  return [];
}

export function splitFeatured(items) {
  // TODO 4: const [featured, ...others] = items; then return { featured, others }
  return { featured: undefined, others: [] };
}

export function totalPaise(prices) {
  // TODO 5: make prices a rest parameter (...prices), then add them all up, starting from 0
  return 0;
}
