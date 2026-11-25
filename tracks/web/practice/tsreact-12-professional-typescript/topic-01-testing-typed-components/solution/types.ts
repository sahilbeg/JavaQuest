/** Where an order is on its way to the customer. */
export type OrderStatus = "placed" | "packed" | "shipped" | "delivered" | "cancelled";

/** One item in an order. Money is whole paise. */
export interface OrderLine {
  id: string;
  name: string;
  paise: number;
  qty: number;
}

/** An order on dhaaga.in, as the account page shows it. */
export interface Order {
  id: string;
  customer: string;
  status: OrderStatus;
  lines: OrderLine[];
}
