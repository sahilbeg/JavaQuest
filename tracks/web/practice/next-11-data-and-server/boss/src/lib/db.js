// A pretend database. Every call waits a moment before it answers, like a real database over the network.
const SEED = [
  { id: "order-17", name: "Asha Rao", phone: "9876543210", lines: [{ itemId: "DC-4410", qty: 1 }], totalPaise: 41000, status: "NEW" },
  { id: "order-18", name: "Ravi Joshi", phone: "9876543211", lines: [{ itemId: "DC-1042", qty: 1 }], totalPaise: 51000, status: "PACKED" },
  { id: "order-19", name: "Neha Desai", phone: "9876543212", lines: [{ itemId: "DC-2001", qty: 2 }], totalPaise: 36000, status: "NEW" },
];

const copy = (row) => JSON.parse(JSON.stringify(row));
const aMoment = () => Promise.resolve();

let orders = new Map(SEED.map((row) => [row.id, copy(row)]));
let lastNumber = 19;

/** Puts the database back to how it started. The checks call this before each test. */
export function resetDb() {
  orders = new Map(SEED.map((row) => [row.id, copy(row)]));
  lastNumber = 19;
}

/** How many orders are saved. */
export async function countOrders() {
  await aMoment();
  return orders.size;
}

/** A copy of the order with this id, or undefined. */
export async function findOrder(id) {
  await aMoment();
  const order = orders.get(id);
  return order ? copy(order) : undefined;
}

/** Copies of every saved order. */
export async function allOrders() {
  await aMoment();
  return [...orders.values()].map(copy);
}

/** Saves an order. An order with the same id is replaced. */
export async function insertOrder(order) {
  await aMoment();
  orders.set(order.id, copy(order));
}

/** Removes an order. */
export async function deleteOrder(id) {
  await aMoment();
  orders.delete(id);
}

/** The database's order sequence: every call gets the next number, 20, 21, ..., never the same one twice. */
export async function nextOrderNumber() {
  await aMoment();
  lastNumber += 1;
  return lastNumber;
}
