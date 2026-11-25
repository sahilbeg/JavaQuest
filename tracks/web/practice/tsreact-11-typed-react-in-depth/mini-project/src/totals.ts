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
  // TODO 1: items adds up paise * qty for every line. delivery is DELIVERY_PAISE, but 0 when items is above
  //         FREE_DELIVERY_ABOVE_PAISE, and 0 for an empty cart. total is items + delivery.
  return { items: 0, delivery: 0, total: 0 };
}
