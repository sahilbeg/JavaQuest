// Asking Dhaaga's server how many of an item are left.
// The server answers GET /api/stock/DC-1042 with { "id": "DC-1042", "stock": 3 }.

/** The stock endpoint for one item, for example "/api/stock/DC-1042". */
export function stockUrl(id) {
  // TODO 1: return the URL for this item's stock.
  return "";
}

/**
 * Asks the server for one item's stock and returns the number.
 * Throws an Error whose message includes the status (for example 404) when the answer isn't ok.
 */
export async function getStock(id) {
  // TODO 2: fetch stockUrl(id) and wait for the response.
  // TODO 3: if response.ok is false, throw new Error(`Stock check failed: ${response.status}`).
  // TODO 4: wait for response.json() and return the stock number from it.
  return 0;
}

/** True when the item has at least one left. */
export async function isInStock(id) {
  // TODO 5: use getStock(id) and return whether the stock is more than 0.
  return false;
}
