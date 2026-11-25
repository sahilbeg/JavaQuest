// The data layer: items and orders, kept in memory. The checks call resetStore() before each check.
const START = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000, stock: 7, description: "A small crocheted elephant in Indigo and Marigold, modelled on our Golu." },
  { id: "DC-1101", name: "Marigold coaster set", paise: 15000, stock: 12, description: "Four round coasters in Marigold cotton." },
  { id: "DC-1205", name: "Indigo keychain", paise: 12000, stock: 0, description: "A tiny Indigo flower on a steel ring." },
  { id: "DC-1301", name: "Tote bag", paise: 89900, stock: 2, description: "A strong Off-white tote with Rani pink straps." },
  { id: "DC-1402", name: "Baby booties", paise: 35000, stock: 4, description: "Soft Mint booties for the first six months." },
];

let items = [];
let orders = [];
let nextNumber = 1;

/** Back to the start: full stock, no orders, and the next order is order-1. */
export function resetStore() {
  items = START.map((item) => ({ ...item }));
  orders = [];
  nextNumber = 1;
}

resetStore();

function copyOrder(order) {
  return { ...order, lines: order.lines.map((line) => ({ ...line })), totals: { ...order.totals } };
}

/** Every item, in catalogue order, as copies. */
export async function listItems() {
  return items.map((item) => ({ ...item }));
}

/** One item by id, as a copy, or null. */
export async function getItem(id) {
  const item = items.find((candidate) => candidate.id === id);
  return item ? { ...item } : null;
}

/** One order by id, as a copy, or null. */
export async function getOrder(id) {
  const order = orders.find((candidate) => candidate.id === id);
  return order ? copyOrder(order) : null;
}

/** Every order, as copies. */
export async function listOrders() {
  return orders.map(copyOrder);
}

/**
 * Saves { name, phone, lines: [{ id, qty, paise }], totals } as a new order and takes its items out of stock.
 * Returns the saved order (with its id) as a copy, or null when any item is missing or short of stock.
 */
export async function createOrder(order) {
  // TODO 6: add up the qty asked for each id (an id can appear twice). If any item doesn't exist or has less stock
  // than that, return null and change nothing. Otherwise take the stock away, save
  // { id: "order-<n>", name, phone, lines, totals } with the next number, and return a copy.
  return null;
}
