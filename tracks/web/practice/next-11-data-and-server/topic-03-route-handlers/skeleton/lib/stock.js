// The stock, kept in memory. A real shop would keep it in a database.
const START = { "DC-1042": 7, "DC-2001": 0, "DC-3007": 3, "DC-4410": 12, "DC-5120": 25 };
let left = { ...START };

/** Puts the stock back to how it started. The checks call this before each test. */
export function resetStock() {
  left = { ...START };
}

/** How many are left (0 is a real answer), or undefined for an unknown item. */
export function getLeft(id) {
  return Object.hasOwn(left, id) ? left[id] : undefined;
}

/** Adds count to an item that exists, and returns the new number left. */
export function addStock(id, count) {
  left[id] += count;
  return left[id];
}
