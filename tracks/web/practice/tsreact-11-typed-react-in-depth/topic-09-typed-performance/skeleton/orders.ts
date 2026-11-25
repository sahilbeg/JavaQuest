export interface PastOrder {
  id: string;
  customer: string;
  totalPaise: number;
}

export const ORDERS: PastOrder[] = [
  { id: "order-17", customer: "Asha Rao", totalPaise: 45000 },
  { id: "order-18", customer: "Vikram Nair", totalPaise: 89900 },
  { id: "order-19", customer: "Asha Rao", totalPaise: 15000 },
  { id: "order-20", customer: "Farah Khan", totalPaise: 35000 },
  { id: "order-21", customer: "Mrs. Kulkarni", totalPaise: 129900 },
];

/** How many times filterOrders has run, so the checks (and the preview) can count the work. */
export const filterStats = { runs: 0 };

/** The orders whose id or customer contains the query, ignoring case. */
export function filterOrders(orders: PastOrder[], query: string): PastOrder[] {
  filterStats.runs += 1;
  const q = query.trim().toLowerCase();
  return orders.filter((order) => order.id.includes(q) || order.customer.toLowerCase().includes(q));
}
