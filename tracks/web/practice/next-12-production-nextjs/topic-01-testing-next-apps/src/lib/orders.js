// The orders, kept in memory. Tests empty it with resetOrders() so each check starts the same way.
let orders = [];

/** Empties the order list. */
export function resetOrders() {
  orders = [];
}

/** Saves an order with the next id (order-1, order-2, ...) and returns a copy. */
export function addOrder(order) {
  const saved = { id: `order-${orders.length + 1}`, ...order };
  orders.push(saved);
  return { ...saved };
}

/** Every saved order, as copies. */
export function listOrders() {
  return orders.map((order) => ({ ...order }));
}
