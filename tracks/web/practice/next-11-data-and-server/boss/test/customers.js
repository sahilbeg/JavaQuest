// Customers ordering in the checks.
export const ASHA = { name: "Asha Rao", phone: "9876543210", lines: [{ itemId: "DC-1042", qty: 1 }] };
export const RAVI = { name: "Ravi Joshi", phone: "9876543211", lines: [{ itemId: "DC-4410", qty: 2 }] };
export const SEEDED = ["order-17", "order-18", "order-19"];

/** The number in an order id: 20 for "order-20". */
export function numberOf(id) {
  return Number(String(id).split("-")[1]);
}
