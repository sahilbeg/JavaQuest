// Asking Dhaaga's server how many of an item are left.
// The server answers GET /api/stock/DC-1042 with { "id": "DC-1042", "stock": 3 }.

/** The stock endpoint for one item, for example "/api/stock/DC-1042". */
export function stockUrl(id) {
  return `/api/stock/${id}`;
}

/**
 * Asks the server for one item's stock and returns the number.
 * Throws an Error whose message includes the status (for example 404) when the answer isn't ok.
 */
export async function getStock(id) {
  const response = await fetch(stockUrl(id));
  if (!response.ok) {
    throw new Error(`Stock check failed: ${response.status}`);
  }
  const data = await response.json();
  return data.stock;
}

/** True when the item has at least one left. */
export async function isInStock(id) {
  const stock = await getStock(id);
  return stock > 0;
}
