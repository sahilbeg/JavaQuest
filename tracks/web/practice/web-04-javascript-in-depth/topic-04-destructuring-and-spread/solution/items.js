// Small helpers for the item cards.

export function cardLabel({ name, paise, colour = "Off-white" }) {
  return name + " (" + colour + "): ₹" + (paise / 100).toFixed(2);
}

export function withStock(item, stock) {
  return { ...item, stock };
}

export function mergeCatalogues(stall, shop) {
  return [...stall, ...shop];
}

export function splitFeatured(items) {
  const [featured, ...others] = items;
  return { featured, others };
}

export function totalPaise(...prices) {
  return prices.reduce((sum, p) => sum + p, 0);
}
