// The orders, kept in memory. A real shop would keep them in a database.
const SEED = [
  { id: "order-17", name: "Asha Rao", status: "NEW", totalPaise: 41000 },
  { id: "order-18", name: "Ravi Joshi", status: "PACKED", totalPaise: 51000 },
  { id: "order-19", name: "Neha Desai", status: "NEW", totalPaise: 36000 },
];
let orders = SEED.map((order) => ({ ...order }));

/** Puts the orders back to how they started. The checks call this before each test. */
export function resetOrders() {
  orders = SEED.map((order) => ({ ...order }));
}

/** A copy of the order with this id, or undefined. */
export function getOrder(id) {
  const order = orders.find((o) => o.id === id);
  return order ? { ...order } : undefined;
}

/** Copies of every order. */
export function listOrders() {
  return orders.map((order) => ({ ...order }));
}

/** Changes the status of an order that exists. */
export function setStatus(id, status) {
  const order = orders.find((o) => o.id === id);
  order.status = status;
}
