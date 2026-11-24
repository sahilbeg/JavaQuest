// Five answers from the catalogue list, each one array method.

export function names(items) {
  return items.map((item) => item.name);
}

export function inStock(items) {
  return items.filter((item) => item.stock > 0);
}

export function stockValue(items) {
  return items.reduce((sum, item) => sum + item.paise * item.stock, 0);
}

export function findById(items, id) {
  return items.find((item) => item.id === id);
}

export function cheapestFirst(items) {
  return items.slice().sort((a, b) => a.paise - b.paise);
}
