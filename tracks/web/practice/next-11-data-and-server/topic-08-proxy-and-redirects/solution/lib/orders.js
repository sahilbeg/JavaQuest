const ORDERS = [
  { id: "order-17", phone: "9876543210", status: "NEW" },
  { id: "order-18", phone: "9876543211", status: "PACKED" },
  { id: "order-19", phone: "9876543212", status: "NEW" },
];

/** A copy of the order with this id, or undefined. */
export function findOrder(id) {
  const order = ORDERS.find((o) => o.id === id);
  return order ? { ...order } : undefined;
}
