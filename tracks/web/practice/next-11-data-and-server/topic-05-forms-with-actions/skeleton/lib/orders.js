// Orders placed on the website, kept in memory. A real shop would keep them in a database.
let orders = [];
let nextNumber = 20;

/** Empties the orders. The checks call this before each test. */
export function resetOrders() {
  orders = [];
  nextNumber = 20;
}

/** Saves a new order and returns a copy of it, with its id, like order-20. */
export function createOrder({ name, phone, itemId, qty }) {
  const order = { id: `order-${nextNumber}`, name, phone, itemId, qty, status: "NEW" };
  nextNumber += 1;
  orders.push(order);
  return { ...order };
}

/** A copy of the order with this id, or undefined. */
export function getOrder(id) {
  const order = orders.find((o) => o.id === id);
  return order ? { ...order } : undefined;
}

/** How many orders have been placed. */
export function countOrders() {
  return orders.length;
}
