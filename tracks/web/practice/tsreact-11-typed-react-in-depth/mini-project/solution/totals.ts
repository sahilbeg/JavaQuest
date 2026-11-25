export interface CartLine {
  code: string;
  name: string;
  paise: number;
  qty: number;
}

export interface Totals {
  items: number;
  delivery: number;
  total: number;
}

/** Delivery inside Pune: ₹60, free when the items come to more than ₹999. */
export const DELIVERY_PAISE = 6000;
export const FREE_DELIVERY_ABOVE_PAISE = 99900;

/** What the cart costs, in paise. */
export function checkoutTotals(lines: CartLine[]): Totals {
  const items = lines.reduce((sum, line) => sum + line.paise * line.qty, 0);
  const delivery = lines.length === 0 || items > FREE_DELIVERY_ABOVE_PAISE ? 0 : DELIVERY_PAISE;
  return { items, delivery, total: items + delivery };
}
