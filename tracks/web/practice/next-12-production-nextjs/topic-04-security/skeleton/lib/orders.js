// The orders, kept in memory. order-17 belongs to Asha and holds her phone number and address.
const SEED = [
  {
    id: "order-17",
    customerId: "cust-asha",
    itemId: "DC-1042",
    qty: 1,
    totalPaise: 45000,
    status: "PACKED",
    phone: "98765 43210",
    address: "12 Lane 4, Aundh, Pune",
  },
];

let orders = SEED.map((order) => ({ ...order }));
let nextNumber = 18;

/** Puts the orders back to the start: only order-17, and the next new order is order-18. */
export function resetOrders() {
  orders = SEED.map((order) => ({ ...order }));
  nextNumber = 18;
}

/** Saves a new order with the next id and returns a copy. */
export function addOrder(order) {
  const saved = { id: `order-${nextNumber}`, status: "NEW", ...order };
  nextNumber += 1;
  orders.push(saved);
  return { ...saved };
}

/** One order by id, as a copy, or null. */
export function findOrder(id) {
  const order = orders.find((candidate) => candidate.id === id);
  return order ? { ...order } : null;
}

/** Every order, as copies. */
export function listOrders() {
  return orders.map((order) => ({ ...order }));
}
