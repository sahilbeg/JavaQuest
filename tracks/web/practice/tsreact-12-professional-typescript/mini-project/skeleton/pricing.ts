import type { CartLine, Totals } from "./types.ts";

/** The Diwali offer starts above ₹1,500. */
export const OFFER_ABOVE = 150000;
/** Delivery inside Pune is free above ₹999, counted after the offer. */
export const FREE_DELIVERY_ABOVE = 99900;
/** Delivery costs ₹60. */
export const DELIVERY_PAISE = 6000;

/** The subtotal, the Diwali offer, delivery and the total, all in paise. */
export function cartTotals(lines: readonly CartLine[]): Totals {
  // TODO 1: subtotal is paise × qty for every line. discount is 10% of the subtotal, rounded down, only when the subtotal is above OFFER_ABOVE.
  // delivery is DELIVERY_PAISE, but 0 for an empty cart or when subtotal - discount is above FREE_DELIVERY_ABOVE. total is subtotal - discount + delivery.
  return { subtotal: 0, discount: 0, delivery: 0, total: 0 };
}
